package seu.qz.qzapp.database;

import org.litepal.LitePal;

import java.util.List;

import seu.qz.qzapp.entity.AppCustomer;
import seu.qz.qzapp.entity.SearchHistoryItem;

/**
 * LitePal数据框架的工具类
 */
public class LitePalUtils {

    //存储一个AppCustomer
    public static void saveNewCustomer(AppCustomer customer){
        customer.save();
    }

    //存储一个AppCustomer，并保证SQLite里与形参同名的AppCustomer只有一个:
    //  如果没有则直接存该对象
    //  如果已有则先删除原同名对象再存入新对象
    public static void saveSingleCustomer(AppCustomer customer){
        List<AppCustomer> nick_name = LitePal.where("user_nickName = ?", customer.getUser_nickName()).find(AppCustomer.class);
        if (!nick_name.isEmpty()) {
            LitePal.deleteAll(AppCustomer.class, "user_nickName = ?", customer.getUser_nickName());
        }
        //LitePal频繁先删除再存储会导致数据不存储
        customer.assignBaseObjId(0);
        saveNewCustomer(customer);
    }

    //获取SQLLite中唯一一个指定username的AppCustomer对象，如果找到多个，则返回第一个并在数据库中删除其他的
    public static AppCustomer getSingleCustomer(String username){
        List<AppCustomer> list = LitePal.where("user_nickName = ?", username).find(AppCustomer.class);
        if(list.size() == 0){
            return null;
        }
        if(list.size() > 1){
            for(int i = 1; i < list.size(); i++){
                list.get(i).delete();
            }
        }
        return list.get(0);
    }

    //删除SQLite里所有与形参同名的AppCustomer
    public static void deleteCache(AppCustomer customer) {
        LitePal.deleteAll(AppCustomer.class, "user_nickName = ?", customer.getUser_nickName());
    }

    public static void deleteFirstCustomer(){
        LitePal.delete(AppCustomer.class, 1);
    }


    public static void saveSingleSearchHistoryItem(SearchHistoryItem item){
        List<SearchHistoryItem> items = LitePal.where("search_item = ?", item.getSearch_item()).find(SearchHistoryItem.class);
        if (!items.isEmpty()) {
            LitePal.deleteAll(SearchHistoryItem.class, "search_item = ?", item.getSearch_item());
        }
        //LitePal频繁先删除再存储会导致数据不存储
        item.assignBaseObjId(0);
        item.save();
    }
}
