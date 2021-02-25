package seu.qz.qzapp.fragment;

import androidx.lifecycle.ViewModelProviders;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;

import io.agora.rtm.ErrorInfo;
import io.agora.rtm.LocalInvitation;
import io.agora.rtm.RemoteInvitation;
import io.agora.rtm.ResultCallback;
import io.agora.rtm.RtmCallEventListener;
import io.agora.rtm.RtmCallManager;
import io.agora.rtm.RtmClient;
import seu.qz.qzapp.R;
import seu.qz.qzapp.activity.AgoraChatActivity;
import seu.qz.qzapp.activity.BaseApplication;
import seu.qz.qzapp.activity.MainActivity;
import seu.qz.qzapp.agora.ChatManager;
import seu.qz.qzapp.agora.RtmMessagePool;
import seu.qz.qzapp.entity.BriefChatItem;
import seu.qz.qzapp.fragment.viewmodel.BaseViewModel;
import seu.qz.qzapp.fragment.viewmodel.ChatViewModel;
import seu.qz.qzapp.activity.viewmodel.MainViewModel;
import seu.qz.qzapp.main.ChatAdapter;
import seu.qz.qzapp.main.CustomizeRecyclerScrollListener;
import seu.qz.qzapp.main.OrderAdapter;
import seu.qz.qzapp.utils.CommonUtils;
import seu.qz.qzapp.utils.MessageUtil;
import seu.qz.qzapp.utils.PropertyUtil;

public class ChatFragment extends Fragment {

    //数据viewmodel
    MainViewModel mainViewModel;
    ChatViewModel chatViewModel;

    //UI控件
    SwipeRefreshLayout swipeRefreshLayout;
    RecyclerView recyclerView;
    ChatAdapter adapter;

    //Agora相关
    private RtmClient mRtmClient;
    ChatManager mChatManager;
    RtmCallManager callManager;
    //是否登录成功

    //临时控件，测试用
    Button add_chats;
    private static final String TAG = "MainActivity";

    public static ChatFragment newInstance() {
        return new ChatFragment();
    }

    public ChatFragment() {
    }

    public ChatViewModel getViewModel(){
        return chatViewModel;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainViewModel = ViewModelProviders.of(getActivity()).get(MainViewModel.class);
        chatViewModel = ViewModelProviders.of(this).get(ChatViewModel.class);

    }

    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);
        swipeRefreshLayout = view.findViewById(R.id.chat_swipe_fresh);
        recyclerView = view.findViewById(R.id.chat_recycle);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        adapter = new ChatAdapter(MessageUtil.getMessageListBeanList());
        Log.d(TAG, "current MessageUtil list size:" + MessageUtil.getMessageListBeanList().size() );
        if(MessageUtil.getMessageListBeanList().size() > 0){
            Log.d(TAG, "current chatItem:" + MessageUtil.getMessageListBeanList().get(0).getChatItem());
        }
        ChatManager manager = BaseApplication.the().getChatManager();
        manager.setAdapter(adapter);
        Log.d(TAG, "ChatFragment set adapter:" + adapter);
        RtmMessagePool messagePool = manager.getmMessagePool();
        messagePool.setAdapter(adapter);
        //chatViewModel.initBriefItems(mainViewModel.getMainCustomer().getUser_id().toString(), adapter, getActivity(), swipeRefreshLayout);
        adapter.setListener( new ChatAdapter.OnItemClickListener() {
            @Override
            public void onClick(View view, BriefChatItem item, int Item_order) {
                MainActivity activity = (MainActivity) getActivity();
                Intent intent = new Intent(activity, AgoraChatActivity.class);
                //将聊天缓存文件列表重新排序，置顶当前要聊天的对象文件，再进入聊天界面
                item.setNewsCount(0);
//                changeOrderByClick(String.valueOf(Item_order), getActivity());
                adapter.notifyDataSetChanged();
                intent.putExtra("username", mainViewModel.getMainCustomer().getUser_nickName());
                intent.putExtra("peerId", String.valueOf(item.getUser_id()));
                //      intent.putExtra("briefOrderItem", item);
                startActivityForResult(intent, 1);
                //开启活动内容界面，考虑到可能会有返回值
                //根据返回值做出相应操作！！！！！！！！！！！！！！！！！！！！！！！
            }
        });
        recyclerView.setAdapter(adapter);
        recyclerView.addOnScrollListener(new CustomizeRecyclerScrollListener() {
            @Override
            protected void onLoadData() {
                //滑到低后加载数据的操作，此处暂无！！！！！！！！！！！！！！！！！！！！！！
            }
        });
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //chatViewModel.refresh(mainViewModel.getMainCustomer().getUser_nickName(), adapter, getActivity(), swipeRefreshLayout);
                adapter.notifyDataSetChanged();
            }
        });



        //测试用UI
//        add_chats = view.findViewById(R.id.add_chats);
//        add_chats.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                String directory =getActivity().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).getPath();
//                File file = new File(directory +"/" + PropertyUtil.getPropertyByKey("chatCaches", getActivity()) + "/" + mainViewModel.getMainCustomer().getUser_id());
//                String[] list = file.list();
//                int i = 0;
//                if(list != null && list.length != 0){
//                    i = list.length;
//                }
//                File newFile = new File(file.toPath() + "/" + String.valueOf(i + 1) + "-"
//                        + mainViewModel.getMainCustomer().getUser_id().toString() + "_" + String.valueOf(i + 1) +
//                        "_小华" + "_" + Boolean.toString(i%2 == 0) + ".txt");
//                try {
//                    newFile.createNewFile();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                chatViewModel.refresh(mainViewModel.getMainCustomer().getUser_id().toString(), adapter, getActivity(), swipeRefreshLayout);
//            }
//        });



        return view;
    }

    //由于点击进入聊天窗口而重新规划聊天界面的排序
    private void changeOrderByClick(String order, Context context) {
//        String combined_Id = order + ";" + mainViewModel.getMainCustomer().getUser_id().toString();
//        chatViewModel.changeChatsOrder(combined_Id, 2, context);
//        chatViewModel.refresh(mainViewModel.getMainCustomer().getUser_id().toString(), adapter, context, swipeRefreshLayout);

        adapter.notifyDataSetChanged();
    }



    //有新消息发来而导致的聊天顺序改变
    public void changeChatsOrderByNewMessage(String name_or_id, Context context) {
        chatViewModel.changeChatsOrder(name_or_id, 1, context);
        chatViewModel.refresh(mainViewModel.getMainCustomer().getUser_id().toString(), adapter, context, swipeRefreshLayout);
        adapter.notifyDataSetChanged();
    }







    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // TODO: Use the ViewModel


    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        adapter = null;
        Log.i("MainActivity", "chat_onDestroyView:启动!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! ");
        ChatManager manager = BaseApplication.the().getChatManager();
        manager.setAdapter(adapter);
        RtmMessagePool messagePool = manager.getmMessagePool();
        messagePool.setAdapter(adapter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("MainActivity", "chat_onDestroy:启动！！！！！！！！！！！！！！！！！！！！！！！！！！ ");
    }

    public ChatAdapter getAdapter() {
        return adapter;
    }

    public void setAdapter(ChatAdapter adapter) {
        this.adapter = adapter;
    }
}