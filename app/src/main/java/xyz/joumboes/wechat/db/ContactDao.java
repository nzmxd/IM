package xyz.joumboes.wechat.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;


import xyz.joumboes.wechat.bean.Contact;

@Dao
public interface ContactDao {

    @Query("SELECT * FROM contact")
    LiveData<List<Contact>> getAllContactLive();

    @Insert
    void insertAll(Contact... contacts);

    @Delete
    void delete(Contact... contacts);

    @Update
    int updata(Contact... contact);

    @Query("SELECT * FROM contact WHERE account=:account")
    List<Contact> findByAccount(String account);

    @Query("Delete  from contact")
    void deleteAllContact();

}
