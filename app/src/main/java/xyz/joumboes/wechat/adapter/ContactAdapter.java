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
import xyz.joumboes.wechat.bean.Contact;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.MyViewHolder> {
    private List<Contact> contactList = new ArrayList<>();
    private Context context;


    public void setContext(Context context) {
        this.context = context;
    }

    public void setContactList(List<Contact> contactList) {
        this.contactList = contactList;
    }


    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View itemview = layoutInflater.inflate(R.layout.item_contact, parent, false);

        return new MyViewHolder(itemview);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Contact contact = contactList.get(position);
        holder.nickname.setText(contact.getNickname());
        holder.sig.setText(contact.getSignature());
        //设置头像
        if (contact.getAvatar() != null) {
            Bitmap bmp = BitmapFactory.decodeFile(contact.getAvatar());
            holder.icon.setImageBitmap(bmp);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ChatActivity.class);
                intent.putExtra("accountid", contactList.get(position).getAccount());
                intent.putExtra("nickname", contactList.get(position).getNickname());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return contactList.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView nickname, sig;

        MyViewHolder(@NonNull View itemView) {
            super(itemView);
            nickname = itemView.findViewById(R.id.contact_nicknameTv);
            sig = itemView.findViewById(R.id.contact_sigTv);
            icon = itemView.findViewById(R.id.contact_imageVw);
        }
    }
}
