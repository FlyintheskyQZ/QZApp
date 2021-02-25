package seu.qz.qzapp.activity.viewmodel;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;


import java.io.File;
import java.util.Date;
import java.util.List;

import seu.qz.qzapp.R;
import seu.qz.qzapp.activity.AdoraDisplayActivity;
import seu.qz.qzapp.activity.OrderContentActivity;
import seu.qz.qzapp.activity.OrderSettingActivity;
import seu.qz.qzapp.database.LitePalUtils;
import seu.qz.qzapp.entity.AppCustomer;
import seu.qz.qzapp.entity.BriefOrderItem;
import seu.qz.qzapp.entity.FinishedOrder;
import seu.qz.qzapp.entity.LOIInstrument;
import seu.qz.qzapp.entity.ProvideOrder;
import seu.qz.qzapp.activity.operation.OrderContentOperation;
import seu.qz.qzapp.utils.DateRelatedUtils;

public class OrderContentViewModel extends ViewModel {
    //当前登录的用户信息
    private AppCustomer customer_owner;
    //用户类型为1，商家类型为2
    public int CUSTOMER_TYPE_USER = 1;
    public int CUSTOMER_TYPE_SALER = 2;
    //当前订单的对方信息
    private AppCustomer customer_opposite;
    //用户身份分类：false（普通用户），true（商家）
    private boolean CustomerClassification;
    //订单的简信息
    private BriefOrderItem item;
    //当前order种类，二取一，另一个为null
    private ProvideOrder provideOrder;
    private FinishedOrder finishedOrder;
    //数据操作类
    private OrderContentOperation orderContentOperation = new OrderContentOperation();
    //用于记录当前查询到的order是ProvideOrder（代号3）还是FinishedOrder(代号2）
    private int provide_or_finished = 1;
    //通过判断是否是进入OrderContentActivity后第一次点击pdf图标按钮来判断当前存在的pdf文件是暂停下载中（false）的pdf还是已经下载完存在的pdf(true)
    private boolean isPDFDownloaded = true;
    //判断当前是否正在下载PDF
    private boolean isDownloadingPDF = false;



    //异步获取订单详细信息
    public void getOrderInformation(final Context context) {
         final Handler handler = new Handler() {
            @Override
            public void handleMessage(@NonNull Message msg) {
                switch (msg.what){
                    //网络未连接，弹出窗口提示，直接跳转到网络设置界面
                    case 0:
                        AlertDialog.Builder dialog_noNet = new AlertDialog.Builder(context);
                        dialog_noNet.setTitle(R.string.order_error);
                        dialog_noNet.setMessage(R.string.order_netError_content);
                        dialog_noNet.setCancelable(false);
                        dialog_noNet.setPositiveButton(R.string.order_netError_dialog_ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(Settings.ACTION_SETTINGS);
                                context.startActivity(intent);
                                ((Activity)context).onBackPressed();
                            }
                        });
                        dialog_noNet.setNegativeButton(R.string.order_netError_dialog_cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ((Activity)context).finish();
                            }
                        });
                        dialog_noNet.show();
                        break;
                        //未查找到订单，可能订单被删除或是状态发生改变，弹出窗口提示，并返回MainActivity进行列表全部刷新
                    case 1:
                        AlertDialog.Builder dialog_noOrder = new AlertDialog.Builder(context);
                        dialog_noOrder.setTitle(R.string.orderContent_noOrder_title);
                        dialog_noOrder.setMessage(R.string.orderContent_noOrder_content);
                        dialog_noOrder.setCancelable(false);
                        dialog_noOrder.setPositiveButton(R.string.orderContent_noOrder_ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ((Activity) context).setResult(1);
                                ((Activity)context).finish();
                            }
                        });
                        dialog_noOrder.setNegativeButton(R.string.order_netError_dialog_cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ((Activity) context).setResult(1);
                                ((Activity)context).finish();
                            }
                        });
                        dialog_noOrder.show();
                        break;
                        //找到对应order且为FinishedOrder，则继续查找合作的商家（或者是用户）的信息
                    case 2:
                        finishedOrder = (FinishedOrder) msg.obj;
                        setProvide_or_finished(2);
                        int opposite_id_finished = 0;
                        if(isCustomerClassification()){
                            opposite_id_finished = finishedOrder.getSaler_id();
                        }else {
                            opposite_id_finished = finishedOrder.getUser_id();
                        }
                        orderContentOperation.getCustomerOpposite(opposite_id_finished, context, this);
                        break;
                        //找到对应的Order且是ProvideOrder，则继续查找合作的商家（或者是用户）的信息
                    case 3:
                        provideOrder = (ProvideOrder) msg.obj;
                        setProvide_or_finished(3);
                        int opposite_id_provide = 0;
                        if(isCustomerClassification()){
                            opposite_id_provide = provideOrder.getSaler_id();
                        }else {
                            if(provideOrder.getUser_id() == null){
                                //此处需要对商户登录时，查看未被订购的订单的区域3中的信息配置！！！！！！！！！！！！！！！！！！！！！
                                break;
                            }
                            opposite_id_provide = provideOrder.getUser_id();
                        }
                        orderContentOperation.getCustomerOpposite(opposite_id_provide, context, this);
                        break;

                    case 4:
                        //以通知的方法告知查询时出现错误
                        AlertDialog.Builder dialog_error = new AlertDialog.Builder(context);
                        dialog_error.setTitle(R.string.order_error);
                        dialog_error.setMessage(R.string.order_unknownError);
                        dialog_error.setCancelable(false);
                        dialog_error.setPositiveButton(R.string.order_netError_dialog_ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ((Activity)context).finish();
                            }
                        });
                        dialog_error.show();
                        break;
                        //查找合作对象信息时没找到
                    case 5:
                        AlertDialog.Builder dialog_noOpposite = new AlertDialog.Builder(context);
                        dialog_noOpposite.setTitle(R.string.orderContent_noOppositeCustomer_title);
                        dialog_noOpposite.setMessage(R.string.orderContent_noOppositeCustomer_content);
                        dialog_noOpposite.setCancelable(false);
                        dialog_noOpposite.setPositiveButton(R.string.order_netError_dialog_ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ((Activity)context).finish();
                            }
                        });
                        dialog_noOpposite.show();
                        break;
                        //查找到对应用户，则本轮信息查询由order到customer完成，进行显示(显示需要区分当前用户是买家还是商家，以及订单是未完成订单还是已完成订单）
                    case 6:
                        customer_opposite = (AppCustomer) msg.obj;
                        if(getProvide_or_finished() == 2){
                            if(customer_owner.getAuthority_level() == 1){
                                displayFinishedActivityView((Activity) context, finishedOrder, CUSTOMER_TYPE_USER);
                            }else {
                                displayFinishedActivityView((Activity) context, finishedOrder, CUSTOMER_TYPE_SALER);
                            }
                        }else {
                            if(customer_owner.getAuthority_level() == 1){
                                displayProvideActivityView((Activity) context, provideOrder, CUSTOMER_TYPE_USER);
                            }else {
                                displayProvideActivityView((Activity) context, provideOrder, CUSTOMER_TYPE_SALER);
                            }
                        }
                        setProvide_or_finished(1);
                        break;
                    default:break;
                }

            }
        };
        orderContentOperation.getOrderInformation(item, context, handler);
    }

    public void displayFinishedActivityView(final Activity activity, FinishedOrder order, int customerType){
        final OrderContentActivity newActivity = (OrderContentActivity) activity;
        newActivity.getOrderContent_stateIcon().setImageResource(R.mipmap.ic_ordercontent_state_finished);
        newActivity.getOrderContent_orderState().setText("已完成");
        newActivity.getOrderContent_merchant_nameAndPhone().setText(customer_opposite.getUser_registerName() + " " + customer_opposite.getPhoneNumber());
        newActivity.getOrderContent_orderNumber().setText("订单号：" + order.getPlaced_orderNumber());
        newActivity.getOrderContent_orderPlaced_time().setText("下单时间：" + DateRelatedUtils.FormatOutputByDate(order.getOrder_placed(), DateRelatedUtils.TYPE_ORIGINAL));
        newActivity.getOrderContent_rentTime_begin().setText("租赁起始时间：" + DateRelatedUtils.FormatOutputByDate(order.getRentTime_begin(), DateRelatedUtils.TYPE_ORIGINAL));
        newActivity.getOrderContent_rentTime_end().setText("租赁结束时间：" + DateRelatedUtils.FormatOutputByDate(order.getRentTime_end(), DateRelatedUtils.TYPE_ORIGINAL));
        newActivity.getOrderContent_price().setText("订单金额： ￥" + order.getDeal_price());
        //newActivity.getOrderContent_orderModifyButton().setVisibility(View.GONE);
        newActivity.getOrderContent_noData().setVisibility(View.INVISIBLE);
        newActivity.getOrderContent_approvalError().setVisibility(View.GONE);
        newActivity.getOrderContent_orderResult().setText("氧指数为：" + order.getResult_data() + "%");
        newActivity.getOrderContent_materialName().setText("材料名称:" + order.getMaterialName());
        newActivity.getOrderContent_materialType().setText("材料类型：" + order.getMaterialType() + "型");
        newActivity.getOrderContent_operatorName().setText("操作人员：" + order.getOperator_name());
        newActivity.getOrderContent_pdfButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OrderContentActivity originalActivity = (OrderContentActivity) activity;
                //判断当前是否正在下载PDF：
                //1.如果是则弹框询问是继续下载还是取消
                //2.如果没在下载则再判断
                if(isDownloadingPDF){
                    //暂停或取消下载PDF
                    originalActivity.startBinderTask(1);
                    isDownloadingPDF = false;
                }else {
                    //判断pdf文件是否存在，如果存在：
                    // 1.如果isPDFDownloaded为true，则表示是进入OrderContentActivity后第一次点击该按钮，证明该pdf已经下载好了；
                    // 2.如果isPDFDownloaded为false，则表示进入OrderContentActivity开启后不是第一次点击该按钮，则为断点下载
                    if(isPDFExisted(activity)){
                        Log.d("哈哈哈", "下载了");
                        //pdf已下载好，则调用其他pdf阅读软件打开
                        if(isPDFDownloaded){
                            ((OrderContentActivity) activity).startBinderTask(3);
                        }else {
                            //继续下载PDF
                            originalActivity.startBinderTask(2);
                            isDownloadingPDF = true;
                            isPDFDownloaded = false;
                        }
                    }else {
                        Log.d("哈哈哈", "没下载");
                        //开始下载PDF
                        originalActivity.startBinderTask(0);
                        isDownloadingPDF = true;
                        isPDFDownloaded = false;
                    }
                }
            }
        });
        newActivity.getOrderContent_pdf_email().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder send_email = new AlertDialog.Builder(newActivity);
                send_email.setTitle("发送Email");
                send_email.setMessage("将当前实验结果报告发送到我的邮箱！");
                send_email.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ProgressBar bar = newActivity.getOrderContent_sendingEmail();
                        bar.setVisibility(View.VISIBLE);
                        sendReportToEmail(bar, newActivity);
                    }
                });
                send_email.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                send_email.setCancelable(false);
                send_email.show();
            }
        });
        newActivity.getOrderContent_icon4().setVisibility(View.VISIBLE);
        newActivity.getOrderContent_place().setVisibility(View.GONE);
        newActivity.getOrderContent_place_text().setVisibility(View.GONE);
        newActivity.getOrderContent_cancel().setVisibility(View.GONE);
        newActivity.getOrderContent_cancel_text().setVisibility(View.GONE);
        if(customerType == CUSTOMER_TYPE_USER){
            newActivity.getOrderContent_factoryAndMachine().setText(order.getFactory_name() + order.getDevice_orderForSaler() + "号机");
            newActivity.getOrderContent_merchant_location().setText("地址：" + order.getDevice_address());
            newActivity.getOrderContent_pdf_email().setVisibility(View.VISIBLE);
        }else {
            newActivity.getOrderContent_factoryAndMachine().setText("");
            newActivity.getOrderContent_merchant_location().setText("");
            newActivity.getOrderContent_pdf_email().setVisibility(View.GONE);
        }

    }

    private void sendReportToEmail(final ProgressBar bar, final Context context) {
        Handler handler = new Handler(){
            @Override
            public void handleMessage(@NonNull Message msg) {
                switch (msg.what){
                    case 0:
                        if(bar != null){
                            bar.setVisibility(View.GONE);
                        }
                        AlertDialog.Builder dialog_netError = new AlertDialog.Builder(context);
                        dialog_netError.setTitle(R.string.order_error);
                        dialog_netError.setMessage(R.string.order_netError_content);
                        dialog_netError.setCancelable(false);
                        dialog_netError.setPositiveButton(R.string.order_netError_dialog_ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(Settings.ACTION_SETTINGS);
                                context.startActivity(intent);
                            }
                        });
                        dialog_netError.setNegativeButton(R.string.order_netError_dialog_cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });
                        dialog_netError.show();
                        break;
                    //返回为空，稍后再试
                    case 1:
                        if(bar != null){
                            bar.setVisibility(View.GONE);
                        }
                        AlertDialog.Builder dialog_empty = new AlertDialog.Builder(context);
                        dialog_empty.setTitle(R.string.passwordsetting_updateError_empty_title);
                        dialog_empty.setMessage(R.string.passwordsetting_updateError_empty_message);
                        dialog_empty.setCancelable(false);
                        dialog_empty.setPositiveButton(R.string.order_netError_dialog_ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });
                        dialog_empty.show();
                        break;
                    //发送Email报告成功
                    case 2:
                        if(bar != null){
                            bar.setVisibility(View.GONE);
                        }
                        AlertDialog.Builder send_success = new AlertDialog.Builder(context);
                        send_success.setTitle(R.string.orderContent_sendEmailSuccess_title);
                        send_success.setMessage(R.string.orderContent_sendEmailSuccess_message);
                        send_success.setPositiveButton(R.string.orderContent_sendEmailSuccess_positive, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });
                        send_success.setCancelable(false);
                        send_success.show();
                        break;
                    case 3:
                        if(bar != null){
                            bar.setVisibility(View.GONE);
                        }
                        //以通知的方法告知查询时出现错误
                        AlertDialog.Builder dialog_error = new AlertDialog.Builder(context);
                        dialog_error.setTitle(R.string.order_error);
                        dialog_error.setMessage(R.string.order_unknownError);
                        dialog_error.setCancelable(false);
                        dialog_error.setPositiveButton(R.string.order_netError_dialog_ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });
                        dialog_error.show();
                        break;
                    default:break;
                }
            }
        };
        orderContentOperation.sendReportToEmail(finishedOrder.getOrder_id(), context, handler);
    }

    public void displayProvideActivityView(final Activity activity, final ProvideOrder order, int customerType){
        final OrderContentActivity newActivity = (OrderContentActivity) activity;
        //区间一部分内容（图标+合作方的姓名+手机号+机器+地址）、区间二的全部内容以及区间三的部分内容（实验数据、材料属性、操作人员、pfd图标）等不需随着订单状态而变化的部分
        newActivity.getOrderContent_stateIcon().setImageResource(R.mipmap.ic_ordercontent_state_notfinished);
        newActivity.getOrderContent_merchant_nameAndPhone().setText(customer_opposite.getUser_registerName() + " " + customer_opposite.getPhoneNumber());
        if(customerType == CUSTOMER_TYPE_USER){
            newActivity.getOrderContent_factoryAndMachine().setText(order.getFactory_name() + order.getDevice_orderForSaler() + "号机");
            newActivity.getOrderContent_merchant_location().setText("地址：" + order.getDevice_address());
        }else {
            newActivity.getOrderContent_factoryAndMachine().setText("");
            newActivity.getOrderContent_merchant_location().setText("");
        }
        newActivity.getOrderContent_orderNumber().setText("订单号：" + (order.getPlaced_orderNumber() == null ? "" : order.getPlaced_orderNumber()));
        newActivity.getOrderContent_orderPlaced_time().setText("下单时间：" + DateRelatedUtils.FormatOutputByDate(order.getOrder_placed(), DateRelatedUtils.TYPE_ORIGINAL));
        newActivity.getOrderContent_rentTime_begin().setText("租赁起始时间：" + DateRelatedUtils.FormatOutputByDate(order.getRentTime_begin(), DateRelatedUtils.TYPE_ORIGINAL));
        newActivity.getOrderContent_rentTime_end().setText("租赁结束时间：" + DateRelatedUtils.FormatOutputByDate(order.getRentTime_end(), DateRelatedUtils.TYPE_ORIGINAL));
        newActivity.getOrderContent_price().setText("订单金额：￥" + order.getDisplay_price());
        newActivity.getOrderContent_icon4().setVisibility(View.GONE);
        newActivity.getOrderContent_orderResult().setVisibility(View.INVISIBLE);
        newActivity.getOrderContent_materialName().setVisibility(View.INVISIBLE);
        newActivity.getOrderContent_materialType().setVisibility(View.INVISIBLE);
        newActivity.getOrderContent_operatorName().setVisibility(View.INVISIBLE);
        newActivity.getOrderContent_pdfButton().setVisibility(View.GONE);


        //需要根据情况进行改变的共有六个UI组件：
        //1.错误提示：当买家的材料信息输入有问题被商家驳回需要修改时的提示，同时对商家显示"等待买家修改"——OrderContent_approvalError
        //2.空数据提示：当订单正式建立合作关系时，由于尚未进行实验，此时买家和卖家无法更改信息，也无法取消订单，实验数据区（即区间三）应提示空数据——OrderContent_noData
        //3.两个按钮，分别代表积极意义（构建订单，修改订单内容，查看内容）和消极意义（取消订单）——OrderContent_place(积极）、OrderContent_cancel（消极）
        //4.两个TextView，分别对应两个按钮的文字提示——OrderContent_place_text(积极）、OrderContent_cancel_text（消极）



        //还需要对响应控件的点击事件进行注册！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！
        switch (order.getOrder_confirmed()){
            //订单刚创立，未被下单
            case 0:
                newActivity.getOrderContent_orderState().setText("未被下单");
                //商家页面
                if(customerType == CUSTOMER_TYPE_SALER){
                    //OrderContent_place图标使用默认展示，文字显示"修改"
                    newActivity.getOrderContent_place_text().setText("修改订单");
                    //OrderContent_cancel图标使用默认展示，文字显示"撤销"
                    newActivity.getOrderContent_cancel_text().setText("撤销订单");
                    //未被下单时无客户信息
                    newActivity.getOrderContent_merchant_nameAndPhone().setText("");
                    //设置修改订单按钮的监听事件
                    newActivity.getOrderContent_place().setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(newActivity, OrderSettingActivity.class);
                            intent.putExtra("username", customer_owner.getUser_nickName());
                            intent.putExtra("order_id", provideOrder.getOrder_id());
                            //ambition:为1表示创建新订单，为2表示查看、修改、审核订单
                            intent.putExtra("ambition", 2);
                            //modifyorCheck：为1表示修改订单，为2表示审核订单
                            intent.putExtra("modifyorCheck", 1);
                            //requestCode：为2表示创建新订单，为1表示查看、修改、审核订单
                            newActivity.startActivityForResult(intent, 1);
                        }
                    });
                    //设置撤销订单按钮的监听事件
                    newActivity.getOrderContent_cancel().setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            AlertDialog.Builder dialog_cancel = new AlertDialog.Builder(activity);
                            dialog_cancel.setTitle(R.string.orderContent_saler_cancelOrder_title);
                            dialog_cancel.setMessage(R.string.orderContent_saler_cancelOrder_message);
                            dialog_cancel.setCancelable(false);
                            dialog_cancel.setPositiveButton(R.string.orderContent_saler_cancelOrder_yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    deleteProvideOrder(order.getOrder_id(), activity);
                                }
                            });
                            dialog_cancel.setNegativeButton(R.string.orderContent_saler_cancelOrder_no, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            });
                            dialog_cancel.show();
                        }
                    });
                    //客户界面
                }else {
                    //OrderContent_place图标使用默认展示，文字显示"订购并填写信息"
                    newActivity.getOrderContent_place_text().setText("订购并填写信息");
                    newActivity.getOrderContent_place().setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(newActivity, OrderSettingActivity.class);
                            intent.putExtra("username", customer_owner.getUser_nickName());
                            intent.putExtra("order_id", provideOrder.getOrder_id());
                            intent.putExtra("ambition", 2);
                            intent.putExtra("modifyorCheck", 1);
                            newActivity.startActivityForResult(intent, 1);
                        }
                    });
                    //OrderContent_cancel图标及文字均撤销
                    newActivity.getOrderContent_cancel().setVisibility(View.GONE);
                    newActivity.getOrderContent_cancel_text().setVisibility(View.GONE);

                }
                //无空数据提示
                newActivity.getOrderContent_noData().setVisibility(View.GONE);
                //无错误数据提示
                newActivity.getOrderContent_approvalError().setVisibility(View.GONE);
                break;
            case 1:
                newActivity.getOrderContent_orderState().setText("等待商家确认");
                if(customerType == CUSTOMER_TYPE_SALER){
                    //OrderContent_place图标使用默认展示，文字显示"修改"
                    newActivity.getOrderContent_place_text().setText("修改订单");
                    newActivity.getOrderContent_place_text().setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(newActivity, OrderSettingActivity.class);
                            intent.putExtra("username", customer_owner.getUser_nickName());
                            intent.putExtra("order_id", provideOrder.getOrder_id());
                            intent.putExtra("ambition", 2);
                            intent.putExtra("modifyorCheck", 1);
                            newActivity.startActivityForResult(intent, 1);
                        }
                    });
                    //OrderContent_cancel使用审核图标，文字显示"审核订单"
                    newActivity.getOrderContent_cancel().setImageResource(R.drawable.ic_ordercontent_approval);
                    newActivity.getOrderContent_cancel_text().setText("审核订单");
                    newActivity.getOrderContent_cancel_text().setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(newActivity, OrderSettingActivity.class);
                            intent.putExtra("username", customer_owner.getUser_nickName());
                            intent.putExtra("order_id", provideOrder.getOrder_id());
                            intent.putExtra("ambition", 2);
                            intent.putExtra("modifyorCheck", 2);
                            newActivity.startActivityForResult(intent, 1);
                        }
                    });
                }else {
                    //OrderContent_place图标使用默认展示，文字显示"修改订单"
                    newActivity.getOrderContent_place_text().setText("修改资料");
                    newActivity.getOrderContent_place_text().setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(newActivity, OrderSettingActivity.class);
                            intent.putExtra("username", customer_owner.getUser_nickName());
                            intent.putExtra("order_id", provideOrder.getOrder_id());
                            intent.putExtra("ambition", 2);
                            intent.putExtra("modifyorCheck", 1);
                            newActivity.startActivityForResult(intent, 1);
                        }
                    });
                    //OrderContent_cancel使用默认图标，文字显示"撤销订单"
                    newActivity.getOrderContent_cancel_text().setText("撤销订购");
                    newActivity.getOrderContent_cancel_text().setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            AlertDialog.Builder dialog_drawback = new AlertDialog.Builder(activity);
                            dialog_drawback.setTitle(R.string.orderContent_saler_cancelOrder_title);
                            dialog_drawback.setMessage(R.string.orderContent_saler_cancelOrder_message);
                            dialog_drawback.setCancelable(false);
                            dialog_drawback.setPositiveButton(R.string.orderContent_saler_cancelOrder_yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    drawbackProvideOrder(order.getOrder_id(), activity);
                                }
                            });
                            dialog_drawback.setNegativeButton(R.string.orderContent_saler_cancelOrder_no, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            });
                            dialog_drawback.show();
                        }
                    });
                }
                //无空数据提示
                newActivity.getOrderContent_noData().setVisibility(View.GONE);
                //无错误数据提示
                newActivity.getOrderContent_approvalError().setVisibility(View.GONE);
                break;
            case 2:
                newActivity.getOrderContent_orderState().setText("资料有误");
                if(customerType == CUSTOMER_TYPE_SALER){
                    //OrderContent_place图标使用默认展示，文字显示"修改"
                    newActivity.getOrderContent_place_text().setText("修改订单");
                    newActivity.getOrderContent_place_text().setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(newActivity, OrderSettingActivity.class);
                            intent.putExtra("username", customer_owner.getUser_nickName());
                            intent.putExtra("order_id", provideOrder.getOrder_id());
                            intent.putExtra("ambition", 2);
                            intent.putExtra("modifyorCheck", 1);
                            newActivity.startActivityForResult(intent, 1);
                        }
                    });
                    //OrderContent_cancel使用审核图标，文字显示"审核订单"
                    newActivity.getOrderContent_cancel().setImageResource(R.drawable.ic_ordercontent_approval);
                    newActivity.getOrderContent_cancel_text().setText("审核订单");
                    newActivity.getOrderContent_cancel_text().setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(newActivity, OrderSettingActivity.class);
                            intent.putExtra("username", customer_owner.getUser_nickName());
                            intent.putExtra("order_id", provideOrder.getOrder_id());
                            intent.putExtra("ambition", 2);
                            intent.putExtra("modifyorCheck", 2);
                            newActivity.startActivityForResult(intent, 1);
                        }
                    });
                    //错误数据提示卖家"等待买家修改内容"
                    newActivity.getOrderContent_approvalError().setText("等待买家修改信息");
                }else {
                    //OrderContent_place图标使用默认展示，文字显示"订购并填写信息"
                    newActivity.getOrderContent_place_text().setText("修改信息");
                    newActivity.getOrderContent_place_text().setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(newActivity, OrderSettingActivity.class);
                            intent.putExtra("username", customer_owner.getUser_nickName());
                            intent.putExtra("order_id", provideOrder.getOrder_id());
                            intent.putExtra("ambition", 2);
                            intent.putExtra("modifyorCheck", 1);
                            newActivity.startActivityForResult(intent, 1);
                        }
                    });
                    //OrderContent_cancel显示默认图标，文字显示"撤销订单"
                    newActivity.getOrderContent_cancel_text().setText("撤销订单");
                    newActivity.getOrderContent_cancel_text().setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            AlertDialog.Builder dialog_drawback = new AlertDialog.Builder(activity);
                            dialog_drawback.setTitle(R.string.orderContent_saler_cancelOrder_title);
                            dialog_drawback.setMessage(R.string.orderContent_saler_cancelOrder_message);
                            dialog_drawback.setCancelable(false);
                            dialog_drawback.setPositiveButton(R.string.orderContent_saler_cancelOrder_yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    drawbackProvideOrder(order.getOrder_id(), activity);
                                }
                            });
                            dialog_drawback.setNegativeButton(R.string.orderContent_saler_cancelOrder_no, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            });
                            dialog_drawback.show();
                        }
                    });
                    //错误数据提示买家"需要修改数据"
                    newActivity.getOrderContent_approvalError().setText("需要修改数据");
                }
                //无空数据提示
                newActivity.getOrderContent_noData().setVisibility(View.GONE);
                break;
            case 3:
                newActivity.getOrderContent_orderState().setText("等待实验");
                //OrderContent_place图标使用默认展示，文字显示"订购并填写信息"
                newActivity.getOrderContent_place().setVisibility(View.GONE);
                newActivity.getOrderContent_place_text().setVisibility(View.GONE);
                //OrderContent_cancel图标及文字均撤销
                newActivity.getOrderContent_cancel().setVisibility(View.GONE);
                newActivity.getOrderContent_cancel_text().setVisibility(View.GONE);
                //无空数据提示
                Date rentBegin = provideOrder.getRentTime_begin();
                Date now_time = new Date(System.currentTimeMillis());
                //实验开始后可以开启视频观看
                if(DateRelatedUtils.nearTime(now_time, rentBegin, 2)){
                    newActivity.getOrderContent_noData().setText("实验即将开始");
                }else {
                    newActivity.getOrderContent_noData().setText("实验未完成，空数据");
                }
                if(rentBegin.getTime() < now_time.getTime()){
                    newActivity.getOrderContent_noData().setText("实验已开始，可进入视频通讯界面观看实验进程");
                    //视频观看按钮可见
                    newActivity.getOrderContent_video().setVisibility(View.VISIBLE);
                    newActivity.getOrderContent_video().setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(newActivity, AdoraDisplayActivity.class);
                            intent.putExtra("order", provideOrder);
                            newActivity.startActivity(intent);
                        }
                    });
                }else {
                    newActivity.getOrderContent_video().setVisibility(View.GONE);
                }
                //无错误数据提示
                newActivity.getOrderContent_approvalError().setVisibility(View.GONE);
                break;
            default:break;
        }

    }

    //客户撤销订单
    private void drawbackProvideOrder(Integer order_id, final Context context) {
        Handler handler = new Handler(){
            @Override
            public void handleMessage(@NonNull Message msg) {
                switch (msg.what){
                    //网络未连接，弹出窗口提示，直接跳转到网络设置界面
                    case 0:
                        AlertDialog.Builder dialog_noNet = new AlertDialog.Builder(context);
                        dialog_noNet.setTitle(R.string.order_error);
                        dialog_noNet.setMessage(R.string.order_netError_content);
                        dialog_noNet.setCancelable(false);
                        dialog_noNet.setPositiveButton(R.string.order_netError_dialog_ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(Settings.ACTION_SETTINGS);
                                context.startActivity(intent);
                                ((Activity)context).onBackPressed();
                            }
                        });
                        dialog_noNet.setNegativeButton(R.string.order_netError_dialog_cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ((Activity)context).onBackPressed();
                            }
                        });
                        dialog_noNet.show();
                        break;
                    //服务端未执行删除动作，或没有反馈
                    case 1:
                        AlertDialog.Builder dialog_noAction = new AlertDialog.Builder(context);
                        dialog_noAction.setTitle(R.string.orderContent_noAction_title);
                        dialog_noAction.setMessage(R.string.orderContent_noAction_content);
                        dialog_noAction.setCancelable(false);
                        dialog_noAction.setPositiveButton(R.string.orderContent_noAction_ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                        dialog_noAction.show();
                        break;
                    case 2:
                        orderContentOperation.adjustLocalInformationForUser((Activity) context, customer_owner, provideOrder);
                        break;
                    //订单删除失败，状态可能已经发生改变（被订购）
                    case 3:
                        AlertDialog.Builder dialog_Failed = new AlertDialog.Builder(context);
                        dialog_Failed.setTitle(R.string.orderContent_deleteFailed_title);
                        dialog_Failed.setMessage(R.string.orderContent_deleteFailed_content);
                        dialog_Failed.setCancelable(false);
                        dialog_Failed.setPositiveButton(R.string.orderContent_deleteFailed_ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ((Activity)context).onBackPressed();
                            }
                        });
                        dialog_Failed.show();
                        break;
                    case 4:
                        //以通知的方法告知查询时出现错误
                        AlertDialog.Builder dialog_error = new AlertDialog.Builder(context);
                        dialog_error.setTitle(R.string.order_error);
                        dialog_error.setMessage(R.string.order_unknownError);
                        dialog_error.setCancelable(false);
                        dialog_error.setPositiveButton(R.string.order_netError_dialog_ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ((Activity)context).finish();
                            }
                        });
                        dialog_error.show();
                        break;
                    default:break;
                }

            }
        };
        orderContentOperation.drawbackProvideOrder(order_id, context, handler);
    }

    //商家删除订单
    private void deleteProvideOrder(int order_id, final Context context) {
        Handler handler = new Handler(){
            @Override
            public void handleMessage(@NonNull Message msg) {
                switch (msg.what){
                    //网络未连接，弹出窗口提示，直接跳转到网络设置界面
                    case 0:
                        AlertDialog.Builder dialog_noNet = new AlertDialog.Builder(context);
                        dialog_noNet.setTitle(R.string.order_error);
                        dialog_noNet.setMessage(R.string.order_netError_content);
                        dialog_noNet.setCancelable(false);
                        dialog_noNet.setPositiveButton(R.string.order_netError_dialog_ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(Settings.ACTION_SETTINGS);
                                context.startActivity(intent);
                                ((Activity)context).onBackPressed();
                            }
                        });
                        dialog_noNet.setNegativeButton(R.string.order_netError_dialog_cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ((Activity)context).onBackPressed();
                            }
                        });
                        dialog_noNet.show();
                        break;
                        //服务端未执行删除动作，或没有反馈
                    case 1:
                        AlertDialog.Builder dialog_noAction = new AlertDialog.Builder(context);
                        dialog_noAction.setTitle(R.string.orderContent_noAction_title);
                        dialog_noAction.setMessage(R.string.orderContent_noAction_content);
                        dialog_noAction.setCancelable(false);
                        dialog_noAction.setPositiveButton(R.string.orderContent_noAction_ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                        dialog_noAction.show();
                        break;
                    case 2:
                        orderContentOperation.adjustLocalInformationForSaler((Activity) context, customer_owner);
                        break;
                        //订单删除失败，状态可能已经发生改变（被订购）
                    case 3:
                        AlertDialog.Builder dialog_Failed = new AlertDialog.Builder(context);
                        dialog_Failed.setTitle(R.string.orderContent_deleteFailed_title);
                        dialog_Failed.setMessage(R.string.orderContent_deleteFailed_content);
                        dialog_Failed.setCancelable(false);
                        dialog_Failed.setPositiveButton(R.string.orderContent_deleteFailed_ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ((Activity)context).onBackPressed();
                            }
                        });
                        dialog_Failed.show();
                        break;
                    case 4:
                        //以通知的方法告知查询时出现错误
                        AlertDialog.Builder dialog_error = new AlertDialog.Builder(context);
                        dialog_error.setTitle(R.string.order_error);
                        dialog_error.setMessage(R.string.order_unknownError);
                        dialog_error.setCancelable(false);
                        dialog_error.setPositiveButton(R.string.order_netError_dialog_ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ((Activity)context).finish();
                            }
                        });
                        dialog_error.show();
                        break;
                    default:break;
                }

            }
        };
        orderContentOperation.deleteProvideOrder(order_id, context, handler);
    }

    public boolean isPDFExisted(Context context){
        if(finishedOrder != null){
            String fileName = finishedOrder.getUser_name() + "-" + finishedOrder.getOrder_id().toString();
            String directory =context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).getPath();
            File file = new File(directory + "/" + fileName);
            //若该文件存在，则表示当前为接续上次下载
            return file.exists();
        }
        return false;
    }



    public void refresh(Context context) {
        isPDFDownloaded = true;
        isDownloadingPDF = false;
        customer_owner = LitePalUtils.getSingleCustomer(customer_owner.getUser_nickName());
        getOrderInformation(context);
    }

    public boolean isPDFDownloaded() {
        return isPDFDownloaded;
    }

    public void setPDFDownloaded(boolean PDFDownloaded) {
        isPDFDownloaded = PDFDownloaded;
    }

    public boolean isDownloadingPDF() {
        return isDownloadingPDF;
    }

    public void setDownloadingPDF(boolean downloadingPDF) {
        isDownloadingPDF = downloadingPDF;
    }

    public int getProvide_or_finished() {
        return provide_or_finished;
    }

    public void setProvide_or_finished(int provide_or_finished) {
        this.provide_or_finished = provide_or_finished;
    }

    public ProvideOrder getProvideOrder() {
        return provideOrder;
    }

    public BriefOrderItem getItem() {
        return item;
    }

    public void setItem(BriefOrderItem item) {
        this.item = item;
    }

    public void setProvideOrder(ProvideOrder provideOrder) {
        this.provideOrder = provideOrder;
    }

    public FinishedOrder getFinishedOrder() {
        return finishedOrder;
    }

    public void setFinishedOrder(FinishedOrder finishedOrder) {
        this.finishedOrder = finishedOrder;
    }

    public AppCustomer getCustomer_owner() {
        return customer_owner;
    }

    public void setCustomer_owner(AppCustomer customer_owner) {
        this.customer_owner = customer_owner;
    }

    public AppCustomer getCustomer_opposite() {
        return customer_opposite;
    }

    public void setCustomer_opposite(AppCustomer customer_opposite) {
        this.customer_opposite = customer_opposite;
    }

    public boolean isCustomerClassification() {
        return CustomerClassification;
    }

    public void setCustomerClassification(boolean customerClassification) {
        CustomerClassification = customerClassification;
    }



}
