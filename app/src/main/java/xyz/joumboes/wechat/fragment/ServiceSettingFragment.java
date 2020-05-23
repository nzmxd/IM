package xyz.joumboes.wechat.fragment;

import android.app.Activity;
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
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import xyz.joumboes.wechat.R;
import xyz.joumboes.wechat.util.ThreadUtils;
import xyz.joumboes.wechat.util.ToastUtils;

import static android.content.Context.MODE_PRIVATE;

/**
 * A simple {@link Fragment} subclass.
 */
public class ServiceSettingFragment extends Fragment {

    @BindView(R.id.serviceIpEt)
    EditText serviceIpEt;
    @BindView(R.id.servicePortEt)
    EditText servicePortEt;
    @BindView(R.id.serviceNameEt)
    EditText serviceNameEt;
    @BindView(R.id.serviceTestBt)
    Button serviceTestBt;
    @BindView(R.id.saveConfigBt)
    Button saveConfigBt;
    @BindView(R.id.resetBt)
    Button resetBt;


    XMPPTCPConnectionConfiguration.Builder builder = XMPPTCPConnectionConfiguration.builder();
    XMPPTCPConnectionConfiguration config = null;
    XMPPTCPConnection conn = null;
    SharedPreferences.Editor editor;
    SharedPreferences sharedPre;
    Activity mActivity;


    private static String HOST = "47.107.80.247";
    private static int PORT = 5222;
    private static String ServiceName = "joumboes.xyz";


    public ServiceSettingFragment() {
        // Required empty public constructor
    }

    public void setActivity(Activity activity) {
        this.mActivity = activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_service_setting, container, false);
        ButterKnife.bind(this, view);
        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        init();
    }

    private void init() {
        serviceIpEt.addTextChangedListener(new MyTextWatcher());
        servicePortEt.addTextChangedListener(new MyTextWatcher());
        serviceNameEt.addTextChangedListener(new MyTextWatcher());
        sharedPre = getActivity().getSharedPreferences("service_setting", MODE_PRIVATE);
        editor = sharedPre.edit();
        HOST = sharedPre.getString("host", HOST);
        PORT = sharedPre.getInt("port", PORT);
        ServiceName = sharedPre.getString("servicename", ServiceName);
        serviceIpEt.setText(HOST);
        servicePortEt.setText(String.valueOf(PORT));
        serviceNameEt.setText(ServiceName);

    }

    @OnClick({R.id.regBack, R.id.toLoginBt, R.id.serviceTestBt, R.id.saveConfigBt, R.id.resetBt})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.regBack:
                back();
                break;
            case R.id.toLoginBt:
                back();
                break;
            case R.id.serviceTestBt:
                test();
                break;
            case R.id.saveConfigBt:
                save();
                break;
            case R.id.resetBt:
                reset();
                break;
        }
    }

    //返回
    private void back() {
        getActivity().onBackPressed();
    }

    //测试
    private void test() {

        ThreadUtils.runInThread(new Runnable() {
            @Override
            public void run() {
                String host = serviceIpEt.getText().toString();
                String port = servicePortEt.getText().toString();
                String serviceName = serviceNameEt.getText().toString();

                try {
                    config = builder.setHostAddressByNameOrIp(host)
                            .setPort(Integer.parseInt(port))
                            .setXmppDomain(serviceName)
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
                    ToastUtils.showToastSafe(getActivity(), "连接失败！！");
                    e.printStackTrace();
                    return;
                }

                ToastUtils.showToastSafe(getActivity(), "连接成功！");



            }
        });



    }

    //保存
    private void save() {
        String host = serviceIpEt.getText().toString();
        String port = servicePortEt.getText().toString();
        String serviceName = serviceNameEt.getText().toString();

        if (conn==null||!conn.isConnected()) {
            ToastUtils.showToastSafe(getActivity(), "请先测试连接再保存。");
            return;
        }

        editor.putString("host", host);
        editor.putInt("port", Integer.parseInt(port));
        editor.putString("servicename", serviceName);

        editor.commit();

        ToastUtils.showToastSafe(getActivity(), "保存成功！");


    }


    //重置
    private void reset() {
        String host = "47.107.80.247";
        String port = "5222";
        String serviceName = "joumboes.xyz";
        editor.putString("host", host);
        editor.putString("port", port);
        editor.putString("servicename", serviceName);
        serviceIpEt.setText(host);
        servicePortEt.setText(port);
        serviceNameEt.setText(serviceName);
        editor.commit();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (conn != null && conn.isConnected()) {
            conn.disconnect();
        }

        mActivity.findViewById(R.id.toolbar2).setVisibility(View.VISIBLE);
        mActivity.findViewById(R.id.loginLinLout).setVisibility(View.VISIBLE);
    }

    class MyTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (!"".equals(serviceIpEt.getText().toString()) && !"".equals(servicePortEt.getText().toString()) && !"".equals(serviceNameEt.getText().toString())) {
                serviceTestBt.setBackgroundColor(Color.parseColor("#4CAF50"));
                serviceTestBt.setTextColor(Color.parseColor("#FFFFFF"));
                serviceTestBt.setEnabled(true);
                saveConfigBt.setBackgroundColor(Color.parseColor("#4CAF50"));
                saveConfigBt.setTextColor(Color.parseColor("#FFFFFF"));
                saveConfigBt.setEnabled(true);
                resetBt.setBackgroundColor(Color.parseColor("#4CAF50"));
                resetBt.setTextColor(Color.parseColor("#FFFFFF"));
                resetBt.setEnabled(true);
            } else {
                serviceTestBt.setBackgroundColor(Color.parseColor("#55A8A5A5"));
                serviceTestBt.setTextColor(Color.parseColor("#84000000"));
                serviceTestBt.setEnabled(false);
                saveConfigBt.setBackgroundColor(Color.parseColor("#55A8A5A5"));
                saveConfigBt.setTextColor(Color.parseColor("#84000000"));
                saveConfigBt.setEnabled(false);
                resetBt.setBackgroundColor(Color.parseColor("#55A8A5A5"));
                resetBt.setTextColor(Color.parseColor("#84000000"));
                resetBt.setEnabled(false);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    }

}
