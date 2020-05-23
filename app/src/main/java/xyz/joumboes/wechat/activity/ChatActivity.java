package xyz.joumboes.wechat.activity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.entity.LocalMedia;
import com.nbsp.materialfilepicker.ui.FilePickerActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import xyz.joumboes.wechat.R;
import xyz.joumboes.wechat.adapter.ChatAdapter;
import xyz.joumboes.wechat.bean.AudioMsgBody;
import xyz.joumboes.wechat.bean.FileMsgBody;
import xyz.joumboes.wechat.bean.ImageMsgBody;
import xyz.joumboes.wechat.bean.Message;
import xyz.joumboes.wechat.bean.MsgSendStatus;
import xyz.joumboes.wechat.bean.MsgType;
import xyz.joumboes.wechat.bean.TextMsgBody;
import xyz.joumboes.wechat.bean.VideoMsgBody;
import xyz.joumboes.wechat.db.MessageRespository;
import xyz.joumboes.wechat.service.IMService;
import xyz.joumboes.wechat.util.AddMessageDate;
import xyz.joumboes.wechat.util.AddMessageFile;
import xyz.joumboes.wechat.util.AddMessageTime;
import xyz.joumboes.wechat.util.AddMessageType;
import xyz.joumboes.wechat.util.Base64Util;
import xyz.joumboes.wechat.util.ChatUiHelper;
import xyz.joumboes.wechat.util.LogUtil;
import xyz.joumboes.wechat.util.PictureFileUtil;
import xyz.joumboes.wechat.widget.MediaManager;
import xyz.joumboes.wechat.widget.RecordButton;
import xyz.joumboes.wechat.widget.StateButton;


public class ChatActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    @BindView(R.id.llContent)
    LinearLayout mLlContent;
    @BindView(R.id.rv_chat_list)
    RecyclerView mRvChat;
    @BindView(R.id.et_content)
    EditText mEtContent;
    @BindView(R.id.bottom_layout)
    RelativeLayout mRlBottomLayout;//表情,添加底部布局
    @BindView(R.id.ivAdd)
    ImageView mIvAdd;
    @BindView(R.id.ivEmo)
    ImageView mIvEmo;
    @BindView(R.id.btn_send)
    StateButton mBtnSend;//发送按钮
    @BindView(R.id.ivAudio)
    ImageView mIvAudio;//录音图片
    @BindView(R.id.btnAudio)
    RecordButton mBtnAudio;//录音按钮
    @BindView(R.id.rlEmotion)
    LinearLayout mLlEmotion;//表情布局
    @BindView(R.id.llAdd)
    LinearLayout mLlAdd;//添加布局
    @BindView(R.id.swipe_chat)
    SwipeRefreshLayout mSwipeRefresh;//下拉刷新
    @BindView(R.id.chatTop)
    LinearLayout mTop;//顶部布局
    private ChatAdapter mAdapter;
    public static final String mSenderId = "right";
    public static final String mTargetId = "left";
    public static final int REQUEST_CODE_IMAGE = 0000;
    public static final int REQUEST_CODE_VEDIO = 1111;
    public static final int REQUEST_CODE_FILE = 2222;

    String nickname;
    String toJid;
    Base64Util base64Util = new Base64Util();
    private IMService imservice;
    Conn conn;
    //历史消息
    private List<Message> historyMsg;
    private boolean flag = false;
    private int msg_id = -1;

    SimpleDateFormat sdf = new SimpleDateFormat("MM-dd HH:mm");
    Date senddate = new Date();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        conn = new Conn();
        Intent service = new Intent(this, IMService.class);
        bindService(service, conn, BIND_AUTO_CREATE);

        nickname = getIntent().getStringExtra("nickname");
        toJid = getIntent().getStringExtra("accountid");
        initContent();
        new MessageRespository(this, toJid).getSessionMessageLive().observe(this, new Observer<List<Message>>() {
            @Override
            public void onChanged(List<Message> messages) {
                if (messages.size() == 0) {
                    return;
                }

                if (historyMsg == null && !flag) {
                    historyMsg = new ArrayList<>();
                    historyMsg = messages;
                    if (historyMsg.size() > 10) {
                        msg_id = historyMsg.get(historyMsg.size() - 1).get_id();
                        for (int i = 0; i < 10; i++) {
                            showMessage(historyMsg.get(historyMsg.size() - 1), 0);
                            historyMsg.remove(historyMsg.size() - 1);
                        }

                    } else {

                        Collections.reverse(historyMsg);
                        msg_id = historyMsg.get(0).get_id();
                        for (Message message : historyMsg) {
                            showMessage(message, 0);
                        }
                        historyMsg = null;
                    }
                    flag = true;
                    return;
                }

                LogUtil.d(String.valueOf(msg_id));
                Message lastmessage = messages.get(messages.size() - 1);

                LogUtil.d(String.valueOf(lastmessage.get_id()));
                if (msg_id == lastmessage.get_id()) {
                    return;
                }
                msg_id = lastmessage.get_id();
                showMessage(lastmessage);
            }
        });

    }

    private void showMessage(Message message) {


        if (toJid.equals(message.getTargetId())) {
            String iconpath = IMService.iconMap.get(IMService.currentAccount);
            if ("text".equals(message.getType())) {
                Message mMessgae = getBaseSendMessage(MsgType.TEXT);

                if (iconpath != null) {
                    mMessgae.setAvatar(iconpath);
                }

                TextMsgBody textMsgBody = new TextMsgBody();
                textMsgBody.setMessage(message.getMsgbody());
                mMessgae.setSentTime(message.getSentTime());
                mMessgae.setBody(textMsgBody);
                mAdapter.addData(mMessgae);
                updateMsg(mMessgae);
            }
            if ("image".equals(message.getType())) {
                Message mMessgaeImage = getBaseSendMessage(MsgType.IMAGE);
                if (iconpath != null) {
                    mMessgaeImage.setAvatar(iconpath);
                }
//                mMessgaeImage.setSentTime(message.getSentTime());
                ImageMsgBody mImageMsgBody = new ImageMsgBody();
                mImageMsgBody.setThumbUrl(message.getFilepath());
                mImageMsgBody.setLocalPath(message.getFilepath());
                mMessgaeImage.setBody(mImageMsgBody);
                mAdapter.addData(mMessgaeImage);
                updateMsg(mMessgaeImage);
            }
            if ("file".equals(message.getType())) {
                Message mMessgaeFile = getBaseSendMessage(MsgType.FILE);
//                mMessgaeFile.setSentTime(message.getSentTime());
                if (iconpath != null) {
                    mMessgaeFile.setAvatar(iconpath);
                }
                FileMsgBody mFileMsgBody = new FileMsgBody();
                mFileMsgBody.setLocalPath(message.getFilepath());
                File file = new File(message.getFilepath());
                mFileMsgBody.setDisplayName(file.getName());
                mFileMsgBody.setSize(file.length());
                mMessgaeFile.setBody(mFileMsgBody);
                mAdapter.addData(mMessgaeFile);
                updateMsg(mMessgaeFile);
            }
            if ("voice".equals(message.getType())) {
                Message mMessgaeAudio = getBaseSendMessage(MsgType.AUDIO);
//                mMessgaeAudio.setSentTime(message.getSentTime());
                if (iconpath != null) {
                    mMessgaeAudio.setAvatar(iconpath);
                }
                AudioMsgBody audioMsgBody = new AudioMsgBody();
                audioMsgBody.setDuration(Long.parseLong(message.getVoicetime()));
                audioMsgBody.setLocalPath(message.getFilepath());
                mMessgaeAudio.setBody(audioMsgBody);
                mAdapter.addData(mMessgaeAudio);
                updateMsg(mMessgaeAudio);
            }

        } else {
            String iconpath = IMService.iconMap.get(toJid);

            if ("text".equals(message.getType())) {
                Message mMessgae = getBaseReceiveMessage(MsgType.TEXT);
                if (iconpath != null) {
                    mMessgae.setAvatar(iconpath);
                }
                mMessgae.setSentTime(message.getSentTime());
                TextMsgBody textMsgBody = new TextMsgBody();
                textMsgBody.setMessage(message.getMsgbody());
                mMessgae.setBody(textMsgBody);
                mAdapter.addData(mMessgae);
                updateMsg(mMessgae);
            }
            if ("image".equals(message.getType())) {
                Message mMessgaeImage = getBaseReceiveMessage(MsgType.IMAGE);
                if (iconpath != null) {
                    mMessgaeImage.setAvatar(iconpath);
                }
                ImageMsgBody mImageMsgBody = new ImageMsgBody();
                mImageMsgBody.setThumbUrl(message.getFilepath());
                mImageMsgBody.setLocalPath(message.getFilepath());
                mMessgaeImage.setBody(mImageMsgBody);
                mAdapter.addData(mMessgaeImage);
                updateMsg(mMessgaeImage);
            }
            if ("file".equals(message.getType())) {
                Message mMessgaeFile = getBaseReceiveMessage(MsgType.FILE);
                if (iconpath != null) {
                    mMessgaeFile.setAvatar(iconpath);
                }
                FileMsgBody mFileMsgBody = new FileMsgBody();
                mFileMsgBody.setLocalPath(message.getFilepath());
                File file = new File(message.getFilepath());
                mFileMsgBody.setDisplayName(file.getName());
                mFileMsgBody.setSize(file.length());
                mMessgaeFile.setBody(mFileMsgBody);
                mAdapter.addData(mMessgaeFile);
                updateMsg(mMessgaeFile);
            }
            if ("voice".equals(message.getType())) {
                Message mMessgaeAudio = getBaseReceiveMessage(MsgType.AUDIO);
                if (iconpath != null) {
                    mMessgaeAudio.setAvatar(iconpath);
                }

                AudioMsgBody audioMsgBody = new AudioMsgBody();
                audioMsgBody.setDuration(Long.parseLong(message.getVoicetime()));
                audioMsgBody.setLocalPath(message.getFilepath());
                mMessgaeAudio.setBody(audioMsgBody);
                mAdapter.addData(mMessgaeAudio);
                updateMsg(mMessgaeAudio);
            }


        }
    }

    private void showMessage(Message message, int i) {


        if (toJid.equals(message.getTargetId())) {
            String iconpath = IMService.iconMap.get(IMService.currentAccount);
            if ("text".equals(message.getType())) {
                Message mMessgae = getBaseSendMessage(MsgType.TEXT);

                if (iconpath != null) {
                    mMessgae.setAvatar(iconpath);
                }

                TextMsgBody textMsgBody = new TextMsgBody();
                textMsgBody.setMessage(message.getMsgbody());
                mMessgae.setSentTime(message.getSentTime());
                mMessgae.setBody(textMsgBody);
                mAdapter.addData(0, mMessgae);
                updateMsg(mMessgae);
            }
            if ("image".equals(message.getType())) {
                Message mMessgaeImage = getBaseSendMessage(MsgType.IMAGE);
                if (iconpath != null) {
                    mMessgaeImage.setAvatar(iconpath);
                }
//                mMessgaeImage.setSentTime(message.getSentTime());
                ImageMsgBody mImageMsgBody = new ImageMsgBody();
                mImageMsgBody.setThumbUrl(message.getFilepath());
                mImageMsgBody.setLocalPath(message.getFilepath());
                mMessgaeImage.setBody(mImageMsgBody);
                mAdapter.addData(0, mMessgaeImage);
                updateMsg(mMessgaeImage);
            }
            if ("file".equals(message.getType())) {
                Message mMessgaeFile = getBaseSendMessage(MsgType.FILE);
//                mMessgaeFile.setSentTime(message.getSentTime());
                if (iconpath != null) {
                    mMessgaeFile.setAvatar(iconpath);
                }
                FileMsgBody mFileMsgBody = new FileMsgBody();
                mFileMsgBody.setLocalPath(message.getFilepath());
                File file = new File(message.getFilepath());
                mFileMsgBody.setDisplayName(file.getName());
                mFileMsgBody.setSize(file.length());
                mMessgaeFile.setBody(mFileMsgBody);
                mAdapter.addData(0, mMessgaeFile);
                updateMsg(mMessgaeFile);
            }
            if ("voice".equals(message.getType())) {
                Message mMessgaeAudio = getBaseSendMessage(MsgType.AUDIO);
//                mMessgaeAudio.setSentTime(message.getSentTime());
                if (iconpath != null) {
                    mMessgaeAudio.setAvatar(iconpath);
                }
                AudioMsgBody audioMsgBody = new AudioMsgBody();
                audioMsgBody.setDuration(Long.parseLong(message.getVoicetime()));
                audioMsgBody.setLocalPath(message.getFilepath());
                mMessgaeAudio.setBody(audioMsgBody);
                mAdapter.addData(0, mMessgaeAudio);
                updateMsg(mMessgaeAudio);
            }

        } else {
            String iconpath = IMService.iconMap.get(toJid);

            if ("text".equals(message.getType())) {
                Message mMessgae = getBaseReceiveMessage(MsgType.TEXT);
                if (iconpath != null) {
                    mMessgae.setAvatar(iconpath);
                }
                mMessgae.setSentTime(message.getSentTime());
                TextMsgBody textMsgBody = new TextMsgBody();
                textMsgBody.setMessage(message.getMsgbody());
                mMessgae.setBody(textMsgBody);
                mAdapter.addData(0, mMessgae);
                updateMsg(mMessgae);
            }
            if ("image".equals(message.getType())) {
                Message mMessgaeImage = getBaseReceiveMessage(MsgType.IMAGE);
                if (iconpath != null) {
                    mMessgaeImage.setAvatar(iconpath);
                }
                ImageMsgBody mImageMsgBody = new ImageMsgBody();
                mImageMsgBody.setThumbUrl(message.getFilepath());
                mImageMsgBody.setLocalPath(message.getFilepath());
                mMessgaeImage.setBody(mImageMsgBody);
                mAdapter.addData(0, mMessgaeImage);
                updateMsg(mMessgaeImage);
            }
            if ("file".equals(message.getType())) {
                Message mMessgaeFile = getBaseReceiveMessage(MsgType.FILE);
                if (iconpath != null) {
                    mMessgaeFile.setAvatar(iconpath);
                }
                FileMsgBody mFileMsgBody = new FileMsgBody();
                mFileMsgBody.setLocalPath(message.getFilepath());
                File file = new File(message.getFilepath());
                mFileMsgBody.setDisplayName(file.getName());
                mFileMsgBody.setSize(file.length());
                mMessgaeFile.setBody(mFileMsgBody);
                mAdapter.addData(0, mMessgaeFile);
                updateMsg(mMessgaeFile);
            }
            if ("voice".equals(message.getType())) {
                Message mMessgaeAudio = getBaseReceiveMessage(MsgType.AUDIO);
                if (iconpath != null) {
                    mMessgaeAudio.setAvatar(iconpath);
                }

                AudioMsgBody audioMsgBody = new AudioMsgBody();
                audioMsgBody.setDuration(Long.parseLong(message.getVoicetime()));
                audioMsgBody.setLocalPath(message.getFilepath());
                mMessgaeAudio.setBody(audioMsgBody);
                mAdapter.addData(0, mMessgaeAudio);
                updateMsg(mMessgaeAudio);
            }


        }
    }

    private ImageView ivAudio;

    protected void initContent() {
        ButterKnife.bind(this);
        mAdapter = new ChatAdapter(this, new ArrayList<Message>());
        LinearLayoutManager mLinearLayout = new LinearLayoutManager(this);
        mRvChat.setLayoutManager(mLinearLayout);
        mRvChat.setAdapter(mAdapter);
        mSwipeRefresh.setOnRefreshListener(this);
        initChatUi();
        mAdapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
            @Override
            public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {

                final boolean isSend = mAdapter.getItem(position).getSenderId().equals(ChatActivity.mSenderId);
                if (ivAudio != null) {
                    if (isSend) {
                        ivAudio.setBackgroundResource(R.mipmap.audio_animation_list_right_3);
                    } else {
                        ivAudio.setBackgroundResource(R.mipmap.audio_animation_list_left_3);
                    }
                    ivAudio = null;
                    MediaManager.reset();
                } else {
                    ivAudio = view.findViewById(R.id.ivAudio);
                    MediaManager.reset();
                    if (isSend) {
                        ivAudio.setBackgroundResource(R.drawable.audio_animation_right_list);
                    } else {
                        ivAudio.setBackgroundResource(R.drawable.audio_animation_left_list);
                    }
                    AnimationDrawable drawable = (AnimationDrawable) ivAudio.getBackground();
                    drawable.start();
                    MediaManager.playSound(ChatActivity.this, ((AudioMsgBody) mAdapter.getData().get(position).getBody()).getLocalPath(), new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            if (isSend) {
                                ivAudio.setBackgroundResource(R.mipmap.audio_animation_list_right_3);
                            } else {
                                ivAudio.setBackgroundResource(R.mipmap.audio_animation_list_left_3);
                            }

                            MediaManager.release();
                        }
                    });
                }
            }
        });

    }


    @Override
    public void onRefresh() {

        System.out.println(historyMsg);
        //上拉显示历史信息
        if (historyMsg == null) {
            mSwipeRefresh.setRefreshing(false);
            mSwipeRefresh.setEnabled(false);
            return;
        }

        Collections.reverse(historyMsg);
        for (Message message : historyMsg) {
            showMessage(message, 0);
        }

        mSwipeRefresh.setRefreshing(false);
        mSwipeRefresh.setEnabled(false);

      /*  {
            //下拉刷新模拟获取历史消息
            List<Message> mReceiveMsgList = new ArrayList<Message>();
            //构建文本消息
            Message mMessgaeText = getBaseReceiveMessage(MsgType.TEXT);
            TextMsgBody mTextMsgBody = new TextMsgBody();
            mTextMsgBody.setMessage("收到的消息");
            mMessgaeText.setBody(mTextMsgBody);
            mReceiveMsgList.add(mMessgaeText);
            //构建图片消息
            Message mMessgaeImage = getBaseReceiveMessage(MsgType.IMAGE);
            ImageMsgBody mImageMsgBody = new ImageMsgBody();
            mImageMsgBody.setThumbUrl("https://c-ssl.duitang.com/uploads/item/201208/30/20120830173930_PBfJE.thumb.700_0.jpeg");
            mMessgaeImage.setBody(mImageMsgBody);
            mReceiveMsgList.add(mMessgaeImage);
            //构建文件消息
            Message mMessgaeFile = getBaseReceiveMessage(MsgType.FILE);
            FileMsgBody mFileMsgBody = new FileMsgBody();
            mFileMsgBody.setDisplayName("收到的文件");
            mFileMsgBody.setSize(12);
            mMessgaeFile.setBody(mFileMsgBody);
            mReceiveMsgList.add(mMessgaeFile);
            mAdapter.addData(0, mReceiveMsgList);
            mSwipeRefresh.setRefreshing(false);

        }*/


    }


    private void initChatUi() {
        //mBtnAudio
        final ChatUiHelper mUiHelper = ChatUiHelper.with(this);
        mUiHelper.bindContentLayout(mLlContent)
                .bindttToSendButton(mBtnSend)
                .bindEditText(mEtContent)
                .bindBottomLayout(mRlBottomLayout)
                .bindEmojiLayout(mLlEmotion)
                .bindAddLayout(mLlAdd)
                .bindToAddButton(mIvAdd)
                .bindToEmojiButton(mIvEmo)
                .bindAudioBtn(mBtnAudio)
                .bindAudioIv(mIvAudio)
                .bindTopLayout(mTop, nickname)
                .bindEmojiData();
        //底部布局弹出,聊天列表上滑
        mRvChat.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if (bottom < oldBottom) {
                    mRvChat.post(new Runnable() {
                        @Override
                        public void run() {
                            if (mAdapter.getItemCount() > 0) {
                                mRvChat.smoothScrollToPosition(mAdapter.getItemCount() - 1);
                            }
                        }
                    });
                }
            }
        });
        //点击空白区域关闭键盘
        mRvChat.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                mUiHelper.hideBottomLayout(false);
                mUiHelper.hideSoftInput();
                mEtContent.clearFocus();
                mIvEmo.setImageResource(R.mipmap.ic_emoji);
                return false;
            }
        });
        //
        ((RecordButton) mBtnAudio).setOnFinishedRecordListener(new RecordButton.OnFinishedRecordListener() {
            @Override
            public void onFinishedRecord(String audioPath, int time) {
                LogUtil.d("录音结束回调");
                File file = new File(audioPath);
                if (file.exists()) {
                    sendAudioMessage(audioPath, time);
                }
            }
        });

    }

    @OnClick({R.id.btn_send, R.id.rlPhoto, R.id.rlVideo, R.id.rlLocation, R.id.rlFile})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_send:
                sendTextMsg(mEtContent.getText().toString());
                mEtContent.setText("");
                break;
            case R.id.rlPhoto:
                PictureFileUtil.openGalleryPic(ChatActivity.this, REQUEST_CODE_IMAGE);
                break;
            case R.id.rlVideo:
                PictureFileUtil.openGalleryAudio(ChatActivity.this, REQUEST_CODE_VEDIO);
                break;
            case R.id.rlFile:
                PictureFileUtil.openFile(ChatActivity.this, REQUEST_CODE_FILE);
                break;
            case R.id.rlLocation:
                break;
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 100) {
            finish();
        }


        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_FILE:
                    String filePath = data.getStringExtra(FilePickerActivity.RESULT_FILE_PATH);
                    LogUtil.d("获取到的文件路径:" + filePath);
                    sendFileMessage(mSenderId, mTargetId, filePath);
                    break;
                case REQUEST_CODE_IMAGE:
                    // 图片选择结果回调
                    List<LocalMedia> selectListPic = PictureSelector.obtainMultipleResult(data);
                    for (LocalMedia media : selectListPic) {
                        LogUtil.d("获取图片路径成功:" + media.getPath());
                        sendImageMessage(media);
                    }
                    break;
                case REQUEST_CODE_VEDIO:
                    // 视频选择结果回调
                    List<LocalMedia> selectListVideo = PictureSelector.obtainMultipleResult(data);
                    for (LocalMedia media : selectListVideo) {
                        LogUtil.d("获取视频路径成功:" + media.getPath());
                        sendVedioMessage(media);
                    }
                    break;
            }
        }
    }


    //文本消息
    private void sendTextMsg(String hello) {
        org.jivesoftware.smack.packet.Message msg = new org.jivesoftware.smack.packet.Message();
        AddMessageType type = new AddMessageType();
        AddMessageDate date = new AddMessageDate();
        type.setTypeText("text");
        senddate.setTime(System.currentTimeMillis());
        date.setDateText(sdf.format(senddate));
        msg.setBody(hello);
        msg.addExtension(type);
        msg.addExtension(date);
        imservice.sendMsg(msg, toJid);
//        final Message mMessgae = getBaseSendMessage(MsgType.TEXT);
//        TextMsgBody mTextMsgBody = new TextMsgBody();
//        mTextMsgBody.setMessage(hello);
//        mMessgae.setBody(mTextMsgBody);
    }


    //图片消息
    private void sendImageMessage(final LocalMedia media) {
        org.jivesoftware.smack.packet.Message msg = new org.jivesoftware.smack.packet.Message();
        AddMessageType type = new AddMessageType();
        AddMessageDate date = new AddMessageDate();
        AddMessageFile file = new AddMessageFile();
        type.setTypeText("image");
        senddate.setTime(System.currentTimeMillis());
        date.setDateText(sdf.format(senddate));
        msg.addExtension(type);
        msg.addExtension(date);
        String data = base64Util.fileToBase64(new File(media.getCompressPath()));
        msg.setBody(data);
        //替换掉文件名中不可传输的字符串
        file.setFileText(media.getCompressPath().replace("&", ""));
        msg.addExtension(file);


        imservice.sendMsg(msg, toJid);
//        final Message mMessgae = getBaseSendMessage(MsgType.IMAGE);
//        ImageMsgBody mImageMsgBody = new ImageMsgBody();
//        mImageMsgBody.setThumbUrl(media.getCompressPath());
//        mMessgae.setBody(mImageMsgBody);
//        //开始发送
//        mAdapter.addData(mMessgae);
//        //模拟两秒后发送成功
//        updateMsg(mMessgae);
    }


    //视频消息
    private void sendVedioMessage(final LocalMedia media) {
        final Message mMessgae = getBaseSendMessage(MsgType.VIDEO);
        //生成缩略图路径
        String vedioPath = media.getPath();
        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        mediaMetadataRetriever.setDataSource(vedioPath);
        Bitmap bitmap = mediaMetadataRetriever.getFrameAtTime();
        String imgname = System.currentTimeMillis() + ".jpg";
        String urlpath = Environment.getExternalStorageDirectory() + "/" + imgname;
        File f = new File(urlpath);
        try {
            if (f.exists()) {
                f.delete();
            }
            FileOutputStream out = new FileOutputStream(f);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            LogUtil.d("视频缩略图路径获取失败：" + e.toString());
            e.printStackTrace();
        }
        VideoMsgBody mImageMsgBody = new VideoMsgBody();
        mImageMsgBody.setExtra(urlpath);
        mMessgae.setBody(mImageMsgBody);
        //开始发送
        mAdapter.addData(mMessgae);
        //模拟两秒后发送成功
        updateMsg(mMessgae);

    }

    //文件消息
    private void sendFileMessage(String from, String to, final String path) {
        org.jivesoftware.smack.packet.Message msg = new org.jivesoftware.smack.packet.Message();
        AddMessageType type = new AddMessageType();
        AddMessageDate date = new AddMessageDate();
        AddMessageFile file = new AddMessageFile();
        type.setTypeText("file");
        senddate.setTime(System.currentTimeMillis());
        date.setDateText(sdf.format(senddate));

        String data = base64Util.fileToBase64(new File(path));

        msg.setBody(data);
        file.setFileText(path.replace("&", ""));


        msg.addExtension(type);
        msg.addExtension(date);
        msg.addExtension(file);
        imservice.sendMsg(msg, toJid);

//        final Message mMessgae = getBaseSendMessage(MsgType.FILE);
//        FileMsgBody mFileMsgBody = new FileMsgBody();
//        mFileMsgBody.setLocalPath(path);
//        mFileMsgBody.setDisplayName(FileUtils.getFileName(path));
//        mFileMsgBody.setSize(FileUtils.getFileLength(path));
//        mMessgae.setBody(mFileMsgBody);
//        //开始发送
//        mAdapter.addData(mMessgae);
//        //模拟两秒后发送成功
//        updateMsg(mMessgae);

    }

    //语音消息
    private void sendAudioMessage(final String path, int time) {
        org.jivesoftware.smack.packet.Message msg = new org.jivesoftware.smack.packet.Message();
        AddMessageType type = new AddMessageType();
        AddMessageDate date = new AddMessageDate();
        AddMessageFile file = new AddMessageFile();
        AddMessageTime vctime = new AddMessageTime();
        type.setTypeText("voice");
        senddate.setTime(System.currentTimeMillis());
        date.setDateText(sdf.format(senddate));
        msg.addExtension(type);
        msg.addExtension(date);
        String data = base64Util.fileToBase64(new File(path));
        msg.setBody(data);
        file.setFileText(path);
        msg.addExtension(file);
        vctime.setTimeText(String.valueOf(time));
        msg.addExtension(vctime);
        imservice.sendMsg(msg, toJid);
//        final Message mMessgae = getBaseSendMessage(MsgType.AUDIO);
//        AudioMsgBody mFileMsgBody = new AudioMsgBody();
//        mFileMsgBody.setLocalPath(path);
//        mFileMsgBody.setDuration(time);
//        mMessgae.setBody(mFileMsgBody);
//        //开始发送
//        mAdapter.addData(mMessgae);
//        //模拟两秒后发送成功
//        updateMsg(mMessgae);
    }


    private Message getBaseSendMessage(MsgType msgType) {
        Message mMessgae = new Message();
        mMessgae.setUuid(UUID.randomUUID() + "");
        mMessgae.setSenderId(mSenderId);
        mMessgae.setTargetId(mTargetId);
        mMessgae.setSentTime(String.valueOf(System.currentTimeMillis()));
        mMessgae.setSentStatus(MsgSendStatus.SENDING);
        mMessgae.setMsgType(msgType);
        return mMessgae;
    }


    private Message getBaseReceiveMessage(MsgType msgType) {
        Message mMessgae = new Message();
        mMessgae.setUuid(UUID.randomUUID() + "");
        mMessgae.setSenderId(mTargetId);
        mMessgae.setTargetId(mSenderId);
//        mMessgae.setSentTime(String.valueOf(System.currentTimeMillis()));
        mMessgae.setSentStatus(MsgSendStatus.SENDING);
        mMessgae.setMsgType(msgType);
        return mMessgae;
    }


    private void updateMsg(final Message mMessgae) {
        mRvChat.scrollToPosition(mAdapter.getItemCount() - 1);
        //模拟2秒后发送成功
        new Handler().postDelayed(new Runnable() {
            public void run() {
                int position = 0;
                mMessgae.setSentStatus(MsgSendStatus.SENT);
                //更新单个子条目
                for (int i = 0; i < mAdapter.getData().size(); i++) {
                    Message mAdapterMessage = mAdapter.getData().get(i);
                    if (mMessgae.getUuid().equals(mAdapterMessage.getUuid())) {
                        position = i;
                    }
                }
                mAdapter.notifyItemChanged(position);
            }
        }, 2000);

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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }


}
