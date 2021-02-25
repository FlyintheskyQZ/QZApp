package seu.qz.qzapp.objectfactory;

import java.util.Map;

import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * OkHttp的工厂类（工具类）
 */
public class OkHttpFactory {

    //返回一个指定url的request
    public static Request getOkHttpRequestWithURL(String url){
        System.out.println("url is "+url);
        return new Request.Builder().url(url).build();
    }

    //返回一个RequestBody
    public static RequestBody getRequestBody(){
        return new FormBody.Builder().build();
    }

    //返回一个携带形参中map里包含的参数的requestBody——用户post请求
    public static RequestBody getRequestBodyWithSettings(Map<String, String> settings){
        FormBody.Builder builder = new FormBody.Builder();
        for(Map.Entry<String, String> entry: settings.entrySet()){
            builder.add(entry.getKey(), entry.getValue());
        }
        return builder.build();
    }


}
