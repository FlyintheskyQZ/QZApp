package seu.qz.qzapp.agora;

import java.util.ArrayList;
import java.util.List;

import io.agora.rtm.RtmMessage;
import io.agora.rtm.RtmMessageType;
import seu.qz.qzapp.entity.BriefChatItem;

/**
 * 对来自某个用户的所有聊天缓存消息的封装，添加了对方账号
 * 其中的消息来自ChatManager中维护的RtmMessage池，一个对应一个List<RtmMessage>(以peerid对应)
 */
public class MessageListBean {
    private String accountOther;
    private List<MessageBean> messageBeanList;
    private BriefChatItem chatItem;

    public MessageListBean(String account, List<MessageBean> messageBeanList) {
        this.accountOther = account;
        this.messageBeanList = messageBeanList;
        this.chatItem = null;
    }

    public MessageListBean(String accountOther, List<MessageBean> messageBeanList, BriefChatItem chatItem) {
        this.accountOther = accountOther;
        this.messageBeanList = messageBeanList;
        this.chatItem = chatItem;
    }

    /**
     * Create message list bean from offline messages
     *
     * @param account     peer user id to find offline messages from
     * @param chatManager chat manager that managers offline message pool
     */
    public MessageListBean(String account, ChatManager chatManager) {
        accountOther = account;
        messageBeanList = new ArrayList<>();
        this.chatItem = null;

        List<RtmMessage> messageList = chatManager.getAllOfflineMessages(account);
        for (RtmMessage m : messageList) {

            //将cacheFile属性都填上
            //String message_text = m.getText();
            // All offline messages are from peer users
            MessageBean bean = new MessageBean(account, m, false);
            //来自聊天时接收的非当前聊天对象发送的图片信息
            if(m.getMessageType() == RtmMessageType.IMAGE){
                bean.setCacheFile("");
            }
            messageBeanList.add(bean);
        }
    }

    public String getAccountOther() {
        return accountOther;
    }

    public void setAccountOther(String accountOther) {
        this.accountOther = accountOther;
    }

    public List<MessageBean> getMessageBeanList() {
        return messageBeanList;
    }

    public void setMessageBeanList(List<MessageBean> messageBeanList) {
        this.messageBeanList = messageBeanList;
    }

    public BriefChatItem getChatItem() {
        return chatItem;
    }

    public void setChatItem(BriefChatItem chatItem) {
        this.chatItem = chatItem;
    }
}