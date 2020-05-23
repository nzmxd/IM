package xyz.joumboes.wechat.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import xyz.joumboes.wechat.R;
import xyz.joumboes.wechat.activity.ChatActivity;
import xyz.joumboes.wechat.activity.MainActivity;
import xyz.joumboes.wechat.bean.Contact;
import xyz.joumboes.wechat.bean.Message;
import xyz.joumboes.wechat.db.ContactRespository;
import xyz.joumboes.wechat.util.LogUtil;

import static xyz.joumboes.wechat.activity.MainActivity.msgnums;
import static xyz.joumboes.wechat.activity.MainActivity.msg_id;

public class SessionAdapter extends RecyclerView.Adapter<SessionAdapter.MyViewHolder> {

    private Context context;
    List<Message> messageList = new ArrayList<>();




    public void setContext(Context context) {
        this.context = context;
    }

    public void setSessionMsg(List<Message> sessionMsg) {
        this.messageList = sessionMsg;
    }


    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View itemview = layoutInflater.inflate(R.layout.item_session, parent, false);
        return new MyViewHolder(itemview);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Message message = messageList.get(position);
        String account = message.getSession();
        LogUtil.d(message.getSession());
        Contact contact = new ContactRespository(context).findByAccount(account);


        if (contact != null) {
            String type = message.getType();
            String msgbody = message.getMsgbody();
            String nickname = contact.getNickname();
            //设置昵称
            holder.nickname.setText(nickname);
            //设置头像
            if (contact.getAvatar()!=null){
                Bitmap bmp = BitmapFactory.decodeFile(contact.getAvatar());
                holder.icon.setImageBitmap(bmp);
            }else {
                holder.icon.setImageResource(R.mipmap.ic_contact);
            }
            //当前用户收到消息
            if (message.getSenderId().equals(account)) {
                if ("text".equals(type)) {
                    holder.msg.setText(nickname + ":" + msgbody);
                } else if ("image".equals(type)) {
                    holder.msg.setText(nickname + ":[图片]");

                } else if ("voice".equals(type)) {
                    holder.msg.setText(nickname + ":[语音]");

                } else {
                    holder.msg.setText(nickname + ":[文件]");
                }
                //设置未读消息条数

                if (messageList.get(0).getSession().equals(account)&&!msgnums.containsKey(account)){
                    msgnums.put(account,0);
                    msg_id.put(account,message.get_id());
                }

                if(messageList.get(0).getSession().equals(account)&&msgnums.containsKey(account)&&msg_id.containsKey(account)){
                    if (msg_id.get(account).intValue()==messageList.get(0).get_id().intValue()){
                        msgnums.put(account, msgnums.get(account));
                    }else {
                        msgnums.put(account, msgnums.get(account) + 1);
                        msg_id.put(account,message.get_id());
                    }
                }

                holder.nums.setText(String.valueOf(msgnums.get(account)));
                holder.time.setText(message.getSentTime());

            } else {
                if ("text".equals(type)) {
                    holder.msg.setText(msgbody);
                } else if ("image".equals(type)) {
                    holder.msg.setText("[图片]");

                } else if ("voice".equals(type)) {
                    holder.msg.setText("[语音]");

                } else {
                    holder.msg.setText("[文件]");
                }
                holder.time.setText(message.getSentTime());
                holder.nums.setText("");
            }




        }
        //点击事件
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(context, ChatActivity.class);
                intent.putExtra("accountid",contact.getAccount());
                intent.putExtra("nickname",contact.getNickname());
                //清零未读信息
                msgnums.put(account,0);
                notifyDataSetChanged();
                context.startActivity(intent);

            }
        });


    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView nickname, time, msg, nums;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.session_icon);
            nickname = itemView.findViewById(R.id.session_nameTv);
            time = itemView.findViewById(R.id.session_timeTv);
            msg = itemView.findViewById(R.id.session_msgTv);
            nums = itemView.findViewById(R.id.session_numsTv);
        }
    }
}
