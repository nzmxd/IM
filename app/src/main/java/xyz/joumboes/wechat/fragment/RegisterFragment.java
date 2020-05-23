package xyz.joumboes.wechat.fragment;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.iqregister.AccountManager;
import org.jxmpp.jid.parts.Localpart;
import org.jxmpp.stringprep.XmppStringprepException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import xyz.joumboes.wechat.R;
import xyz.joumboes.wechat.activity.LoginActivity;
import xyz.joumboes.wechat.util.ThreadUtils;
import xyz.joumboes.wechat.util.ToastUtils;

import static android.content.Context.MODE_PRIVATE;

/**
 * A simple {@link Fragment} subclass.
 */
public class RegisterFragment extends Fragment {

    AccountManager accountManager;
    @BindView(R.id.resusernameEt)
    EditText resusernameEt;
    @BindView(R.id.respasswordEt)
    EditText respasswordEt;
    @BindView(R.id.quepasswordEt)
    EditText quepasswordEt;
    @BindView(R.id.toLoginBt)
    Button toLoginBt;
    @BindView(R.id.registerBt)
    Button registerBt;

    private static String HOST = "47.107.80.247";
    private static int PORT = 5222;
    private static String ServiceName = "joumboes.xyz";

    XMPPTCPConnectionConfiguration.Builder builder = XMPPTCPConnectionConfiguration.builder();
    XMPPTCPConnectionConfiguration config = null;
    XMPPTCPConnection conn = null;
    Activity mActivity;

    public RegisterFragment() {
        // Required empty public constructor
    }

    public void setActivity(Activity activity) {
        this.mActivity = activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_register, container, false);
        view.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));

        ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        resusernameEt.addTextChangedListener(new MyTextWatcher());
        respasswordEt.addTextChangedListener(new MyTextWatcher());
        quepasswordEt.addTextChangedListener(new MyTextWatcher());
        init();
    }

    private void init() {

        ThreadUtils.runInThread(new Runnable() {
            @Override
            public void run() {


                SharedPreferences sharedPre = getActivity().getSharedPreferences("service_setting", MODE_PRIVATE);
                HOST = sharedPre.getString("host", HOST);
                PORT = sharedPre.getInt("port", PORT);
                ServiceName = sharedPre.getString("servicename", ServiceName);


                try {
                    // ip 端口 主机名 用户名密码 资源
                    config = builder.setHostAddressByNameOrIp(HOST)
                            .setPort(PORT)
                            .setXmppDomain(ServiceName)
                            .setResource("Smack")
                            .setSecurityMode(ConnectionConfiguration.SecurityMode.disabled)
                            //debug上线关闭
                            .enableDefaultDebugger().build();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                conn = new XMPPTCPConnection(config);


                try {
                    conn.connect();
                } catch (Exception e) {
                    ToastUtils.showToastSafe(getContext(), "服务器连接失败，请检查配置或网络连接。");
                    e.printStackTrace();
                }

                accountManager = AccountManager.getInstance(conn);

            }
        });

    }


    private void resgister() {
        String username = resusernameEt.getText().toString();
        String passwd = respasswordEt.getText().toString();
        String querypasswd = quepasswordEt.getText().toString();
        if (accountManager == null) {
            init();
        }
        if (!passwd.equals(querypasswd)) {
            ToastUtils.showToastSafe(getContext(), "两次密码输入不一致！");
            return;
        }


        try {
            accountManager.sensitiveOperationOverInsecureConnection(true);
            accountManager.createAccount(Localpart.from(username), passwd);
        } catch (SmackException.NoResponseException e) {

            return;
        } catch (XMPPException.XMPPErrorException e) {
            e.printStackTrace();
            ToastUtils.showToastSafe(getContext(), "注册失败,用户名已存在");
            return;
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
            ToastUtils.showToastSafe(getContext(), "注册失败,连接丢失");
            return;

        } catch (InterruptedException e) {
            e.printStackTrace();
            ToastUtils.showToastSafe(getContext(), "注册失败,连接中断");
            return;
        } catch (XmppStringprepException e) {
            ToastUtils.showToastSafe(getContext(), "用户名已存在");
            resusernameEt.setText("");
            e.printStackTrace();
        }


        ToastUtils.showToastSafe(getContext(), "注册成功！");
        toLogin();
    }


    private void toLogin() {
        getActivity().startActivity(new Intent(getActivity(), LoginActivity.class));
        getActivity().onBackPressed();
    }


    @OnClick({R.id.toLoginBt, R.id.registerBt, R.id.regBack})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.toLoginBt:
                toLogin();
                break;
            case R.id.registerBt:
                resgister();
                break;
            case R.id.regBack:
                getActivity().onBackPressed();
                break;
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (conn != null && conn.isConnected()) {
            conn.disconnect();
        }
        View view = mActivity.findViewById(R.id.splashBottom);
        view.setVisibility(View.VISIBLE);
    }

    class MyTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (!"".equals(resusernameEt.getText().toString()) && !"".equals(respasswordEt.getText().toString()) && !"".equals(quepasswordEt.getText().toString())) {
                registerBt.setBackgroundColor(Color.parseColor("#4CAF50"));
                registerBt.setTextColor(Color.parseColor("#FFFFFF"));
                registerBt.setEnabled(true);
            } else {
                registerBt.setBackgroundColor(Color.parseColor("#55A8A5A5"));
                registerBt.setTextColor(Color.parseColor("#84000000"));
                registerBt.setEnabled(false);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    }


}
