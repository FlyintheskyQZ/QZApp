package seu.qz.qzapp.utils;




import android.app.Application;
import android.content.Context;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

/**
 * 属性配置工具类：用于从assets下的配置文件中取出对应的配置值
 */
public class PropertyUtil {

    public static Properties properties = null;


    //从assets下的webrequesturl.properties文件中取出对应key值的value
    public static String getPropertyByKey(String key, Context context){
        if(properties == null){
            properties = new Properties();
            try {
                InputStream is = context.getAssets().open("webrequesturl.properties");
                properties.load(is);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        String value = properties.getProperty(key);
        return value;
    }
    //由url_key从配置文件中取出url
    public static String getUrl(String url_key, Context context){
       return getPropertyByKey(url_key, context);
    }
    //给定params参数集，并通过url参数的方式整合到url路径上，返回处理后的url
    public static String getUrlWithParams(String url_key, Context context, Map<String, String> params){
        String original_url = getUrl(url_key, context);
        if(params.isEmpty()){
            return original_url;
        }
        original_url = original_url + "?";
        for(Map.Entry<String, String> entry: params.entrySet()){
            original_url = original_url + entry.getKey() + "=" + entry.getValue() + "&";
        }
        return original_url.substring(0, original_url.length() - 1);
    }
}
