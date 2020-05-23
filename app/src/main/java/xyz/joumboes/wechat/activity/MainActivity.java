package xyz.joumboes.wechat.activity;

import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.chaychan.library.BottomBarItem;
import com.chaychan.library.BottomBarLayout;

import org.jivesoftware.smack.android.AndroidSmackInitializer;
import org.jivesoftware.smackx.vcardtemp.packet.VCard;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import xyz.joumboes.wechat.R;
import xyz.joumboes.wechat.adapter.MyFragmentAdapter;
import xyz.joumboes.wechat.service.IMService;
import xyz.joumboes.wechat.util.ThreadUtils;
import xyz.joumboes.wechat.util.ToastUtils;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.vp_content)
    ViewPager vpContent;
    @BindView(R.id.bbl)
    BottomBarLayout bbl;
    @BindView(R.id.textView)
    TextView textView;
    @BindView(R.id.mainTb)
    Toolbar mainTb;


    Conn conn;
    IMService imservice;
    public static Map<String, Integer> msgnums = new HashMap<>();
    public static Map<String, Integer> msg_id = new HashMap<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        AndroidSmackInitializer.initialize(this);
        conn = new Conn();
        Intent service = new Intent(this, IMService.class);
        bindService(service, conn, BIND_AUTO_CREATE);

        initView();
    }

    private void initView() {

        ThreadUtils.runInUIThread(new Runnable() {
            @Override
            public void run() {
                //添加好友
                PopupMenu popupMenu = new PopupMenu(MainActivity.this, mainTb.findViewById(R.id.action_setting1));
                // 获取布局文件
                popupMenu.getMenuInflater().inflate(R.menu.main_popupmenu, popupMenu.getMenu());

                //
                vpContent.setAdapter(new MyFragmentAdapter(getSupportFragmentManager()));
                bbl.setViewPager(vpContent);
                //滑动效果
                bbl.setSmoothScroll(true);
                //设置标题改变
                bbl.setOnItemSelectedListener(new BottomBarLayout.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(BottomBarItem bottomBarItem, int i, int i1) {
                        if (bottomBarItem.getId() == R.id.bottom_message) {
                            textView.setText("消息");
                            mainTb.setVisibility(View.VISIBLE);
                        } else if (bottomBarItem.getId() == R.id.bottom_contact) {
                            textView.setText("联系人");
                            mainTb.setVisibility(View.VISIBLE);
                        } else {
                            mainTb.setVisibility(View.GONE);
                        }
                    }
                });


                mainTb.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if (item.getItemId() == R.id.action_setting1) {
                            popupMenu.show();
                        }
                        return false;
                    }
                });

                AlertDialog alertDialog = myDialog();

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if (item.getItemId() == R.id.addContact) {
                            alertDialog.show();
                        }
                        return false;
                    }
                });


            }
        });


    }

    private AlertDialog myDialog() {


        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.add_contact_dialog, null);
        builder.setView(view);
        TextView addTv = view.findViewById(R.id.addContactEt);


        builder.setPositiveButton("添加", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked OK button
                String jid = addTv.getText().toString();
                try {
                    imservice.addContact(JidCreate.bareFrom(jid+"@"+LoginActivity.ServiceName));
                } catch (XmppStringprepException e) {
                    e.printStackTrace();
                };
                ToastUtils.showToastSafe(MainActivity.this, "添加成功");

            }
        });

        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            }
        });
        builder.setTitle("添加好友");
        return builder.create();
    }


    private class Conn implements ServiceConnection {
        //绑定服务成功的监听  service:是服务绑定成功后的返回值：onbund方法的返回值
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            IMService.MyBinder bind = (IMService.MyBinder) service;
            imservice = bind.getService();
        }

        //因为异常，绑定服务失败的监听
        @Override
        public void onServiceDisconnected(ComponentName name) {
            // TODO Auto-generated method stub

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (conn != null) {
            unbindService(conn);
        }
    }

}
