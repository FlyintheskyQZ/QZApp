package seu.qz.qzapp.utils;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import io.agora.rtm.RtmClient;
import io.agora.rtm.RtmImageMessage;
import io.agora.rtm.RtmMessage;
import io.agora.rtm.RtmMessageType;
import seu.qz.qzapp.activity.BaseApplication;
import seu.qz.qzapp.agora.ChatManager;
import seu.qz.qzapp.agora.MessageBean;
import seu.qz.qzapp.agora.MessageListBean;
import seu.qz.qzapp.agora.RtmMessagePool;
import seu.qz.qzapp.entity.BriefChatItem;

public class FileUtil {
    private static final String CACHE_TEXT = "chatCacheText";
    public static final String CACHE_IMAGE = "chatCacheImage";
    private static final String TAG = "MainActivity";

    public static String getChatCacheTextPath (Context context, String local_user_id){
        File parent = new File(context.getCacheDir(), CACHE_TEXT);
        //若缓存根文件不存在，则创建
        if (!parent.exists()) {
            parent.mkdirs();
        }
        //返回缓存目录
        File local_user_directory = new File(parent, local_user_id);
        if(!local_user_directory.exists()){
            local_user_directory.mkdir();
        }
        return local_user_directory.getAbsolutePath();
    }

    public static void clearAllChatCache(String user_id, Context context){
        File root_chatText = new File(FileUtil.getChatCacheTextPath(context, user_id));
        File root_chatImage = new File(FileUtil.getChatCacheImagePath(context, user_id));
        File[] text_caches = root_chatText.listFiles();
        File[] image_caches = root_chatImage.listFiles();
        try {
            if(text_caches.length > 0){
                for(int i = 0; i < text_caches.length; i++){
                    File file = text_caches[i];
                    if (file.isFile() && file.exists()) {
                        file.delete();
                    }
                }

            }
            if(image_caches.length > 0){
                for(int i = 0; i < text_caches.length; i++){
                    File file = image_caches[i];
                    if (file.isFile() && file.exists()) {
                        file.delete();
                    }
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }

    }

    //图片缓存默认放在getCacheDir()/chatCacheImage/{userId}路径下
    public static String getChatCacheImagePath (Context context, String local_user_id){
        File parent = new File(context.getCacheDir(), CACHE_IMAGE);
        //若缓存根文件不存在，则创建
        if (!parent.exists()) {
            parent.mkdirs();
        }
        File local_user_directory = new File(parent, local_user_id);
        if(!local_user_directory.exists()){
            local_user_directory.mkdir();
        }
        return local_user_directory.getAbsolutePath();
    }

    public static void loadCacheFromFileToFlash(final Context context, File chatCache_text) {
        ChatManager mManager = BaseApplication.the().getChatManager();
        RtmClient client = mManager.getRtmClient();
        RtmMessagePool messagePool = mManager.getmMessagePool();
        if(!chatCache_text.exists() || chatCache_text.length() == 0){
            return;
        }
        String file_name = chatCache_text.getName();
        file_name = file_name.substring(0, file_name.indexOf(".txt"));
        String[] file_name_parts = file_name.split("_");
        String peerId =file_name_parts[1];
        String peerName = file_name_parts[2];
        boolean isMale = Boolean.parseBoolean(file_name_parts[3]);
        BriefChatItem chatItem = new BriefChatItem(Integer.parseInt(peerId), peerName, isMale);
        FileInputStream in = null;
        InputStreamReader inReader = null;
        BufferedReader bufferedReader = null;
        try {
            in = new FileInputStream(chatCache_text);
            inReader = new InputStreamReader(in, "UTF-8");
            bufferedReader = new BufferedReader(inReader);
            //首行字符串为未读取的信息数
            int num_tobeRead = Integer.parseInt(bufferedReader.readLine());
            String line = "";
            if(num_tobeRead > 0){
                List<RtmMessage> list = new ArrayList<>();
                for(int i = 0; i < num_tobeRead; i++){
                    line = bufferedReader.readLine();
                    Log.d(TAG, "loadCacheFromFileToFlash未阅:" + line);
                    String[] line_parts = line.split("-");
                    RtmMessage message = null;
                    //如果有查看的图片信息
                    if(line_parts[1].equals("Media")){
                        //考虑到文件名可能会有"-"
                        String media_id = line_parts[3];
                        String width_str = line_parts[4];
                        String height_str = line_parts[5];
                        final int width = Integer.parseInt(width_str);
                        final int height = Integer.parseInt(height_str);
                        final String cache_file = line_parts[2];
                        final RtmImageMessage imageMessage = client.createImageMessageByMediaId(media_id);
                        new Timer().schedule(new TimerTask() {
                            @Override
                            public void run() {
                                imageMessage.setThumbnail(ImageUtil.preloadImage(context, cache_file, width, height));
                            }
                        }, 0);
                        //message = client.createMessage(image_data, line);
                        imageMessage.setWidth(width);
                        imageMessage.setHeight(height);
                        imageMessage.setThumbnailHeight(height/5);
                        imageMessage.setThumbnailWidth(width/5);
                        message = imageMessage;
                        message.setText(cache_file);
                        //有未查看的文本信息
                    }else if(line_parts[1].equals("Text")){
                        message = client.createMessage(line_parts[2]);
                    }
                    list.add(message);
                }
                if(list.size() > 1){
                    Collections.reverse(list);
                }
                chatItem.setNewsCount(list.size());
                messagePool.addRtmMessageList(peerId, list);
            }
            List<MessageBean> beans = new ArrayList<>();
            while (!TextUtils.isEmpty(line = bufferedReader.readLine())) {
                Log.d(TAG, "loadCacheFromFileToFlash已阅:" + line);
                String[] line_parts = line.split("-");
                RtmMessage message = null;
                MessageBean bean = null;
                boolean isSelf = Integer.parseInt(line_parts[0]) == 1;
                //如果有查看的图片信息
                if(line_parts[1].equals("Media")){
                    String media_id = line_parts[3];
                    String width_str = line_parts[4];
                    String height_str = line_parts[5];
                    final int width = Integer.parseInt(width_str);
                    final int height = Integer.parseInt(height_str);
                    final String cache_file = line_parts[2];
                    final RtmImageMessage imageMessage = client.createImageMessageByMediaId(media_id);
                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {
                            imageMessage.setThumbnail(ImageUtil.preloadImage(context, cache_file, width, height));
                        }
                    }, 0);
                    imageMessage.setWidth(width);
                    imageMessage.setHeight(height);
                    imageMessage.setThumbnailHeight(height/5);
                    imageMessage.setThumbnailWidth(width/5);
                    message = imageMessage;
                    message.setText(cache_file);
                    bean = new MessageBean(peerId, message, isSelf);
                    bean.setCacheFile(cache_file);
                    //有未查看的文本信息
                }else if(line_parts[1].equals("Text")){
                    message = client.createMessage(line_parts[2]);
                    bean = new MessageBean(peerId, message, isSelf);
                }
                beans.add(bean);
            }
            if(beans.size() > 1){
                Collections.reverse(beans);
            }
            MessageUtil.addMessageListBeanList(new MessageListBean(peerId, beans, chatItem));
        } catch (Exception e) {
          e.printStackTrace();
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (inReader != null) {
                try {
                    inReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void storeCacheFromFlashToFile(String userId, Context context) {
        ChatManager mManager = BaseApplication.the().getChatManager();
        RtmMessagePool messagePool = mManager.getmMessagePool();
        Map<String, List<RtmMessage>> pool_cache = messagePool.getmOfflineMessageMap();
        List<MessageListBean> messageListBeans = MessageUtil.getMessageListBeanList();
        //清空缓存文件
        File chat_text_directory = new File(getChatCacheTextPath(context, userId));
        File[] old_files=chat_text_directory.listFiles();
        Log.d(TAG, "storeCacheFromFlashToFile:" + messageListBeans.size());
        if(messageListBeans.size() == 0){
            return;
        }
        for(File file:old_files){
            if(file.isFile()){
                file.delete();
            }
        }
        //获取文件中的缓存数据时，先读取的（order序号低）应该是旧的，后读取的（order序号高）应该是新的，所以写入的时候，按照序号，先写旧的（list靠前的），再写新的（list中靠后的）
        FileOutputStream out = null;
        OutputStreamWriter outWriter = null;
        BufferedWriter bufWrite = null;
        try {
            for(int i = 0; i < messageListBeans.size(); i++){
                MessageListBean listBean = messageListBeans.get(i);
                BriefChatItem chatItem = listBean.getChatItem();
                String peerName = chatItem.getUsername();
                boolean isMale = chatItem.isMale();
                String peerId = listBean.getAccountOther();
                File new_file = new File(chat_text_directory, String.valueOf(i + 1) + "-" +
                        userId + "_" + peerId + "_" + peerName + "_" + String.valueOf(isMale) + ".txt");
                new_file.createNewFile();
                out = new FileOutputStream(new_file);
                outWriter = new OutputStreamWriter(out, "UTF-8");
                bufWrite = new BufferedWriter(outWriter);
                //如果有消息还未读取，存于RtmMessagePool中，则先存储这些消息，按照消息从新到旧从list后面到前面存
                if(pool_cache.containsKey(peerId)){
                    List<RtmMessage> offlineList = pool_cache.get(peerId);
                    int list_size = offlineList.size();
                    bufWrite.write(String.valueOf(list_size) + "\r\n");
                    for(int j = list_size - 1; j >= 0; j--){
                        RtmMessage message = offlineList.get(j);
                        String message_record = "";
                        //如果是文本信息
                        if(message.getMessageType() == RtmMessageType.TEXT){
                            message_record = "2-Text-" + message.getText() + "\r\n";
                            //如果是图片信息
                        }else {
                            RtmImageMessage imageMessage = (RtmImageMessage) message;
                            String message_text = imageMessage.getText();
                            message_record = "2-Media-" + message_text + "-" + imageMessage.getMediaId() + "-" + imageMessage.getWidth() + "-" + imageMessage.getHeight();
                        }
                        bufWrite.write(message_record + "\r\n");
                        Log.d(TAG, "未读消息存储1:" + message_record);
                    }
                }else {
                    bufWrite.write(String.valueOf(0) + "\r\n");
                }
                List<MessageBean> list = listBean.getMessageBeanList();
                for(int j = list.size() - 1; j >= 0; j--){
                    MessageBean bean = list.get(j);
                    RtmMessage message = bean.getMessage();
                    String message_record = "";
                    String isSelf = bean.isBeSelf() ? "1" : "2";
                    if(message.getMessageType() == RtmMessageType.TEXT){
                        message_record = isSelf + "-Text-" + message.getText();
                    }else if(message.getMessageType() == RtmMessageType.IMAGE){
                        RtmImageMessage imageMessage = (RtmImageMessage) message;
                        message_record = isSelf + "-Media-" + bean.getCacheFile() + "-" +
                                imageMessage.getMediaId() + "-" + imageMessage.getWidth() + "-" + imageMessage.getHeight();
                    }
                    bufWrite.write(message_record + "\r\n");
                    Log.d(TAG, "已读消息存储2:" + message_record);
                }
                bufWrite.close();
                outWriter.close();
                out.close();
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if (bufWrite != null) {
                try {
                    bufWrite.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (outWriter != null) {
                try {
                    outWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }
}
