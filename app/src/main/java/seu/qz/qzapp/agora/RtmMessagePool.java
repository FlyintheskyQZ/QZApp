package seu.qz.qzapp.agora;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ashokvarma.bottomnavigation.TextBadgeItem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import io.agora.rtm.RtmMessage;
import seu.qz.qzapp.R;
import seu.qz.qzapp.entity.BriefChatItem;
import seu.qz.qzapp.main.ChatAdapter;
import seu.qz.qzapp.utils.MessageUtil;

/**
 * Receives and manages messages from RTM engine.
 * 接收和管理来自RTM引擎的消息，RTM消息缓存池，维护来自多个对象传来的线下信息池
 */
public class RtmMessagePool {
    private Map<String, List<RtmMessage>> mOfflineMessageMap = new HashMap<>();
    ChatAdapter adapter;
    TextBadgeItem chat_tobeRead;
    private static final String TAG = "MainActivity";
    //插入线下信息
    public void insertOfflineMessage(RtmMessage rtmMessage, String peerId, Activity activity) {

        Log.d(TAG, "insertOfflineMessage:" + peerId + ";" + rtmMessage.getText());
        //判断缓存池中是否包含指定对象peerId的缓存池
        boolean contains = mOfflineMessageMap.containsKey(peerId);
        //如果有，则直接将参数中的RTM消息放入对应池中
        List<RtmMessage> list = contains ? mOfflineMessageMap.get(peerId) : new ArrayList<RtmMessage>();
            if (list != null) {
            list.add(rtmMessage);
        }
        //如果没有，则创建新的信息池加入到总池中进行维护
        if (!contains) {
            mOfflineMessageMap.put(peerId, list);
        }
        //同时判断MessageUtil维护的缓存池中是否有对应peerId的MessageListBean，如果有没有也创建一个，方便后面写入到缓存文件中
        MessageListBean new_beanList = MessageUtil.getExistMessageListBean(peerId);
        String message_text = rtmMessage.getText();
        int useinf_endIndex = message_text.indexOf(":");
        String user_inf = message_text.substring(0, useinf_endIndex);
        String valid_text = message_text.substring(useinf_endIndex + 1);
        rtmMessage.setText(valid_text);
        if(new_beanList == null){
            Log.d(TAG, "insertOfflineMessage:new Peer send Message!");
            String[] informs = user_inf.split("-");
            new_beanList = new MessageListBean(peerId, new ArrayList<MessageBean>());
            BriefChatItem chatItem = new BriefChatItem(Integer.parseInt(peerId), informs[0], Boolean.parseBoolean(informs[1]), 0);
            new_beanList.setChatItem(chatItem);
        }
        BriefChatItem item = new_beanList.getChatItem();
        item.setNewsCount(item.getNewsCount() + 1);
        MessageUtil.addMessageListBeanList(new_beanList);
        Log.d(TAG, "new ListBean is:" +MessageUtil.getMessageListBeanList().size() + ":" + MessageUtil.getMessageListBeanList().get(0).getAccountOther());
        if(adapter != null){
            Log.d(TAG, "adapter notify!");
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    adapter.notifyDataSetChanged();
                    if(chat_tobeRead != null){
                        int num = numOfAllMessage();
                        if(num == 0){
                            chat_tobeRead.setBackgroundColorResource(R.color.barActiveColor).setText(" ");
                        }else {
                            chat_tobeRead.setBackgroundColorResource(R.color.badgeItem_bg).setText(String.valueOf(num));
                        }
                    }
                }
            });
        }
    }

    public void addRtmMessageList(String peerId, List<RtmMessage> new_list){
        boolean contains = mOfflineMessageMap.containsKey(peerId);
        if(contains){
            List<RtmMessage> list = mOfflineMessageMap.get(peerId);
            list.addAll(new_list);
        }else {
            mOfflineMessageMap.put(peerId, new_list);
        }
    }

    public int numOfAllMessage(){
        int num = 0;
        for (List<RtmMessage> value : mOfflineMessageMap.values()) {
            num += value.size();
        }
        return num;
    }

    public TextBadgeItem getChat_tobeRead() {
        return chat_tobeRead;
    }

    public void setChat_tobeRead(TextBadgeItem chat_tobeRead) {
        this.chat_tobeRead = chat_tobeRead;
    }

    //返回指定peerId对应的信息缓存池，如果没有该用户，则返回新的List（为空）
    List<RtmMessage> getAllOfflineMessages(String peerId) {
        return mOfflineMessageMap.containsKey(peerId) ?
                mOfflineMessageMap.get(peerId) : new ArrayList<RtmMessage>();
    }
    //从总池中移除指定peerId对应的池子，不再维护
    void removeAllOfflineMessages(String peerId) {
        mOfflineMessageMap.remove(peerId);
    }

    public Map<String, List<RtmMessage>> getmOfflineMessageMap() {
        return mOfflineMessageMap;
    }

    public ChatAdapter getAdapter() {
        return adapter;
    }

    public void setAdapter(ChatAdapter adapter) {
        this.adapter = adapter;
    }

}

