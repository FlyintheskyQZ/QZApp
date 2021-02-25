package seu.qz.qzapp.utils;

import android.content.Context;
import android.content.SharedPreferences;

import seu.qz.qzapp.entity.AppCustomer;

public class SharedPreferencesUtils {

    public static void storeAppCustomer(Context context, String username, String password){
        SharedPreferences.Editor editor = context.getSharedPreferences(PropertyUtil.getPropertyByKey("loginCacheData", context), Context.MODE_PRIVATE).edit();
        editor.putString("username", username);
        editor.putString("password", password);
        editor.apply();
    }

    public static String getLoginData(Context context, String key){
        return context.getSharedPreferences(PropertyUtil.getPropertyByKey("loginCacheData", context), Context.MODE_PRIVATE).getString(key, "");
    }

    public static void deleteAppCustomer(Context context) {
        SharedPreferences.Editor editor = context.getSharedPreferences(PropertyUtil.getPropertyByKey("loginCacheData", context), Context.MODE_PRIVATE).edit();
        editor.putString("username", "");
        editor.putString("password", "");
        editor.apply();
    }
}
