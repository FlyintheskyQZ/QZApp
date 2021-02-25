package seu.qz.qzapp.activity.viewmodel;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;

import java.util.List;

import seu.qz.qzapp.R;
import seu.qz.qzapp.activity.OrderSettingActivity;
import seu.qz.qzapp.activity.operation.OrderSettingOperation;
import seu.qz.qzapp.database.LitePalUtils;
import seu.qz.qzapp.entity.AppCustomer;
import seu.qz.qzapp.entity.LOIInstrument;
import seu.qz.qzapp.entity.ProvideOrder;

public class OrderSettingViewModel extends ViewModel {

    //操作类
    private OrderSettingOperation operation = new OrderSettingOperation();

    //当前用户
    //更新ProvideOrder时，先更新本地mainCustomer，待服务端更新成功反馈后，再将该mainCustomer存入SQLLite中，然后返回上一Activity
    private AppCustomer mainCustomer;


    //saler持有的仪器数
    private List<LOIInstrument> instruments;

    //跳转到OrderSettingActivity的目的：
    //1:表示创建新的订单
    //2：表示修改、查看、审核订单
    private int ambition;

    //用于表示订单状态为1、2时，区分修改订单或者审核订单
    //1：表示修改
    //2：表示审核
    private int modifyorCheck;

    //对应的ProvideOrder
    private ProvideOrder order;

    //材料种类，应用于Spinner的ArrayAdapter作为数据源
    private String[] material_types = {"", "Ⅰ型", "Ⅱ型", "Ⅲ型", "Ⅳ型", "Ⅴ型"};





    public void getAmbitionOrder(int order_id, final Context context) {
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
                                ((Activity)context).finish();
                            }
                        });
                        dialog_noNet.show();
                        break;
                    //未查找到订单，可能订单被删除或是状态发生改变，弹出窗口提示，并返回
                    case 1:
                        AlertDialog.Builder dialog_noOrder = new AlertDialog.Builder(context);
                        dialog_noOrder.setTitle(R.string.orderSetting_error_NoOrder_title);
                        dialog_noOrder.setMessage(R.string.orderSetting_error_NoOrder_message);
                        dialog_noOrder.setCancelable(false);
                        dialog_noOrder.setPositiveButton(R.string.orderSetting_error_NoOrder_button, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ((Activity) context).setResult(1);
                                ((Activity)context).finish();
                            }
                        });
                        dialog_noOrder.show();
                        break;
                    case 2:
                        ProvideOrder order = (ProvideOrder) msg.obj;
                        setOrder(order);
                        System.out.println("the order is !!!!!!!!!!!!!!!!!!!!!:" + order);
                        getInstrumentsFromServer(order.getSaler_id(), context);
                        break;
                    case 3:
                        AlertDialog.Builder dialog_exception = new AlertDialog.Builder(context);
                        dialog_exception.setTitle(R.string.orderSetting_error_exception_title);
                        dialog_exception.setMessage(R.string.orderSetting_error_exception_message);
                        dialog_exception.setCancelable(false);
                        dialog_exception.setPositiveButton(R.string.orderSetting_error_exception_button, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ((Activity) context).setResult(2);
                                ((Activity)context).finish();
                            }
                        });
                        dialog_exception.show();
                        break;
                    default:break;
                }
            }
        };
        operation.getAmbitionOrder(order_id, handler, context);
    }


    public void getInstrumentsFromServer(int saler_id, final Context context) {
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
                                ((Activity)context).finish();
                            }
                        });
                        dialog_noNet.show();
                        break;
                    //未查找到订单，可能订单被删除或是状态发生改变，弹出窗口提示，并返回
                    case 1:
                        AlertDialog.Builder dialog_noOrder = new AlertDialog.Builder(context);
                        dialog_noOrder.setTitle(R.string.orderSetting_error_InstrumentLack_title);
                        dialog_noOrder.setMessage(R.string.orderSetting_error_InstrumentLack_message);
                        dialog_noOrder.setCancelable(false);
                        dialog_noOrder.setPositiveButton(R.string.orderSetting_error_InstrumentLack_button, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ((Activity) context).setResult(3);
                                ((Activity)context).finish();
                            }
                        });
                        dialog_noOrder.show();
                        break;
                    case 2:
                        List<LOIInstrument> devices = (List<LOIInstrument>) msg.obj;
                        setInstruments(devices);
                        if(getAmbition() == 2){
                            OrderSettingActivity activity = (OrderSettingActivity) context;
                            activity.showOrderContent();
                        }else if (getAmbition() == 1){
                            OrderSettingActivity activity = (OrderSettingActivity) context;
                            activity.showOrderContent();
                        }
                        break;
                    case 3:
                        AlertDialog.Builder dialog_exception = new AlertDialog.Builder(context);
                        dialog_exception.setTitle(R.string.orderSetting_error_exception_title);
                        dialog_exception.setMessage(R.string.orderSetting_error_exception_message);
                        dialog_exception.setCancelable(false);
                        dialog_exception.setPositiveButton(R.string.orderSetting_error_exception_button, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ((Activity) context).setResult(2);
                                ((Activity)context).finish();
                            }
                        });
                        dialog_exception.show();
                        break;
                    default:break;
                }
            }
        };
        operation.getInstrumentsFromServer(saler_id, handler, context);
    }

    //更新ProviderOrder的方法
    //currentStatus为当前订单状态，statusTobe为将要到达的状态
    public void updateProvideOrder(ProvideOrder order, Context context) {
        if(order == null){
            return;
        }
        //服务器端更新ProviderOrder、LOIInstrument及AppCustomer
        updateOrder(order, context);
    }


    private void updateOrder(final ProvideOrder order, final Context context) {
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
                                ((Activity)context).finish();
                            }
                        });
                        dialog_noNet.show();
                        break;
                    //无返回结果
                    case 1:
                        AlertDialog.Builder dialog_noOrder = new AlertDialog.Builder(context);
                        dialog_noOrder.setTitle(R.string.orderSetting_noReturn_title);
                        dialog_noOrder.setMessage(R.string.orderSetting_noReturn_message);
                        dialog_noOrder.setCancelable(false);
                        dialog_noOrder.setPositiveButton(R.string.orderSetting_noReturn_button, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                        dialog_noOrder.show();
                        break;
                        //更新ProvideOrder成功
                    case 2:
                        LitePalUtils.saveSingleCustomer(mainCustomer);
                        OrderSettingActivity activity = (OrderSettingActivity) context;
                        activity.backToLastActivity(3);
                        break;
                        //更新ProvideOrder失败
                    case 3:
                        AlertDialog.Builder dialog_failed = new AlertDialog.Builder(context);
                        dialog_failed.setTitle(R.string.orderSetting_updateorderfailed_title);
                        dialog_failed.setMessage(R.string.orderSetting_updateorderfailedmessage);
                        dialog_failed.setCancelable(false);
                        dialog_failed.setPositiveButton(R.string.orderSetting_updateorderfailed_button, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                        dialog_failed.show();
                        break;
                        //出现异常
                    case 4:
                        AlertDialog.Builder dialog_exception = new AlertDialog.Builder(context);
                        dialog_exception.setTitle(R.string.orderSetting_error_exception_title);
                        dialog_exception.setMessage(R.string.orderSetting_error_exception_message);
                        dialog_exception.setCancelable(false);
                        dialog_exception.setPositiveButton(R.string.orderSetting_error_exception_button, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ((Activity)context).finish();
                            }
                        });
                        dialog_exception.show();
                        break;
                    default:break;
                }
            }
        };
        operation.updateOrder(order, handler, context);
    }

    public void addNewProvideOrder(ProvideOrder order, final Context context) {
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
                                ((Activity)context).finish();
                            }
                        });
                        dialog_noNet.show();
                        break;
                    //无返回结果
                    case 1:
                        AlertDialog.Builder dialog_noOrder = new AlertDialog.Builder(context);
                        dialog_noOrder.setTitle(R.string.orderSetting_noReturn_title);
                        dialog_noOrder.setMessage(R.string.orderSetting_noReturn_message);
                        dialog_noOrder.setCancelable(false);
                        dialog_noOrder.setPositiveButton(R.string.orderSetting_noReturn_button, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                        dialog_noOrder.show();
                        break;
                    //创建ProvideOrder成功
                    case 2:
                        mainCustomer.setNumberForProvideOrders(mainCustomer.getNumberForProvideOrders() + 1);
                        LitePalUtils.saveSingleCustomer(mainCustomer);
                        OrderSettingActivity activity = (OrderSettingActivity) context;
                        //创建订单成功回到主界面并未对结果返回有任何操作，暂时保留！！！！！！！！！！！！！！！！！！！！！！！
                        activity.backToLastActivity(3);
                        break;
                    //创建ProvideOrder失败
                    case 3:
                        AlertDialog.Builder dialog_failed = new AlertDialog.Builder(context);
                        dialog_failed.setTitle(R.string.orderSetting_updateorderfailed_title);
                        dialog_failed.setMessage(R.string.orderSetting_updateorderfailedmessage);
                        dialog_failed.setCancelable(false);
                        dialog_failed.setPositiveButton(R.string.orderSetting_updateorderfailed_button, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                        dialog_failed.show();
                        break;
                    //出现异常
                    case 4:
                        AlertDialog.Builder dialog_exception = new AlertDialog.Builder(context);
                        dialog_exception.setTitle(R.string.orderSetting_error_exception_title);
                        dialog_exception.setMessage(R.string.orderSetting_error_exception_message);
                        dialog_exception.setCancelable(false);
                        dialog_exception.setPositiveButton(R.string.orderSetting_error_exception_button, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ((Activity)context).finish();
                            }
                        });
                        dialog_exception.show();
                        break;
                    default:break;
                }
            }
        };
        operation.addNewProvideOrder(order, handler, context);
    }


    public List<LOIInstrument> getInstruments() {
        return instruments;
    }

    public void setInstruments(List<LOIInstrument> instruments) {
        this.instruments = instruments;
    }

    public AppCustomer getMainCustomer() {
        return mainCustomer;
    }

    public void setMainCustomer(AppCustomer mainCustomer) {
        this.mainCustomer = mainCustomer;
    }

    public int getAmbition() {
        return ambition;
    }

    public void setAmbition(int ambition) {
        this.ambition = ambition;
    }

    public ProvideOrder getOrder() {
        return order;
    }

    public void setOrder(ProvideOrder order) {
        this.order = order;
    }

    public String[] getMaterial_types() {
        return material_types;
    }

    public void setMaterial_types(String[] material_types) {
        this.material_types = material_types;
    }

    public int getModifyorCheck() {
        return modifyorCheck;
    }

    public void setModifyorCheck(int modifyorCheck) {
        this.modifyorCheck = modifyorCheck;
    }



}
