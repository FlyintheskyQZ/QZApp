package seu.qz.qzapp.activity.viewmodel;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.view.View;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;

import seu.qz.qzapp.R;
import seu.qz.qzapp.activity.RegisterDeviceActivity;
import seu.qz.qzapp.activity.operation.RegisterDeviceOperation;
import seu.qz.qzapp.database.LitePalUtils;
import seu.qz.qzapp.entity.AppCustomer;
import seu.qz.qzapp.entity.LOIInstrument;

public class RegisterDeviceViewModel extends ViewModel {

    RegisterDeviceOperation operation = new RegisterDeviceOperation();

    AppCustomer mainCustomer;


    public AppCustomer getMainCustomer() {
        return mainCustomer;
    }

    public void setMainCustomer(AppCustomer mainCustomer) {
        this.mainCustomer = mainCustomer;
    }

    public void registerDevice(LOIInstrument new_device, final ProgressBar progressBar, final Context context) {
        Handler handler = new Handler(){
            @Override
            public void handleMessage(@NonNull Message msg) {
                switch (msg.what){
                    case 0:
                        if(progressBar != null){
                            progressBar.setVisibility(View.GONE);
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
                        if(progressBar != null){
                            progressBar.setVisibility(View.GONE);
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
                    //修改成功
                    case 2:
                        if(progressBar != null){
                            progressBar.setVisibility(View.GONE);
                        }
                        String new_device_id = (String) msg.obj;
                        String related_devices = mainCustomer.getRelated_device_id();
                        if(related_devices == null || related_devices.isEmpty()){
                            mainCustomer.setRelated_device_id(new_device_id);
                        }else {
                            related_devices = related_devices + ";" + new_device_id;
                            mainCustomer.setRelated_device_id(related_devices);
                        }
                        LitePalUtils.saveSingleCustomer(mainCustomer);
                        AlertDialog.Builder dialog_success = new AlertDialog.Builder(context);
                        dialog_success.setTitle(R.string.registerdevice_Success_title);
                        dialog_success.setMessage(R.string.registerdevice_Success_message);
                        dialog_success.setCancelable(false);
                        dialog_success.setPositiveButton(R.string.registerdevice_Success_positive, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                RegisterDeviceActivity activity = (RegisterDeviceActivity) context;
                                activity.setResult(1);
                                activity.finish();
                            }
                        });
                        dialog_success.show();
                        break;
                    case 3:
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
        operation.registerDevice(new_device, handler, context);
    }
}
