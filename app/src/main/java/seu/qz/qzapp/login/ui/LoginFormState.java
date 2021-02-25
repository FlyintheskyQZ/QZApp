package seu.qz.qzapp.login.ui;

import androidx.annotation.Nullable;

/**
 * Data validation state of the login form.
 * 用户登录表单信息形式状态类：
 *      用户名格式错误
 *      密码格式错误
 *      格式是否验证通过
 *
 */
public class LoginFormState {
    @Nullable
    private Integer usernameError;
    @Nullable
    private Integer passwordError;
    private boolean isDataValid;

    public LoginFormState(@Nullable Integer usernameError, @Nullable Integer passwordError) {
        this.usernameError = usernameError;
        this.passwordError = passwordError;
        this.isDataValid = false;
    }

    LoginFormState(boolean isDataValid) {
        this.usernameError = null;
        this.passwordError = null;
        this.isDataValid = isDataValid;
    }

    @Nullable
    public Integer getUsernameError() {
        return usernameError;
    }

    @Nullable
    public Integer getPasswordError() {
        return passwordError;
    }

    public boolean isDataValid() {
        return isDataValid;
    }
}