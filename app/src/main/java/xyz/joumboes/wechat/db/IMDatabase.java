package xyz.joumboes.wechat.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;


import xyz.joumboes.wechat.bean.Contact;
import xyz.joumboes.wechat.bean.Message;

//单例模式
@Database(entities ={Contact.class, Message.class},version =1,exportSchema=false)
public  abstract class IMDatabase extends RoomDatabase {
    public static final String DB_NAME = "Im.db";
    private static volatile IMDatabase instance;

    public static synchronized IMDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(), IMDatabase.class, DB_NAME).allowMainThreadQueries().build();
        }
        return instance;
    }


    public abstract ContactDao getContactDao();
    public abstract MessageDao getMessageDao();

}
