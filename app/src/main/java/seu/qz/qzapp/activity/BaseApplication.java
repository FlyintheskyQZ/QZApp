package seu.qz.qzapp.activity;

import android.app.Application;

import com.baidu.mapapi.CoordType;
import com.baidu.mapapi.SDKInitializer;

import org.litepal.LitePal;

import seu.qz.qzapp.agora.ChatManager;

public class BaseApplication extends Application {

    private static BaseApplication application;
    private ChatManager mChatManager;

    public static BaseApplication the() {
        return application;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // 初始化LitePal数据库
        LitePal.initialize(this);
        //初始化百度地图
        SDKInitializer.initialize(this);
        //包括BD09LL和GCJ02两种坐标，默认是BD09LL坐标。
        SDKInitializer.setCoordType(CoordType.BD09LL);
        //给Application对象赋值（单例）
        application = this;
        //创建chatmanager(单例）
        mChatManager = new ChatManager(this);
        //初始化chatManager
        mChatManager.init();

    }

    public ChatManager getChatManager() {
        return mChatManager;
    }
}
