package seu.qz.qzapp.login.data;

/**
 * 注册用户对象类：
 *      用户Id,用户昵称(实际为username和password）
 *
 */
public class LoggedInUser {

    private String userId;
    private String displayName;

    public LoggedInUser(String userId, String displayName) {
        this.userId = userId;
        this.displayName = displayName;
    }

    public String getUserId() {
        return userId;
    }

    public String getDisplayName() {
        return displayName;
    }
}