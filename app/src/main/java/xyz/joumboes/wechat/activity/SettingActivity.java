package xyz.joumboes.wechat.activity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;

import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.entity.LocalMedia;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.vcardtemp.VCardManager;
import org.jivesoftware.smackx.vcardtemp.packet.VCard;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import xyz.joumboes.wechat.R;
import xyz.joumboes.wechat.bean.Contact;
import xyz.joumboes.wechat.bean.Message;
import xyz.joumboes.wechat.db.ContactRespository;
import xyz.joumboes.wechat.db.MessageRespository;
import xyz.joumboes.wechat.service.IMService;
import xyz.joumboes.wechat.util.LogUtil;
import xyz.joumboes.wechat.util.PictureFileUtil;

public class SettingActivity extends AppCompatActivity {

    VCardManager cardManager;
    Contact contact;
    @BindView(R.id.setIcon)
    ImageView setIcon;

    @BindView(R.id.changeIcon)
    ImageView changeIcon;

    @BindView(R.id.changeNameEt)
    EditText changeNameEt;
    @BindView(R.id.maleRb)
    RadioButton maleRb;
    @BindView(R.id.femaleRb)
    RadioButton femaleRb;
    @BindView(R.id.unkonwRb)
    RadioButton unkonwRb;
    @BindView(R.id.changPhoneEt)
    EditText changPhoneEt;
    @BindView(R.id.emailEt)
    EditText emailEt;
    @BindView(R.id.changeCityEt)
    EditText changeCityEt;
    @BindView(R.id.changeSignatureEt)
    EditText changeSignatureEt;
    @BindView(R.id.changeInfoBt)
    Button changeInfoBt;
    @BindView(R.id.SaveInfoBt)
    Button SaveInfoBt;
    @BindView(R.id.jidTv)
    TextView jidTv;
    @BindView(R.id.delcontactBt)
    Button delcontactBt;

    List<View> viewList = new ArrayList<>();
    public static final int REQUEST_CODE_IMAGE = 0000;
    byte[] bytes;
    String iconpath;
    String account;
    Conn conn;
    IMService imservice;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        ButterKnife.bind(this);
        conn = new Conn();
        Intent service = new Intent(this, IMService.class);
        account = getIntent().getStringExtra("account");
        bindService(service, conn, BIND_AUTO_CREATE);

        init();


    }


    private void init() {
        if (!account.equals(IMService.currentAccount)) {
            changeInfoBt.setVisibility(View.GONE);
            SaveInfoBt.setVisibility(View.GONE);
            delcontactBt.setVisibility(View.VISIBLE);
            delcontactBt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        imservice.delContact(JidCreate.bareFrom(account));
                        setResult(100,new Intent());
                        finish();
                    } catch (XmppStringprepException e) {
                        e.printStackTrace();
                    }
                }
            });
        }


        cardManager = VCardManager.getInstanceFor(IMService.conn);
        contact = new ContactRespository(this).findByAccount(account);
        String nickname = contact.getNickname();
        String avatar = contact.getAvatar();
        String address = contact.getAddress();
        String phone = contact.getPhone();
        String email = contact.getEmail();
        String sex = contact.getSex();
        String signature = contact.getSignature();
        jidTv.setText(account);
        if (avatar != null) {
            Bitmap bmp = BitmapFactory.decodeFile(avatar);
            changeIcon.setImageBitmap(bmp);
        }

        if (phone != null) {
            changPhoneEt.setText(phone);
        }

        if (email != null) {
            emailEt.setText(email);
        }

        if (signature != null) {
            changeSignatureEt.setText(signature);
        }
        if (address != null) {
            changeCityEt.setText(address);
        }


        changeNameEt.setText(nickname);
        if ("保密".equals(sex)) {
            unkonwRb.setChecked(true);
        } else if ("男".equals(sex)) {
            maleRb.setChecked(true);
        } else {
            femaleRb.setChecked(true);
        }


        viewList.add(setIcon);
        viewList.add(changeNameEt);
        viewList.add(maleRb);
        viewList.add(femaleRb);
        viewList.add(unkonwRb);
        viewList.add(changPhoneEt);
        viewList.add(emailEt);
        viewList.add(changeCityEt);
        viewList.add(changeSignatureEt);

        for (View view : viewList) {
            view.setEnabled(false);
        }
    }

    private void changeIngo() {
        VCard vCard = null;
        try {
            vCard = cardManager.loadVCard();
        } catch (SmackException.NoResponseException e) {
            e.printStackTrace();
            return;
        } catch (XMPPException.XMPPErrorException e) {
            e.printStackTrace();
            return;
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
            return;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return;
        }

        String name = changeNameEt.getText().toString();
        String phone = changPhoneEt.getText().toString();
        String email = emailEt.getText().toString();
        String city = changeCityEt.getText().toString();
        String signature = changeSignatureEt.getText().toString();

        System.out.println(phone);
        if (bytes != null) {
            vCard.setAvatar(bytes);
        }
        vCard.setNickName(name);


        if (maleRb.isChecked()) {
            vCard.setField("sex", "男");
        } else if (femaleRb.isChecked()) {
            vCard.setField("sex", "女");
        } else {
            vCard.setField("sex", "保密");
        }


        vCard.setJabberId(IMService.currentAccount);
        vCard.setEmailHome(email);
        vCard.setField("mobiePhone", phone);
        vCard.setField("city", city);
        vCard.setField("signature", signature);

        try {
            cardManager.saveVCard(vCard);
        } catch (SmackException.NoResponseException e) {
            e.printStackTrace();
        } catch (XMPPException.XMPPErrorException e) {
            e.printStackTrace();
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        saOrUpContact(vCard);
        for (View v : viewList) {
            v.setEnabled(false);
        }
    }

    @OnClick({R.id.changeInfoBt, R.id.SaveInfoBt})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.changeInfoBt:
                for (View v : viewList) {
                    v.setEnabled(true);
                }
                break;
            case R.id.SaveInfoBt:
                changeIngo();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_IMAGE:
                    // 图片选择结果回调
                    List<LocalMedia> selectListPic = PictureSelector.obtainMultipleResult(data);
                    for (LocalMedia media : selectListPic) {
                        LogUtil.d("获取图片路径成功:" + media.getPath());
                        iconpath = media.getCompressPath();
                        try {

                            InputStream fis = new FileInputStream(iconpath);
                            bytes = new byte[fis.available()];
                            fis.read(bytes);
                            fis.close();
                            Bitmap bmp = BitmapFactory.decodeFile(iconpath);
                            changeIcon.setImageBitmap(bmp);

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    break;

            }
        }
    }


    @OnClick(R.id.setIcon)
    public void onViewClicked() {
        PictureFileUtil.openGalleryPic(SettingActivity.this, REQUEST_CODE_IMAGE);
    }

    public void saOrUpContact(VCard vCard) {
        Contact contact = new Contact();
        String avatarpath = null;
        String uid = vCard.getJabberId();
        String nickName = vCard.getNickName();
        String city = vCard.getAddressFieldHome("city");
        String email = vCard.getEmailHome();
        String mobiePhone = vCard.getPhoneHome("mobiePhone");
        String sex = vCard.getField("sex");
        String signature = vCard.getField("signature");
        //TODO 获取联系人头像
        byte[] avatar = vCard.getAvatar();

        System.out.println(sex);

        //联系人没设置名称时简单处理一下
        if (nickName == null || "".equals(nickName)) {
            nickName = uid.replace("@" + LoginActivity.ServiceName, "");
        }

        if (avatar != null) {
            try {
                FileOutputStream outputStream = openFileOutput(nickName + ".jpg", MODE_PRIVATE);
                outputStream.write(avatar);
                outputStream.close();
                avatarpath = getFilesDir().getCanonicalPath() + "/" + nickName + ".jpg";
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        //简单处理签名
        if (signature == null || "".equals(signature)) {
            signature = "这个人很懒，什么都没留下";
        }
        //性别
//        if (sex == null || "".equals(sex)) {
//            sex = "保密";
//        }

        contact.setAccount(uid);
        contact.setAddress(city);
        contact.setNickname(nickName);
        contact.setSex(sex);
        contact.setEmail(email);
        contact.setPhone(mobiePhone);
        contact.setSignature(signature);
        contact.setAvatar(avatarpath);
        new ContactRespository(this).insetOrUpdata(contact);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (conn != null) {
            unbindService(conn);
        }
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


}
