package seu.qz.qzapp.activity.viewmodel;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.List;

import seu.qz.qzapp.R;
import seu.qz.qzapp.activity.DeviceDisplayActivity;
import seu.qz.qzapp.activity.operation.DeviceDisplayOperation;
import seu.qz.qzapp.entity.AppCustomer;
import seu.qz.qzapp.entity.BriefReportItem;
import seu.qz.qzapp.entity.LOIInstrument;
import seu.qz.qzapp.main.DeviceAdapter;

public class DeviceDisplayViewModel extends ViewModel {

    DeviceDisplayOperation operation = new DeviceDisplayOperation();

    List<LOIInstrument> loiInstruments;

    AppCustomer mainCustomer;


    public AppCustomer getMainCustomer() {
        return mainCustomer;
    }

    public void setMainCustomer(AppCustomer mainCustomer) {
        this.mainCustomer = mainCustomer;
    }

    public List<LOIInstrument> getLoiInstruments() {
        return loiInstruments;
    }

    public void setLoiInstruments(List<LOIInstrument> loiInstruments) {
        this.loiInstruments = loiInstruments;
    }

    public void getInstrumentsFromServer(final DeviceAdapter adapter, final SwipeRefreshLayout refreshLayout, final Context context) {
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
                        List<LOIInstrument> list = (List<LOIInstrument>) msg.obj;
                        if(adapter != null){
                            adapter.setInstruments(list);
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
        operation.getLOIInstrumentsBySalerId(mainCustomer.getUser_id().toString(), handler, context);
    }
}
