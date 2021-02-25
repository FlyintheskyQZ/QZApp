package seu.qz.qzapp.login.data;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import seu.qz.qzapp.database.LitePalUtils;
import seu.qz.qzapp.entity.AppCustomer;
import seu.qz.qzapp.login.ui.LoginViewModel;
import seu.qz.qzapp.utils.GsonUtils;
import seu.qz.qzapp.utils.PropertyUtil;
import seu.qz.qzapp.objectfactory.OkHttpFactory;
import seu.qz.qzapp.utils.SystemStateUtil;

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 * 对于登录数据的验证和撤销操作在这个类中
 * 对于登录数据验证：login（）
 * 撤销验证：logout（）
 */
public class LoginDataSource {

    //开启登录，返回登陆结果
    public Result<LoggedInUser> login(final String username, final String password, final Context context) {

        final Integer[] login_success = {0, 0};
        //开启新线程，并用OkHttp来发起Http的Post请求进行网络通讯
        new Thread(new Runnable() {
            @Override
            public void run() {

                if(!SystemStateUtil.isNetworkConnected(context)){
                    login_success[0] = 3;
                    return;
                }
                try {
                    OkHttpClient client = new OkHttpClient();
                    Map<String, String> settings = new HashMap<>();
                    settings.put("username", username);
                    settings.put("password", password);
                    RequestBody requestBody = OkHttpFactory.getRequestBodyWithSettings(settings);
                    Request request = new Request.Builder()
                    .url(PropertyUtil.getUrl("login", context))
                    .post(requestBody)
                    .build();
                    Response response = client.newCall(request).execute();
                    String responseBody = response.body().string();
                    AppCustomer customer = GsonUtils.getCustomerFromJson(responseBody);
                    if (customer.getUser_id() == null){
                        login_success[0] = 2;
                    }else {
                        login_success[0] = 1;
                        LitePalUtils.saveSingleCustomer(customer);
                    }
                } catch (Exception e){
                    e.printStackTrace();
                }
                login_success[1] = 1;
            }
        }).start();
        //由于login部分整体框架已经形成，并非完全的异步处理方式，主线程需要等待网络请求线程的完成来获取信息，故在此处对主线程进行沉睡操作，每次睡0.5s，最多睡6次
        try{
            int count = 0;
            while(login_success[1] == 0 && count <6){
                Thread.sleep(500);
                count++;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        //根据结果返回相应的Result对象
        switch (login_success[0]){
            case 1:
                return new Result.Success<>(new LoggedInUser(username, password));
            case 2:
                return new Result.Error(new IOException("Wrong LoginInfo"));
            case 3:
                return new Result.Error(new IOException("NetWork unconnected"));
            default:
                return new Result.Error(new IOException("Login failed"));
        }

    }


    public void logout() {
        // TODO: revoke authentication
    }
}