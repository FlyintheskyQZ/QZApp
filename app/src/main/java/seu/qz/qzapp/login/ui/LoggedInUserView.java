package seu.qz.qzapp.login.ui;

/**
 * Class exposing authenticated user details to the UI.
 * 此类用于展示认证后的用户UI信息
 */
public class LoggedInUserView {
    private String displayName;
    //... other data fields that may be accessible to the UI

    public LoggedInUserView(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}