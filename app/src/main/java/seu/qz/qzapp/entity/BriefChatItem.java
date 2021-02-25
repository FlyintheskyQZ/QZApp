package seu.qz.qzapp.entity;

import java.io.Serializable;

public class BriefChatItem implements Serializable {

    //对方（与之通话的用户）的id
    int user_id;
    //对方的昵称
    String username;
    //是否是男性
    boolean isMale;
    //新消息数量
    int newsCount;


    public BriefChatItem(int user_id, String username, boolean isMale, int newsCount) {
        this.user_id = user_id;
        this.username = username;
        this.isMale = isMale;
        this.newsCount = newsCount;
    }

    public BriefChatItem(int user_id, String username, boolean isMale) {
        this.user_id = user_id;
        this.username = username;
        this.isMale = isMale;
        this.newsCount = 0;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public boolean isMale() {
        return isMale;
    }

    public void setMale(boolean male) {
        isMale = male;
    }

    public int getNewsCount() {
        return newsCount;
    }

    public void setNewsCount(int newsCount) {
        this.newsCount = newsCount;
    }
}
