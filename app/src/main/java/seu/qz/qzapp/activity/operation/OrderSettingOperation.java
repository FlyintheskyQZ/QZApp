package seu.qz.qzapp.activity.operation;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import seu.qz.qzapp.entity.AppCustomer;
import seu.qz.qzapp.entity.BriefOrderItem;
import seu.qz.qzapp.entity.LOIInstrument;
import seu.qz.qzapp.entity.ProvideOrder;
import seu.qz.qzapp.objectfactory.OkHttpFactory;
import seu.qz.qzapp.utils.GsonUtils;
import seu.qz.qzapp.utils.PropertyUtil;
import seu.qz.qzapp.utils.SystemStateUtil;

public class OrderSettingOperation {


    public void getAmbitionOrder(final int order_id, final Handler handler, final Context context) {
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
                    url_key = PropertyUtil.getUrl("getProvideOrderById", context);
                    settings.put("order_id", String.valueOf(order_id));
                    RequestBody requestBody = OkHttpFactory.getRequestBodyWithSettings(settings);
                    Request request = new Request.Builder()
                            .url(url_key)
                            .post(requestBody)
                            .build();
                    Response response = client.newCall(request).execute();
                    String responseBody = response.body().string();
                    //将查询结果转为BriefOrderItem的List对象，如果查询结果不够,即<contentnumber,则标志为1（返回结果为空则使list为size=0对象），若查询结果够了，则标志为2，并返回list
                    if(responseBody == null || responseBody.isEmpty()){
                        message.what = 1;
                        message.obj = null;
                    }else{
                        Gson gson = GsonUtils.gson;
                        ProvideOrder order = gson.fromJson(responseBody, ProvideOrder.class);
                        message.what = 2;
                        message.obj = order;
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

    public void getInstrumentsFromServer(final int saler_id, final Handler handler, final Context context) {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                Message message = new Message();
                List<LOIInstrument> list = null;
                if(!SystemStateUtil.isNetworkConnected(context)){
                    message.what = 0;
                    message.obj = null;
                    handler.sendMessage(message);
                    return;
                }
                Gson gson = GsonUtils.gson;;
                try {
                    OkHttpClient client = new OkHttpClient();
                    Map<String, String> settings = new HashMap<>();
                    settings.put("saler_id", String.valueOf(saler_id));
                    RequestBody requestBody = OkHttpFactory.getRequestBodyWithSettings(settings);
                    Request request = new Request.Builder()
                            .url(PropertyUtil.getUrl("getInstrumentsById", context))
                            .post(requestBody)
                            .build();
                    Response response = client.newCall(request).execute();
                    String responseBody = response.body().string();
                    System.out.println(responseBody);
                    //将查询结果转为LOIInstrument的List对象
                    if(responseBody == null || responseBody.isEmpty()){
                        message.what = 1;
                        list = null;
                    }else{
                        list = gson.fromJson(responseBody, new TypeToken<List<LOIInstrument>>(){}.getType());
                        message.what = 2;
                    }
                    message.obj = list;
                }catch (Exception e){
                    e.printStackTrace();
                    message.what = 3;
                    message.obj = null;
                }
                handler.sendMessage(message);
            }
        }, 500);
    }

    public void updateOrder(final ProvideOrder order, final Handler handler, final Context context) {
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
                Gson gson = GsonUtils.gson;
                try {
                    OkHttpClient client = new OkHttpClient();
                    String provideOrder_Json = gson.toJson(order);
                    System.out.println("啦啦啦" + provideOrder_Json);
                    MediaType JSON = MediaType.parse("application/json; charset=utf-8");
                    RequestBody requestBody = RequestBody.create(JSON, provideOrder_Json);
                    Request request = new Request.Builder()
                            .url(PropertyUtil.getUrl("updateProvideOrder", context))
                            .post(requestBody)
                            .build();
                    Response response = client.newCall(request).execute();
                    String responseBody = response.body().string();
                    System.out.println(responseBody);
                    //将查询结果转为LOIInstrument的List对象
                    if(responseBody == null || responseBody.isEmpty()){
                        message.what = 1;
                    }else{
                        if(responseBody.equals("Success")){
                            message.what = 2;
                        }else if(responseBody.equals("Failed")){
                            message.what = 3;
                        }
                    }
                }catch (Exception e){
                    e.printStackTrace();
                    message.what = 4;
                }
                handler.sendMessage(message);
            }
        }, 500);
    }

    public void addNewProvideOrder(final ProvideOrder order, final Handler handler, final Context context) {
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
                Gson gson = GsonUtils.gson;
                try {
                    OkHttpClient client = new OkHttpClient();
                    String provideOrder_Json = gson.toJson(order);
                    System.out.println("啦啦啦" + provideOrder_Json);
                    MediaType JSON = MediaType.parse("application/json; charset=utf-8");
                    RequestBody requestBody = RequestBody.create(JSON, provideOrder_Json);
                    Request request = new Request.Builder()
                            .url(PropertyUtil.getUrl("addNewProvideOrder", context))
                            .post(requestBody)
                            .build();
                    Response response = client.newCall(request).execute();
                    String responseBody = response.body().string();
                    System.out.println(responseBody);
                    //将查询结果转为LOIInstrument的List对象
                    if(responseBody == null || responseBody.isEmpty()){
                        message.what = 1;
                    }else{
                        if(responseBody.equals("Success")){
                            message.what = 2;
                        }else if(responseBody.equals("Failed")){
                            message.what = 3;
                        }
                    }
                }catch (Exception e){
                    e.printStackTrace();
                    message.what = 4;
                }
                handler.sendMessage(message);
            }
        }, 500);
    }
}
