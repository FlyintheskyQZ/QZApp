package seu.qz.qzapp.activity.operation;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.google.gson.Gson;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import seu.qz.qzapp.entity.AppCustomer;
import seu.qz.qzapp.entity.BriefOrderItem;
import seu.qz.qzapp.utils.GsonUtils;
import seu.qz.qzapp.utils.PropertyUtil;
import seu.qz.qzapp.utils.SystemStateUtil;

public class CustomerInfoSettingOperation {

    public void updatePassword(final AppCustomer customer, final Handler handler, final Context context) {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                Message message = new Message();
                List<BriefOrderItem> list = null;
                if(!SystemStateUtil.isNetworkConnected(context)){
                    message.what = 0;
                    message.obj = null;
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
                            .url(PropertyUtil.getUrl("updateCustomer", context))
                            .post(requestBody)
                            .build();
                    Response response = client.newCall(request).execute();
                    String responseBody = response.body().string();
                    if(responseBody == null || responseBody.isEmpty()){
                        message.what = 1;;
                    }else{
                        if(responseBody.equals("Success")){
                            message.what = 2;
                        }else {
                            message.what = 1;
                            message.obj = responseBody;
                        }
                    }
                }catch (Exception e){
                    e.printStackTrace();
                    message.what = 3;
                }
                handler.sendMessage(message);
            }
        }, 0);
    }
}
