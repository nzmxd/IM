package xyz.joumboes.wechat.fragment;

import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import xyz.joumboes.wechat.R;
import xyz.joumboes.wechat.adapter.ContactAdapter;
import xyz.joumboes.wechat.bean.Contact;
import xyz.joumboes.wechat.db.ContactRespository;

/**
 * A simple {@link Fragment} subclass.
 */
public class ContactFragment extends Fragment {
    ContactAdapter adapter;
    RecyclerView recyclerView;
    private View rootView;

    public ContactFragment() {
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
            rootView = inflater.inflate(R.layout.fragment_contact, null);
        }

        // Inflate the layout for this fragment
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        adapter = new ContactAdapter();
        adapter.setContext(getContext());
        recyclerView = getView().findViewById(R.id.contact_RcView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
//        registerForContextMenu(recyclerView);
        initListener();


    }

    private void initListener() {
        //监听数据库消息改变
        new ContactRespository(getContext()).getAllContactLive().observe(getViewLifecycleOwner(), new Observer<List<Contact>>() {
            @Override
            public void onChanged(List<Contact> contacts) {
                //设置并监听改变
                adapter.setContactList(contacts);
                adapter.notifyDataSetChanged();

            }
        });
    }



//    @Override
//    public void onCreateContextMenu(@NonNull ContextMenu menu, @NonNull View v, @Nullable ContextMenu.ContextMenuInfo menuInfo) {
//        super.onCreateContextMenu(menu, v, menuInfo);
//        MenuInflater inflater = getActivity().getMenuInflater();
//        inflater.inflate(R.menu.item_contact_menu, menu);
//    }
//
//    @Override
//    public boolean onContextItemSelected(@NonNull MenuItem item) {
//        switch (item.getItemId()) {
//            case R.id.item_session:
//                System.out.println(adapter.getmPosition());
//
//                return true;
//            case R.id.item_look:
//                return true;
//            case R.id.item_delete:
//                return true;
//            default:
//                return super.onContextItemSelected(item);
//        }
//    }


}
