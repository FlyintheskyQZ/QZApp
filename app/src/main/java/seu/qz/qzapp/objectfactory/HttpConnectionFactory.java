package seu.qz.qzapp.objectfactory;

import android.content.Context;

import java.net.HttpURLConnection;
import java.net.URL;

import seu.qz.qzapp.utils.PropertyUtil;

/**
 * HttpURLConnection类的工厂类：目前已弃用
 */
public class HttpConnectionFactory {

    public static HttpURLConnection getHttpConnectionByURLStr(String url, Context context){
        String httpUrl = PropertyUtil.getPropertyByKey(url, context);
        HttpURLConnection connection = null;
        try{
            URL url1 = new URL(httpUrl);
            connection = (HttpURLConnection) url1.openConnection();
        }catch (Exception e){

        }
        return connection;
    }
}
