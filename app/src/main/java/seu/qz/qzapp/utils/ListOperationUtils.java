package seu.qz.qzapp.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import seu.qz.qzapp.entity.BriefChatItem;
import seu.qz.qzapp.entity.BriefOrderItem;

public class ListOperationUtils {

    public static List<BriefOrderItem> moveBToA(List<BriefOrderItem> a, List<BriefOrderItem> b){
        if(b.size() < 20){
            for(int i = 0; i < b.size(); i++){
                a.add(b.get(i));
            }
        }else {
            a.addAll(b);
        }
        return a;
    }

    //从前往后的时间顺序排
    public static void adjustBriefOrderForwardByDate(List<BriefOrderItem> orders){
        Collections.sort(orders, new Comparator<BriefOrderItem>() {
            //若返回值为1，则o1排在后面!!!!!!!!!!
            @Override
            public int compare(BriefOrderItem o1, BriefOrderItem o2) {
                Date o1_date = DateRelatedUtils.formDateByString(o1.getDate(), DateRelatedUtils.TYPE_SHORT);
                Date o2_date = DateRelatedUtils.formDateByString(o2.getDate(), DateRelatedUtils.TYPE_SHORT);
                return o1_date.compareTo(o2_date);
            }
        });
    }
    //从后往前的顺序排
    public static void adjustBriefOrderBackwardByDate(List<BriefOrderItem> orders){
        Collections.sort(orders, new Comparator<BriefOrderItem>() {
            //若返回值为1，则o1排在后面!!!!!!!!!!
            @Override
            public int compare(BriefOrderItem o1, BriefOrderItem o2) {
                Date o1_date = DateRelatedUtils.formDateByString(o1.getDate(), DateRelatedUtils.TYPE_SHORT);
                Date o2_date = DateRelatedUtils.formDateByString(o2.getDate(), DateRelatedUtils.TYPE_SHORT);
                return -o1_date.compareTo(o2_date);
            }
        });
    }

    public static List<BriefOrderItem> filterItemsInTimeInterval(List<BriefOrderItem> old_list, Date time_start, Date time_end){
        if(old_list == null || old_list.isEmpty()){
            return new ArrayList<>();
        }
        //time_end必须比time_start大
        if(time_start.compareTo(time_end) != -1){
            return new ArrayList<>();
        }
        List<BriefOrderItem> new_list = new ArrayList<>();
        for(int i = 0; i < old_list.size(); i++){
            BriefOrderItem item = old_list.get(i);
            Date rentTime_begin = DateRelatedUtils.formDateByString(item.getDate(), DateRelatedUtils.TYPE_SHORT);
            if(rentTime_begin.compareTo(time_start) >= 0 && rentTime_begin.compareTo(time_end) <= 0){
                new_list.add(item);
            }
        }
        return new_list;
    }

    public static List<BriefOrderItem> filterItemsInPriceInterval(List<BriefOrderItem> old_list, int price_bottom, int price_top){
        if(old_list == null || old_list.isEmpty()){
            return new ArrayList<>();
        }
        if(price_bottom > price_top){
            return new ArrayList<>();
        }
        List<BriefOrderItem> new_list = new ArrayList<>();
        for(int i = 0; i < old_list.size(); i++){
            BriefOrderItem item = old_list.get(i);
            int price = Integer.parseInt(item.getCost().replace("￥", ""));
            if(price >= price_bottom && price <= price_top){
                new_list.add(item);
            }
        }
        return new_list;
    }

    //从便宜到贵
    public static void adjustBriefOrderForwardByPrice(List<BriefOrderItem> orders){
        Collections.sort(orders, new Comparator<BriefOrderItem>() {
            //若返回值为1，则o1排在后面!!!!!!!!!!
            @Override
            public int compare(BriefOrderItem o1, BriefOrderItem o2) {
                int price_o1 = Integer.parseInt(o1.getCost().replace("￥", ""));
                int price_o2 = Integer.parseInt(o2.getCost().replace("￥", ""));
                return price_o1 >= price_o2 ? 1 : -1;
            }
        });
    }
    //从贵的到便宜
    public static void adjustBriefOrderBackwardByPrice(List<BriefOrderItem> orders){
        Collections.sort(orders, new Comparator<BriefOrderItem>() {
            //若返回值为1，则o1排在后面!!!!!!!!!!
            @Override
            public int compare(BriefOrderItem o1, BriefOrderItem o2) {
                int price_o1 = Integer.parseInt(o1.getCost().replace("￥", ""));
                int price_o2 = Integer.parseInt(o2.getCost().replace("￥", ""));
                return price_o1 > price_o2 ? -1 : 1;
            }
        });
    }
    //从近到远
    public static void filterBriefOrderForwardByLocation(List<BriefOrderItem> orders, final double longitude, final double latitude){
        Collections.sort(orders, new Comparator<BriefOrderItem>() {
            //若返回值为1，则o1排在后面!!!!!!!!!!
            @Override
            public int compare(BriefOrderItem o1, BriefOrderItem o2) {
                String[] location_o1 = o1.getLocation().split(":");
                double o1_longitude = Double.parseDouble(location_o1[0]);
                double o1_latitude = Double.parseDouble(location_o1[1]);
                double instance_o1 = Math.pow((longitude - o1_longitude), 2) + Math.pow((latitude - o1_latitude), 2);
                String[] location_o2 = o1.getLocation().split(":");
                double o2_longitude = Double.parseDouble(location_o2[0]);
                double o2_latitude = Double.parseDouble(location_o2[1]);
                double instance_o2 = Math.pow((longitude - o2_longitude), 2) + Math.pow((latitude - o2_latitude), 2);
                return instance_o1 >= instance_o2 ? 1 : -1;
            }
        });
    }
    //从远到近
    public static void filterBriefOrderBackwardByLocation(List<BriefOrderItem> orders, final double longitude, final double latitude){
        Collections.sort(orders, new Comparator<BriefOrderItem>() {
            //若返回值为1，则o1排在后面!!!!!!!!!!
            @Override
            public int compare(BriefOrderItem o1, BriefOrderItem o2) {
                String[] location_o1 = o1.getLocation().split(":");
                double o1_longitude = Double.parseDouble(location_o1[0]);
                double o1_latitude = Double.parseDouble(location_o1[1]);
                double instance_o1 = Math.pow((longitude - o1_longitude), 2) + Math.pow((latitude - o1_latitude), 2);
                String[] location_o2 = o1.getLocation().split(":");
                double o2_longitude = Double.parseDouble(location_o2[0]);
                double o2_latitude = Double.parseDouble(location_o2[1]);
                double instance_o2 = Math.pow((longitude - o2_longitude), 2) + Math.pow((latitude - o2_latitude), 2);
                return instance_o1 >= instance_o2 ? -1 : 1;
            }
        });
    }

    //search的字符串匹配
    public static List<BriefOrderItem> filterBriefOrderBySearch(List<BriefOrderItem> old_list, String search_item){
        if(old_list == null || old_list.isEmpty()){
            return new ArrayList<>();
        }
        if(search_item == null || search_item.isEmpty()){
            return new ArrayList<>(old_list);
        }
        List<BriefOrderItem> new_list = new ArrayList<>();
        for(int i = 0; i < old_list.size(); i++){
            BriefOrderItem item = old_list.get(i);
            String factory_name = item.getInstrument().split("-")[0];
            if(factory_name.contains(search_item)){
                new_list.add(item);
            }
        }
        return new_list;
    }
}
