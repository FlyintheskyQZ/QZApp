package seu.qz.qzapp.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Environment;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.IOException;

/**
 * 系统状态工具类：
 */
public class SystemStateUtil {

    //判断网络连接是否可用
    public static boolean isNetworkConnected(Context context){
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
            .getSystemService(Context.CONNECTIVITY_SERVICE);
            if (mConnectivityManager != null) {
                NetworkInfo info = mConnectivityManager.getActiveNetworkInfo();
                if (info != null && info.isConnected()){
                    // 当前网络是连接的
                    if (info.getState() == NetworkInfo.State.CONNECTED){
                        // 当前所连接的网络可用
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static void requestPermission(Activity context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(context, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE}, 0);

            }
        }
    }

    public static String isExistDir(Context context, String saveDir) throws IOException {
        // 下载位置
        File downloadFile = new File(context.getExternalFilesDir(null), saveDir);
        if (!downloadFile.mkdirs()) {
            downloadFile.createNewFile();
        }
        String savePath = downloadFile.getAbsolutePath();
        return savePath;
    }

}
