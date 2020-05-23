package xyz.joumboes.wechat.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import xyz.joumboes.wechat.R;
import xyz.joumboes.wechat.adapter.SessionAdapter;
import xyz.joumboes.wechat.bean.Contact;
import xyz.joumboes.wechat.bean.Message;
import xyz.joumboes.wechat.db.ContactRespository;
import xyz.joumboes.wechat.db.MessageRespository;
import xyz.joumboes.wechat.util.LogUtil;
import xyz.joumboes.wechat.util.ThreadUtils;

/**
 * A simple {@link Fragment} subclass.
 */
public class SessionFragment extends Fragment {
    private SessionAdapter adapter;
    private RecyclerView recyclerView;
    private static List<Message> sessionMsg = new ArrayList<>();

    private View rootView;
    private boolean flag = true;


    public SessionFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (null != rootView) {
            ViewGroup parent = (ViewGroup) rootView.getParent();
            if (null != parent) {
                parent.removeView(rootView);
            }

        } else {
            rootView = inflater.inflate(R.layout.fragment_session, null);
            LogUtil.d("2");
        }

        // Inflate the layout for this fragment
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        adapter = new SessionAdapter();
        adapter.setContext(getContext());
        recyclerView = getView().findViewById(R.id.session_RcView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
        initListener();
        LogUtil.d("session");
        flag = false;


    }


    private void initListener() {
        //监听数据库改变并展示
        new MessageRespository(getContext()).getallMessageLive().observe(getViewLifecycleOwner(), new Observer<List<Message>>() {
            @Override
            public void onChanged(List<Message> messages) {

                ThreadUtils.runInUIThread(new Runnable() {
                    @Override
                    public void run() {

                        if (messages == null || messages.size() == 0) {
                            return;
                        }
                        Message lastmsg = messages.get(messages.size() - 1);
                        for (Message message : sessionMsg) {
                            if (lastmsg.getSession().equals(message.getSession())) {
                                sessionMsg.remove(message);
                                break;
                            }
                        }
                        Contact contact = new ContactRespository(getActivity()).findByAccount(lastmsg.getSession());
                        if (contact == null) {
                            return;
                        }

                        sessionMsg.add(lastmsg);
                        //最新消息顶置
                        Collections.reverse(sessionMsg);


                        adapter.setSessionMsg(sessionMsg);
                        adapter.notifyDataSetChanged();

                    }
                });


            }
        });
        //左滑删除
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int adapterPosition = viewHolder.getAdapterPosition();
                sessionMsg.remove(adapterPosition);
                adapter.notifyDataSetChanged();
            }
        }).attachToRecyclerView(recyclerView);


    }
}
