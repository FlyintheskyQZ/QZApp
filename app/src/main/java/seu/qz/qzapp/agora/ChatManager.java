package seu.qz.qzapp.agora;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.agora.rtm.RtmClient;
import io.agora.rtm.RtmClientListener;
import io.agora.rtm.RtmFileMessage;
import io.agora.rtm.RtmImageMessage;
import io.agora.rtm.RtmMediaOperationProgress;
import io.agora.rtm.RtmMessage;
import io.agora.rtm.SendMessageOptions;
import seu.qz.qzapp.BuildConfig;
import seu.qz.qzapp.R;
import seu.qz.qzapp.main.ChatAdapter;

/**
 * 聊条管理器：
 *      1.创建RTM客户端实例，并初始化（注册全局监听器-用于调用基层监听器的对应方法）
 *      2.维护消息缓存池，一个聊天对象对应一个小消息池
 *      3.线下消息（暂不支持）
 */
public class ChatManager {
    private static final String TAG = ChatManager.class.getSimpleName();

    //环境上下文
    private Context mContext;
    //RTM客户端实例，所有的实时消息都是基于此建立的，面向Agora系统的客户端实例
    private RtmClient mRtmClient;
    //信息发送选项，关系到线下信息和信息缓存，只读，目前来看不支持线下消息模式
    private SendMessageOptions mSendMsgOptions;
    //RTM客户端监听器的list
    private List<RtmClientListener> mListenerList = new ArrayList<>();
    //实时消息的缓存池，维护有多个聊天对象的小池子
    private RtmMessagePool mMessagePool = new RtmMessagePool();
    //用于更新ChatFragment的界面
    private ChatAdapter adapter;
    //配置执行UI界面
    private Activity activity;
    //是否登录成功
    boolean loginSuccess = false;

    public ChatManager(Context context) {
        mContext = context;
    }

    public void init() {
        //获取AppId
        String appID = mContext.getString(R.string.agora_app_id);

        try {
            //创建新实时消息客户端实例，并注册监听器
            mRtmClient = RtmClient.createInstance(mContext, appID, new RtmClientListener() {
                //连接状态监听器，当链接状态发生变化时，调用所有的mListenerList维护的监听器的onConnectionStateChanged（）方法
                @Override
                public void onConnectionStateChanged(int state, int reason) {
                    for (RtmClientListener listener : mListenerList) {
                        listener.onConnectionStateChanged(state, reason);
                    }
                }
                //实时消息接收监听器
                @Override
                public void onMessageReceived(RtmMessage rtmMessage, String peerId) {
                    //如果没有监听器处理该消息（没有相应的回调调用）,该消息会被存储进缓存池中
                    if (mListenerList.isEmpty()) {
                        // If currently there is no callback to handle this
                        // message, this message is unread yet. Here we also
                        // take it as an offline message.
                        mMessagePool.insertOfflineMessage(rtmMessage, peerId, activity);
                        //如果有监听器，则调用每个监听器的onMessageReceived（）方法
                    } else {
                        for (RtmClientListener listener : mListenerList) {
                            listener.onMessageReceived(rtmMessage, peerId);
                        }
                    }
                }
                //实时图片消息接收监听器
                @Override
                public void onImageMessageReceivedFromPeer(final RtmImageMessage rtmImageMessage, final String peerId) {
                    //如果没有监听器处理该消息（没有相应的回调调用）,该图片消息会被存储进缓存池中
                    if (mListenerList.isEmpty()) {
                        // If currently there is no callback to handle this
                        // message, this message is unread yet. Here we also
                        // take it as an offline message.
                        mMessagePool.insertOfflineMessage(rtmImageMessage, peerId, activity);
                        //如果有监听器，则调用每个监听器的onImageMessageReceivedFromPeer（）方法
                    } else {
                        for (RtmClientListener listener : mListenerList) {
                            listener.onImageMessageReceivedFromPeer(rtmImageMessage, peerId);
                        }
                    }
                }

                //文件实时消息接收监听器
                @Override
                public void onFileMessageReceivedFromPeer(RtmFileMessage rtmFileMessage, String s) {

                }
                //多媒体上传过程
                @Override
                public void onMediaUploadingProgress(RtmMediaOperationProgress rtmMediaOperationProgress, long l) {

                }
                //多媒体文件下载过程
                @Override
                public void onMediaDownloadingProgress(RtmMediaOperationProgress rtmMediaOperationProgress, long l) {

                }
                //Token过期监听器
                @Override
                public void onTokenExpired() {

                }
                //群聊成员在线状态变化监听器
                @Override
                public void onPeersOnlineStatusChanged(Map<String, Integer> status) {

                }
            });

//            if (BuildConfig.DEBUG) {
//                mRtmClient.setParameters("{\"rtm.log_filter\": 65535}");
//            }
        } catch (Exception e) {
//            Log.e(TAG, Log.getStackTraceString(e));
//            throw new RuntimeException("NEED TO check rtm sdk init fatal error\n" + Log.getStackTraceString(e));
        }
        //全局选项，主要用于决定是否支持线下信息（目前不支持）
        // Global option, mainly used to determine whether
        // to support offline messages now.
        mSendMsgOptions = new SendMessageOptions();
    }

    public RtmClient getRtmClient() {
        return mRtmClient;
    }

    public void registerListener(RtmClientListener listener) {
        mListenerList.add(listener);
    }

    public void unregisterListener(RtmClientListener listener) {
        mListenerList.remove(listener);
    }

    public void enableOfflineMessage(boolean enabled) {
        mSendMsgOptions.enableOfflineMessaging = enabled;
    }

    public boolean isOfflineMessageEnabled() {
        return mSendMsgOptions.enableOfflineMessaging;
    }

    public SendMessageOptions getSendMessageOptions() {
        return mSendMsgOptions;
    }

    public List<RtmMessage> getAllOfflineMessages(String peerId) {
        return mMessagePool.getAllOfflineMessages(peerId);
    }

    public void removeAllOfflineMessages(String peerId) {
        mMessagePool.removeAllOfflineMessages(peerId);
    }

    public RtmMessagePool getmMessagePool() {
        return mMessagePool;
    }

    public void setmMessagePool(RtmMessagePool mMessagePool) {
        this.mMessagePool = mMessagePool;
    }

    public ChatAdapter getAdapter() {
        return adapter;
    }

    public void setAdapter(ChatAdapter adapter) {
        this.adapter = adapter;
    }

    public Activity getActivity() {
        return activity;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    public boolean isLoginSuccess() {
        return loginSuccess;
    }

    public void setLoginSuccess(boolean loginSuccess) {
        this.loginSuccess = loginSuccess;
    }
}
