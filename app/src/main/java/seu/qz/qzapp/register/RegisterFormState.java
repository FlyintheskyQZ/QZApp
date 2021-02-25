package seu.qz.qzapp.register;

/**
 * 注册界面的数据填写格式状态类：用于标志和管理注册界面各个输入类的UI控件的填写格式是否正确
 */
public class RegisterFormState {

    //昵称填写是否正确
    private Integer user_nickName_error;
    //密码填写是否正确
    private Integer user_password_error;
    //注册真实姓名填写是否正确
    private Integer user_registerName_error;
    //身份证号填写是否正确
    private Integer user_identityId_error;
    //电话号码（手机或者座机，座机需要写区号，区号与座机号之间的“-”可加可不加）填写是否正确
    private Integer phoneNumber_error;
    //Email地址填写是否正确
    private Integer email_error;
    //用户注册类型是否选择（普通用户或者商家）
    private Integer select_error;
    //整体填写状态是否均正确
    private boolean isDataValid;

    public RegisterFormState() {
    }

    public RegisterFormState(Integer user_nickName_error, Integer user_password_error, Integer user_registerName_error,
                             Integer user_identityId_error, Integer phoneNumber_error, Integer email_error, Integer select_error) {
        this.user_nickName_error = user_nickName_error;
        this.user_password_error = user_password_error;
        this.user_registerName_error = user_registerName_error;
        this.user_identityId_error = user_identityId_error;
        this.phoneNumber_error = phoneNumber_error;
        this.email_error = email_error;
        this.select_error = select_error;
        this.isDataValid = false;
    }

    public RegisterFormState(boolean isDataValid) {
        this.user_nickName_error = null;
        this.user_password_error = null;
        this.user_registerName_error = null;
        this.user_identityId_error = null;
        this.phoneNumber_error = null;
        this.email_error = null;
        this.select_error = null;
        this.isDataValid = isDataValid;
    }

    public Integer getPhoneNumber_error() {
        return phoneNumber_error;
    }

    public Integer getEmail_error() {
        return email_error;
    }

    public Integer getUser_nickName_error() {
        return user_nickName_error;
    }

    public Integer getUser_password_error() {
        return user_password_error;
    }

    public Integer getUser_registerName_error() {
        return user_registerName_error;
    }

    public Integer getUser_identityId_error() {
        return user_identityId_error;
    }

    public Integer getSelect_error() {
        return select_error;
    }

    public boolean isDataValid() {
        return isDataValid;
    }
}
