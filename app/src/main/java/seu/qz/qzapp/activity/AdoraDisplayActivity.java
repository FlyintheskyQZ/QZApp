package seu.qz.qzapp.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.RtcEngine;
import io.agora.rtc.video.VideoCanvas;
import io.agora.rtc.video.VideoEncoderConfiguration;
import seu.qz.qzapp.R;
import seu.qz.qzapp.entity.ProvideOrder;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;


public class AdoraDisplayActivity extends AppCompatActivity {

    private static final String TAG = AdoraDisplayActivity.class.getSimpleName();

    private static final int PERMISSION_REQ_ID = 22;

    // App 运行时确认麦克风和摄像头设备的使用权限。
    private static final String[] REQUESTED_PERMISSIONS = {
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    // 创建一个 SurfaceView 对象。
    //作为一个容器，记录当前频道内加入的用户
    private RelativeLayout mRemoteContainer;
    private SurfaceView mRemoteView;

    // 创建 SurfaceView 对象。
    FrameLayout mLocalContainer;
    SurfaceView mLocalView;

    //对应的订单对象
    ProvideOrder order;

    private RtcEngine mRtcEngine;
    private boolean mCallEnd;
    private boolean mMuted;

    private ImageView mCallBtn;
    private ImageView mMuteBtn;
    private ImageView mSwitchCameraBtn;
    private Button mStartBtn;
    //是否开启本地视图
    private int local_needed = 0;

    //对启动按钮的调控，只有开启视频后才能触发案件效果
    private int start_enbled = 0;
    //private LoggerRecyclerView mLogView;

    //注册一些事件发生时的回调函数（发生在线程中）
    private final IRtcEngineEventHandler mRtcEventHandler = new IRtcEngineEventHandler() {
        @Override
        // 注册 onJoinChannelSuccess 回调。
        // 本地用户成功加入频道时，会触发该回调。
        public void onJoinChannelSuccess(String channel, final int uid, int elapsed) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(start_enbled != -1){
                        start_enbled = 1;
                        mStartBtn.setEnabled(true);
                    }
                    Log.i("agora","Join channel success, uid: " + (uid & 0xFFFFFFFFL));
                }
            });
        }

        @Override
        // 注册 onFirstRemoteVideoDecoded 回调。
        // SDK 接收到第一帧远端视频并成功解码时，会触发该回调。
        // 可以在该回调中调用 setupRemoteVideo 方法设置远端视图。
        public void onFirstRemoteVideoDecoded(final int uid, int width, int height, int elapsed) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.i("agora","First remote video decoded, uid: " + (uid & 0xFFFFFFFFL));
                    setupRemoteVideo(uid);
                }
            });
        }

        @Override
        // 注册 onUserOffline 回调。
        // 远端用户离开频道或掉线时，会触发该回调。
        public void onUserOffline(final int uid, int reason) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.i("agora","User offline, uid: " + (uid & 0xFFFFFFFFL));
                    onRemoteUserLeft();
                }
            });
        }
    };



    // 初始化 RtcEngine 对象。
    private void initializeEngine() {
        try {
            mRtcEngine = RtcEngine.create(getBaseContext(), getString(R.string.agora_app_id), mRtcEventHandler);
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
            throw new RuntimeException("NEED TO check rtc sdk init fatal error\n" + Log.getStackTraceString(e));
        }
        if(mRtcEngine == null){
            Log.e(TAG,"mrtcEngine is null ------------------");
        }
        else {
            Log.e(TAG,"mrtcEngine is not null ------------------");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(mRtcEngine == null){
            Log.e(TAG,"mrtcEngine is null ------------------");
        }
        else {
            Log.e(TAG,"mrtcEngine is not null ------------------");
        }
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_agoratest);
        order = (ProvideOrder) getIntent().getSerializableExtra("order");
        initUI();
        // 获取权限后，初始化 RtcEngine，并加入频道。
        if (checkSelfPermission(REQUESTED_PERMISSIONS[0], PERMISSION_REQ_ID) &&
                checkSelfPermission(REQUESTED_PERMISSIONS[1], PERMISSION_REQ_ID) &&
                checkSelfPermission(REQUESTED_PERMISSIONS[2], PERMISSION_REQ_ID)) {
            initEngineAndJoinChannel();
        }
        registerListener();
    }

    private void registerListener() {
        mStartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(start_enbled == 1){
                    start_enbled = -1;
                    mStartBtn.setEnabled(false);
                    //发送指令到服务器，开启试验！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！
                }
            }
        });
    }

    private void initUI() {
        mLocalContainer = findViewById(R.id.local_video_view_container);
        mRemoteContainer = findViewById(R.id.remote_video_view_container);
        mCallBtn = findViewById(R.id.btn_call);
        mMuteBtn = findViewById(R.id.btn_mute);
        mSwitchCameraBtn = findViewById(R.id.btn_switch_camera);
        mStartBtn = findViewById(R.id.btn_start);
        mStartBtn.setEnabled(false);
        //mLogView = findViewById(R.id.log_recycler_view);
        // Sample logs are optional.
        //showSampleLogs();
    }



    private boolean checkSelfPermission(String permission, int requestCode) {
        if (ContextCompat.checkSelfPermission(this, permission) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, REQUESTED_PERMISSIONS, requestCode);
            return false;
        }
        return true;
    }

    /**
     * 对动态权限的申请结果处理
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQ_ID) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED ||
                    grantResults[1] != PackageManager.PERMISSION_GRANTED ||
                    grantResults[2] != PackageManager.PERMISSION_GRANTED) {
                showLongToast("Need permissions " + Manifest.permission.RECORD_AUDIO +

                        "/" + Manifest.permission.CAMERA + "/" + Manifest.permission.WRITE_EXTERNAL_STORAGE);
                finish();
                return;
            }
            // Here we continue only if all permissions are granted.
            // The permissions can also be granted in the system settings manually.
            initEngineAndJoinChannel();
        }
    }
    private void showLongToast(final String msg) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
            }
        });
    }

    //远端用户离开
    private void onRemoteUserLeft() {
        removeRemoteVideo();
    }

    private void removeRemoteVideo() {
        if (mRemoteView != null) {
            mRemoteContainer.removeView(mRemoteView);
        }
        // Destroys remote view
        mRemoteView = null;
    }

    private void initEngineAndJoinChannel() {
        initializeEngine();
        setupVideoConfig();
        setupLocalVideo();
        joinChannel();
    }

    private void setupVideoConfig() {
        // In simple use cases, we only need to enable video capturing
        // and rendering once at the initialization step.
        // Note: audio recording and playing is enabled by default.
        mRtcEngine.enableVideo();
        // Please go to this page for detailed explanation
        // https://docs.agora.io/en/Video/API%20Reference/java/classio_1_1agora_1_1rtc_1_1_rtc_engine.html#af5f4de754e2c1f493096641c5c5c1d8f
        mRtcEngine.setVideoEncoderConfiguration(new VideoEncoderConfiguration(
                VideoEncoderConfiguration.VD_640x360,
                VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_15,
                VideoEncoderConfiguration.STANDARD_BITRATE,
                VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_FIXED_PORTRAIT));
    }
    //设置本地视图
    private void setupLocalVideo() {

        // 启用视频模块。
        //mRtcEngine.enableVideo();
        if(local_needed == 1){
            mLocalView = RtcEngine.CreateRendererView(getBaseContext());
            mLocalView.setZOrderMediaOverlay(true);
            mLocalContainer.addView(mLocalView);
            // 设置本地视图。
            VideoCanvas localVideoCanvas = new VideoCanvas(mLocalView, VideoCanvas.RENDER_MODE_HIDDEN, 0);
            mRtcEngine.setupLocalVideo(localVideoCanvas);
        }

    }

    //加入频道
    private void joinChannel() {

        // 调用 joinChannel 方法 加入频道。
        String token = null;
        mRtcEngine.joinChannel(token, order.getDevice_id().toString(), "Extra Optional Data", 0);
    }

    //设置远端视图
    private void setupRemoteVideo(int uid) {
        mRemoteView = RtcEngine.CreateRendererView(getBaseContext());
        mRemoteContainer.addView(mRemoteView);
        // 设置远端视图。
        mRtcEngine.setupRemoteVideo(new VideoCanvas(mRemoteView, VideoCanvas.RENDER_MODE_HIDDEN, uid));

    }

    //停止发送本地音视频流
    public void onLocalAudioMuteClicked(View view) {
        mMuted = !mMuted;
        mRtcEngine.muteLocalAudioStream(mMuted);
        int res = mMuted ? R.drawable.btn_mute : R.drawable.btn_unmute;
        mMuteBtn.setImageResource(res);
    }

    //切换前后摄像头
    public void onSwitchCameraClicked(View view) {
        mRtcEngine.switchCamera();
    }

    public void onCallClicked(View view) {
        if (mCallEnd) {
            startCall();
            mCallEnd = false;
            mCallBtn.setImageResource(R.drawable.btn_endcall);
        } else {
            endCall();
            mCallEnd = true;
            mCallBtn.setImageResource(R.drawable.btn_startcall);
        }
        showButtons(!mCallEnd);

    }

    private void showButtons(boolean show) {
        int visibility = show ? View.VISIBLE : View.GONE;
        mMuteBtn.setVisibility(visibility);
        //不启动摄像头转换！！！！！！！！！！！！！！！
        //mSwitchCameraBtn.setVisibility(visibility);
    }

    private void startCall() {
        setupLocalVideo();
        joinChannel();
    }

    private void endCall() {
        removeLocalVideo();
        removeRemoteVideo();
        leaveChannel();
    }

    private void removeLocalVideo() {
        if (mLocalView != null) {
            mLocalContainer.removeView(mLocalView);
        }
        mLocalView = null;
    }

    private void leaveChannel() {
        // 离开当前频道。
        mRtcEngine.leaveChannel();
        if(start_enbled != -1){
            start_enbled = 0;
            mStartBtn.setEnabled(false);
        }
    }
    protected void onDestroy() {
        super.onDestroy();
        if (!mCallEnd) {
            leaveChannel();
        }
        RtcEngine.destroy();
    }

}