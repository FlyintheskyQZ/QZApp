package seu.qz.qzapp.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.lifecycle.ViewModelProviders;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.Window;

import com.ashokvarma.bottomnavigation.BottomNavigationBar;
import com.ashokvarma.bottomnavigation.BottomNavigationItem;
import com.ashokvarma.bottomnavigation.TextBadgeItem;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.agora.rtm.ErrorInfo;
import io.agora.rtm.LocalInvitation;
import io.agora.rtm.RemoteInvitation;
import io.agora.rtm.ResultCallback;
import io.agora.rtm.RtmCallEventListener;
import io.agora.rtm.RtmCallManager;
import io.agora.rtm.RtmClient;
import seu.qz.qzapp.R;
import seu.qz.qzapp.agora.ChatManager;
import seu.qz.qzapp.database.LitePalUtils;
import seu.qz.qzapp.entity.AppCustomer;
import seu.qz.qzapp.fragment.ChatFragment;
import seu.qz.qzapp.fragment.MapsFragment;
import seu.qz.qzapp.fragment.OrderFragment;
import seu.qz.qzapp.fragment.SearchFragment;
import seu.qz.qzapp.activity.viewmodel.MainViewModel;
import seu.qz.qzapp.fragment.SettingsFragment;
import seu.qz.qzapp.fragment.viewmodel.ChatViewModel;
import seu.qz.qzapp.fragment.viewmodel.MapsViewModel;
import seu.qz.qzapp.fragment.viewmodel.OrderViewModel;
import seu.qz.qzapp.fragment.viewmodel.SearchViewModel;
import seu.qz.qzapp.fragment.viewmodel.SettingViewModel;
import seu.qz.qzapp.main.ChatAdapter;
import seu.qz.qzapp.utils.FileUtil;
import seu.qz.qzapp.utils.MessageUtil;

public class MainActivity extends AppCompatActivity {

    private List<Fragment> fragments;
    private List<BottomNavigationItem> barItemList;
    private Map<String, TextBadgeItem> textBadgeItems;
    ViewPager viewPager;
    BottomNavigationBar bottom_bar;
    private MainViewModel mainViewModel;
    ContentViewPagerAdapter pagerAdapter;
    private static final String TAG = "MainActivity";

    //Agora相关
    private RtmClient mRtmClient;
    ChatManager mChatManager;
    RtmCallManager callManager;
    //是否登录成功
    private boolean loginSuccess = false;
    //控制显示BottomBar中“未读消息”的数量
    TextBadgeItem chat_tobeRead;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        viewPager = findViewById(R.id.viewpager);
        bottom_bar = findViewById(R.id.bottom_bar);
        mainViewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        AppCustomer customer = LitePalUtils.getSingleCustomer(getIntent().getStringExtra("username"));
        System.out.println("mainCustomer is !!!!!!!!!!!!!!!!!!!:" + customer);
        mainViewModel.setMainCustomer(customer);
 //       mainViewModel.setMainCustomer((AppCustomer) (getIntent().getSerializableExtra("customer")));
        loadChatCache();
        LoginInAgora();
        initViewPager(mainViewModel);
        initBottomBar(mainViewModel);

    }

    private void loadChatCache() {
        File root_chatText = new File(FileUtil.getChatCacheTextPath(this, mainViewModel.getMainCustomer().getUser_id().toString()));
        Log.d(TAG, "chatTextRoot:" + root_chatText.getAbsolutePath());
        File root_chatImage = new File(FileUtil.getChatCacheImagePath(this, mainViewModel.getMainCustomer().getUser_id().toString()));
        Log.d(TAG, "chatImageRoot:" + root_chatImage.getAbsolutePath());
        File[] files_chatText = root_chatText.listFiles();
        for(int i = 0; i < files_chatText.length; i++){
            //Cache文件中
            if(files_chatText[i].isFile()){
                Log.d(TAG, "loadFile:" + files_chatText[i].getAbsolutePath());
                FileUtil.loadCacheFromFileToFlash(this, files_chatText[i]);
            }
        }
        if(chat_tobeRead != null){
            int num = mChatManager.getmMessagePool().numOfAllMessage();
            if(num == 0){
                chat_tobeRead.setBackgroundColorResource(R.color.barActiveColor).setText(" ");
            }else {
                chat_tobeRead.setBackgroundColorResource(R.color.badgeItem_bg).setText(String.valueOf(num));
            }
        }
    }


    //初始化ViewPager，创建并添加Fragments到Fragment源List中，并且设置ViewPager相关参数
    private void initViewPager(MainViewModel mainViewModel) {
        if(fragments == null){
            fragments = new ArrayList<Fragment>();
            fragments.add(SearchFragment.newInstance());
            fragments.add(OrderFragment.newInstance());
            fragments.add(ChatFragment.newInstance());
            fragments.add(SettingsFragment.newInstance());
        }else{
            //刷新Fragment中的对应内容
        }
        //缓存参数mOffscreenPageLimit至少为1，设置为3，保证跳转到MainActivity中时所有Fragment都直接创建
        viewPager.setOffscreenPageLimit(3);
        pagerAdapter = new ContentViewPagerAdapter(getSupportFragmentManager(), FragmentPagerAdapter.BEHAVIOR_SET_USER_VISIBLE_HINT);
        viewPager.setAdapter(pagerAdapter);

    }

    private void initBottomBar(MainViewModel mainViewModel){
        bottom_bar.setMode(BottomNavigationBar.MODE_FIXED);
        bottom_bar.setBackgroundStyle(BottomNavigationBar.BACKGROUND_STYLE_RIPPLE);
        //华为荣耀V10实测为BottomNavigationBar的整体背景颜色
        bottom_bar.setActiveColor(R.color.barActiveColor);
        //实测为未选中时的文字颜色
        bottom_bar.setInActiveColor(R.color.barBackColor_inactive);
        //实测为选中状态时的文字颜色
        bottom_bar.setBarBackgroundColor(R.color.barBackColor_active);
        barItemList = new ArrayList<>();
        textBadgeItems = new HashMap<>();
        initBarItemList(fragments, barItemList, textBadgeItems);
        for(int i = 0; i < barItemList.size(); i++){
            bottom_bar.addItem(barItemList.get(i));
        }
        bottom_bar.setFirstSelectedPosition(0).initialise();
        bottom_bar.setTabSelectedListener(new BottomNavigationBar.OnTabSelectedListener() {
            @Override
            public void onTabSelected(int position) {
                if(viewPager != null){
                    viewPager.setCurrentItem(position);
                }
            }

            @Override
            public void onTabUnselected(int position) {

            }

            @Override
            public void onTabReselected(int position) {

            }
        });
    }

    public void initBarItemList(List<Fragment> fragments, List<BottomNavigationItem> barItemList, Map<String, TextBadgeItem> textBadgeItems){
        TextBadgeItem textBadgeItem_search = new TextBadgeItem().setBackgroundColorResource(R.color.barActiveColor).setGravity(Gravity.TOP | Gravity.RIGHT)
                .setTextColorResource(R.color.badgeItem_text).setText("").setHideOnSelect(false);
        textBadgeItems.put("search", textBadgeItem_search);
        BottomNavigationItem item_search = new BottomNavigationItem(R.mipmap.ic_bottom_searchs_active, R.string.ic_bottom_search)
                .setInactiveIconResource(R.mipmap.ic_bottom_search_inactive).setBadgeItem(textBadgeItem_search);
        BottomNavigationItem item_order = new BottomNavigationItem(R.mipmap.ic_bottom_orders_active, R.string.ic_bottom_order)
                .setInactiveIconResource(R.mipmap.ic_bottom_orders_inactive);
        TextBadgeItem textBadgeItem_chat = new TextBadgeItem().setBackgroundColorResource(R.color.badgeItem_bg).setGravity(Gravity.TOP | Gravity.RIGHT)
                .setTextColorResource(R.color.badgeItem_text).setText(" ").setHideOnSelect(false);
        chat_tobeRead = textBadgeItem_chat;
        textBadgeItems.put("chat", textBadgeItem_chat);
        mChatManager.getmMessagePool().setChat_tobeRead(chat_tobeRead);
        BottomNavigationItem item_chat = new BottomNavigationItem(R.mipmap.ic_bottom_chats_active, R.string.ic_bottom_chat)
                .setInactiveIconResource(R.mipmap.ic_bottom_chats_inactive).setBadgeItem(textBadgeItem_chat);
        BottomNavigationItem item_setting = new BottomNavigationItem(R.mipmap.ic_bottom_settings_active, R.string.ic_bottom_setting)
                .setInactiveIconResource(R.mipmap.ic_bottom_settings_inactive);
        barItemList.add(item_search);
        barItemList.add(item_order);
        barItemList.add(item_chat);
        barItemList.add(item_setting);
    }

    //以RtmClient的身份登陆上Agora的系统
    private void LoginInAgora() {
        //mIsInChat = true;
        //登录Id为AppCustomer的user_id
        final String mUserId = mainViewModel.getMainCustomer().getUser_id().toString();
        mChatManager = BaseApplication.the().getChatManager();
        mRtmClient = mChatManager.getRtmClient();
        //此处Token后期补上！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！
        mRtmClient.login(null, mUserId, new ResultCallback<Void>() {
            //登陆成功
            @Override
            public void onSuccess(Void responseInfo) {
                Log.i(TAG, "login success");
                loginSuccess = true;
                mChatManager.setLoginSuccess(true);
                initCallManager();

//                getActivity().runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {

//
//                        Intent intent = new Intent(getActivity(), AgoraChatActivity.class);
//                        intent.putExtra(MessageUtil.INTENT_EXTRA_USER_ID, mUserId);
//                        startActivity(intent);
//                    }
//                });
            }
            //登陆失败
            @Override
            public void onFailure(ErrorInfo errorInfo) {
                Log.i(TAG, "login failed: " + errorInfo.getErrorCode());
                loginSuccess = false;
//                getActivity().runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//
//                    }
//                }
//                {
//                    mLoginBtn.setEnabled(true);
//                    mIsInChat = false;
//                    showToast(getString(R.string.login_failed));
//                }
//                );
            }
        });
    }

    private void doLogout() {
        mRtmClient.logout(null);
        loginSuccess = false;
        mChatManager.setLoginSuccess(false);
        //清空所有聊天信息list
        //MessageUtil.cleanMessageListBeanList();
    }

    public void initCallManager(){
        callManager = mRtmClient.getRtmCallManager();
        mChatManager.enableOfflineMessage(true);
        callManager.setEventListener(new RtmCallEventListener() {
            @Override
            public void onLocalInvitationReceivedByPeer(LocalInvitation localInvitation) {

            }

            @Override
            public void onLocalInvitationAccepted(LocalInvitation localInvitation, String s) {

            }

            @Override
            public void onLocalInvitationRefused(LocalInvitation localInvitation, String s) {

            }

            @Override
            public void onLocalInvitationCanceled(LocalInvitation localInvitation) {

            }

            @Override
            public void onLocalInvitationFailure(LocalInvitation localInvitation, int i) {

            }

            @Override
            public void onRemoteInvitationReceived(final RemoteInvitation remoteInvitation) {
                callManager.acceptRemoteInvitation(remoteInvitation, new ResultCallback<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                    }

                    @Override
                    public void onFailure(ErrorInfo errorInfo) {

                    }
                });
            }

            @Override
            public void onRemoteInvitationAccepted(RemoteInvitation remoteInvitation) {

            }

            @Override
            public void onRemoteInvitationRefused(RemoteInvitation remoteInvitation) {

            }

            @Override
            public void onRemoteInvitationCanceled(RemoteInvitation remoteInvitation) {

            }

            @Override
            public void onRemoteInvitationFailure(RemoteInvitation remoteInvitation, int i) {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            //从OrderContentActivity返回的结果
            case 1:
                switch (resultCode){
                    //返回结果为1，没有找到对应order，可能已删除或者状态发生改变，需要更新orderFragment的RecyclerView
                    case 1:
                        OrderFragment orderFragment = (OrderFragment) fragments.get(1);
                        orderFragment.getOrderViewModel().refresh(mainViewModel.getMainCustomer().getUser_nickName(),
                                orderFragment.getAdapter(), this,((OrderFragment) (fragments.get(1))).getSwipeRefreshLayout());
                        break;
                    //从OrderContentActivity返回的结果，商家删除ProvideOrder成功,更新orderFragment中RecyclerView的内容
                    case 2:
                        OrderFragment orderFragment_2 = (OrderFragment) fragments.get(1);
                        orderFragment_2.getOrderViewModel().refresh(mainViewModel.getMainCustomer().getUser_nickName(),
                                orderFragment_2.getAdapter(), this,((OrderFragment) (fragments.get(1))).getSwipeRefreshLayout());
                        break;
                    //从OrderContentActivity返回的结果，客户撤销ProvideOrder成功,更新orderFragment中RecyclerView的内容
                    case 3:
                        //客户撤销一个ProvideOrder，更新OrderFragment列表
                        OrderFragment orderFragment_3 = (OrderFragment) fragments.get(1);
                        orderFragment_3.getOrderViewModel().refresh(mainViewModel.getMainCustomer().getUser_nickName(),
                                orderFragment_3.getAdapter(), this,((OrderFragment) (fragments.get(1))).getSwipeRefreshLayout());
                    default:break;
                }
                break;
            //searchFragment中的时间条件查询进入TimeSettingActivity中的返回结果
            case 2:
                switch (resultCode){
                    //顺序排列
                    case 1:
                        Log.d(TAG, "onActivityResult: 回到Main");
                        Date date_begin1 = (Date) data.getSerializableExtra("date_begin");
                        Date date_end1 = (Date) data.getSerializableExtra("date_end");
                        SearchFragment fragment1 = (SearchFragment) fragments.get(0);
                        fragment1.showItemsByTimeFilter(fragment1.getAdapter().getSearch_list(), date_begin1, date_end1, 1);
                        break;
                        //倒序排列
                    case 2:
                        Date date_begin2 = (Date) data.getSerializableExtra("date_begin");
                        Date date_end2 = (Date) data.getSerializableExtra("date_end");
                        SearchFragment fragment2 = (SearchFragment) fragments.get(0);
                        fragment2.showItemsByTimeFilter(fragment2.getAdapter().getSearch_list(), date_begin2, date_end2, 2);
                        break;
                    default:break;

                }
                break;
                //
            case 3:
            case 4:
                switch (resultCode){
                    case 1:
                        Log.d("RechargeActivity", "更新余额" + mainViewModel.getMainCustomer().getUser_balance());
                        SettingsFragment fragment = (SettingsFragment) fragments.get(3);
                        AppCustomer mainCustomer = LitePalUtils.getSingleCustomer(mainViewModel.getMainCustomer().getUser_nickName());
                        mainViewModel.getMainCustomer().adjustSelf(mainCustomer);
                        fragment.refresh();
                        break;
                    default:break;
                }
                break;
            default:break;
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 2:
                SearchFragment fragment = (SearchFragment) fragments.get(0);
                fragment.initGPS();
                break;
            default:break;
        }
    }

    //各Activity均需如此操作，在压栈回归后更新MainCustomer的内容
    @Override
    protected void onStart() {
        super.onStart();
        AppCustomer mainCustomer = LitePalUtils.getSingleCustomer(mainViewModel.getMainCustomer().getUser_nickName());
        mainViewModel.getMainCustomer().adjustSelf(mainCustomer);
        mChatManager.setActivity(this);
        ChatAdapter adapter = mChatManager.getAdapter();
        if(adapter != null){
            adapter.notifyDataSetChanged();
        }
        if(chat_tobeRead != null){
            int num = mChatManager.getmMessagePool().numOfAllMessage();
            if(num == 0){
                chat_tobeRead.setBackgroundColorResource(R.color.barActiveColor).setText(" ");
            }else {
                chat_tobeRead.setBackgroundColorResource(R.color.badgeItem_bg).setText(String.valueOf(num));
            }
        }
         Log.i(TAG, "MainActivity_onStart: 启动！！！！！！！！！！！！！！！！！！！！！！！！！!!");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG, "MainActivity_onStop: 启动！！！！！！！！！！！！！！！！！！！！！！！！！！");
        mChatManager.setActivity(null);
        //FileUtil.storeCacheFromFlashToFile(mainViewModel.getMainCustomer().getUser_id().toString(), this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        pagerAdapter = null;
        FileUtil.storeCacheFromFlashToFile(mainViewModel.getMainCustomer().getUser_id().toString(), this);
        mChatManager.getmMessagePool().getmOfflineMessageMap().clear();
        MessageUtil.getMessageListBeanList().clear();
        mChatManager.getmMessagePool().setChat_tobeRead(null);
        doLogout();
        Log.i(TAG, "onDestroy: 方法启动！！！！！！！！！！！！！！！！！！！！！！！！！！！！");
    }

    class ContentViewPagerAdapter extends FragmentPagerAdapter{

        Fragment fragment;
        boolean hasChanged = false;

        public ContentViewPagerAdapter(@NonNull FragmentManager fm, int behavior) {
            super(fm, behavior);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        /**
         * 该方法会在FragmentViewPager发生翻转时进行调用，不过会在新进缓存Fragment（由于缓存FragmentViewPager
         * 的mItems大小在当前fragment两边都有空位时最少为3，故并非当前的转到的Fragment）的onCreate()和onCreateView()
         * 之前调用一次，之后调用两三次，即一次翻转会调用多次该方法，通过添加fragment全局引用与postion对应的object是否相同，
         * 可以判断当前调用是否为反转后的第一次调用；考虑到如果通过点击bottombar的按键进行跳转，则可能会发生mItems的内容全部更换，
         * 则这时需要在setPrimaryItem（）反转后第二次调用中进行UI更新，只是此时View已形成，肯定会有UI界面的延迟（可以考虑添加加载界面来缓冲过度）
         * @param container
         * @param position
         * @param object
         */
        //此处尚未进行翻转后进行更新UI的操作
        @Override
        public void setPrimaryItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            boolean UIChange = false;
            boolean changeBottomBar = false;
            if(fragment != null){
                if(fragment == (Fragment) object){
                    UIChange = true;
                    changeBottomBar = false;

                }else{
                    UIChange = false;
                    hasChanged = false;
                    fragment = (Fragment)object;
                    changeBottomBar = true;
                }
            }
            if(fragment == null){
                fragment = (Fragment) object;
                UIChange = true;
                hasChanged = false;
            }
            super.setPrimaryItem(container, position, object);
            if(changeBottomBar == true && bottom_bar != null){
                bottom_bar.selectTab(position);
            }

            //UI界面翻转到时更新!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
            //TextView textView;
            if(object instanceof ChatFragment){
                ChatFragment fragment = (ChatFragment) object;
                //textView = fragment.getChatText();
                ChatViewModel viewModel = fragment.getViewModel();
                if(viewModel != null && UIChange == true && hasChanged == false){
                    //进行UI界面更新

                    hasChanged = true;
                }
            }else if(object instanceof SettingsFragment){
                SettingsFragment fragment = (SettingsFragment) object;
                SettingViewModel viewModel = fragment.getSettingViewModel();
                //textView = fragment.getMaps_text();
                if(viewModel != null && UIChange == true && hasChanged == false){
                    //进行UI界面更新
                    hasChanged = true;
                }
            }else if(object instanceof OrderFragment){
                OrderFragment fragment = (OrderFragment) object;
                OrderViewModel viewModel = fragment.getViewModel();
                //textView = fragment.getOrderText();
                if(viewModel != null && UIChange == true && hasChanged == false){
                    //进行UI界面更新
        //            viewModel.refresh(getMainViewModel().getMainCustomer().getUser_nickName(), fragment.getAdapter(), fragment.getActivity());
                    hasChanged = true;
                }
            }else{
                SearchFragment fragment = (SearchFragment) object;
                SearchViewModel viewModel = fragment.getViewModel();
                //textView = fragment.getSearchText();
                if(viewModel != null && UIChange == true && hasChanged == false){
                    //进行UI界面更新
                    hasChanged = true;
                }
            }

        }
    }

    public BottomNavigationBar getBottom_bar() {
        return bottom_bar;
    }

    public void setBottom_bar(BottomNavigationBar bottom_bar) {
        this.bottom_bar = bottom_bar;
    }

    public MainViewModel getMainViewModel() {
        return mainViewModel;
    }

    public void setMainViewModel(MainViewModel mainViewModel) {
        this.mainViewModel = mainViewModel;
    }

    public List<Fragment> getFragments() {
        return fragments;
    }

    public void setFragments(List<Fragment> fragments) {
        this.fragments = fragments;
    }

    public List<BottomNavigationItem> getBarItemList() {
        return barItemList;
    }

    public void setBarItemList(List<BottomNavigationItem> barItemList) {
        this.barItemList = barItemList;
    }

    public Map<String, TextBadgeItem> getTextBadgeItems() {
        return textBadgeItems;
    }

    public void setTextBadgeItems(Map<String, TextBadgeItem> textBadgeItems) {
        this.textBadgeItems = textBadgeItems;
    }

    public boolean isLoginSuccess() {
        return loginSuccess;
    }

    public void setLoginSuccess(boolean loginSuccess) {
        this.loginSuccess = loginSuccess;
    }
}