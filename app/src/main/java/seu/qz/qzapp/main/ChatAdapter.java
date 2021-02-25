package seu.qz.qzapp.main;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import seu.qz.qzapp.R;
import seu.qz.qzapp.agora.MessageListBean;
import seu.qz.qzapp.entity.BriefChatItem;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private OnItemClickListener listener;
    //Adapter的数据源，由chat缓存文件名转化来
    private List<MessageListBean> chatsList;
    private static final String TAG = "MainActivity";
    public ChatAdapter(List<MessageListBean> chatsList, OnItemClickListener listener) {
        this.chatsList = chatsList;
        this.listener = listener;
    }
    public ChatAdapter(List<MessageListBean> chatsList) {
        this.chatsList = chatsList;
    }

    static class ChatViewHolder extends RecyclerView.ViewHolder{

        ImageView item_chat_customer;
        TextView item_chat_name;
        TextView item_chat_extra;
        TextView item_chat_newsCount;
        View item_chat_bottomLine;
        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            item_chat_customer = itemView.findViewById(R.id.item_chat_customer);
            item_chat_name = itemView.findViewById(R.id.item_chat_name);
            item_chat_extra = itemView.findViewById(R.id.item_chat_extra);
            item_chat_newsCount = itemView.findViewById(R.id.item_chat_newsCount);
            item_chat_bottomLine = itemView.findViewById(R.id.item_chat_bottomLine);
        }
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        ChatViewHolder chatHolder = (ChatViewHolder) holder;
        final BriefChatItem briefChatItem = chatsList.get(getItemCount() - 1 - position).getChatItem();
        Log.d(TAG, "onBindViewHolder;position is:" + position + ";" + briefChatItem.getUser_id() + ";" + briefChatItem.getUsername() + ";" + briefChatItem.getNewsCount());
        if(briefChatItem.isMale()){
            chatHolder.item_chat_customer.setImageResource(R.mipmap.ic_chat_item_boy);
        }else {
            chatHolder.item_chat_customer.setImageResource(R.mipmap.ic_chat_item_girl);
        }
        chatHolder.item_chat_name.setText(briefChatItem.getUsername());
        if(briefChatItem.getNewsCount() > 0){
            chatHolder.item_chat_newsCount.setVisibility(View.VISIBLE);
            chatHolder.item_chat_newsCount.setText(String.valueOf(briefChatItem.getNewsCount()));
        }else {
            chatHolder.item_chat_newsCount.setVisibility(View.GONE);
        }
        chatHolder.item_chat_extra.setText("用户ID: " + briefChatItem.getUser_id() + "号");
        if(position == chatsList.size() - 1){
            chatHolder.item_chat_bottomLine.setVisibility(View.GONE);
        }else {
            chatHolder.item_chat_bottomLine.setVisibility(View.VISIBLE);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(listener != null){
                    listener.onClick(v, briefChatItem, getItemCount() - position);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        if(chatsList == null){
            return 0;
        }
        return chatsList.size();
    }



    public interface OnItemClickListener{
        void onClick(View view, BriefChatItem item, int item_order);
    }

    public List<MessageListBean> getChatsList() {
        return chatsList;
    }

    public void setChatsList(List<MessageListBean> chatsList) {
        this.chatsList = chatsList;
    }

    public OnItemClickListener getListener() {
        return listener;
    }

    public void setListener(OnItemClickListener listener) {
        this.listener = listener;
    }
}
