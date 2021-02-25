package seu.qz.qzapp.activity.operation;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import seu.qz.qzapp.entity.BriefOrderItem;
import seu.qz.qzapp.utils.GsonUtils;
import seu.qz.qzapp.utils.PropertyUtil;
import seu.qz.qzapp.utils.SystemStateUtil;

public class SearchFragmentOperation {


    public void loadOriginalItems(final Handler handler, final Context context) {
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
                Gson gson = GsonUtils.gson;
                try {
                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder()
                            .url(PropertyUtil.getUrl("getSearchedItems", context))
                            .build();
                    Response response = client.newCall(request).execute();
                    String responseBody = response.body().string();
                    if(responseBody == null || responseBody.isEmpty()){
                        message.what = 1;;
                    }else{
                        list = gson.fromJson(responseBody, new TypeToken<List<BriefOrderItem>>(){}.getType());
                        System.out.println(list.toString());
                        message.what = 2;
                        message.obj = list;
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
