package seu.qz.qzapp.register;

import android.app.Notification;
import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import seu.qz.qzapp.entity.AppCustomer;
import seu.qz.qzapp.objectfactory.OkHttpFactory;
import seu.qz.qzapp.utils.GsonUtils;
import seu.qz.qzapp.utils.PropertyUtil;
import seu.qz.qzapp.utils.SystemStateUtil;

/**
 * 注册动作类：此类主要用于提供注册的后台服务，如注册方法，服务类，所以是单例
 */
public class RegisterOptions {

    private static volatile RegisterOptions instance = null;

    private RegisterOptions(){};

    public static RegisterOptions getInstance(){
        if(instance == null){
            instance = new RegisterOptions();
        }
        return instance;
    }

    //注册方法，采用OkHttp进行网络通信，真正的异步通信，通信结果由消息message发送至主线程的handler中进行处理
    public void register(final AppCustomer customer, final Context context, final Handler handler){

        new Thread(new Runnable() {
            @Override
            public void run() {
                Message message = new Message();
                if(!SystemStateUtil.isNetworkConnected(context)){
                    message.what = 0;
                    message.obj = new String("网络连接失败，请检查网络！");
                    handler.sendMessage(message);
                    return;
                }
                try {
                    Gson gson = GsonUtils.gson;
                    OkHttpClient client = new OkHttpClient();
                    String customer_Json = gson.toJson(customer);
                    MediaType JSON = MediaType.parse("application/json; charset=utf-8");
                    RequestBody requestBody = RequestBody.create(JSON, customer_Json);
                    Request request = new Request.Builder()
                            .url(PropertyUtil.getUrl("register", context))
                            .post(requestBody)
                            .build();
                    Response response = client.newCall(request).execute();
                    String responseBody = response.body().string();
                    char capital = responseBody.charAt(0);
                    //若服务器返回值首字母是数字，则表示返回的是用户id，注册成功！若返回的是汉字，则说明是失败原因，注册失败
                    if(capital <= '9' && capital > '0'){
                        message.what = 1;
                        customer.setUser_id(Integer.valueOf(responseBody));
                        customer.setUser_balance(0);
                        customer.setNumberForFinishedOrders(0);
                        customer.setNumberForProvideOrders(0);
                        message.obj = customer;
                    }else {
                        message.what = 0;
                        message.obj = responseBody;
                    }
                    handler.sendMessage(message);
                } catch (Exception e){
                    e.printStackTrace();
                    message.what = 0;
                    message.obj = new String("与服务器通讯出现异常！！");
                }
            }
        }).start();


    }
}
