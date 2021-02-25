package seu.qz.qzapp.activity.operation;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

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
import seu.qz.qzapp.entity.BriefReportItem;
import seu.qz.qzapp.entity.LOIInstrument;
import seu.qz.qzapp.objectfactory.OkHttpFactory;
import seu.qz.qzapp.utils.GsonUtils;
import seu.qz.qzapp.utils.PropertyUtil;
import seu.qz.qzapp.utils.SystemStateUtil;

public class DeviceDisplayOperation {
    public void getLOIInstrumentsBySalerId(final String saler_id, final Handler handler, final Context context) {
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
                Gson gson = GsonUtils.gson;
                try {
                    OkHttpClient client = new OkHttpClient();
                    Map<String, String> settings = new HashMap<>();
                    settings.put("saler_id", saler_id);
                    RequestBody requestBody = OkHttpFactory.getRequestBodyWithSettings(settings);
                    Request request = new Request.Builder()
                            .url(PropertyUtil.getUrl("getInstrumentsById", context))
                            .post(requestBody)
                            .build();
                    Response response = client.newCall(request).execute();
                    String responseBody = response.body().string();
                    System.out.println(responseBody);
                    //将查询结果转为BriefOrderItem的List对象，如果查询结果不够,即<contentnumber,则标志为1（返回结果为空则使list为size=0对象），若查询结果够了，则标志为2，并返回list
                    if(responseBody == null || responseBody.isEmpty()){
                        message.what = 1;
                    }else{
                        list = gson.fromJson(responseBody, new TypeToken<List<LOIInstrument>>(){}.getType());
                        System.out.println(list.toString());
                        message.what = 2;
                    }
                    message.obj = list;
                }catch (Exception e){
                    e.printStackTrace();
                    System.out.println("是这里嘛？！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！");
                    message.what = 3;
                    message.obj = null;
                }
                handler.sendMessage(message);
            }
        }, 0);
    }
}
