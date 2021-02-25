package seu.qz.qzapp.utils;

import android.content.Intent;

import java.util.ArrayList;
import java.util.List;

import seu.qz.qzapp.entity.BriefChatItem;

public class CommonUtils {


    public static int getPriceFromString(String display_price){
        if(display_price == null || display_price.isEmpty()){
            return 0;
        }
        display_price = display_price.trim();
        int display_length = display_price.length();
        int real_price = 0;
        for(int i = display_length - 1; i >= 0; i--){
            char s = display_price.charAt(i);
            if(s < '0' || s > '9'){
                real_price = Integer.parseInt(display_price.substring(i + 1));
                break;
            }
        }
        return real_price;
    }

    //将缓存文件名list转化为聊天子项BriefChatItemlist
    public static List<BriefChatItem> transFileNamesToChatItems(List<String> fileList){
        if(fileList == null || fileList.isEmpty()){
            return null;
        }
        List<BriefChatItem> chatItems = new ArrayList<>();
        for(int i = 0; i < fileList.size(); i++){
            BriefChatItem chatItem = transFileNameToChatItem(fileList.get(i));
            if(chatItem != null){
                chatItems.add(chatItem);
            }
        }
        return chatItems;
    }
    //缓存文件名为"序号-本地id-对方id-对方昵称-对象性别.txt"
    public static BriefChatItem transFileNameToChatItem(String fileName) {
        if(fileName == null || fileName.isEmpty()){
            return null;
        }
        String realName = (fileName.split("-"))[1];
        realName = realName.substring(0, realName.length() - 4);
        //删除".txt"
        String[] nameParts = realName.split("_");
        int opposite_user_id = Integer.parseInt(nameParts[1]);
        String username = nameParts[2];
        boolean isMale = Boolean.parseBoolean(nameParts[3]);
        return new BriefChatItem(opposite_user_id, username, isMale, 0);
    }



}
