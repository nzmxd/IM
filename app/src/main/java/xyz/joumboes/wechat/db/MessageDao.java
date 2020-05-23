package xyz.joumboes.wechat.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;
import xyz.joumboes.wechat.bean.Message;

@Dao
public interface MessageDao {
    @Query("SELECT * FROM t_sms")
    LiveData<List<Message>> getAllMessageLive();

    @Insert
    void insertAll(Message... messages);

    @Delete
    void delete(Message... messages);

    @Update
    void updata(Message... messages);

    @Query("select * from (select * from t_sms where from_account= :from_account or to_account=:to_account order by time asc) group by session_account")
    List<Message> findHistory(String from_account, String to_account);

    @Query("Delete  from t_sms")
    void deleteAllMessage();

    @Query("select * from  t_sms WHERE session_account= :to_account")
    LiveData<List<Message>> getSessionMsg(String to_account);
}
