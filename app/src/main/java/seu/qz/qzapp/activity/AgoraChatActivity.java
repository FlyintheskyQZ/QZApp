package seu.qz.qzapp.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import io.agora.rtm.ErrorInfo;
import io.agora.rtm.LocalInvitation;
import io.agora.rtm.RemoteInvitation;
import io.agora.rtm.ResultCallback;
import io.agora.rtm.RtmCallEventListener;
import io.agora.rtm.RtmCallManager;
import io.agora.rtm.RtmChannel;
import io.agora.rtm.RtmChannelAttribute;
import io.agora.rtm.RtmChannelListener;
import io.agora.rtm.RtmChannelMember;
import io.agora.rtm.RtmClient;
import io.agora.rtm.RtmClientListener;
import io.agora.rtm.RtmFileMessage;
import io.agora.rtm.RtmImageMessage;
import io.agora.rtm.RtmMediaOperationProgress;
import io.agora.rtm.RtmMessage;
import io.agora.rtm.RtmMessageType;
import io.agora.rtm.RtmStatusCode;
import seu.qz.qzapp.R;
import seu.qz.qzapp.activity.viewmodel.AgoraChatViewModel;
import seu.qz.qzapp.agora.ChatManager;
import seu.qz.qzapp.agora.MessageBean;
import seu.qz.qzapp.agora.MessageListBean;
import seu.qz.qzapp.database.LitePalUtils;
import seu.qz.qzapp.entity.AppCustomer;
import seu.qz.qzapp.entity.BriefChatItem;
import seu.qz.qzapp.main.ChatAdapter;
import seu.qz.qzapp.main.MessageAdapter;
import seu.qz.qzapp.utils.DateRelatedUtils;
import seu.qz.qzapp.utils.FileUtil;
import seu.qz.qzapp.utils.ImageUtil;
import seu.qz.qzapp.utils.MessageUtil;

public class AgoraChatActivity extends AppCompatActivity {

    //数据维护对象
    AgoraChatViewModel agoraChatViewModel;

    //Agora相关
    RtmClient rtmClient;
    //针对当前用户和对方用户的封装消息池，同时也是Adapter的数据源
    private List<MessageBean> mMessageBeanList = new ArrayList<>();
    //聊天界面的RecyclerView的Adapter
    private MessageAdapter mMessageAdapter;
    //频道成员数，由于是p2p，只为1，后面可以去掉
    private int mChannelMemberCount = 1;
    //本地id，前一个Activity传过来
    private String mUserId = "";
    //对方id，前一个Activity传过来
    private String mPeerId = "";
    //频道名称
    private String mChannelName = "";
    //chat管理器
    private ChatManager mChatManager;
    //本地客户端聊天监听器
    private RtmClientListener mClientListener;
    //是否是p2p(是的，后面可以去掉)
    private boolean mIsPeerToPeerMode = true;
    //RTM频道
    private RtmChannel mRtmChannel;



    //UI控件
    TextView chat_message_title;
    TextView chat_selection_chatBtn;
    ImageView chat_selection_imgBtn;
    EditText chat_message_editText;
    RecyclerView chat_message_list;
    ImageView chat_big_image;
    //是否已经登陆Aogra的服务器
    boolean loginSuccess = false;

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_agora_chat);

        //UI控件获取
        chat_message_title = findViewById(R.id.chat_message_title);
        chat_selection_chatBtn = findViewById(R.id.chat_selection_chatbtn);
        chat_selection_imgBtn = findViewById(R.id.chat_selection_imgbtn);
        chat_message_editText = findViewById(R.id.chat_message_edittext);
        chat_message_list = findViewById(R.id.chat_message_list);
        chat_big_image = findViewById(R.id.chat_big_image);

        //初始化ViewModel的相关数据
        agoraChatViewModel = ViewModelProviders.of(this).get(AgoraChatViewModel.class);
        agoraChatViewModel.setMainCustomer(LitePalUtils.getSingleCustomer(getIntent().getStringExtra("username")));

        mPeerId = getIntent().getStringExtra("peerId");
        //agoraChatViewModel.setChatItem((BriefChatItem) getIntent().getSerializableExtra("chat_cache"));


        //获取相关Agora实例
        mChatManager = BaseApplication.the().getChatManager();
        loginSuccess = mChatManager.isLoginSuccess();
        Log.d(TAG, "loginstatus:" + loginSuccess);
        rtmClient = mChatManager.getRtmClient();
        //注册Agora的客户端监听器
        mClientListener = new MyRtmClientListener();
        mChatManager.registerListener(mClientListener);
        Log.d(TAG, "current MessageUtil list size:" + MessageUtil.getMessageListBeanList().size() );
        if(MessageUtil.getMessageListBeanList().size() > 0){
            Log.d(TAG, "current chatItem:" + MessageUtil.getMessageListBeanList().get(0).getChatItem());
        }

        //配置Agora相关设置
        mUserId = agoraChatViewModel.getMainCustomer().getUser_id().toString();
        if (mIsPeerToPeerMode) {
            //获取聊天对象信息，设置聊天框标题
//            chat_message_title.setText(mPeerId);


            // 加载聊天历史记录
            //从MessageUtil中的messageListBeanList取出对应的用户的mMessageBeanList
            MessageListBean messageListBean = MessageUtil.getExistMessageListBean(mPeerId);
            BriefChatItem chatItem = messageListBean.getChatItem();
            agoraChatViewModel.setChatItem(chatItem);
            String peerName = agoraChatViewModel.getChatItem().getUsername();
            chat_message_title.setText(peerName);
            if (messageListBean != null) {
                mMessageBeanList.addAll(messageListBean.getMessageBeanList());
            }
            // load offline messages since last chat with this peer.
            // Then clear cached offline messages from message pool
            // since they are already consumed.
            //从ChatManager的mMessagePool中取出未查看的数据，进行显示，并清空mMessagePool中对应peerId的信息缓存（未查看的查看了就算已查看的消息）
            MessageListBean offlineMessageBean = new MessageListBean(mPeerId, mChatManager);
            mMessageBeanList.addAll(offlineMessageBean.getMessageBeanList());
            mChatManager.removeAllOfflineMessages(mPeerId);

            //多人聊天，待改进
        } else {
//            mChannelName = targetName;
//            mChannelMemberCount = 1;
//            mTitleTextView.setText(MessageFormat.format("{0}({1})", mChannelName, mChannelMemberCount));
//            createAndJoinChannel();
        }
        //配置聊天消息的Recycler的Adapter
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        mMessageAdapter = new MessageAdapter(this, mMessageBeanList, agoraChatViewModel.getMainCustomer(), new MessageAdapter.OnItemClickListener() {
            //注册RecyclerView中子项（即消息）被点击的监听器：当为图片信息时点击则立马找到图片放大全图
            @Override
            public void onItemClick(final MessageBean message) {
                //如果是图片消息
                if (message.getMessage().getMessageType() == RtmMessageType.IMAGE) {
                    //如果messagebean中的cacheFile不为空，点击图片时会放大图片到整个屏幕，只有未被查看的消息才会确实cacheFile的属性赋值，而缓存到本地只有在点击该图片时才会发生
                    if (!TextUtils.isEmpty(message.getCacheFile())) {
                        Glide.with(AgoraChatActivity.this).load(message.getCacheFile()).into(chat_big_image);
                        chat_big_image.setVisibility(View.VISIBLE);
                        //如果messagebean中的cacheFile为空，则找到缓存文件补全该属性(如果本地没有则下载下来到指定文件中)，同时将缓存图片放大到全屏
                    } else {
                        ImageUtil.cacheImage(AgoraChatActivity.this, rtmClient, (RtmImageMessage) message.getMessage(), new ResultCallback<String>() {
                            @Override
                            public void onSuccess(final String file) {
                                File original = new File(file);
                                final File ambition = new File(getCacheDir(), FileUtil.CACHE_IMAGE + "/" + mUserId + "/" +
                                        DateRelatedUtils.FormatOutputByDate(new Date(System.currentTimeMillis()), DateRelatedUtils.TYPE_CACHE) + ".jpg");
                                original.renameTo(ambition);
                                message.setCacheFile(ambition.getAbsolutePath());
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Glide.with(AgoraChatActivity.this).load(ambition).into(chat_big_image);
                                        chat_big_image.setVisibility(View.VISIBLE);
                                    }
                                });
                            }

                            @Override
                            public void onFailure(ErrorInfo errorInfo) {

                            }
                        });
                    }
                }
            }
        });
        chat_message_list.setLayoutManager(layoutManager);
        chat_message_list.setAdapter(mMessageAdapter);

        //UI控件添加监听器
        //消息发送按钮
        chat_selection_chatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //进入本界面前已登陆成功
                if(loginSuccess){
                    //获取数据框的文本数据
                    String msg = chat_message_editText.getText().toString();
                    if (!msg.equals("")) {
                        //创建RtmMessage实例，放入输入的字符串
                        RtmMessage message = rtmClient.createMessage();
                        message.setText(msg);
                        //将消息封装进MessageBean中，存入缓存池内
                        MessageBean messageBean = new MessageBean(mUserId, message, true);
                        mMessageBeanList.add(messageBean);
                        //Adapter更新UI
                        mMessageAdapter.notifyItemRangeChanged(mMessageBeanList.size(), 1);
                        //RecyclerView滑动至新发送的数据处
                        chat_message_list.scrollToPosition(mMessageBeanList.size() - 1);
                        //根据当前模式（单人还是多人）发送message
                        if (mIsPeerToPeerMode) {
                            sendPeerMessage(message);
                        } else {
                            sendChannelMessage(message);
                        }
                    }
                    chat_message_editText.setText("");
                    //尝试再次登录
                }else {
                    android.app.AlertDialog.Builder dialog = new AlertDialog.Builder(AgoraChatActivity.this);
                    dialog.setTitle(R.string.chat_need_login_title);
                    dialog.setMessage(R.string.chat_need_login_message);
                    dialog.setCancelable(false);
                    dialog.setPositiveButton(R.string.chat_positive_button, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            LoginInAgora();
                        }
                    });
                    dialog.show();
                }
            }
        });
        //图片选取按钮点击监听
        chat_selection_imgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity().start(AgoraChatActivity.this);
            }
        });
        //全屏图点击监听
        chat_big_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chat_big_image.setVisibility(View.GONE);
            }
        });
    }

    private void LoginInAgora() {
        //mIsInChat = true;
        //登录Id为AppCustomer的user_id
        final String mUserId = agoraChatViewModel.getMainCustomer().getUser_id().toString();
        //此处Token后期补上！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！
        rtmClient.login(null, mUserId, new ResultCallback<Void>() {
            //登陆成功
            @Override
            public void onSuccess(Void responseInfo) {
                Log.i(TAG, "login success");

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        loginSuccess = true;
                        mChatManager.setLoginSuccess(true);
                        initCallManager();
//
//                        Intent intent = new Intent(getActivity(), AgoraChatActivity.class);
//                        intent.putExtra(MessageUtil.INTENT_EXTRA_USER_ID, mUserId);
//                        startActivity(intent);
                    }
                });
            }
            //登陆失败
            @Override
            public void onFailure(ErrorInfo errorInfo) {
                Log.i(TAG, "login failed: " + errorInfo.getErrorCode());
                loginSuccess = false;
//                getActivity().runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//
//                    }
//                }
//                {
//                    mLoginBtn.setEnabled(true);
//                    mIsInChat = false;
//                    showToast(getString(R.string.login_failed));
//                }
//                );
            }
        });
    }

    public void initCallManager(){
        final RtmCallManager callManager = rtmClient.getRtmCallManager();
        callManager.setEventListener(new RtmCallEventListener() {
            @Override
            public void onLocalInvitationReceivedByPeer(LocalInvitation localInvitation) {

            }

            @Override
            public void onLocalInvitationAccepted(LocalInvitation localInvitation, String s) {

            }

            @Override
            public void onLocalInvitationRefused(LocalInvitation localInvitation, String s) {

            }

            @Override
            public void onLocalInvitationCanceled(LocalInvitation localInvitation) {

            }

            @Override
            public void onLocalInvitationFailure(LocalInvitation localInvitation, int i) {

            }

            @Override
            public void onRemoteInvitationReceived(final RemoteInvitation remoteInvitation) {
                callManager.acceptRemoteInvitation(remoteInvitation, new ResultCallback<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //加入呼叫的频道！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！
                        String chat_cache = remoteInvitation.getContent();

                    }

                    @Override
                    public void onFailure(ErrorInfo errorInfo) {

                    }
                });
            }

            @Override
            public void onRemoteInvitationAccepted(RemoteInvitation remoteInvitation) {

            }

            @Override
            public void onRemoteInvitationRefused(RemoteInvitation remoteInvitation) {

            }

            @Override
            public void onRemoteInvitationCanceled(RemoteInvitation remoteInvitation) {

            }

            @Override
            public void onRemoteInvitationFailure(RemoteInvitation remoteInvitation, int i) {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                final String file = resultUri.getPath();
                ImageUtil.uploadImage(this, rtmClient, file, new ResultCallback<RtmImageMessage>() {
                    @Override
                    public void onSuccess(final RtmImageMessage rtmImageMessage) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                MessageBean messageBean = new MessageBean(mUserId, rtmImageMessage, true);
                                File original = new File(file);
                                File ambition = new File(getCacheDir(), FileUtil.CACHE_IMAGE + "/" + mUserId + "/" +
                                        DateRelatedUtils.FormatOutputByDate(new Date(System.currentTimeMillis()), DateRelatedUtils.TYPE_CACHE) + ".jpg");
                                original.renameTo(ambition);
                                messageBean.setCacheFile(ambition.getAbsolutePath());
                                rtmImageMessage.setText(ambition.getAbsolutePath());
                                mMessageBeanList.add(messageBean);
                                mMessageAdapter.notifyItemRangeChanged(mMessageBeanList.size(), 1);
                                chat_message_list.scrollToPosition(mMessageBeanList.size() - 1);

                                if (mIsPeerToPeerMode) {
                                    sendPeerMessage(rtmImageMessage);
                                } else {
                                    sendChannelMessage(rtmImageMessage);
                                }
                            }
                        });
                    }

                    @Override
                    public void onFailure(ErrorInfo errorInfo) {

                    }
                });
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                result.getError().printStackTrace();
            }
        }
    }


    public void onClickFinish(View v) {
        finish();
    }

    /**
     * API CALL: send message to peer
     * 发送RtmMessage
     */
    private void sendPeerMessage(final RtmMessage message) {
        //给message中的text添加用户信息前缀
        MessageUtil.addUserInfToMessage(message, agoraChatViewModel.getMainCustomer());
        rtmClient.sendMessageToPeer(mPeerId, message, mChatManager.getSendMessageOptions(), new ResultCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                MessageUtil.deleteUserInfInMessage(message);
                // do nothing
            }

            @Override
            public void onFailure(ErrorInfo errorInfo) {
                // refer to RtmStatusCode.PeerMessageState for the message state
                final int errorCode = errorInfo.getErrorCode();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        switch (errorCode) {
                            case RtmStatusCode.PeerMessageError.PEER_MESSAGE_ERR_TIMEOUT:
                            case RtmStatusCode.PeerMessageError.PEER_MESSAGE_ERR_FAILURE:
                                showToast(getString(R.string.send_msg_failed));
                                break;
                            case RtmStatusCode.PeerMessageError.PEER_MESSAGE_ERR_PEER_UNREACHABLE:
                                showToast(getString(R.string.peer_offline));
                                break;
                            case RtmStatusCode.PeerMessageError.PEER_MESSAGE_ERR_CACHED_BY_SERVER:
                                showToast(getString(R.string.message_cached));
                                break;
                        }
                    }
                });
            }
        });
    }

    /**
     * API CALL: create and join channel
     * 创建并加入频道
     */
    private void createAndJoinChannel() {
        // step 1: create a channel instance
        //创建一个频道实例
        mRtmChannel = rtmClient.createChannel(mChannelName, new MyChannelListener());
        if (mRtmChannel == null) {
            showToast(getString(R.string.join_channel_failed));
            finish();
            return;
        }

        Log.e("channel", mRtmChannel + "");

        // step 2: join the channel
        //加入频道，获取在线成员list，刷新显示列表
        mRtmChannel.join(new ResultCallback<Void>() {
            @Override
            public void onSuccess(Void responseInfo) {
                Log.i(TAG, "join channel success");
                getChannelMemberList();
            }

            @Override
            public void onFailure(ErrorInfo errorInfo) {
                Log.e(TAG, "join channel failed");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showToast(getString(R.string.join_channel_failed));
                        finish();
                    }
                });
            }
        });
    }

    /**
     * API CALL: get channel member list
     * 获取当前频道在线成员列表，刷新人数以及标题显示
     */
    private void getChannelMemberList() {
        //获取当前频道在线成员列表
        mRtmChannel.getMembers(new ResultCallback<List<RtmChannelMember>>() {
            @Override
            public void onSuccess(final List<RtmChannelMember> responseInfo) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //更新频道成员数，并刷新频道标题（刷新人数）
                        mChannelMemberCount = responseInfo.size();
                        refreshChannelTitle();
                    }
                });
            }

            @Override
            public void onFailure(ErrorInfo errorInfo) {
                Log.e(TAG, "failed to get channel members, err: " + errorInfo.getErrorCode());
            }
        });
    }

    /**
     * API CALL: send message to a channel
     * 发送消息到Channel
     */
    private void sendChannelMessage(RtmMessage message) {
        mRtmChannel.sendMessage(message, new ResultCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

            }

            @Override
            public void onFailure(ErrorInfo errorInfo) {
                // refer to RtmStatusCode.ChannelMessageState for the message state
                final int errorCode = errorInfo.getErrorCode();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        switch (errorCode) {
                            case RtmStatusCode.ChannelMessageError.CHANNEL_MESSAGE_ERR_TIMEOUT:
                            case RtmStatusCode.ChannelMessageError.CHANNEL_MESSAGE_ERR_FAILURE:
                                showToast(getString(R.string.send_msg_failed));
                                break;
                        }
                    }
                });
            }
        });
    }

    /**
     * API CALL: leave and release channel
     */
    private void leaveAndReleaseChannel() {
        if (mRtmChannel != null) {
            mRtmChannel.leave(null);
            mRtmChannel.release();
            mRtmChannel = null;
        }
    }



    class MyRtmClientListener implements RtmClientListener {

        @Override
        public void onConnectionStateChanged(final int state, int reason) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    switch (state) {
                        case RtmStatusCode.ConnectionState.CONNECTION_STATE_RECONNECTING:
                            showToast(getString(R.string.reconnecting));
                            break;
                        case RtmStatusCode.ConnectionState.CONNECTION_STATE_ABORTED:
                            showToast(getString(R.string.account_offline));
                            setResult(MessageUtil.ACTIVITY_RESULT_CONN_ABORTED);
                            finish();
                            break;
                    }
                }
            });
        }

        @Override
        public void onMessageReceived(final RtmMessage message, final String peerId) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //如果消息对应的用户id与当前活动对应的用户id相同，则直接添加到当前活动的MessageBean的List（Adatper的数据源）中，
                    // 如果不相同，则表明该信息是来自其他用户的，存储到MessageUtil的缓存池中
                    if (peerId.equals(mPeerId)) {
                        //去除用户信息前缀
                        String message_text = message.getText();
                        message_text = message_text.substring(message_text.indexOf(":") + 1);
                        message.setText(message_text);
                        MessageBean messageBean = new MessageBean(peerId, message, false);
                        messageBean.setBackground(getMessageColor(peerId));
                        mMessageBeanList.add(messageBean);
                        mMessageAdapter.notifyItemRangeChanged(mMessageBeanList.size(), 1);
                        chat_message_list.scrollToPosition(mMessageBeanList.size() - 1);
                        //重新排序chatFragment
//                        MessageListBean listBean = MessageUtil.getExistMessageListBean(peerId);
//                        MessageUtil.addMessageListBeanList(listBean);
//                        ChatAdapter adapter = mChatManager.getAdapter();
//                        if(adapter != null){
//                            adapter.notifyDataSetChanged();
//                        }
                    } else {
                        //MessageUtil.addMessageBean(peerId, message);
                        mChatManager.getmMessagePool().insertOfflineMessage(message, peerId, AgoraChatActivity.this);
                    }
                }
            });
        }

        //方法效果同上述，只是文本消息改成了图片消息
        @Override
        public void onImageMessageReceivedFromPeer(final RtmImageMessage rtmImageMessage, final String peerId) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (peerId.equals(mPeerId)) {
                        final MessageBean messageBean = new MessageBean(peerId, rtmImageMessage, false);
                        messageBean.setBackground(getMessageColor(peerId));
                        mMessageBeanList.add(messageBean);
                        mMessageAdapter.notifyItemRangeChanged(mMessageBeanList.size(), 1);
                        chat_message_list.scrollToPosition(mMessageBeanList.size() - 1);
                        ImageUtil.cacheImage(AgoraChatActivity.this, rtmClient, rtmImageMessage, new ResultCallback<String>() {
                            @Override
                            public void onSuccess(final String file) {
                                File original = new File(file);
                                File ambition = new File(getCacheDir(), FileUtil.CACHE_IMAGE + "/" + mUserId + "/" +
                                        DateRelatedUtils.FormatOutputByDate(new Date(System.currentTimeMillis()), DateRelatedUtils.TYPE_CACHE) + ".jpg");
                                original.renameTo(ambition);
                                //去除用户信息前缀
                                rtmImageMessage.setText(ambition.getAbsolutePath());
                                messageBean.setCacheFile(ambition.getAbsolutePath());
                            }

                            @Override
                            public void onFailure(ErrorInfo errorInfo) {

                            }
                        });
//                        //重新排序chatFragment
//                        MessageListBean listBean = MessageUtil.getExistMessageListBean(peerId);
//                        MessageUtil.addMessageListBeanList(listBean);
//                        ChatAdapter adapter = mChatManager.getAdapter();
//                        if(adapter != null){
//                            adapter.notifyDataSetChanged();
//                        }
                    } else {
                        //MessageUtil.addMessageBean(peerId, rtmImageMessage);
                        //接收的非来自当前聊天对象的消息全部放到RtmMessagePool中，并将Image类型消息的text赋值为"file-cache_file(文件路径名)"

                        mChatManager.getmMessagePool().insertOfflineMessage(rtmImageMessage, peerId, AgoraChatActivity.this);
                        ImageUtil.cacheImage(AgoraChatActivity.this, rtmClient, rtmImageMessage, new ResultCallback<String>() {
                            @Override
                            public void onSuccess(final String file) {
                                File original = new File(file);
                                File ambition = new File(getCacheDir(), FileUtil.CACHE_IMAGE + "/" + mUserId + "/" +
                                        DateRelatedUtils.FormatOutputByDate(new Date(System.currentTimeMillis()), DateRelatedUtils.TYPE_CACHE) + ".jpg");
                                original.renameTo(ambition);
                                rtmImageMessage.setText(ambition.getAbsolutePath());
                            }

                            @Override
                            public void onFailure(ErrorInfo errorInfo) {

                            }
                        });
                    }
                }
            });
        }

        @Override
        public void onFileMessageReceivedFromPeer(RtmFileMessage rtmFileMessage, String s) {

        }

        @Override
        public void onMediaUploadingProgress(RtmMediaOperationProgress rtmMediaOperationProgress, long l) {

        }

        @Override
        public void onMediaDownloadingProgress(RtmMediaOperationProgress rtmMediaOperationProgress, long l) {

        }

        @Override
        public void onTokenExpired() {

        }

        @Override
        public void onPeersOnlineStatusChanged(Map<String, Integer> map) {

        }
    }

    /**
     * API CALLBACK: rtm channel event listener
     */
    class MyChannelListener implements RtmChannelListener {
        @Override
        public void onMemberCountUpdated(int i) {

        }

        @Override
        public void onAttributesUpdated(List<RtmChannelAttribute> list) {

        }

        private static final String TAG = "MyChannelListener";
        @Override
        public void onMessageReceived(final RtmMessage message, final RtmChannelMember fromMember) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String account = fromMember.getUserId();
                    Log.i(TAG, "onMessageReceived account = " + account + " msg = " + message);
                    MessageBean messageBean = new MessageBean(account, message, false);
                    messageBean.setBackground(getMessageColor(account));
                    mMessageBeanList.add(messageBean);
                    mMessageAdapter.notifyItemRangeChanged(mMessageBeanList.size(), 1);
                    chat_message_list.scrollToPosition(mMessageBeanList.size() - 1);
                }
            });
        }

        @Override
        public void onImageMessageReceived(final RtmImageMessage rtmImageMessage, final RtmChannelMember rtmChannelMember) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String account = rtmChannelMember.getUserId();
                    Log.i(TAG, "onMessageReceived account = " + account + " msg = " + rtmImageMessage);
                    MessageBean messageBean = new MessageBean(account, rtmImageMessage, false);
                    messageBean.setBackground(getMessageColor(account));
                    mMessageBeanList.add(messageBean);
                    mMessageAdapter.notifyItemRangeChanged(mMessageBeanList.size(), 1);
                    chat_message_list.scrollToPosition(mMessageBeanList.size() - 1);
                }
            });
        }

        @Override
        public void onFileMessageReceived(RtmFileMessage rtmFileMessage, RtmChannelMember rtmChannelMember) {

        }

        @Override
        public void onMemberJoined(RtmChannelMember member) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mChannelMemberCount++;
                    refreshChannelTitle();
                }
            });
        }

        @Override
        public void onMemberLeft(RtmChannelMember member) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mChannelMemberCount--;
                    refreshChannelTitle();
                }
            });
        }
    }

    private int getMessageColor(String account) {
        for (int i = 0; i < mMessageBeanList.size(); i++) {
            if (account.equals(mMessageBeanList.get(i).getAccount())) {
                return mMessageBeanList.get(i).getBackground();
            }
        }
        return MessageUtil.COLOR_ARRAY[MessageUtil.RANDOM.nextInt(MessageUtil.COLOR_ARRAY.length)];
    }

    //刷新频道标题，格式为"频道名（人数）"
    private void refreshChannelTitle() {
        String titleFormat = getString(R.string.channel_title);
        String title = String.format(titleFormat, mChannelName, mChannelMemberCount);
        chat_message_title.setText(title);
    }

    private void showToast(final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(AgoraChatActivity.this, text, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //退出当前Activity后，此Activity中的消息缓存mMessageBeanList会存入MessageUtil中的messageListBeanList中
        if (mIsPeerToPeerMode) {
            MessageUtil.addMessageListBeanList(new MessageListBean(mPeerId, mMessageBeanList, agoraChatViewModel.getChatItem()));
            ChatAdapter adapter = mChatManager.getAdapter();
            if(adapter != null) {
                adapter.notifyDataSetChanged();
            }
        } else {
            leaveAndReleaseChannel();
        }
        Log.d(TAG, "current MessageUtil list size:" + MessageUtil.getMessageListBeanList().size() );
        if(MessageUtil.getMessageListBeanList().size() > 0){
            Log.d(TAG, "current chatItem:" + MessageUtil.getMessageListBeanList().get(0).getChatItem());
        }
        //此处注销监听器后，所有接收的消息都会进入ChatManager中的RtmMessagePool中
        mChatManager.unregisterListener(mClientListener);
    }
}