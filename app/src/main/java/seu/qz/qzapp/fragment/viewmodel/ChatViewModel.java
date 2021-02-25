package seu.qz.qzapp.fragment.viewmodel;

import android.app.Activity;
import android.content.Context;

import androidx.fragment.app.FragmentActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.List;

import seu.qz.qzapp.activity.operation.ChatFragmentOperation;
import seu.qz.qzapp.entity.BriefChatItem;
import seu.qz.qzapp.main.ChatAdapter;
import seu.qz.qzapp.utils.CommonUtils;
import seu.qz.qzapp.utils.MessageUtil;

public class ChatViewModel extends BaseViewModel {

    ChatFragmentOperation operation = new ChatFragmentOperation();
    //文件名
    List<String> fileNameItems;

    public void initBriefItems(String user_id, ChatAdapter adapter, Activity activity, SwipeRefreshLayout swipeRefreshLayout) {
        fileNameItems = operation.fillFileNameItems(user_id, activity);
        //还有一些初始化工作！！！！！！！！！！！！！！！！！！！！！！！！！！！！！




    }

    public List<String> getFileNameItems() {
        return fileNameItems;
    }

    public void setFileNameItems(List<String> fileNameItems) {
        this.fileNameItems = fileNameItems;
    }

    public void refresh(String user_id, ChatAdapter adapter, Context context, SwipeRefreshLayout swipeRefreshLayout) {
        //可能存在异步更新然后关闭刷新动画！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！
        fileNameItems = operation.fillFileNameItems(user_id, context);
        adapter.setChatsList(MessageUtil.getMessageListBeanList());
        adapter.notifyDataSetChanged();
        if(swipeRefreshLayout.isRefreshing()){
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    //改变聊天缓存文件的顺序，触发原因分为：
    //1.被动，由对方首先发送消息过来，以呼叫的方式来建立聊天链接
    //2.主动，由点击聊天窗口的方式来触发置顶功能（主动创建聊天窗口的功能在OrdercontentActivity中完成）
    public void changeChatsOrder(String name_or_id, int origin_reason, Context context) {
        //改变缓存文件的Order，分为被动修改和主动修改（点击聊天子项或是创建新的聊天窗口，创建的可以考虑放在OrdercontentActivity模块中构建）！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！并刷新当前页面！！！！！！！！！！！！！！！！！！！！！！！！
        switch (origin_reason){
            case 1:
            operation.modifyChatCacheFilesByFileName(name_or_id, context);
                break;
            case 2:
            operation.modifyChatCacheFilesById(name_or_id, context);
                break;
            default:break;
        }
    }
}