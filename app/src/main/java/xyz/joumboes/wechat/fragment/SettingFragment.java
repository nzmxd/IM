package xyz.joumboes.wechat.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import xyz.joumboes.wechat.R;
import xyz.joumboes.wechat.activity.SettingActivity;
import xyz.joumboes.wechat.bean.Contact;
import xyz.joumboes.wechat.db.ContactRespository;
import xyz.joumboes.wechat.db.MessageRespository;
import xyz.joumboes.wechat.service.IMService;
import xyz.joumboes.wechat.util.ThreadUtils;

import static android.content.Context.MODE_PRIVATE;

/**
 * A simple {@link Fragment} subclass.
 */
public class SettingFragment extends Fragment {

    private View rootView;//缓存Fragment view

    public SettingFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (null != rootView) {
            ViewGroup parent = (ViewGroup) rootView.getParent();
            if (null != parent) {
                parent.removeView(rootView);
            }
        } else {
            rootView = inflater.inflate(R.layout.fragment_setting, null);
        }

        // Inflate the layout for this fragment
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        init();
    }

    private void init() {


        ThreadUtils.runInUIThread(new Runnable() {
            @Override
            public void run() {

                TextView name = getView().findViewById(R.id.SettingnameTv);
                TextView jidTv = getView().findViewById(R.id.jidTv);
                ImageView IconIm = getView().findViewById(R.id.Icon_Im);


                Contact account = new ContactRespository(getContext()).findByAccount(IMService.currentAccount);
                if (account == null) {
                    return;
                }

                String avatar = account.getAvatar();
                String nickname = account.getNickname();
                String Jid = account.getAccount();
                if (avatar != null) {
                    Bitmap bmp = BitmapFactory.decodeFile(avatar);
                    IconIm.setImageBitmap(bmp);
                }
                name.setText(nickname);
                jidTv.setText(String.valueOf("账号: " + Jid));


            }
        });


        ThreadUtils.runInUIThread(new Runnable() {
            @Override
            public void run() {
                View setingInfo = getView().findViewById(R.id.setting_info);

                View settingBt = getView().findViewById(R.id.settingLinLout);
                View exitBt = getView().findViewById(R.id.exitLinLout);
                View changeBt = getView().findViewById(R.id.changeUserLinLout);
                View securityBt = getView().findViewById(R.id.securityLinLout);
                setingInfo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getContext(), SettingActivity.class);
                        intent.putExtra("account", IMService.currentAccount);
                        startActivity(intent);
                    }
                });

                changeBt.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showDialog();

                    }
                });

                exitBt.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        System.exit(0);
                    }
                });
            }
        });

    }

    public void restartApplication(Context context) {
        final Intent intent = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    private void showDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("警告！");
        builder.setMessage("切换账号会清楚所有信息，请确认。");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deletedAllinfo();
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void deletedAllinfo() {
        new ContactRespository(getContext()).deleteAllContacts();
        new MessageRespository(getContext()).deleteAllMessages();
        SharedPreferences sharedPre = getActivity().getSharedPreferences("service_setting", MODE_PRIVATE);
        SharedPreferences.Editor edit = sharedPre.edit();
        edit.remove("autologin");
        edit.remove("saveusername");
        edit.remove("savepasswd");
        edit.commit();
        restartApplication(getContext());
    }

}
