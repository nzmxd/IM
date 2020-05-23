package xyz.joumboes.wechat.db;

import android.content.Context;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import java.util.List;

import xyz.joumboes.wechat.bean.Message;

public class MessageRespository {
    private LiveData<List<Message>> allMessageLive;
    private LiveData<List<Message>> allSessionMsg;
    private MessageDao messageDao;
    private String session;

    public MessageRespository(Context context) {
        this.allMessageLive = allMessageLive;
        messageDao= IMDatabase.getInstance(context.getApplicationContext()).getMessageDao();
        allMessageLive=messageDao.getAllMessageLive();
    }

    public MessageRespository(Context context,String session) {
        this.session = session;
        messageDao= IMDatabase.getInstance(context.getApplicationContext()).getMessageDao();
        allSessionMsg=messageDao.getSessionMsg(session);
    }
    public LiveData<List<Message>> getSessionMessageLive() {
        return allSessionMsg;
    }

    public LiveData<List<Message>> getallMessageLive() {
        return allMessageLive;
    }
    public Message getLastMessage() {
        List<Message> messages = allMessageLive.getValue();

        if (messages!=null&&messages.size()>0){
            return messages.get(messages.size() - 1);
        }
        return null;

    }


    public void insertMessages(Message ... Messages){
         new InsertAsyncTask(messageDao).execute(Messages);
    }
    public void updataMessages(Message ... Messages){
        new UpdataAsyncTask(messageDao).execute(Messages);
    }

    public void deleteMessages(Message ... Messages){
        new DeleteAsyncTask(messageDao).execute(Messages);
    }

    public void deleteAllMessages(){
        messageDao.deleteAllMessage();
    }

    static class InsertAsyncTask extends AsyncTask<Message,Void,Void>{
        private MessageDao messageDao;

        InsertAsyncTask(MessageDao MessageDao) {
            this.messageDao = MessageDao;
        }

        @Override
        protected Void doInBackground(Message... Messages) {
            messageDao.insertAll(Messages);
            return null;
        }
    }


    static class UpdataAsyncTask extends AsyncTask<Message,Void,Void>{
        private MessageDao messageDao;

        UpdataAsyncTask(MessageDao MessageDao) {
            this.messageDao = MessageDao;
        }

        @Override
        protected Void doInBackground(Message... Messages) {
            messageDao.updata(Messages);
            return null;
        }

    }


    static class DeleteAsyncTask extends AsyncTask<Message,Void,Void>{
        private MessageDao messageDao;
        DeleteAsyncTask(MessageDao messageDao) {
            this.messageDao = messageDao;
        }

        @Override
        protected Void doInBackground(Message... messages) {
            messageDao.delete(messages);
            return null;
        }
    }




}
