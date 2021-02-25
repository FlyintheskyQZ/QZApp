package seu.qz.qzapp.activity.task;


import android.content.Context;

/**
 * PDF下载监听回调接口
 */
public interface DownloadPDFListener {

    void onProgress(int progress);

    void onSuccess(Context context);

    void onFailed(Context context);

    void onPaused();

    void onCanceled();
}
