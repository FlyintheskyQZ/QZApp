package seu.qz.qzapp.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.lifecycle.ViewModelProviders;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import seu.qz.qzapp.R;
import seu.qz.qzapp.activity.service.DownloadService;
import seu.qz.qzapp.activity.viewmodel.OrderContentViewModel;
import seu.qz.qzapp.agora.ChatManager;
import seu.qz.qzapp.agora.MessageBean;
import seu.qz.qzapp.agora.MessageListBean;
import seu.qz.qzapp.database.LitePalUtils;
import seu.qz.qzapp.entity.AppCustomer;
import seu.qz.qzapp.entity.BriefChatItem;
import seu.qz.qzapp.entity.BriefOrderItem;
import seu.qz.qzapp.entity.FinishedOrder;
import seu.qz.qzapp.main.ChatAdapter;
import seu.qz.qzapp.utils.MessageUtil;

public class OrderContentActivity extends AppCompatActivity {

    private static final String TAG = "OrderContentActivity";
    //DownloadService相关，用于下载PDF
    private DownloadService.DownLoadBinder downLoadBinder;
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            downLoadBinder = (DownloadService.DownLoadBinder) service;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    //判断是否绑定了DownloadService
    private boolean isServiceBinded = false;

    private OrderContentViewModel orderContentViewModel;


    ScrollView orderContent_scrollview;
    View orderContent_background1;
    ImageView orderContent_stateIcon;
    TextView orderContent_orderState;
    ImageView orderContent_icon2;
    TextView orderContent_merchant_nameAndPhone;
    ImageButton orderContent_createChat;
    TextView orderContent_factoryAndMachine;
    TextView orderContent_merchant_location;
    TextView orderContent_orderNumber;
    TextView orderContent_orderPlaced_time;
    TextView orderContent_rentTime_begin;
    TextView orderContent_rentTime_end;
    TextView orderContent_price;
    ImageView orderContent_icon4;
    //ImageButton orderContent_orderModifyButton;
    TextView orderContent_noData;
    TextView orderContent_orderResult;
    TextView orderContent_materialName;
    TextView orderContent_materialType;
    TextView orderContent_operatorName;
    ImageButton orderContent_pdfButton;
    ImageButton orderContent_pdf_email;
    TextView orderContent_approvalError;
    ImageButton orderContent_place;
    TextView orderContent_place_text;
    ImageButton orderContent_cancel;
    TextView orderContent_cancel_text;
    ImageButton orderContent_video;

    Toolbar orderContent_toolbar;
    ProgressBar orderContent_sendingEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_order_content);
        orderContent_scrollview = findViewById(R.id.ordercontent_scrollview);
        orderContent_background1 = findViewById(R.id.ordercontent_background1);
        orderContent_stateIcon = findViewById(R.id.ordercontent_stateicon);
        orderContent_orderState = findViewById(R.id.ordercontent_orderstate);
        orderContent_merchant_nameAndPhone = findViewById(R.id.ordercontent_merchant_nameandphone);
        orderContent_createChat = findViewById(R.id.ordercontent_createChat);
        orderContent_factoryAndMachine = findViewById(R.id.ordercontent_factoryandmachine);
        orderContent_merchant_location = findViewById(R.id.ordercontent_merchant_location);
        orderContent_orderNumber = findViewById(R.id.ordercontent_ordernumber);
        orderContent_orderPlaced_time = findViewById(R.id.ordercontent_orderplaced_time);
        orderContent_rentTime_begin = findViewById(R.id.ordercontent_renttime_begin);
        orderContent_rentTime_end = findViewById(R.id.ordercontent_renttime_end);
        orderContent_price = findViewById(R.id.ordercontent_price);
        orderContent_icon4 = findViewById(R.id.ordercontent_icon4);
        //orderContent_orderModifyButton = findViewById(R.id.ordercontent_orderModifyButton);
        orderContent_noData = findViewById(R.id.ordercontent_nodata);
        orderContent_orderResult = findViewById(R.id.ordercontent_orderresult);
        orderContent_materialName = findViewById(R.id.ordercontent_materialname);
        orderContent_materialType = findViewById(R.id.ordercontent_materialtype);
        orderContent_operatorName = findViewById(R.id.ordercontent_operatorname);
        orderContent_pdfButton = findViewById(R.id.ordercontent_pdfbutton);
        orderContent_pdf_email = findViewById(R.id.ordercontent_pdf_email);
        orderContent_place = findViewById(R.id.ordercontent_place);
        orderContent_place_text = findViewById(R.id.ordercontent_place_text);
        orderContent_cancel = findViewById(R.id.ordercontent_cancel);
        orderContent_approvalError = findViewById(R.id.ordercontent_approvalerror);
        orderContent_cancel_text = findViewById(R.id.ordercontent_cancel_text);
        orderContent_sendingEmail = findViewById(R.id.ordercontent_sendingEmail);
        orderContent_icon2 = findViewById(R.id.ordercontent_icon2);
        orderContent_video = findViewById(R.id.ordercontent_video);

        initToolbar();
        initScrollView();
        initViewModel();
        registerListener();


    }

    //一些通用的监听器，不必区分ProvideOrder或是FinishedOrder，也不必区分saler或是user
    private void registerListener() {
        orderContent_merchant_nameAndPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder call_phone = new AlertDialog.Builder(OrderContentActivity.this);
                call_phone.setTitle(R.string.orderContent_callPhone_title);
                call_phone.setMessage(R.string.orderContent_callPhone_message);
                call_phone.setPositiveButton(R.string.orderContent_callPhone_positive, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Intent.ACTION_DIAL);
                        String phone_number = orderContentViewModel.getCustomer_opposite().getPhoneNumber();
                        Uri data = Uri.parse("tel:" + phone_number);
                        intent.setData(data);
                        startActivity(intent);
                    }
                });
                call_phone.setNegativeButton(R.string.orderContent_callPhone_negative, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                call_phone.setCancelable(false);
                call_phone.show();
            }
        });
        orderContent_createChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<MessageListBean> listBeans = MessageUtil.getMessageListBeanList();
                AppCustomer customer_owner = orderContentViewModel.getCustomer_owner();
                AppCustomer customer_opposite = orderContentViewModel.getCustomer_opposite();
                int chat_exist = MessageUtil.existMessageListBean(customer_opposite.getUser_id().toString());
                if(chat_exist >= 0){
                    //将聊天缓存文件列表重新排序，置顶当前要聊天的对象文件，再进入聊天界面
                    MessageListBean listBean = listBeans.get(chat_exist);
                    BriefChatItem chatItem = listBean.getChatItem();
                    chatItem.setNewsCount(0);
                }else {
                    BriefChatItem new_item = new BriefChatItem(customer_opposite.getUser_id(),
                            customer_opposite.getUser_nickName(), customer_opposite.isMale(), 0);
                    List<MessageBean> new_list = new ArrayList<>();
                    MessageListBean new_bean = new MessageListBean(String.valueOf(customer_opposite.getUser_id()), new_list, new_item);
                    MessageUtil.addMessageListBeanList(new_bean);
                }
                Intent intent = new Intent(OrderContentActivity.this, AgoraChatActivity.class);
                intent.putExtra("username", customer_owner.getUser_nickName());
                intent.putExtra("peerId", String.valueOf(customer_opposite.getUser_id()));
                //      intent.putExtra("briefOrderItem", item);
                startActivity(intent);
            }
        });
        orderContent_icon2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OrderContentActivity.this, DeviceLocationDisplayActivity.class);
                intent.putExtra("item", orderContentViewModel.getItem());
                intent.putExtra("registerdevice", false);
                startActivity(intent);
            }
        });

    }

    private void initToolbar(){
        orderContent_toolbar = findViewById(R.id.ordercontent_toolbar);
        setSupportActionBar(orderContent_toolbar);
    }

    private  void initScrollView(){
        //给SrollView注册监听器
        orderContent_scrollview.setOnScrollChangeListener(new ScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                //获取background1的图片高度
                float scroll_height = orderContent_background1.getHeight();
                //根据滑动的距离实现toolbar的透明化
                if(oldScrollY < scroll_height){
                    //由已经滑动的距离oldScrollY与背景图片的壁纸来确定透明化的量值（0~255）
                    int i = Float.valueOf(oldScrollY/scroll_height).intValue();
                    orderContent_toolbar.getBackground().setAlpha(i > 0 ? i : 0);//考虑到顶端下拉，i可能小于0
                }else {
                    orderContent_toolbar.getBackground().setAlpha(255);
                }
            }
        });
    }

    //初始化viewModel
    protected void initViewModel(){
        orderContentViewModel = ViewModelProviders.of(this).get(OrderContentViewModel.class);
        orderContentViewModel.setCustomer_owner((AppCustomer) (LitePalUtils.getSingleCustomer(getIntent().getStringExtra("username"))));
        orderContentViewModel.setCustomerClassification(orderContentViewModel.getCustomer_owner().getAuthority_level().intValue() == 1);
        orderContentViewModel.setItem((BriefOrderItem) getIntent().getSerializableExtra("briefOrderItem"));
        getOrderInformation();
    }

    public void startSettingActivity(String userName, int provide_orderId, int requestCode){
        Intent intent = new Intent(this, OrderSettingActivity.class);
        intent.putExtra("username", userName);
        intent.putExtra("order_id", provide_orderId);
        //ambition:1,创建订单;2,修改、查看、审核订单。
        intent.putExtra("ambition", 2);
        startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //添加回调方法：OrderSettingActivity的回调，需要进行内容刷新！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！
        switch (requestCode){
            //对应于查看、修改、审核
            case 1:
                switch (resultCode){

                    case 0:
                        break;
                        //OrderSettingActivity根据order_id查询到的订单为空
                    case 1:
                        Toast.makeText(this, "查询订单出现错误，可稍后重新查看！", Toast.LENGTH_SHORT).show();
                        break;
                        //查询ProvideOrder时出现异常
                    case 2:
                        Toast.makeText(this, "查询订单出现异常，可稍后重新查看！", Toast.LENGTH_SHORT).show();
                        break;
                    case 3:
                        //更新订单成功，需要刷新本活动
                        refresh();
                        break;
                        //更新订单成功，需要刷新本活动
                    case 4:
                        refresh();
                        break;
                    //传给OrderSettingActivity的order_id为空
                    case 5:
                        finish();
                        break;
                    default:break;
                }
                break;
                //对应于创建新订单
            case 2:
                switch (resultCode){
                    //未注册就创建订单，返回！
                    case 1:
                        //考虑添加直接跳转到注册仪器的界面！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！
                        break;
                    case 3:
                        //未找到仪器
                        break;
                    default:break;
                }
            default:break;
        }
    }

    private void refresh(){
        orderContentViewModel.refresh(this);
    }


    //Toolbar的菜单显示
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.order_content_toolbar, menu);
        return true;
    }

    private void getOrderInformation() {
        orderContentViewModel.getOrderInformation(this);
    }


    //Toolbar的按键触发
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()){
            //设置返回按钮
            case android.R.id.home:
                onBackPressed();
            default:
                break;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            //对下载PDF权限的申请WRITE_EXTERNAL_STORAGE,READ_EXTERNAL_STORAGE
            case 10:
                boolean hasPermissionDismiss = false;
                for (int i = 0; i < grantResults.length; i++) {
                    if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                        hasPermissionDismiss = true;
                        break;
                    }
                }
                //若申请失败则弹框提醒
                if(hasPermissionDismiss){
                    AlertDialog.Builder dialog_error = new AlertDialog.Builder(this);
                    dialog_error.setTitle("缺少授权！");
                    dialog_error.setMessage("无授权不可下载PDF文件！");
                    dialog_error.setCancelable(false);
                    dialog_error.setPositiveButton("好的", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
                    dialog_error.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
                    dialog_error.show();
                }else {
                    //若申请成功，则开启DownLoadService中Binder中的下载程序
                    startBinderTask(2);
                }
                break;
            default:break;
        }
    }

    public DownloadService.DownLoadBinder getDownLoadBinder() {
        return downLoadBinder;
    }

    public void startBinderTask(int task_type){
        final Activity activity = this;
        switch (task_type){
            //开始下载PDF
            case 0:
                //动态申请权限WRITE_EXTERNAL_STORAGE,READ_EXTERNAL_STORAGE
                String[] permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE};
                List<String> mPermissionList = null;
                for (int i = 0; i < permissions.length; i++) {
                    if (ContextCompat.checkSelfPermission(this, permissions[i]) != PackageManager.PERMISSION_GRANTED) {
                        if(mPermissionList == null){
                            mPermissionList = new ArrayList<>();
                        }
                        mPermissionList.add(permissions[i]);//添加还未授予的权限
                    }
                }
                //若有未申请的权限则申请，否则直接执行下载
                if(mPermissionList != null){
                    ActivityCompat.requestPermissions(this, permissions, 10);
                }else {
                    startBinderTask(2);
                }
                break;
                //暂停或者取消下载PDF
            case 1:
                AlertDialog.Builder dialog_cancelOrPause = new AlertDialog.Builder(this);
                dialog_cancelOrPause.setTitle(R.string.orderContent_pdf_cancelOrPause);
                dialog_cancelOrPause.setMessage(R.string.orderContent_pdf_cancelOrPause_text);
                dialog_cancelOrPause.setCancelable(false);
                dialog_cancelOrPause.setPositiveButton(R.string.orderContent_pdf_pause, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        downLoadBinder.pauseDownload();
                    }
                });
                dialog_cancelOrPause.setNegativeButton(R.string.orderContent_pdf_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        downLoadBinder.cancelDownload(activity);
                    }
                });
                dialog_cancelOrPause.show();
                break;
                //继续下载PDF
            case 2:
                if(!isServiceBinded){
                    //绑定DownloadService
                    Intent intent = new Intent(OrderContentActivity.this, DownloadService.class);
                    startForegroundService(intent);
                    bindService(intent, connection, BIND_AUTO_CREATE);//绑定DownloadService
                    isServiceBinded = true;
                }
                int content_message;
                if(orderContentViewModel.isPDFExisted(activity)){
                    content_message = R.string.orderContent_pdf_download_still;
                }else {
                    content_message = R.string.orderContent_pdf_downloadConfrim_text;
                }
                AlertDialog.Builder dialog_downloadConfirm = new AlertDialog.Builder(this);
                dialog_downloadConfirm.setTitle(R.string.orderContent_pdf_downloadConfrim);
                dialog_downloadConfirm.setMessage(content_message);
                dialog_downloadConfirm.setCancelable(false);
                dialog_downloadConfirm.setPositiveButton(R.string.orderContent_pdf_downloadConfrim_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        downLoadBinder.startDownload(orderContentViewModel.getFinishedOrder(), OrderContentActivity.this);
                    }
                });
                dialog_downloadConfirm.show();
                break;
                //下载完成，跳出弹框，选择跳转到PDF阅读器
            case 3:
                FinishedOrder finishedOrder = orderContentViewModel.getFinishedOrder();
                String fileName = finishedOrder.getUser_name() + "-" + finishedOrder.getOrder_id().toString();
                String directory =getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).getPath();
                File file = new File(directory + "/" + fileName);
                Intent intent_PDFReader = new Intent(Intent.ACTION_VIEW);
                //判断Android版本，Android7.0以后应用文件提供给外部程序需要进行URI映射和临时权限的赋予
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    intent_PDFReader.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    Uri contentUri = FileProvider.getUriForFile(this, "seu.qz.qzapp.fileprovider",
                            file);
                    intent_PDFReader.setDataAndType(contentUri, "application/pdf");
                } else {
                    Uri uri = Uri.fromFile(file);
                    intent_PDFReader.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent_PDFReader.setDataAndType(uri, "application/pdf");
                }
                try {
                    startActivity(intent_PDFReader);
                } catch (ActivityNotFoundException e) {
                    e.printStackTrace();
                }
                break;
            default:break;
        }
    }

    public void unbindDownloadService(){
        Intent stopService = new Intent(this, DownloadService.class);
        stopService(stopService);
        unbindService(connection);
        isServiceBinded = false;
    }

    @Override
    protected void onStart() {
        super.onStart();
        AppCustomer mainCustomer = LitePalUtils.getSingleCustomer(orderContentViewModel.getCustomer_owner().getUser_nickName());
        orderContentViewModel.getCustomer_owner().adjustSelf(mainCustomer);
        Log.i(TAG, "MainActivity_onStart: 启动！！！！！！！！！！！！！！！！！！！！！！！！！!!");
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(isServiceBinded){
            unbindDownloadService();
        }
    }

    public ImageView getOrderContent_icon4() {
        return orderContent_icon4;
    }

    public void setOrderContent_icon4(ImageView orderContent_icon4) {
        this.orderContent_icon4 = orderContent_icon4;
    }

    public ImageButton getOrderContent_place() {
        return orderContent_place;
    }

    public void setOrderContent_place(ImageButton orderContent_place) {
        this.orderContent_place = orderContent_place;
    }

    public TextView getOrderContent_place_text() {
        return orderContent_place_text;
    }

    public void setOrderContent_place_text(TextView orderContent_place_text) {
        this.orderContent_place_text = orderContent_place_text;
    }

    public ImageButton getOrderContent_cancel() {
        return orderContent_cancel;
    }

    public void setOrderContent_cancel(ImageButton orderContent_cancel) {
        this.orderContent_cancel = orderContent_cancel;
    }

    public TextView getOrderContent_cancel_text() {
        return orderContent_cancel_text;
    }

    public void setOrderContent_cancel_text(TextView orderContent_cancel_text) {
        this.orderContent_cancel_text = orderContent_cancel_text;
    }

    public TextView getOrderContent_approvalError() {
        return orderContent_approvalError;
    }

    public void setOrderContent_approvalError(TextView orderContent_approvalError) {
        this.orderContent_approvalError = orderContent_approvalError;
    }



    public OrderContentViewModel getOrderContentViewModel() {
        return orderContentViewModel;
    }

    public void setOrderContentViewModel(OrderContentViewModel orderContentViewModel) {
        this.orderContentViewModel = orderContentViewModel;
    }

    public ImageView getOrderContent_stateIcon() {
        return orderContent_stateIcon;
    }

    public void setOrderContent_stateIcon(ImageView orderContent_stateIcon) {
        this.orderContent_stateIcon = orderContent_stateIcon;
    }

    public TextView getOrderContent_orderState() {
        return orderContent_orderState;
    }

    public void setOrderContent_orderState(TextView orderContent_orderState) {
        this.orderContent_orderState = orderContent_orderState;
    }

    public TextView getOrderContent_merchant_nameAndPhone() {
        return orderContent_merchant_nameAndPhone;
    }

    public void setOrderContent_merchant_nameAndPhone(TextView orderContent_merchant_nameAndPhone) {
        this.orderContent_merchant_nameAndPhone = orderContent_merchant_nameAndPhone;
    }

    public TextView getOrderContent_factoryAndMachine() {
        return orderContent_factoryAndMachine;
    }

    public void setOrderContent_factoryAndMachine(TextView orderContent_factoryAndMachine) {
        this.orderContent_factoryAndMachine = orderContent_factoryAndMachine;
    }

    public TextView getOrderContent_merchant_location() {
        return orderContent_merchant_location;
    }

    public void setOrderContent_merchant_location(TextView orderContent_merchant_location) {
        this.orderContent_merchant_location = orderContent_merchant_location;
    }

    public TextView getOrderContent_orderNumber() {
        return orderContent_orderNumber;
    }

    public void setOrderContent_orderNumber(TextView orderContent_orderNumber) {
        this.orderContent_orderNumber = orderContent_orderNumber;
    }

    public TextView getOrderContent_orderPlaced_time() {
        return orderContent_orderPlaced_time;
    }

    public void setOrderContent_orderPlaced_time(TextView orderContent_orderPlaced_time) {
        this.orderContent_orderPlaced_time = orderContent_orderPlaced_time;
    }

    public TextView getOrderContent_rentTime_begin() {
        return orderContent_rentTime_begin;
    }

    public void setOrderContent_rentTime_begin(TextView orderContent_rentTime_begin) {
        this.orderContent_rentTime_begin = orderContent_rentTime_begin;
    }

    public TextView getOrderContent_rentTime_end() {
        return orderContent_rentTime_end;
    }

    public void setOrderContent_rentTime_end(TextView orderContent_rentTime_end) {
        this.orderContent_rentTime_end = orderContent_rentTime_end;
    }

    public TextView getOrderContent_price() {
        return orderContent_price;
    }

    public void setOrderContent_price(TextView orderContent_price) {
        this.orderContent_price = orderContent_price;
    }

    public TextView getOrderContent_noData() {
        return orderContent_noData;
    }

    public void setOrderContent_noData(TextView orderContent_noData) {
        this.orderContent_noData = orderContent_noData;
    }

    public TextView getOrderContent_orderResult() {
        return orderContent_orderResult;
    }

    public void setOrderContent_orderResult(TextView orderContent_orderResult) {
        this.orderContent_orderResult = orderContent_orderResult;
    }

    public TextView getOrderContent_materialName() {
        return orderContent_materialName;
    }

    public void setOrderContent_materialName(TextView orderContent_materialName) {
        this.orderContent_materialName = orderContent_materialName;
    }

    public TextView getOrderContent_materialType() {
        return orderContent_materialType;
    }

    public void setOrderContent_materialType(TextView orderContent_materialType) {
        this.orderContent_materialType = orderContent_materialType;
    }

    public TextView getOrderContent_operatorName() {
        return orderContent_operatorName;
    }

    public void setOrderContent_operatorName(TextView orderContent_operatorName) {
        this.orderContent_operatorName = orderContent_operatorName;
    }

    public ImageButton getOrderContent_pdfButton() {
        return orderContent_pdfButton;
    }

    public void setOrderContent_pdfButton(ImageButton orderContent_pdfButton) {
        this.orderContent_pdfButton = orderContent_pdfButton;
    }

    public ImageButton getOrderContent_pdf_email() {
        return orderContent_pdf_email;
    }

    public void setOrderContent_pdf_email(ImageButton orderContent_pdf_email) {
        this.orderContent_pdf_email = orderContent_pdf_email;
    }

    public ProgressBar getOrderContent_sendingEmail() {
        return orderContent_sendingEmail;
    }

    public void setOrderContent_sendingEmail(ProgressBar orderContent_sendingEmail) {
        this.orderContent_sendingEmail = orderContent_sendingEmail;
    }

    public ImageButton getOrderContent_video() {
        return orderContent_video;
    }

    public void setOrderContent_video(ImageButton orderContent_video) {
        this.orderContent_video = orderContent_video;
    }
}