package xyz.joumboes.wechat.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentTransaction;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.SASLAuthentication;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import xyz.joumboes.wechat.R;
import xyz.joumboes.wechat.fragment.ServiceSettingFragment;
import xyz.joumboes.wechat.service.IMService;
import xyz.joumboes.wechat.util.ThreadUtils;
import xyz.joumboes.wechat.util.ToastUtils;

import static xyz.joumboes.wechat.activity.SplashActivity.splashActivity;

public class LoginActivity extends AppCompatActivity {

    @BindView(R.id.usernameEt)
    EditText usernameEt;
    @BindView(R.id.passwordEt)
    EditText passwordEt;
    @BindView(R.id.serviceSetBt)
    Button serviceSetBt;
    @BindView(R.id.loginBt)
    Button loginBt;
    @BindView(R.id.loginLinLout)
    LinearLayout loginLinLout;
    @BindView(R.id.toolbar2)
    Toolbar toolbar2;

    public static String HOST = "47.107.80.247";
    public static int PORT = 5222;
    public static String ServiceName = "joumboes.xyz";
    public static String currentAccount;


    ServiceSettingFragment settingFragment;
    FragmentTransaction transaction;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        init();

    }

    private void init() {

        settingFragment = new ServiceSettingFragment();
        settingFragment.setActivity(this);

        //初始化设置为不可用
        loginBt.setEnabled(false);
        usernameEt.addTextChangedListener(new MyTextWatcher());
        passwordEt.addTextChangedListener(new MyTextWatcher());
        //读取服务器配置
        SharedPreferences sharedPre = getSharedPreferences("service_setting", MODE_PRIVATE);


        System.out.println(HOST);
        System.out.println(PORT);
        System.out.println(ServiceName);
        //自动登录
        String autologin = sharedPre.getString("autologin", "false");
        if ("true".equals(autologin)) {
            login();
        }

    }

    @OnClick({R.id.serviceSetBt, R.id.loginBt})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.serviceSetBt:
                serviceset();
                break;
            case R.id.loginBt:
                login();
                break;
        }
    }

    //登录事件
    public void login() {

        ThreadUtils.runInThread(new Runnable() {
            @Override
            public void run() {
                XMPPTCPConnectionConfiguration.Builder builder = XMPPTCPConnectionConfiguration.builder();
                XMPPTCPConnectionConfiguration config = null;
                XMPPTCPConnection conn = null;

                SharedPreferences sharedPref = getSharedPreferences("service_setting", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();

                HOST = sharedPref.getString("host", HOST);
                PORT = sharedPref.getInt("port", PORT);
                ServiceName = sharedPref.getString("servicename", ServiceName);

                System.out.println(HOST);
                System.out.println(PORT);
                System.out.println(ServiceName);

                //用户名密码
                String username = usernameEt.getText().toString();
                String password = passwordEt.getText().toString();

                username = sharedPref.getString("saveusername", username);
                password = sharedPref.getString("savepasswd", password);

                try {
                    // ip 端口 主机名 用户名密码 资源
                    config = builder.setHostAddressByNameOrIp(HOST)
                            .setPort(PORT)
                            .setXmppDomain(ServiceName)
                            .setUsernameAndPassword(username, password)
                            .setResource("Smack")
                            .setSecurityMode(ConnectionConfiguration.SecurityMode.disabled)
                            //debug上线关闭
                            .enableDefaultDebugger().build();
                    SASLAuthentication.blacklistSASLMechanism("SCRAM-SHA-1");
                } catch (Exception e) {
                    e.printStackTrace();
                }

                conn = new XMPPTCPConnection(config);


                try {
                    conn.connect();

                } catch (Exception e) {
                    ToastUtils.showToastSafe(LoginActivity.this, "服务器连接失败，请检查配置或者网络连接。");
                    e.printStackTrace();
                }

                try {
                    conn.login();
                    Thread.sleep(100);
                    //设置需要确认消息接收回执 确保消息送达
                    conn.setUseStreamManagementResumption(true);
                    if (conn.isAuthenticated()) {
                        ToastUtils.showToastSafe(LoginActivity.this, "登录成功。");
                        //设置自动登录
                        editor.putString("autologin", "true");
                        editor.putString("saveusername", username);
                        editor.putString("savepasswd", password);
                        editor.apply();

                        //TODO 初始化sercice conn
                        IMService.conn = conn;
                        currentAccount = username + "@" + ServiceName;
                        IMService.currentAccount = currentAccount;
                        startService(new Intent(LoginActivity.this, IMService.class));
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);

                        splashActivity.finish();
                        finish();
                    }
                } catch (Exception e) {
                    ToastUtils.showToastSafe(LoginActivity.this, "登录失败,清检查用户名或密码。");
                    e.printStackTrace();
                }


            }
        });


    }

    //服务器设置
    public void serviceset() {
        loginLinLout.setVisibility(View.GONE);
        toolbar2.setVisibility(View.GONE);
        transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.loginLout, settingFragment);
        transaction.addToBackStack(null);

        // Commit the transaction
        transaction.commit();
    }


    //监听文本改变
    class MyTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (!"".equals(usernameEt.getText().toString()) && !"".equals(passwordEt.getText().toString())) {
                loginBt.setBackgroundColor(Color.parseColor("#4CAF50"));
                loginBt.setTextColor(Color.parseColor("#FFFFFF"));
                loginBt.setEnabled(true);
            } else {
                loginBt.setBackgroundColor(Color.parseColor("#55A8A5A5"));
                loginBt.setTextColor(Color.parseColor("#84000000"));
                loginBt.setEnabled(false);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    }

}
