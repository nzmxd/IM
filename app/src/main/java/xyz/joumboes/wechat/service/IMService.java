package xyz.joumboes.wechat.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.IBinder;

import androidx.annotation.Nullable;

import org.jivesoftware.smack.AbstractConnectionClosedListener;
import org.jivesoftware.smack.AbstractConnectionListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.chat2.IncomingChatMessageListener;
import org.jivesoftware.smack.chat2.OutgoingChatMessageListener;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.StanzaTypeFilter;
import org.jivesoftware.smack.packet.ExtensionElement;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jivesoftware.smack.roster.RosterListener;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smackx.offline.OfflineMessageManager;
import org.jivesoftware.smackx.vcardtemp.VCardManager;
import org.jivesoftware.smackx.vcardtemp.packet.VCard;
import org.jxmpp.jid.BareJid;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.Jid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import xyz.joumboes.wechat.activity.LoginActivity;
import xyz.joumboes.wechat.bean.Contact;
import xyz.joumboes.wechat.db.ContactRespository;
import xyz.joumboes.wechat.db.MessageRespository;
import xyz.joumboes.wechat.util.AddMessageDate;
import xyz.joumboes.wechat.util.AddMessageFile;
import xyz.joumboes.wechat.util.AddMessageTime;
import xyz.joumboes.wechat.util.AddMessageType;
import xyz.joumboes.wechat.util.Base64Util;
import xyz.joumboes.wechat.util.LogUtil;
import xyz.joumboes.wechat.util.ThreadUtils;
import xyz.joumboes.wechat.util.ToastUtils;

public class IMService extends Service {
    //总连接
    public static XMPPTCPConnection conn;
    //联系人服务
    public static Roster roster;
    //聊天管理服务
    private ChatManager chatManager;
    //联系人信息服务
    private VCardManager vCardManager;
    //离线信息管理
    private OfflineMessageManager offlineManager;
    //聊天服务
    private Map<String, Chat> chatMap = new HashMap<>();
    public static Chat currentChat;
    public static String currentAccount;
    //联系人列表
    private List<RosterEntry> rosterList = new ArrayList<>();
    //联系人查找
    //UserSearchManager userSearchManager;

    //网络变化监听
    private ConnectivityManager connectivityManager;
    private NetworkInfo info;
    //重连监听
    MyAbstractConnectionListener myAbstractConnectionListener = new MyAbstractConnectionListener();
    MyAbstractConnectionClosedListener myAbstractConnectionClosedListener = new MyAbstractConnectionClosedListener();
    //消息监听
    MyOutgoingChatMessageListener myOutgoingChatMessageListener = new MyOutgoingChatMessageListener();
    MyIncomingChatMessageListener myIncomingChatMessageListener = new MyIncomingChatMessageListener();
    MyRosterListener myRosterListener = new MyRosterListener();

    //联系人头像路径
    public static Map<String, String> iconMap = new HashMap<>();

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                LogUtil.d("网络状态已经改变");
                connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                info = connectivityManager.getActiveNetworkInfo();
                if (info != null && info.isAvailable()) {
                    String name = info.getTypeName();
                    LogUtil.d("当前网络名称：" + name);
                    reconn();
                    //doSomething()
                } else {

                    LogUtil.d("没有可用网络");
                    //doSomething()
                }
            }
        }
    };


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new MyBinder();
    }

    public class MyBinder extends Binder {
        public IMService getService() {
            return IMService.this;
        }
    }


    @Override
    public void onCreate() {
        super.onCreate();
        IntentFilter mFilter = new IntentFilter();
        mFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mReceiver, mFilter);
        //初始化全部服务 防止线程不安全
        roster = Roster.getInstanceFor(conn);
        chatManager = ChatManager.getInstanceFor(conn);
        vCardManager = VCardManager.getInstanceFor(conn);
        offlineManager = new OfflineMessageManager(conn);
        //userSearchManager = new UserSearchManager(conn);
        //初始化全部信息
        init();
        //获取离线信息
        getOfflineMessage();
        //初始化全部监听
        initLinster();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
        Presence p = new Presence(Presence.Type.unavailable);
        try {
            conn.sendStanza(p);
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        chatManager.removeIncomingListener(myIncomingChatMessageListener);
        chatManager.removeOutgoingListener(myOutgoingChatMessageListener);
        conn.removeConnectionListener(myAbstractConnectionListener);
        conn.removeConnectionListener(myAbstractConnectionClosedListener);
        roster.removeRosterListener(myRosterListener);
    }

    private void init() {

        ThreadUtils.runInThread(new Runnable() {
            @Override
            public void run() {
                //读取保存个人信息
                try {
                    VCard myCard = vCardManager.loadVCard(JidCreate.entityBareFrom(IMService.currentAccount));
                    if (myCard == null) {
                        myCard = new VCard();
                    }

                    saOrUpContact(myCard, JidCreate.bareFrom(currentAccount));
                } catch (Exception e) {
                    e.printStackTrace();
                }


                //读取联系人列表
                Set<RosterEntry> entries = roster.getEntries();
                LogUtil.d(String.valueOf(roster.getEntries().size()));
                rosterList.addAll(entries);
                //根据列表读取联系人信息进行更新或保存
                for (RosterEntry rosterEntry : rosterList) {
                    try {
                        BareJid jid = rosterEntry.getJid();
                        LogUtil.d(jid.toString());
                        VCard vCard = vCardManager.loadVCard(jid.asEntityBareJidIfPossible());
                        System.out.println(vCard.toString());
                        saOrUpContact(vCard, jid.asBareJid());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    //保存或更新联系人信息
    public void saOrUpContact(VCard vCard, Jid jid) {

        Contact contact = new Contact();
        String avatarpath = null;
        String uid = vCard.getJabberId();
        String nickName = vCard.getNickName();
        String email = vCard.getEmailHome();


        String mobiePhone = vCard.getField("mobiePhone");
        String city = vCard.getField("city");
        String sex = vCard.getField("sex");
        String signature = vCard.getField("signature");

        System.out.println(city);
        System.out.println(mobiePhone);
        System.out.println(sex);

        //TODO 获取联系人头像
        byte[] avatar = vCard.getAvatar();

        //联系人没设置名称时简单处理一下
        if (uid == null) {
            uid = jid.asBareJid().toString();
        }

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
        if (sex == null || "".equals(sex)) {
            sex = "保密";
        }

        iconMap.put(uid, avatarpath);

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

    private void initLinster() {
        //监听发送出去的会话信息
        chatManager.addOutgoingListener(myOutgoingChatMessageListener);
        //监听收到的会话信息
        chatManager.addIncomingListener(myIncomingChatMessageListener);
        //监听连接释放
        conn.addConnectionListener(myAbstractConnectionListener);
        //监听异常断开
        conn.addConnectionListener(myAbstractConnectionClosedListener);
        //联系人监听
        roster.addRosterListener(myRosterListener);

        AndFilter filter = new AndFilter(new StanzaTypeFilter(Presence.class));
//添加监听
        conn.addAsyncStanzaListener(packetListener, filter);

    }

    //发送消息
    public void sendMsg(Message message, String jid) {

        if (!isNetworkConnected(getApplicationContext()) || !conn.isConnected() || !conn.isAuthenticated()) {
            ToastUtils.showToastSafe(getApplicationContext(), "发送失败，当前无网络连接");
            return;
        }
        ThreadUtils.runInThread(new Runnable() {
            @Override
            public void run() {


                if (chatMap.containsKey(jid)) {
                    currentChat = chatMap.get(jid);
                    try {
                        currentChat.send(message);
                    } catch (SmackException.NotConnectedException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        currentChat = chatManager.chatWith(JidCreate.entityBareFrom(jid));
                        chatMap.put(jid, currentChat);
                        currentChat.send(message);
                    } catch (XmppStringprepException e) {
                        e.printStackTrace();
                    } catch (SmackException.NotConnectedException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }


            }
        });

    }

    //保存收到的消息消息
    void saveIncomingMsg(EntityBareJid from, Message message) {
        if (from == null) {
            return;
        }


        xyz.joumboes.wechat.bean.Message msg = new xyz.joumboes.wechat.bean.Message();
        String filepath = null;
        //接收id 发送id 消息体
        msg.setTargetId(currentAccount);
        msg.setSenderId(from.asEntityBareJidString());
        msg.setSession(from.asEntityBareJidString());
        //扩展字段
        ExtensionElement msgtype = message.getExtension("msgtype", "jabber:client");
        ExtensionElement msgdate = message.getExtension("msgdate", "jabber:client");
        ExtensionElement msgfile = message.getExtension("msgfile", "jabber:client");
        ExtensionElement msgtime = message.getExtension("msgtime", "jabber:client");
        Pattern r = Pattern.compile(">.*<");

        if (msgtype != null) {
            Matcher m = r.matcher(msgtype.toXML(null));
            if (m.find()) {
                String type = m.group(0).replace(">", "").replace("<", "");
                msg.setType(type);
            }
        }
        if (msgdate != null) {
            Matcher m = r.matcher(msgdate.toXML(null));
            if (m.find()) {
                String time = m.group(0).replace(">", "").replace("<", "");
                msg.setSentTime(time);
            }

        }
        if (msgfile != null) {
            Matcher m = r.matcher(msgfile.toXML(null));
            if (m.find()) {
                String file = m.group(0).replace(">", "").replace("<", "");
                filepath = getFilesDir().getPath() + File.separator + new File(file).getName();
                msg.setFilepath(filepath);
                Base64Util base64Util = new Base64Util();
                base64Util.base64ToFile(message.getBody(), filepath);
            }

        }
        if (msgtime != null) {
            Matcher m = r.matcher(msgtime.toXML(null));
            if (m.find()) {
                String time = m.group(0).replace(">", "").replace("<", "");
                msg.setVoicetime(time);
            }
        }
        if ("text".equals(msg.getType())) {
            msg.setMsgbody(message.getBody());
        } else {
            msg.setMsgbody(filepath);
        }

        new MessageRespository(this).insertMessages(msg);

    }

    void saveOutcomingMsg(EntityBareJid to, Message message) {
        xyz.joumboes.wechat.bean.Message msg = new xyz.joumboes.wechat.bean.Message();
        //接收id 发送id 消息体
        msg.setTargetId(to.asEntityBareJidString());
        msg.setSenderId(currentAccount);
        msg.setSession(to.asEntityBareJidString());
        //扩展字段
        AddMessageType type = null;
        AddMessageDate date = null;
        AddMessageFile file = null;
        AddMessageTime time = null;
        //扩展字段
        List<ExtensionElement> extensions = message.getExtensions();
        for (ExtensionElement extension : extensions) {

            if ("msgtype".equals(extension.getElementName())) {
                type = (AddMessageType) extension;
            }
            if ("msgdate".equals(extension.getElementName())) {
                date = (AddMessageDate) extension;
            }
            if ("msgfile".equals(extension.getElementName())) {
                file = (AddMessageFile) extension;
            }
            if ("msgtime".equals(extension.getElementName())) {
                time = (AddMessageTime) extension;
            }
        }

        //TODO 有时间和别的客户端做一下适配
        if (type != null) {
            //发送时间
//            LongToDateUtil todate = new LongToDateUtil();
//            String sendtime = todate.toData(Long.parseLong(date.getDateText()));
            msg.setSentTime(date.getDateText()); //设置时间
            LogUtil.d(date.getDateText());
            msg.setType(type.getTypeText()); //设置类型
            if ("text".equals(type.getTypeText())) {
                msg.setMsgbody(message.getBody());//添加消息体
            } else {
                //filename放在第三位
                //添加文件名
                msg.setFilepath(file.getFileText());
                if ("voice".equals(type.getTypeText())) {
                    //vocietime放在第四位
                    msg.setVoicetime(time.getTimeText()); //添加时长
                }
            }

            new MessageRespository(this).insertMessages(msg);
        }
    }

    //网络状况
    public boolean isNetworkConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            if (mNetworkInfo != null) {
                return mNetworkInfo.isAvailable();
            }
        }
        return false;
    }

    //重连
    public void reconn() {
        ThreadUtils.runInThread(new Runnable() {
            @Override
            public void run() {
                while (!conn.isConnected()) {
                    try {
                        conn.connect().login();
                        Presence presence = new Presence(Presence.Type.available);
                        conn.sendStanza(presence);
                        ToastUtils.showToastSafe(getApplication(), "重连成功");
                    } catch (Exception e1) {
                        e1.printStackTrace();
                        try {
                            Thread.sleep(5 * 1000);
                        } catch (InterruptedException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            }
        });
    }

    //离线信息获取
    void getOfflineMessage() {
        ThreadUtils.runInThread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(2 * 1000);

                    Presence p = new Presence(Presence.Type.unavailable);
                    conn.sendStanza(p);

                    List<Message> messages = offlineManager.getMessages();

                    int size = messages.size();
                    for (Message message : messages) {
                        Jid from = message.getFrom();
                        saveIncomingMsg(from.asEntityBareJidIfPossible(), message);
                    }

                    offlineManager.deleteMessages();
                    //将用户设置为上线
                    Presence presence = new Presence(Presence.Type.available);
                    conn.sendStanza(presence);
                    //删除历史信息
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });
    }

    //TODO 查找联系人 我的版本openfire没实现 不支持这个功能 不写了
    /*
    public void searchContact(String jid) {

        try {
            Form searchForm = userSearchManager.getSearchForm(JidCreate.domainBareFrom("search."+LoginActivity.ServiceName));
        } catch (Exception e){
            e.printStackTrace();
        }

    }
*/

    //添加联系人
    public void addContact(BareJid jid) {

        ThreadUtils.runInThread(new Runnable() {
            @Override
            public void run() {
                try {
                    roster.createEntry(jid, null, null);
                    VCard vCard = vCardManager.loadVCard(jid.asEntityBareJidIfPossible());
                    if (vCard == null) {
                        vCard = new VCard();
                    }

                    saOrUpContact(vCard, jid);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    //删除联系人
    public void delContact(BareJid jid) {
        ThreadUtils.runInThread(new Runnable() {
            @Override
            public void run() {
                RosterEntry entry = roster.getEntry(jid);
                try {
                    roster.removeEntry(entry);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                Contact contact = new ContactRespository(getApplication()).findByAccount(jid.toString());
                if (contact != null) {
                    new ContactRespository(getApplication()).deleteContacts(contact);
                }
                ToastUtils.showToastSafe(getApplication(), "删除成功");

            }
        });
    }

    //监听类
    class MyAbstractConnectionListener extends AbstractConnectionListener {
        @Override
        public void connectionClosedOnError(Exception e) {
            super.connectionClosedOnError(e);
            reconn();
        }
    }

    class MyAbstractConnectionClosedListener extends AbstractConnectionClosedListener {

        @Override
        public void connectionTerminated() {
            reconn();
        }
    }

    class MyOutgoingChatMessageListener implements OutgoingChatMessageListener {

        @Override
        public void newOutgoingMessage(EntityBareJid to, Message message, Chat chat) {
            ThreadUtils.runInThread(new Runnable() {
                @Override
                public void run() {
                    saveOutcomingMsg(to, message);
                }
            });
        }
    }

    class MyIncomingChatMessageListener implements IncomingChatMessageListener {

        @Override
        public void newIncomingMessage(EntityBareJid from, Message message, Chat chat) {
            ThreadUtils.runInThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    saveIncomingMsg(from, message);
                }
            });
        }
    }

    class MyRosterListener implements RosterListener {

        @Override
        public void entriesAdded(Collection<Jid> addresses) {
            for (Jid address : addresses) {
                VCard vCard = null;
                try {
                    vCard = vCardManager.loadVCard(address.asEntityBareJidIfPossible());
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (vCard == null) {
                    vCard = new VCard();
                }
                saOrUpContact(vCard, address.asBareJid());
            }
        }

        @Override
        public void entriesUpdated(Collection<Jid> addresses) {
            for (Jid address : addresses) {
                VCard vCard = null;
                try {
                    vCard = vCardManager.loadVCard(address.asEntityBareJidIfPossible());
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (vCard == null) {
                    vCard = new VCard();
                }
                saOrUpContact(vCard, address.asBareJid());
            }
        }

        @Override
        public void entriesDeleted(Collection<Jid> addresses) {
            for (Jid address : addresses) {
                Contact contact = new ContactRespository(getApplicationContext()).findByAccount(address.toString());
                if (contact != null) {
                    new ContactRespository(getApplicationContext()).deleteContacts(contact);
                }

            }
        }

        @Override
        public void presenceChanged(Presence presence) {

        }
    }

    StanzaListener packetListener = new StanzaListener() {
        @Override
        public void processStanza(Stanza packet) throws SmackException.NotConnectedException, InterruptedException, SmackException.NotLoggedInException {
            if (packet instanceof Presence) {
                Presence presence = (Presence) packet;
                if (presence.getType().equals(Presence.Type.subscribe)) {
                    addContact((BareJid) presence.getFrom());
                } else if (presence.getType().equals(Presence.Type.subscribed)) {//对方同意订阅

                } else if (presence.getType().equals(Presence.Type.unsubscribe)) {//取消订阅
                 delContact((BareJid) presence.getFrom());
                } else if (presence.getType().equals(Presence.Type.unsubscribed)) {//拒绝订阅

                } else if (presence.getType().equals(Presence.Type.unavailable)) {//离线

                } else if (presence.getType().equals(Presence.Type.available)) {//上线

                }
            }
        }

    };
}
