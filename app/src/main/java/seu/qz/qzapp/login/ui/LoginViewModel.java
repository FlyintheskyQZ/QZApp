package seu.qz.qzapp.login.ui;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import android.content.Context;
import android.util.Patterns;

import seu.qz.qzapp.R;
import seu.qz.qzapp.database.LitePalUtils;
import seu.qz.qzapp.entity.AppCustomer;
import seu.qz.qzapp.login.data.LoginRepository;
import seu.qz.qzapp.login.data.Result;
import seu.qz.qzapp.login.data.LoggedInUser;
import seu.qz.qzapp.login.ui.LoggedInUserView;
import seu.qz.qzapp.login.ui.LoginFormState;
import seu.qz.qzapp.login.ui.LoginResult;
import seu.qz.qzapp.utils.SharedPreferencesUtils;

/**
 * 定义一个ViewModel用于存储和管理相关的UI数据，其中数据请求部分的功能由LoginRepository实现，这里只在login方法中调用
 *      维护两个LiveData用于通过观察者模式实现数据更新的实时通知，从而显示到UI上：
 *          表单内容状态
 *          登录结果状态
 *      维护一个LoginRepository，用于实现数据请求
 */
public class LoginViewModel extends ViewModel {

    //MutableLiveData<T>为当数据有变化时，可以更新数据，为观察者模式中的被观察者，可以通知其观察者回调函数调用
    private MutableLiveData<LoginFormState> loginFormState = new MutableLiveData<>();
    private MutableLiveData<LoginResult> loginResult = new MutableLiveData<>();
    private LoginRepository loginRepository;
    private AppCustomer mainCustomer;

    LoginViewModel(LoginRepository loginRepository) {
        this.loginRepository = loginRepository;
    }

    public LiveData<LoginFormState> getLoginFormState() {
        return loginFormState;
    }

    public LiveData<LoginResult> getLoginResult() {
        return loginResult;
    }

    //调用LoginRepository的方法进行数据请求
    public void login(String username, String password, Context context, boolean saveAfterSuccess) {
        // can be launched in a separate asynchronous job
        Result<LoggedInUser> result = loginRepository.login(username, password, context);

        if (result instanceof Result.Success) {
            LoggedInUser data = ((Result.Success<LoggedInUser>) result).getData();
            LoginResult login_result = new LoginResult(new LoggedInUserView(data.getDisplayName()));
            AppCustomer customer = LitePalUtils.getSingleCustomer(username);
            login_result.setCustomer(customer);
            setMainCustomer(customer);
            loginResult.setValue(login_result);
            if(saveAfterSuccess){
                SharedPreferencesUtils.storeAppCustomer(context, username, password);
                System.out.println("the new Customer is saved");
            }
            else{
                SharedPreferencesUtils.deleteAppCustomer(context);
            }
        } else {
            loginResult.setValue(new LoginResult(R.string.login_failed));
        }
    }

    //更新LiveData中的数据
    public void loginDataChanged(String username, String password) {
        if (!isUserNameValid(username)) {
            loginFormState.setValue(new LoginFormState(R.string.invalid_username, null));
        } else if (!isPasswordValid(password)) {
            loginFormState.setValue(new LoginFormState(null, R.string.invalid_password));
        } else {
            loginFormState.setValue(new LoginFormState(true));
        }
    }

    // A placeholder username validation check
    //判断用户名是否合法
    private boolean isUserNameValid(String username) {
        if (username == null) {
            return false;
        }
        if (username.contains("@")) {
            //如果包含@则进行Email地址的匹配（通过邮件地址进行登录）
            return Patterns.EMAIL_ADDRESS.matcher(username).matches();
        } else {
            //返回username删除前后空格后是否为空
            return !username.trim().isEmpty();
        }
    }

    // A placeholder password validation check
    //判断密码是否合法
    private boolean isPasswordValid(String password) {
        return password != null && password.trim().length() > 5;
    }

    public AppCustomer getMainCustomer() {
        return mainCustomer;
    }

    public void setMainCustomer(AppCustomer mainCustomer) {
        this.mainCustomer = mainCustomer;
    }
}