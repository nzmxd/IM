package xyz.joumboes.wechat.db;

import android.content.Context;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import java.util.List;

import xyz.joumboes.wechat.bean.Contact;
import xyz.joumboes.wechat.util.ThreadUtils;

public class ContactRespository {
    private LiveData<List<Contact>> allContactLive;
    private ContactDao contactDao;

    public ContactRespository(Context context) {
        this.allContactLive = allContactLive;
        contactDao = IMDatabase.getInstance(context.getApplicationContext()).getContactDao();
        allContactLive = contactDao.getAllContactLive();
    }

    public LiveData<List<Contact>> getAllContactLive() {
        return allContactLive;
    }


    public void insetOrUpdata(Contact contact) {
        ThreadUtils.runInThread(new Runnable() {
            @Override
            public void run() {
                //根据用户名查找 对应联系人
                List<Contact> daoContant = contactDao.findByAccount(contact.getAccount());
                if (daoContant.size() == 0) {
                    //没找到就插入联系人
                    contactDao.insertAll(contact);
                } else {
                    //找到了就更新联系人
                    contact.set_id(daoContant.get(0).get_id());
                    contactDao.updata(contact);
                }

            }
        });
    }

    public void insertContacts(Contact... contacts) {
        new InsertAsyncTask(contactDao).execute(contacts);
    }

    public void updataContacts(Contact... contacts) {
        new UpdataAsyncTask(contactDao).execute(contacts);
    }

    public void deleteContacts(Contact... contacts) {
        new DeleteAsyncTask(contactDao).execute(contacts);
    }

    public Contact findByAccount(String account) {
        List<Contact> contacts = contactDao.findByAccount(account);
        if (contacts.size() > 0){
            return contacts.get(0);
        }
        return null;
    }

    public void deleteAllContacts(){
        contactDao.deleteAllContact();
    }

    static class InsertAsyncTask extends AsyncTask<Contact, Void, Void> {
        private ContactDao contactDao;

        InsertAsyncTask(ContactDao contactDao) {
            this.contactDao = contactDao;
        }

        @Override
        protected Void doInBackground(Contact... contacts) {
            contactDao.insertAll(contacts);
            return null;
        }
    }


    static class UpdataAsyncTask extends AsyncTask<Contact, Void, Void> {
        private ContactDao contactDao;

        UpdataAsyncTask(ContactDao contactDao) {
            this.contactDao = contactDao;
        }

        @Override
        protected Void doInBackground(Contact... contacts) {
            contactDao.updata(contacts);
            return null;
        }

    }


    static class DeleteAsyncTask extends AsyncTask<Contact, Void, Void> {
        private ContactDao contactDao;

        DeleteAsyncTask(ContactDao contactDao) {
            this.contactDao = contactDao;
        }

        @Override
        protected Void doInBackground(Contact... contacts) {
            contactDao.delete(contacts);
            return null;
        }
    }

}
