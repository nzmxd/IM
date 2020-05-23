package xyz.joumboes.wechat.bean;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "t_sms")
public class Message {
    @Ignore
    private String uuid;
    @Ignore
    private String msgId;
    @Ignore
    private MsgType msgType;
    @Ignore
    private MsgBody body;
    @Ignore
    private MsgSendStatus sentStatus;
    @Ignore
    private TextMsgBody textMsgBody;
    @Ignore
    private String avatar;


    @PrimaryKey(autoGenerate = true)
    private Integer _id;
    @ColumnInfo(name = "from_account")
    private String senderId;
    @ColumnInfo(name = "to_account")
    private String targetId;
    @ColumnInfo(name = "msgbody")
    private String msgbody;
    @ColumnInfo(name = "type")
    private String type;
    @ColumnInfo(name = "time")
    private String sentTime;

    @ColumnInfo(name = "filename")
    private String filepath;

    @ColumnInfo(name = "voicetime")
    private String voicetime;

    @ColumnInfo(name = "session_account")
    private String session;


    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    public MsgType getMsgType() {
        return msgType;
    }

    public void setMsgType(MsgType msgType) {
        this.msgType = msgType;
    }

    public MsgBody getBody() {
        return body;
    }

    public void setBody(MsgBody body) {
        this.body = body;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getTargetId() {
        return targetId;
    }

    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }

    public MsgSendStatus getSentStatus() {
        return sentStatus;
    }

    public void setSentStatus(MsgSendStatus sentStatus) {
        this.sentStatus = sentStatus;
    }

    public String getSentTime() {
        return sentTime;
    }

    public void setSentTime(String sentTime) {
        this.sentTime = sentTime;
    }

    public Integer get_id() {
        return _id;
    }

    public void set_id(Integer _id) {
        this._id = _id;
    }

    public String getMsgbody() {
        return msgbody;
    }

    public void setMsgbody(String msgbody) {
        this.msgbody = msgbody;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSession() {
        return session;
    }

    public void setSession(String session) {
        this.session = session;
    }

    public String getFilepath() {
        return filepath;
    }

    public void setFilepath(String filepath) {
        this.filepath = filepath;
    }

    public String getVoicetime() {
        return voicetime;
    }

    public void setVoicetime(String voicetime) {
        this.voicetime = voicetime;
    }

    public TextMsgBody getTextMsgBody() {
        return textMsgBody;
    }

    public void setTextMsgBody(TextMsgBody textMsgBody) {
        this.textMsgBody = textMsgBody;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public Message() {
    }

    @Override
    public String toString() {
        return "Message{" +
                "uuid='" + uuid + '\'' +
                ", msgId='" + msgId + '\'' +
                ", msgType=" + msgType +
                ", body=" + body +
                ", sentStatus=" + sentStatus +
                ", textMsgBody=" + textMsgBody +
                ", _id=" + _id +
                ", senderId='" + senderId + '\'' +
                ", targetId='" + targetId + '\'' +
                ", msgbody='" + msgbody + '\'' +
                ", type='" + type + '\'' +
                ", sentTime='" + sentTime + '\'' +
                ", filepath='" + filepath + '\'' +
                ", voicetime='" + voicetime + '\'' +
                ", session='" + session + '\'' +
                '}';
    }
}
