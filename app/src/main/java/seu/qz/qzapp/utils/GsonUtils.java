package seu.qz.qzapp.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import seu.qz.qzapp.entity.AppCustomer;

public class GsonUtils {

    public static Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();

    public static AppCustomer getCustomerFromJson(String jsonData){

        return gson.fromJson(jsonData, AppCustomer.class);
    }


}
