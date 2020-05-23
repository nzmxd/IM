package xyz.joumboes.wechat.activity;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.tbruyelle.rxpermissions2.RxPermissions;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.functions.Consumer;
import xyz.joumboes.wechat.R;
import xyz.joumboes.wechat.fragment.RegisterFragment;
import xyz.joumboes.wechat.util.LogUtil;
import xyz.joumboes.wechat.widget.SetPermissionDialog;

public class SplashActivity extends AppCompatActivity {

    @BindView(R.id.loginBt)
    Button loginBt;
    @BindView(R.id.regBt)
    Button regBt;

    static SplashActivity splashActivity;
    @BindView(R.id.splashBottom)
    LinearLayout splashBottom;
    @BindView(R.id.splash_ll)
    LinearLayout splashLl;

    RegisterFragment regFragment;
    FragmentTransaction transaction;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        ButterKnife.bind(this);
        splashActivity = this;
        regFragment = new RegisterFragment();
        regFragment.setActivity(this);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                requestPermisson();
            }
        }, 100);
        LogUtil.d(new String(Character.toChars(0x1F60E)));

    }


    private void requestPermisson() {
        RxPermissions rxPermission = new RxPermissions(this);
        rxPermission
                .request(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,//存储权限
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.CAMERA,
                        Manifest.permission.RECORD_AUDIO
                )
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean aBoolean) throws Exception {
                        if (aBoolean) {
                            SharedPreferences sharedPre = getSharedPreferences("service_setting", MODE_PRIVATE);
                            String autologin = sharedPre.getString("autologin", "false");
                            if ("true".equals(autologin)) {
                                startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                            }
                        } else {

                            SetPermissionDialog mSetPermissionDialog = new SetPermissionDialog(SplashActivity.this);
                            mSetPermissionDialog.show();
                            mSetPermissionDialog.setConfirmCancelListener(new SetPermissionDialog.OnConfirmCancelClickListener() {
                                @Override
                                public void onLeftClick() {

                                    finish();
                                }

                                @Override
                                public void onRightClick() {

                                    finish();
                                }
                            });
                        }
                    }
                });
    }

    @OnClick({R.id.loginBt, R.id.regBt})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.loginBt:
                startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                break;
            case R.id.regBt:
                regster();
                break;
        }
    }

    //注册
    void regster() {
        splashBottom.setVisibility(View.GONE);
        transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.splash_ll, regFragment);
        transaction.addToBackStack(null);

        // Commit the transaction
        transaction.commit();
    }


}
