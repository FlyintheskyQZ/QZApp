package seu.qz.qzapp.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import io.agora.rtm.RtmMessage;
import seu.qz.qzapp.R;
import seu.qz.qzapp.agora.MessageBean;
import seu.qz.qzapp.agora.MessageListBean;
import seu.qz.qzapp.entity.AppCustomer;

/**
 * Message维护工具类，维护有一个MessageListBean的list缓存池
 */
public class MessageUtil {
    public static final int MAX_INPUT_NAME_LENGTH = 64;

    public static final int ACTIVITY_RESULT_CONN_ABORTED = 1;

    public static final String INTENT_EXTRA_IS_PEER_MODE = "chatMode";
    public static final String INTENT_EXTRA_USER_ID = "userId";
    public static final String INTENT_EXTRA_TARGET_NAME = "targetName";

    public static Random RANDOM = new Random();

    public static final int[] COLOR_ARRAY = new int[]{
            R.drawable.shape_circle_black,
            R.drawable.shape_circle_blue,
            R.drawable.shape_circle_pink,
            R.drawable.shape_circle_pink_dark,
            R.drawable.shape_circle_yellow,
            R.drawable.shape_circle_red
    };

    //MessageListBean的缓存池，对应某个用户的List<RtmMessage>封装
    private static List<MessageListBean> messageListBeanList = new ArrayList<>();

    //给缓存池添加成员
    public static void addMessageListBeanList(MessageListBean messageListBean) {
        messageListBeanList.add(messageListBean);
    }

    //登出时清空缓存池
    public static void cleanMessageListBeanList() {
        messageListBeanList.clear();
    }
    //返回指定用户的MessageListBean，并移除池
    public static MessageListBean getExistMessageListBean(String accountOther) {
        int ret = existMessageListBean(accountOther);
        if (ret > -1) {
            return messageListBeanList.remove(ret);
        }
        return null;
    }

    //返回指定用户的MessageListBean在缓存池中的位置
    public static int existMessageListBean(String userId) {
        int size = messageListBeanList.size();
        for (int i = 0; i < size; i++) {
            if (messageListBeanList.get(i).getAccountOther().equals(userId)) {
                return i;
            }
        }
        return -1;
    }
    //添加MessageBean到缓存池中
    public static void addMessageBean(String account, RtmMessage msg) {
        MessageBean messageBean = new MessageBean(account, msg, false);
        int ret = existMessageListBean(account);
        //如果缓存池中没有指定用户的MessageListBean，则创建一个，并添加参数中的RtmMessage
        if (ret == -1) {
            // account not exist new messagelistbean
            messageBean.setBackground(MessageUtil.COLOR_ARRAY[RANDOM.nextInt(MessageUtil.COLOR_ARRAY.length)]);
            List<MessageBean> messageBeanList = new ArrayList<>();
            messageBeanList.add(messageBean);
            messageListBeanList.add(new MessageListBean(account, messageBeanList));
        //如果缓存池中有，则取出该MessageListBean，添加封装后的MessageBean后再放入总池中
        } else {
            // account exist get messagelistbean
            MessageListBean bean = messageListBeanList.remove(ret);
            List<MessageBean> messageBeanList = bean.getMessageBeanList();
            if (messageBeanList.size() > 0) {
                messageBean.setBackground(messageBeanList.get(0).getBackground());
            } else {
                messageBean.setBackground(MessageUtil.COLOR_ARRAY[RANDOM.nextInt(MessageUtil.COLOR_ARRAY.length)]);
            }
            messageBeanList.add(messageBean);
            bean.setMessageBeanList(messageBeanList);
            messageListBeanList.add(bean);
        }
    }

    public static List<MessageListBean> getMessageListBeanList() {
        return messageListBeanList;
    }

    public static void addUserInfToMessage(RtmMessage message, AppCustomer customer){
        if(message == null || customer == null){
            return;
        }
        String text = message.getText();
        String user_inf = customer.getUser_nickName() + "-" + String.valueOf(customer.isMale()) + ":";
        if(text == null || text.isEmpty()){
            text = user_inf;
        }else {
            text = user_inf + text;
        }
        message.setText(text);
        return;
    }

    public static void deleteUserInfInMessage(RtmMessage message){
        if(message == null){
            return;
        }
        String message_text = message.getText();
        message_text = message_text.substring(message_text.indexOf(":") + 1);
        message.setText(message_text);
    }

}
