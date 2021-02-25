package seu.qz.qzapp.register;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import org.litepal.LitePal;


import seu.qz.qzapp.R;
import seu.qz.qzapp.database.LitePalUtils;
import seu.qz.qzapp.entity.AppCustomer;
import seu.qz.qzapp.utils.StringFormatUtil;

/**
 * 注册ViewModel类，用户统一规划注册活动的数据
 */
public class RegisterViewModel extends ViewModel {
    //注册填写格式状态的MutableLiveData
    private MutableLiveData<RegisterFormState> registerFormState = new MutableLiveData<>();
    //注册结果的MutableLiveData
    private MutableLiveData<RegisterResult> registerResult = new MutableLiveData<>();
    //注册动作类
    private RegisterOptions options;

    public RegisterViewModel(RegisterOptions options) {
        this.options = options;
    }


    //注册方法：，由options的register（）创建线程进行网络通讯，通过Handler-Message机制实现异步方式在此处主线程中处理注册结果,如果注册成功则存储到数据库中
    public void register(AppCustomer customer, Context context){
        final RegisterResult result = new RegisterResult();

        Handler handler = new Handler(){
            @Override
            public void handleMessage(@NonNull Message msg) {
                switch (msg.what){
                    case 0:
                        result.setSuccess(false);
                        result.setErrorReason((String) msg.obj);
                        break;
                    case 1:
                       result.setSuccess(true);
                       AppCustomer customer_registered = (AppCustomer) msg.obj;
                       LitePalUtils.saveSingleCustomer(customer_registered);
                       result.setSuccessCustomer(customer_registered);
                       break;
                    default:
                        result.setSuccess(false);
                        result.setErrorReason(new String("不明原因出现问题"));
                        break;
                }
                registerResult.setValue(result);
            }
        };
        options.register(customer, context, handler);

    }

    public MutableLiveData<RegisterFormState> getRegisterFormState() {
        return registerFormState;
    }

    public MutableLiveData<RegisterResult> getRegisterResult() {
        return registerResult;
    }

    public RegisterOptions getOptions() {
        return options;
    }

    //若UI界面各个输入UI控件的内容发生变化，则进行判断是否合法，并给registerFormState赋值，触发观察者机制
    public void registerDataChanged(String username, String password, String realName, String realID,
                                 String phoneNumber, String email, boolean checked) {
        if (!isUserNameValid(username)) {
            registerFormState.setValue(new RegisterFormState(R.string.invalid_username, null,
                    null, null, null, null, null));
        } else if (!isPasswordValid(password)) {
            registerFormState.setValue(new RegisterFormState(null, R.string.invalid_password,
                    null, null, null, null, null));
        } else if(!isRealNameValid(realName)){
            registerFormState.setValue(new RegisterFormState(null, null,
                    R.string.invalid_realname, null, null, null, null));
        }else if(!isRealIDValid(realID)){
            registerFormState.setValue(new RegisterFormState(null, null,
                    null, R.string.invalid_realID, null, null, null));
        }else if(!isPhoneNumberValid(phoneNumber)){
            registerFormState.setValue(new RegisterFormState(null, null,
                    null, null, R.string.invalid_phonenumber, null, null));
        }else if(!isEmailValid(email)){
            registerFormState.setValue(new RegisterFormState(null, null,
                    null, null, null, R.string.invalid_email, null));
        }else if(!isSelectValid(checked)){
            registerFormState.setValue(new RegisterFormState(null, null,
                    null, null, null, null, R.string.invalid_select));
        }else{
            registerFormState.setValue(new RegisterFormState(true));
        }
    }

    private boolean isSelectValid(boolean checked){
        return checked;
    }

    private boolean isEmailValid(String email) {
        if(email == null){
            return false;
        }
        return StringFormatUtil.isValid(email.trim(), StringFormatUtil.REGULAR_EMAIL);
    }

    private boolean isPhoneNumberValid(String phoneNumber) {
        if(phoneNumber == null){
            return false;
        }
        return StringFormatUtil.isValid(phoneNumber.trim(), StringFormatUtil.REGULAR_CELLPHONE) ||
                StringFormatUtil.isValid(phoneNumber.trim(), StringFormatUtil.REGULAR_PHONE);
    }

    private boolean isRealIDValid(String realID) {
        if(realID == null){
            return false;
        }
        return StringFormatUtil.isValid(realID.trim(), StringFormatUtil.REGULAR_REALID);
    }

    private boolean isRealNameValid(String realName) {
        if(realName == null){
            return false;
        }
        return StringFormatUtil.isValid(realName.trim(), StringFormatUtil.REGULAR_REALNAME);
    }

    private boolean isPasswordValid(String password) {
        if(password == null){
            return false;
        }
        return StringFormatUtil.isValid(password.trim(), StringFormatUtil.REGULAR_PASSWORD);
    }

    private boolean isUserNameValid(String username) {
        if(username == null){
            return false;
        }
        return StringFormatUtil.isValid(username.trim(), StringFormatUtil.REGULAR_NICKNAME);
    }
}
