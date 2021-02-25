package seu.qz.qzapp.activity.operation;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import seu.qz.qzapp.utils.PropertyUtil;

public class ChatFragmentOperation {


    public List<String> fillFileNameItems(String user_id, Context context) {
        String directory =context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).getPath();
        File file = new File(directory +"/" + PropertyUtil.getPropertyByKey("chatCaches", context) + "/" + user_id);
        if(!file.exists()){
            file.mkdirs();
        }
        String[] fileNames = file.list();
        if(fileNames == null || fileNames.length == 0){
            return null;
        }
        List<String> list = new ArrayList<>();
        for(int i = 0; i < fileNames.length; i++){
            list.add(fileNames[i]);
        }
        return list;
    }

    //有新消息通过呼叫传达，修改缓存文件名和顺序，参数中的文件名格式为"1-本地id_对方id_对方昵称_对方性别"
    public void modifyChatCacheFilesByFileName(String chat_cache_fileName, Context context){
        if(chat_cache_fileName == null || chat_cache_fileName.isEmpty()){
            return ;
        }
        String local_userId = chat_cache_fileName.substring(chat_cache_fileName.lastIndexOf("-") + 1, chat_cache_fileName.indexOf("_"));
        String directory =context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).getPath();
        String chat_root = directory +"/" + PropertyUtil.getPropertyByKey("chatCaches", context) + "/" + local_userId;
        //获取缓存文件根目录
        File root_file = new File(chat_root);
        String[] fileNames = root_file.list();
        String new_fileName = chat_root + "/" + chat_cache_fileName + ".txt";
        if(fileNames == null || fileNames.length == 0){
            File new_chat = new File(new_fileName);
            try {
                new_chat.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                return ;
            }
            return ;
        }else {
            int existedPosition = -1;
            String compareName = new_fileName.substring(new_fileName.lastIndexOf("-"));
            for(int i = 0; i < fileNames.length; i++){
                if(compareName.equals(fileNames[i].substring(fileNames[i].lastIndexOf("-")))){
                    existedPosition = i + 1;
                    break;
                }
            }
            //获取缓存目录下所有缓存文件
            File[] files = root_file.listFiles();
            //existedPosition>0,代表当前呼叫对应的聊天缓存文件存在，将其挪至第一位，其前面的依次后移一位
            if(existedPosition > 0){
                for(int i = 0; i < existedPosition; i++){
                    File file = files[i];
                    String old_name = fileNames[i];
                    String[] old_names = old_name.split("-");
                    String new_name = null;
                    if(i == existedPosition - 1){
                        new_name = chat_root + "/" + String.valueOf(1) + "-" + old_names[1];
                    }else {
                        new_name = chat_root + "/" + String.valueOf(Integer.parseInt(old_names[0]) + 1) + "-" + old_names[1];
                    }
                    file.renameTo(new File(new_name));
                }
                //existedPosition=-1，代表当前对应的聊天缓存文件不存在，则将所有现存的缓存文件后移一位，再创建当前文件于首位
            }else if(existedPosition == -1){
                for(int i = 0; i < files.length; i++){
                    File file = files[i];
                    String old_name = fileNames[i];
                    String[] old_names = old_name.split("-");
                    String new_name = chat_root + "/" + String.valueOf(Integer.parseInt(old_names[0]) + 1) + "-" + old_names[1];
                    file.renameTo(new File(new_name));
                }
                File new_chat = new File(new_fileName);
                try {
                    new_chat.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }

            }

        }
    }

    public void modifyChatCacheFilesById(String combined_id, Context context) {
        if(combined_id == null || combined_id.isEmpty()){
            return;
        }
        String[] order_id = combined_id.split(";");
        int order = Integer.parseInt(order_id[0]);
        String local_userId = order_id[1];
        String directory =context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).getPath();
        String chat_root = directory +"/" + PropertyUtil.getPropertyByKey("chatCaches", context) + "/" + local_userId;
        File root_file = new File(chat_root);
        File[] files = root_file.listFiles();
        String[] fileNames = root_file.list();
        for(int i = 0; i < order - 1; i++){
            File file = files[i];
            String old_name = fileNames[i];
            String[] old_names = old_name.split("-");
            String new_name = chat_root + "/" + String.valueOf(Integer.parseInt(old_names[0]) + 1) + "-" + old_names[1];
            file.renameTo(new File(new_name));
        }
        File new_order_file = files[order - 1];
        //重新命名需要注意添加根路径
        String new_name = chat_root + "/1" + fileNames[order - 1].substring(fileNames[order - 1].indexOf("-"));
        new_order_file.renameTo(new File(new_name));
    }
}
