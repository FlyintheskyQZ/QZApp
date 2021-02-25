package seu.qz.qzapp.activity.task;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Map;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import seu.qz.qzapp.entity.FinishedOrder;
import seu.qz.qzapp.objectfactory.OkHttpFactory;
import seu.qz.qzapp.utils.PropertyUtil;

/**
 * pdf异步任务下载类
 */
public class DownloadPDFTask extends AsyncTask<String, Integer, Integer> {

    public static final int TYPE_SUCCESS = 0;
    public static final int TYPE_FAILED = 1;
    public static final int TYPE_PAUSED = 2;
    public static final int TYPE_CANCEDLED = 3;

    private DownloadPDFListener listener;
    private Context context;
    private FinishedOrder order;

    private boolean isCanceled = false;
    private boolean isPaused = false;

    private int lastProgress;

    public DownloadPDFTask(DownloadPDFListener listener, Context context, FinishedOrder order) {
        this.listener = listener;
        this.context = context;
        this.order = order;
    }

    //AsyncTask的后台异步线程，完成主要的PDF下载功能
    @Override
    protected Integer doInBackground(String... strings) {
        InputStream is = null;
        RandomAccessFile savedFile = null;
        File file = null;
        try {
            //记录已下载的字节数，实现断点下载的功能
            long downloadedLength = 0;
            //创建下载下来的pdf文件的存放地址
            //String fileName = downLoadUrl.substring(downLoadUrl.lastIndexOf("/"));
            //pdf文件的名称为用户nickname-order_id
            String fileName = order.getUser_name() + "-" + order.getOrder_id().toString();
            String directory =context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).getPath();
            file = new File(directory + "/" + fileName);
            //若该文件存在，则表示当前为接续上次下载
            if(file.exists()){
                downloadedLength = file.length();
            }
            //获取下载文件的大小
            long contentLength = getContentLength("downloadPDF");
            //若文件大小为0则标识无该文件，若文件大小与当前下载字节数相同则表示下载完成
            if(contentLength == 0){
                return TYPE_FAILED;
            }else if(contentLength == downloadedLength){
                return TYPE_SUCCESS;
            }
            //开启下载，以post方式发出请求，告知服务器当前的FinishedOrder的id以返回对应的PDF
            OkHttpClient client = new OkHttpClient();
            Map<String, String> settings = new HashMap<>();
            settings.put("order_id", order.getOrder_id().toString());
            RequestBody requestBody = OkHttpFactory.getRequestBodyWithSettings(settings);
            Request request = new Request.Builder()
                    //添加Header，指定从断点处开始下载
                    .addHeader("Range", "bytes=" + downloadedLength + "-")
                    .url(PropertyUtil.getUrl("downloadPDF", context))
                    .post(requestBody)
                    .build();
            Response response = client.newCall(request).execute();
            if(response != null){
                //获取文件流，找到断点处，循环下载
                is = response.body().byteStream();
                savedFile = new RandomAccessFile(file,"rw");
                savedFile.seek(downloadedLength);
                byte[] b = new byte[1024];
                int total = 0;
                int len;
                while ((len = is.read(b)) != -1){
                    if(isCanceled){
                        return TYPE_CANCEDLED;
                    }else if(isPaused){
                        return TYPE_PAUSED;
                    }else{
                        total += len;
                        savedFile.write(b, 0, len);
                        //同步更新下载进度到UI
                        int progress = (int)((total + downloadedLength) * 100 / contentLength);
                        publishProgress(progress);
                    }
                }
                response.body().close();
                return TYPE_SUCCESS;
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            try {
                if(is != null){
                    is.close();
                }
                if(savedFile != null){
                    savedFile.close();
                }
                //取消下载则删除文件
                if(isCanceled && file != null){
                    file.delete();
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return TYPE_FAILED;
    }


    //UI界面更新下载进度
    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        int progress = values[0];
        if(progress > lastProgress){
            listener.onProgress(progress);
            lastProgress = progress;
        }
    }

    @Override
    protected void onPostExecute(Integer integer) {
        super.onPostExecute(integer);
        switch (integer){
            case TYPE_SUCCESS:
                listener.onSuccess(context);
                break;
            case TYPE_FAILED:
                listener.onFailed(context);
                break;
            case TYPE_PAUSED:
                listener.onPaused();
                break;
            case TYPE_CANCEDLED:
                listener.onCanceled();
                break;
            default:break;
        }
    }

    //获取pdf文件的总长度
    private long getContentLength(String url_key) throws IOException {

        Map<String, String> settings = new HashMap<>();
        settings.put("order_id", order.getOrder_id().toString());
        OkHttpClient client = new OkHttpClient();
        RequestBody requestBody = OkHttpFactory.getRequestBodyWithSettings(settings);
        Request request = new Request.Builder()
                .url(PropertyUtil.getUrl(url_key, context))
                .post(requestBody)
                .build();
        Response response = client.newCall(request).execute();
        if(response != null && response.isSuccessful()){
            long contentLength = response.body().contentLength();
            response.body().close();
            return contentLength;
        }
        return 0;
    }

    public void pauseDownload(){
        isPaused = true;
    }

    public void cancelDownload(){
        isCanceled = true;
    }

}
