package seu.qz.qzapp.fragment.viewmodel;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.ArrayList;
import java.util.List;

import seu.qz.qzapp.R;
import seu.qz.qzapp.entity.BriefOrderItem;
import seu.qz.qzapp.main.OrderAdapter;
import seu.qz.qzapp.activity.operation.OrderFragmentOperation;
import seu.qz.qzapp.utils.ListOperationUtils;
import seu.qz.qzapp.utils.PropertyUtil;

public class OrderViewModel extends BaseViewModel {


    private OrderFragmentOperation operation = OrderFragmentOperation.singleInstance();


    private List<BriefOrderItem> briefOrderItems = new ArrayList<>();
    private int currentNumber_provide = 0;
    private int currentNumber_finished = 0;
    //当新建订单或者有订单完成时，需要更新该变量并清空briefOrderItems
    private int requestType = 0;
    public final int TYPE_BOTH = 0;
    public final int TYPE_PROVIDE_OVER = 1;
    public final int TYPE_BOTH_OVER = 2;
    //用于在获取BriefOrderItems时，每次加载时一定数量（配置文件中initOrderItemsNumber对应的值）的Item时，可能会经历多轮查询，该变量用于记录每轮查询前还需加载的数量
    private int ItemNumber_remain = 0;
    //用于判定加载BriefOrderItems时，单次加载时是否有新item加入到briefOrderItems中（即briefOrderItems是否发生变化）
    private  boolean briefItemsHasChanged = false;

    public List<BriefOrderItem> getBriefOrderItems() {
        return briefOrderItems;
    }

    public void setBriefOrderItems(List<BriefOrderItem> briefOrderItems) {
        this.briefOrderItems = briefOrderItems;
    }



    //有新订单时刷新容器并通知更改
    public void refresh(String mainUsername, OrderAdapter adapter, Activity activity, SwipeRefreshLayout layout){
        briefOrderItems.clear();
        setRequestType(TYPE_BOTH);
        currentNumber_provide = 0;
        currentNumber_finished = 0;
        setItemNumber_remain(0);
        setBriefItemsHasChanged(false);
        initBriefItems(mainUsername, adapter, activity, layout);
        //需要完善一下！！！！！！！！！！！！！！！！！！！！！！！！！！！！！
    }

    public void initBriefItems(String user_nickName, OrderAdapter adapter, Activity activity, SwipeRefreshLayout layout) {
        //做一些初始化工作，会调用到getBriefItems()!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        getBriefItems(user_nickName, adapter, activity, layout);
    }

    public void getBriefItems(final String user_nickName, final OrderAdapter adapter, final Activity activity, final SwipeRefreshLayout layout){
        List<BriefOrderItem> items = null;
        final int contentNumber = Integer.parseInt(PropertyUtil.getPropertyByKey("initOrderItemsNumber", activity.getApplicationContext()));
        setItemNumber_remain(contentNumber);
        Handler handler = new Handler(){
            @Override
            public void handleMessage(@NonNull Message msg) {
                switch (msg.what){
                    //网络连接出错
                    case 0:
                        if(layout != null){
                            layout.setRefreshing(false);
                        }
                        AlertDialog.Builder dialog = new AlertDialog.Builder(activity);
                        dialog.setTitle(R.string.order_error);
                        dialog.setMessage(R.string.order_netError_content);
                        dialog.setCancelable(false);
                        dialog.setPositiveButton(R.string.order_netError_dialog_ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                adapter.setLoadingState(2);
                                Intent intent = new Intent(Settings.ACTION_SETTINGS);
                                activity.startActivity(intent);
                            }
                        });
                        dialog.setNegativeButton(R.string.order_netError_dialog_cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                adapter.setLoadingState(2);
                            }
                        });
                        dialog.show();
                        break;
                        //此种情况表明上一轮查询已经查询到了对应库的末尾，需要更换库，即进入下一个查询类型
                    case 1:
                        List<BriefOrderItem> tempList = (List<BriefOrderItem>) msg.obj;
                        int findNumber = tempList.size();
                        //如果当前状态为TYPE_BOTH，则表示上轮加载已经把ProvideOrder的库加载完了，需要加载FinishedOrder库，即进入TYPE_PROVIDE_OVER状态
                        if(requestType == TYPE_BOTH){
                            //更新当前已加载的provide数量
                            currentNumber_provide += findNumber;
                            //由于上轮加载到ProvideOrder库的底，更新还有多少没加载
                            setItemNumber_remain(getItemNumber_remain() - findNumber);
                            //更新requestType的值
                            setRequestType(TYPE_PROVIDE_OVER);
                            //如果上轮查询不为0，则把加载到的items放入briefOderItems中
                            if(!tempList.isEmpty()){
                                ListOperationUtils.moveBToA(briefOrderItems, tempList);
                                setBriefItemsHasChanged(true);
                            }
                            //以更新后的数据去查询FinishedOrder的库
                            operation.getBriefOrders(user_nickName,currentNumber_finished + 1,
                                    getItemNumber_remain(), operation.TYPE_FINISHED, activity.getApplicationContext(), this);
                            //若当前状态为TYPE_BOTH,则表示上轮加载已经把FinishedOrder加载完了，则全部加载完成
                        }else if(requestType == TYPE_PROVIDE_OVER){
                            currentNumber_finished += findNumber;
                            setItemNumber_remain(getItemNumber_remain() - findNumber);
                            setRequestType(TYPE_BOTH_OVER);
                            if(!tempList.isEmpty()){
                                ListOperationUtils.moveBToA(briefOrderItems, tempList);
                                setBriefItemsHasChanged(true);
                            }
                            //如果briefOderItems加入了新的数据，则通知RecyclerAdapter数据发生更新，并重置相关变量
                            if(isBriefItemsHasChanged()){
                                setBriefItemsHasChanged(false);
                                setItemNumber_remain(0);
                                adapter.setLoadingState(3);
                            }
                            if(layout != null){
                                layout.setRefreshing(false);
                            }
                        }
                        break;
                        //此种状况表示上轮加载加载了所需的全部内容，加载的库并没有见底（或者刚刚好见底但是不知道）
                    case 2:
                        List<BriefOrderItem> fullList = (List<BriefOrderItem>) msg.obj;
                        if(requestType == TYPE_BOTH){
                            currentNumber_provide += getItemNumber_remain();
                        }else if(requestType == TYPE_PROVIDE_OVER){
                            currentNumber_finished += getItemNumber_remain();
                        }
                        setBriefItemsHasChanged(true);
                        ListOperationUtils.moveBToA(briefOrderItems, fullList);
                        setItemNumber_remain(0);
                        setBriefItemsHasChanged(false);
                        adapter.setLoadingState(1);
                        if(layout != null){
                            layout.setRefreshing(false);
                        }
                        break;
                    case 3:
                        //以通知的方法告知查询时出现错误
                        AlertDialog.Builder dialog_error = new AlertDialog.Builder(activity);
                        dialog_error.setTitle(R.string.order_error);
                        dialog_error.setMessage(R.string.order_unknownError);
                        dialog_error.setCancelable(false);
                        dialog_error.setPositiveButton(R.string.order_netError_dialog_ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                adapter.setLoadingState(2);
                            }
                        });
                        dialog_error.show();
                        break;
                    default:
                        break;
                }
            }
        };
        //每次加载时根据上次加载的结果选取适当的加载类型（加载ProvideOrder或者FinishedOrder)
        switch (requestType){
            //ProvideOrder和FinishedOrder均有余量可查询，则优先查询ProvideOrder
            case TYPE_BOTH:
                operation.getBriefOrders(user_nickName,currentNumber_provide + 1,
                        contentNumber, operation.TYPE_PROVIDE, activity.getApplicationContext(), handler);
                break;
                //ProvideOrder无余量，查询FinishedOrder
            case TYPE_PROVIDE_OVER:
                operation.getBriefOrders(user_nickName,currentNumber_finished + 1,
                        contentNumber, operation.TYPE_FINISHED, activity.getApplicationContext(), handler);
                break;
                //两者均无余量，加载完所有的Item
            case TYPE_BOTH_OVER:
                break;
        }
    }



    public boolean isBriefItemsHasChanged() {
        return briefItemsHasChanged;
    }

    public void setBriefItemsHasChanged(boolean briefItemsHasChanged) {
        this.briefItemsHasChanged = briefItemsHasChanged;
    }

    public int getRequestType() {
        return requestType;
    }

    public void setRequestType(int requestType) {
        this.requestType = requestType;
    }

    public int getItemNumber_remain() {
        return ItemNumber_remain;
    }

    public void setItemNumber_remain(int itemNumber_remain) {
        ItemNumber_remain = itemNumber_remain;
    }



}
