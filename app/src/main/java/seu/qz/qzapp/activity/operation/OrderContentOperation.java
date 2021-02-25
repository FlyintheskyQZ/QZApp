package seu.qz.qzapp.activity.operation;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import seu.qz.qzapp.R;
import seu.qz.qzapp.database.LitePalUtils;
import seu.qz.qzapp.entity.AppCustomer;
import seu.qz.qzapp.entity.BriefOrderItem;
import seu.qz.qzapp.entity.FinishedOrder;
import seu.qz.qzapp.entity.ProvideOrder;
import seu.qz.qzapp.objectfactory.OkHttpFactory;
import seu.qz.qzapp.utils.GsonUtils;
import seu.qz.qzapp.utils.PropertyUtil;
import seu.qz.qzapp.utils.SystemStateUtil;

public class OrderContentOperation {
    public void getOrderInformation( final BriefOrderItem item, final Context context, final Handler handler) {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                Message message = new Message();
                ProvideOrder provideOrder = null;
                FinishedOrder finishedOrder = null;
                if(!SystemStateUtil.isNetworkConnected(context)){
                    message.what = 0;
                    message.obj = null;
                    handler.sendMessage(message);
                    return;
                }
                try {
                    OkHttpClient client = new OkHttpClient();
                    Map<String, String> settings = new HashMap<>();
                    String url_key = null;
                    if(item.isOk()){
                        url_key = PropertyUtil.getUrl("getFinishedOrderById", context);
                    }else {
                        url_key = PropertyUtil.getUrl("getProvideOrderById", context);
                    }
                    settings.put("order_id", String.valueOf(item.getOrderNumber()));
                    RequestBody requestBody = OkHttpFactory.getRequestBodyWithSettings(settings);
                    Request request = new Request.Builder()
                            .url(url_key)
                            .post(requestBody)
                            .build();
                    Response response = client.newCall(request).execute();
                    String responseBody = response.body().string();
                    System.out.println(responseBody);
                    if(responseBody == null || responseBody.isEmpty()){
                        message.what = 1;
                        message.obj = null;
                    }else{
                        Gson gson = GsonUtils.gson;
                        if(item.isOk()){
                            message.what = 2;
                            finishedOrder = gson.fromJson(responseBody, FinishedOrder.class);
                            message.obj = finishedOrder;
                        }else {
                            message.what = 3;
                            provideOrder = gson.fromJson(responseBody, ProvideOrder.class);
                            message.obj = provideOrder;
                        }

                    }
                }catch (Exception e){
                    e.printStackTrace();
                    message.what = 4;
                    message.obj = null;
                }
                handler.sendMessage(message);
            }
        }, 0);
    }

    public void getCustomerOpposite(final int id, final Context context, final Handler handler){
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                Message message = new Message();
                if(!SystemStateUtil.isNetworkConnected(context)){
                    message.what = 0;
                    message.obj = null;
                    handler.sendMessage(message);
                    return;
                }
                try {
                    OkHttpClient client = new OkHttpClient();
                    Map<String, String> settings = new HashMap<>();
                    String url_key = null;
                    url_key = PropertyUtil.getUrl("getAppCustomerById", context);
                    settings.put("id", String.valueOf(id));
                    RequestBody requestBody = OkHttpFactory.getRequestBodyWithSettings(settings);
                    Request request = new Request.Builder()
                            .url(url_key)
                            .post(requestBody)
                            .build();
                    Response response = client.newCall(request).execute();
                    String responseBody = response.body().string();
                    System.out.println(responseBody);
                    //将查询结果转为BriefOrderItem的List对象，如果查询结果不够,即<contentnumber,则标志为1（返回结果为空则使list为size=0对象），若查询结果够了，则标志为2，并返回list
                    if(responseBody == null || responseBody.isEmpty()){
                        message.what = 5;
                        message.obj = null;
                    }else{
                        Gson gson = GsonUtils.gson;
                       AppCustomer customer = gson.fromJson(responseBody, AppCustomer.class);
                       message.what = 6;
                       message.obj = customer;
                    }
                }catch (Exception e){
                    e.printStackTrace();
                    message.what = 4;
                    message.obj = null;
                }
                handler.sendMessage(message);
            }
        }, 0);
    }


    //通知服务器发送实验报告到用户邮箱
    public void sendReportToEmail(final int id, final Context context, final Handler handler) {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                Message message = new Message();
                if(!SystemStateUtil.isNetworkConnected(context)){
                    message.what = 0;
                    message.obj = null;
                    handler.sendMessage(message);
                    return;
                }
                try {
                    OkHttpClient client = new OkHttpClient();
                    Map<String, String> settings = new HashMap<>();
                    String url_key = null;
                    url_key = PropertyUtil.getUrl("sendPDFToEmail", context);
                    settings.put("orderId", String.valueOf(id));
                    RequestBody requestBody = OkHttpFactory.getRequestBodyWithSettings(settings);
                    Request request = new Request.Builder()
                            .url(url_key)
                            .post(requestBody)
                            .build();
                    Response response = client.newCall(request).execute();
                    String responseBody = response.body().string();
                    System.out.println(responseBody);
                    //将查询结果转为BriefOrderItem的List对象，如果查询结果不够,即<contentnumber,则标志为1（返回结果为空则使list为size=0对象），若查询结果够了，则标志为2，并返回list
                    if(responseBody == null || responseBody.isEmpty()){
                        message.what = 1;
                        message.obj = null;
                    }else{
                        if(responseBody.equals("Success")){
                            message.what = 2;
                        }else {
                            message.what = 1;
                        }
                    }
                }catch (Exception e){
                    e.printStackTrace();
                    message.what = 3;
                    message.obj = null;
                }
                handler.sendMessage(message);
            }
        }, 0);
    }

    //删除ProvideOrder成功，返回result = 2到MainActivity
    public void adjustLocalInformationForSaler(final Activity activity, AppCustomer customer){
        int numForProvideOrders = customer.getNumberForProvideOrders();
        customer.setNumberForProvideOrders(numForProvideOrders - 1);
        LitePalUtils.saveSingleCustomer(customer);
        AlertDialog.Builder dialog = new AlertDialog.Builder(activity);
        dialog.setTitle(R.string.orderContent_deleteSuccess_title);
        dialog.setMessage(R.string.orderContent_deleteSuccess_content);
        dialog.setCancelable(false);
        dialog.setPositiveButton(R.string.orderContent_deleteSuccess_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                activity.setResult(2);
                activity.finish();
            }
        });
        dialog.show();
    }

    public void deleteProvideOrder(final int order_id, final Context context, final Handler handler) {
        //服务器异步删除ProvideOrder并更新Saler和User以及LOIInstrument相关内容
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                Message message = new Message();
                if(!SystemStateUtil.isNetworkConnected(context)){
                    message.what = 0;
                    message.obj = null;
                    handler.sendMessage(message);
                    return;
                }
                try {
                    OkHttpClient client = new OkHttpClient();
                    Map<String, String> settings = new HashMap<>();
                    String url_key = PropertyUtil.getUrl("deleteProvideOrder", context);
                    settings.put("order_id", String.valueOf(order_id));
                    RequestBody requestBody = OkHttpFactory.getRequestBodyWithSettings(settings);
                    Request request = new Request.Builder()
                            .url(url_key)
                            .post(requestBody)
                            .build();
                    Response response = client.newCall(request).execute();
                    String responseBody = response.body().string();
                    System.out.println(responseBody);
                    if(responseBody == null || responseBody.isEmpty()){
                        message.what = 1;
                        message.obj = null;
                    }else{
                        //成功删除
                        if(responseBody.equals("Success")){
                            message.what = 2;
                            message.obj = null;
                            //删除失败
                        }else if(responseBody.equals("Failed")){
                            message.what = 3;
                            message.obj = null;
                        }else {
                            message.what = 1;
                            message.obj = null;
                        }
                    }
                }catch (Exception e){
                    e.printStackTrace();
                    message.what = 4;
                    message.obj = null;
                }
                handler.sendMessage(message);
            }
        }, 0);
    }

    public void drawbackProvideOrder(final int order_id, final Context context, final Handler handler){
        //服务器异步撤销ProvideOrder并更新User
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                Message message = new Message();
                if(!SystemStateUtil.isNetworkConnected(context)){
                    message.what = 0;
                    message.obj = null;
                    handler.sendMessage(message);
                    return;
                }
                try {
                    OkHttpClient client = new OkHttpClient();
                    Map<String, String> settings = new HashMap<>();
                    String url_key = PropertyUtil.getUrl("drawbackProvideOrder", context);
                    settings.put("order_id", String.valueOf(order_id));
                    RequestBody requestBody = OkHttpFactory.getRequestBodyWithSettings(settings);
                    Request request = new Request.Builder()
                            .url(url_key)
                            .post(requestBody)
                            .build();
                    Response response = client.newCall(request).execute();
                    String responseBody = response.body().string();
                    System.out.println(responseBody);
                    if(responseBody == null || responseBody.isEmpty()){
                        message.what = 1;
                        message.obj = null;
                    }else{
                        //成功撤销
                        if(responseBody.equals("Success")){
                            message.what = 2;
                            message.obj = null;
                            //撤销失败
                        }else if(responseBody.equals("Failed")){
                            message.what = 3;
                            message.obj = null;
                        }else {
                            message.what = 1;
                            message.obj = null;
                        }
                    }
                }catch (Exception e){
                    e.printStackTrace();
                    message.what = 4;
                    message.obj = null;
                }
                handler.sendMessage(message);
            }
        }, 0);
    }



    //客户撤销ProvideOrder成功，返回result = 3到MainActivity
    public void adjustLocalInformationForUser(final Activity activity, AppCustomer customer, ProvideOrder order){
        int numForProvideOrders = customer.getNumberForProvideOrders();
        customer.setNumberForProvideOrders(numForProvideOrders - 1);
        customer.setUser_balance(customer.getUser_balance() + order.getDisplay_price());
        LitePalUtils.saveSingleCustomer(customer);
        AlertDialog.Builder dialog = new AlertDialog.Builder(activity);
        dialog.setTitle(R.string.orderContent_deleteSuccess_title);
        dialog.setMessage(R.string.orderContent_deleteSuccess_content);
        dialog.setCancelable(false);
        dialog.setPositiveButton(R.string.orderContent_deleteSuccess_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                activity.setResult(3);
                activity.finish();
            }
        });
        dialog.show();
    }


}
