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
import seu.qz.qzapp.entity.BriefOrderItem;
import seu.qz.qzapp.entity.LOIInstrument;
import seu.qz.qzapp.utils.GsonUtils;
import seu.qz.qzapp.utils.PropertyUtil;
import seu.qz.qzapp.utils.SystemStateUtil;

public class RegisterDeviceOperation {

    public void registerDevice(final LOIInstrument new_device, final Handler handler, final Context context) {
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
                    Gson gson = GsonUtils.gson;
                    OkHttpClient client = new OkHttpClient();
                    String loiinstrument_Json = gson.toJson(new_device);
                    MediaType JSON = MediaType.parse("application/json; charset=utf-8");
                    RequestBody requestBody = RequestBody.create(JSON, loiinstrument_Json);
                    Request request = new Request.Builder()
                            .url(PropertyUtil.getUrl("registerLOIInstrument", context))
                            .post(requestBody)
                            .build();
                    Response response = client.newCall(request).execute();
                    String responseBody = response.body().string();
                    if(responseBody == null || responseBody.isEmpty()){
                        message.what = 1;;
                    }else{
                        if(responseBody.startsWith("Success")){
                            String device_id = responseBody.substring(8);
                            message.what = 2;
                            message.obj = device_id;
                        }else {
                            message.what = 1;
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
