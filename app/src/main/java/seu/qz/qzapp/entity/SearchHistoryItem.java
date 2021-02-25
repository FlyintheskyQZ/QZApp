package seu.qz.qzapp.entity;

import org.litepal.crud.LitePalSupport;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SearchHistoryItem extends LitePalSupport {

    //对应用户
    String user_id;
    //搜索内容
    String search_item;
    //搜索时间
    Date search_time;

    public SearchHistoryItem(String user_id, String search_item, Date search_time) {
        this.user_id = user_id;
        this.search_item = search_item;
        this.search_time = search_time;
    }

    public SearchHistoryItem() {
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getSearch_item() {
        return search_item;
    }

    public void setSearch_item(String search_item) {
        this.search_item = search_item;
    }

    public Date getSearch_time() {
        return search_time;
    }

    public void setSearch_time(Date search_time) {
        this.search_time = search_time;
    }

    public static void restoreSingleItem(List<SearchHistoryItem> list, SearchHistoryItem item){
        if (item == null) {
            return;
        }
        if(list == null){
            list = new ArrayList<>();
        }
        if(list.isEmpty()){
            list.add(item);
        }else {
            boolean existed = false;
            for(int i = 0; i < list.size(); i++){
                if(list.get(i).getSearch_item().equals(item.getSearch_item())){
                    existed = true;
                    break;
                }
            }
            if(!existed){
                list.add(item);
            }
        }
    }
}
