package seu.qz.qzapp.utils;

import android.content.Context;
import android.graphics.Bitmap;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.concurrent.ExecutionException;

import io.agora.rtm.ErrorInfo;
import io.agora.rtm.ResultCallback;
import io.agora.rtm.RtmClient;
import io.agora.rtm.RtmImageMessage;
import io.agora.rtm.RtmRequestId;

public class ImageUtil {
    //图片缓存目录：/data/data/<application package>/cache/rtm_image_disk_cache/{id}
    private static final String CACHE_DIR = FileUtil.CACHE_IMAGE;
    //返回缓存文件路径/data/data/<application package>/cache/rtm_image_disk_cache/{id}
    public static String getCacheFile(Context context, String id) {
        //缓存目录为/data/data/<application package>/cache/rtm_image_disk_cache
        File parent = new File(context.getCacheDir(), CACHE_DIR);
        //若缓存根文件不存在，则创建
        if (!parent.exists()) {
            parent.mkdirs();
        }
        //返回缓存目录
        return new File(parent, id).getAbsolutePath();
    }

    //将bitmap数据转为字节数组
    public static byte[] bitmapToByteArray(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 10, baos);
        return baos.toByteArray();
    }
    //预加载图片：将指定文件内的数据以指定宽高加载，返回加载的字节数组
    public static byte[] preloadImage(Context context, String file, int width, int height) {
        try {
            Bitmap bitmap = Glide.with(context).asBitmap().encodeQuality(10).load(file).submit(width, height).get();
            return bitmapToByteArray(bitmap);
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }
    //上传图片
    public static void uploadImage(final Context context, RtmClient rtmClient, final String file, @NonNull final ResultCallback<RtmImageMessage> resultCallback) {
        rtmClient.createImageMessageByUploading(file, new RtmRequestId(), new ResultCallback<RtmImageMessage>() {
            //上传成功则将图片长宽缩小到1/5，并调用回调接口的success方法来处理缩略图
            @Override
            public void onSuccess(final RtmImageMessage rtmImageMessage) {
                int width = rtmImageMessage.getWidth() / 5;
                int height = rtmImageMessage.getWidth() / 5;
                rtmImageMessage.setThumbnail(ImageUtil.preloadImage(context, file, width, height));
                rtmImageMessage.setThumbnailWidth(width);
                rtmImageMessage.setThumbnailHeight(height);

                resultCallback.onSuccess(rtmImageMessage);
            }

            @Override
            public void onFailure(ErrorInfo errorInfo) {
                resultCallback.onFailure(errorInfo);
            }
        });
    }
    //缓存图片
    public static void cacheImage(Context context, RtmClient rtmClient, RtmImageMessage rtmImageMessage, @NonNull final ResultCallback<String> resultCallback) {
        //获取缓存文件
        final String cacheFile = getCacheFile(context, rtmImageMessage.getMediaId());
        //若文件存在，则调用参数对象的success方法
        if (new File(cacheFile).exists()) {
            resultCallback.onSuccess(cacheFile);
            //若文件不存在，则下载媒体文件到文件中
        } else {
            rtmClient.downloadMediaToFile(
                    rtmImageMessage.getMediaId(),
                    cacheFile,
                    new RtmRequestId(),
                    new ResultCallback<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            resultCallback.onSuccess(cacheFile);
                        }

                        @Override
                        public void onFailure(ErrorInfo errorInfo) {
                            resultCallback.onFailure(errorInfo);
                        }
                    }
            );
        }
    }
}