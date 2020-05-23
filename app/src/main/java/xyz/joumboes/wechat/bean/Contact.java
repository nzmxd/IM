package xyz.joumboes.wechat.bean;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity
public class Contact {
    @PrimaryKey(autoGenerate = true)
    public int _id;

    @ColumnInfo(name = "account")
    public String account;

    @ColumnInfo(name = "nickname")
    public String nickname;

    @ColumnInfo(name = "sex")
    public String sex;

    @ColumnInfo(name = "address")
    public String address;

    @ColumnInfo(name = "email")
    public String email;

    @ColumnInfo(name = "phone")
    public String phone;

    @ColumnInfo(name = "signature")
    public String signature;

    @ColumnInfo(name = "avatar")
    public String avatar;

    @Ignore
    public String pinyin;

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getPinyin() {
        return pinyin;
    }

    public void setPinyin(String pinyin) {
        this.pinyin = pinyin;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    @Ignore
    public Contact(int _id, String account, String nickname, String avatar, String pinyin) {
        this._id = _id;
        this.account = account;
        this.nickname = nickname;
        this.avatar = avatar;
        this.pinyin = pinyin;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Contact() {
    }

    @Override
    public String toString() {
        return "Contact{" +
                "_id=" + _id +
                ", account='" + account + '\'' +
                ", nickname='" + nickname + '\'' +
                ", avatar='" + avatar + '\'' +
                ", pinyin='" + pinyin + '\'' +
                '}';
    }

}
