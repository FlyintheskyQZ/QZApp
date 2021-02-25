package seu.qz.qzapp.activity.viewmodel;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import seu.qz.qzapp.R;
import seu.qz.qzapp.activity.ReportDisplayActivity;
import seu.qz.qzapp.activity.operation.ReportDisplayOperation;
import seu.qz.qzapp.database.LitePalUtils;
import seu.qz.qzapp.entity.AppCustomer;
import seu.qz.qzapp.entity.BriefReportItem;
import seu.qz.qzapp.main.ReportAdapter;

public class ReportDisplayViewModel extends ViewModel {

    ReportDisplayOperation operation = new ReportDisplayOperation();

    List<BriefReportItem> briefReportItems;

    AppCustomer mainCustomer;

    public void getItemsFromServer(final ReportAdapter adapter, final SwipeRefreshLayout refreshLayout, final Context context) {
        Handler handler = new Handler(){
            @Override
            public void handleMessage(@NonNull Message msg) {
                switch (msg.what){
                    case 0:
                        if(refreshLayout.isRefreshing()){
                            refreshLayout.setRefreshing(false);
                        }
                        AlertDialog.Builder dialog_netError = new AlertDialog.Builder(context);
                        dialog_netError.setTitle(R.string.order_error);
                        dialog_netError.setMessage(R.string.order_netError_content);
                        dialog_netError.setCancelable(false);
                        dialog_netError.setPositiveButton(R.string.order_netError_dialog_ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(Settings.ACTION_SETTINGS);
                                context.startActivity(intent);
                            }
                        });
                        dialog_netError.setNegativeButton(R.string.order_netError_dialog_cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });
                        dialog_netError.show();
                        break;
                    //返回为空，稍后再试
                    case 1:
                        if(refreshLayout.isRefreshing()){
                            refreshLayout.setRefreshing(false);
                        }
                        AlertDialog.Builder dialog_empty = new AlertDialog.Builder(context);
                        dialog_empty.setTitle(R.string.passwordsetting_updateError_empty_title);
                        dialog_empty.setMessage(R.string.passwordsetting_updateError_empty_message);
                        dialog_empty.setCancelable(false);
                        dialog_empty.setPositiveButton(R.string.order_netError_dialog_ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });
                        dialog_empty.show();
                        break;
                    //获取BriefReportItem成功
                    case 2:
                        if(refreshLayout.isRefreshing()){
                            refreshLayout.setRefreshing(false);
                        }
                        List<BriefReportItem> list = (List<BriefReportItem>) msg.obj;
                        initItemReportNames(list);
                        initItemDownloadStatus(list, context);
                        setBriefReportItems(list);
                        if(adapter != null){
                            adapter.setReportItems(list);
                            adapter.notifyDataSetChanged();
                        }
                        break;
                    case 3:
                        if(refreshLayout.isRefreshing()){
                            refreshLayout.setRefreshing(false);
                        }
                        //以通知的方法告知查询时出现错误
                        AlertDialog.Builder dialog_error = new AlertDialog.Builder(context);
                        dialog_error.setTitle(R.string.order_error);
                        dialog_error.setMessage(R.string.order_unknownError);
                        dialog_error.setCancelable(false);
                        dialog_error.setPositiveButton(R.string.order_netError_dialog_ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });
                        dialog_error.show();
                        break;
                    default:break;
                }
            }
        };
        operation.getBriefReportItems(mainCustomer.getUser_id().toString(), handler, context);
    }

    private void initItemReportNames(List<BriefReportItem> list) {
        if(list.size() == 0){
            return;
        }
        for(int i = 0; i < list.size(); i++){
            BriefReportItem item = list.get(i);
            item.setReport_name(mainCustomer.getUser_nickName() + "-" + item.getOrder_id());
        }
    }

    private void initItemDownloadStatus(List<BriefReportItem> list, Context context) {
        if(list.size() == 0){
            return;
        }
        String directory = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).getPath();
        File root_dir = new File(directory);
        if(!root_dir.exists()){
            return;
        }
        String[] pdf_files = root_dir.list();
        if(pdf_files.length == 0){
            return;
        }
        for(int i = 0; i < list.size(); i++){
            BriefReportItem item = list.get(i);
            String item_filename = mainCustomer.getUser_nickName() + "-" + item.getOrder_id();
            for(int j = 0; j < pdf_files.length; j++){
                if(pdf_files[j].equals(item_filename)){
                    item.setDownloaded(true);
                    break;
                }
            }
        }
    }


    public List<BriefReportItem> getBriefReportItems() {
        return briefReportItems;
    }

    public void setBriefReportItems(List<BriefReportItem> briefReportItems) {
        this.briefReportItems = briefReportItems;
    }

    public AppCustomer getMainCustomer() {
        return mainCustomer;
    }

    public void setMainCustomer(AppCustomer mainCustomer) {
        this.mainCustomer = mainCustomer;
    }


}
