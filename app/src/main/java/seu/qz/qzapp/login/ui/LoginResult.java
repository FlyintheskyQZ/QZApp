package seu.qz.qzapp.login.ui;

import androidx.annotation.Nullable;

import seu.qz.qzapp.entity.AppCustomer;

/**
 * Authentication result : success (user details) or error message.
 * 用户信息验证结果类：
 *    二选一：
 *      验证成功后显示的用户信息
 *      登陆失败
 */
public class LoginResult {
    @Nullable
    private LoggedInUserView success;
    private AppCustomer customer;
    @Nullable
    private Integer error;

    LoginResult(@Nullable Integer error) {
        this.error = error;
    }

    public LoginResult(@Nullable LoggedInUserView success) {
        this.success = success;
    }

    public AppCustomer getCustomer() {
        return customer;
    }

    public void setCustomer(AppCustomer customer) {
        this.customer = customer;
    }

    @Nullable
    public LoggedInUserView getSuccess() {
        return success;
    }

    @Nullable
    public Integer getError() {
        return error;
    }
}