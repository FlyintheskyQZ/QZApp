package seu.qz.qzapp.activity.service;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.io.File;

import seu.qz.qzapp.R;
import seu.qz.qzapp.activity.OrderContentActivity;
import seu.qz.qzapp.activity.task.DownloadPDFListener;
import seu.qz.qzapp.activity.task.DownloadPDFTask;
import seu.qz.qzapp.activity.viewmodel.OrderContentViewModel;
import seu.qz.qzapp.entity.FinishedOrder;

public class DownloadService extends Service {

    private DownloadPDFTask downloadPDFTask;
    private FinishedOrder order;
    private DownloadPDFListener listener = new DownloadPDFListener() {

        //显示通知的方法
        @Override
        public void onProgress(int progress) {
            getNotificationManager().notify(1, getNotification("下载...", progress));
        }

        @Override
        public void onSuccess(Context context) {
            downloadPDFTask = null;
            //下载成功时关闭前台通知，并创建下载成功的通知
            stopForeground(true);
            getNotificationManager().notify(1, getNotification("下载成功！", -1));
            Toast.makeText(seu.qz.qzapp.activity.service.DownloadService.this, "成功下载PDF文件！", Toast.LENGTH_SHORT).show();
            OrderContentActivity activity = (OrderContentActivity) context;
            //置位OrderContentViewModel的DownloadingPDF，PDFDownloaded两个有关pdf图标按钮复用的标志变量
            activity.getOrderContentViewModel().setDownloadingPDF(false);
            activity.getOrderContentViewModel().setPDFDownloaded(true);
            activity.unbindDownloadService();
            activity.startBinderTask(3);
        }

        @Override
        public void onFailed(Context context) {
            downloadPDFTask = null;
            stopForeground(true);
            getNotificationManager().notify(1, getNotification("下载失败！", -1));
            OrderContentActivity activity = (OrderContentActivity) context;
            //置位OrderContentViewModel的DownloadingPDF，PDFDownloaded两个有关pdf图标按钮复用的标志变量
            activity.getOrderContentViewModel().setDownloadingPDF(false);
            activity.getOrderContentViewModel().setPDFDownloaded(true);
            Toast.makeText(seu.qz.qzapp.activity.service.DownloadService.this, "下载PDF文件失败！", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onPaused() {
            downloadPDFTask = null;
            Toast.makeText(seu.qz.qzapp.activity.service.DownloadService.this, "暂停下载PDF", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onCanceled() {
            downloadPDFTask = null;
            stopForeground(true);
            Toast.makeText(seu.qz.qzapp.activity.service.DownloadService.this, "已取消下载PDF文件！", Toast.LENGTH_SHORT).show();
        }
    };


    private seu.qz.qzapp.activity.service.DownloadService.DownLoadBinder binder = new seu.qz.qzapp.activity.service.DownloadService.DownLoadBinder();
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }


    public class DownLoadBinder extends Binder{

        //开始下载pdf
        public void startDownload(FinishedOrder finishedOrder, Context context){
            if(downloadPDFTask == null){
                order = finishedOrder;
                downloadPDFTask = new DownloadPDFTask(listener, context, order);
                downloadPDFTask.execute();
                startForeground(1, getNotification("下载...", 0));
                Toast.makeText(seu.qz.qzapp.activity.service.DownloadService.this, "开始下载PDF", Toast.LENGTH_SHORT).show();
            }
        }

        //暂停下载
        public void pauseDownload(){
            if(downloadPDFTask != null){
                downloadPDFTask.pauseDownload();
            }
        }

        public void cancelDownload(Context context){
            if(downloadPDFTask != null){
                downloadPDFTask.cancelDownload();
            }
            if(order != null){
                String fileName = order.getUser_name() + order.getOrder_id().toString();
                String directory =context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).getPath();
                File file = new File(directory + fileName);
                //若该文件存在，则表示当前为接续上次下载
                if(file.exists()){
                    file.delete();
                }
                getNotificationManager().cancel(1);
                stopForeground(true);
                Toast.makeText(seu.qz.qzapp.activity.service.DownloadService.this, "取消下载", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private NotificationManager getNotificationManager(){
        return (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }


    //获取指定Title的通知并设置其显示下载进度
    private Notification getNotification(String title, int progress){
        Intent intent = new Intent(this, OrderContentActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this, 0, intent, 0);
        NotificationChannel notificationChannel = new NotificationChannel("001", "downloadPDF", NotificationManager.IMPORTANCE_HIGH);
        getNotificationManager().createNotificationChannel(notificationChannel);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "001");

        builder.setSmallIcon(R.mipmap.odercontent_pdfdownload_notification)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(),R.mipmap.ordercontent_pdfdownload_largeicon))
                .setContentIntent(pi)
                .setContentTitle(title);
        if(progress >= 0){
            builder.setContentText(progress + "%");
            builder.setProgress(100, progress, false);
        }
        return builder.build();
    }
}

