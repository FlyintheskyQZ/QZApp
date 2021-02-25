package seu.qz.qzapp.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.ashokvarma.bottomnavigation.TextBadgeItem;

import java.util.List;
import java.util.Map;

import io.agora.rtm.RtmMessage;
import seu.qz.qzapp.R;
import seu.qz.qzapp.activity.BaseApplication;
import seu.qz.qzapp.activity.CustomerInfoSettingActivity;
import seu.qz.qzapp.activity.DeviceDisplayActivity;
import seu.qz.qzapp.activity.PasswordSettingActivity;
import seu.qz.qzapp.activity.RechargeActivity;
import seu.qz.qzapp.activity.RegisterDeviceActivity;
import seu.qz.qzapp.activity.ReportDisplayActivity;
import seu.qz.qzapp.activity.viewmodel.MainViewModel;
import seu.qz.qzapp.agora.ChatManager;
import seu.qz.qzapp.entity.AppCustomer;
import seu.qz.qzapp.fragment.viewmodel.SettingViewModel;
import seu.qz.qzapp.main.ChatAdapter;
import seu.qz.qzapp.utils.FileUtil;
import seu.qz.qzapp.utils.MessageUtil;

public class SettingsFragment extends Fragment {

    //数据管理viewmodel
    MainViewModel mainViewModel;
    SettingViewModel settingViewModel;

    //UI控件
    //top部分
    ImageView settingFragment_customer_picture;
    TextView settingFragment_top_nickname;
    TextView settingFragment_top_ordernum;
    TextView settingFragment_top_balanceordevicenum;
    TextView settingFragment_top_balanceordevicenum_text;

    //修改密码
    ConstraintLayout settingFragment_changepassword;
    //修改个人信息
    ConstraintLayout settingFragment_changeinfo;
    //查看实验报告文件(user)或仪器信息(saler)
    ConstraintLayout settingFragment_filelistordevicelist;
    ImageView settingFragment_filelistordevicelist_icon;
    TextView settingFragment_filelistordevicelist_text;
    //注册仪器（saler）或者充值（user）
    ConstraintLayout settingFragment_deviceOrbalance;
    ImageView settingFragment_deviceOrbalance_icon;
    TextView settingFragment_deviceOrbalance_text;
    //清理缓存
    ConstraintLayout settingFragment_clearChatCache;
    //登出
    Button setting_fragment_logout;

    public SettingsFragment(){

    }

    public static SettingsFragment newInstance(){
        return new SettingsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainViewModel = ViewModelProviders.of(getActivity()).get(MainViewModel.class);
        settingViewModel = ViewModelProviders.of(this).get(SettingViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setting, container, false);
        initUI(view);
        initUIContent();
        registerListener(view);
        return view;
    }

    private void registerListener(View view) {
        settingFragment_changepassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), PasswordSettingActivity.class);
                intent.putExtra("username", mainViewModel.getMainCustomer().getUser_nickName());
                startActivity(intent);
            }
        });
        settingFragment_changeinfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), CustomerInfoSettingActivity.class);
                intent.putExtra("username", mainViewModel.getMainCustomer().getUser_nickName());
                startActivity(intent);
            }
        });
        settingFragment_deviceOrbalance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mainViewModel.getMainCustomer().getAuthority_level() == 1){
                    Intent intent = new Intent(getActivity(), RechargeActivity.class);
                    intent.putExtra("username", mainViewModel.getMainCustomer().getUser_nickName());
                    getActivity().startActivityForResult(intent, 3);
                }else {
                    Intent intent = new Intent(getActivity(), RegisterDeviceActivity.class);
                    intent.putExtra("username", mainViewModel.getMainCustomer().getUser_nickName());
                    getActivity().startActivityForResult(intent, 4);
                }
            }
        });
        settingFragment_clearChatCache.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(R.string.setting_clearcache_title);
                builder.setMessage(R.string.setting_clearcache_message);
                builder.setPositiveButton(R.string.setting_clearcache_positive, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ChatManager manager = BaseApplication.the().getChatManager();
                        Map<String, List<RtmMessage>> map = manager.getmMessagePool().getmOfflineMessageMap();
                        map.clear();
                        MessageUtil.getMessageListBeanList().clear();
                        ChatAdapter adapter = manager.getAdapter();
                        if(adapter != null){
                            adapter.notifyDataSetChanged();
                        }
                        FileUtil.clearAllChatCache(mainViewModel.getMainCustomer().getUser_id().toString(), getActivity());
                        TextBadgeItem chat_tobeRead = manager.getmMessagePool().getChat_tobeRead();
                        if(chat_tobeRead != null){
                            chat_tobeRead.setBackgroundColorResource(R.color.barActiveColor).setText(" ");
                        }
                    }
                });
                builder.setNegativeButton(R.string.setting_clearcache_negative, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                builder.show();
            }
        });
        settingFragment_filelistordevicelist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppCustomer mainCustomer = mainViewModel.getMainCustomer();
                if(mainCustomer.getAuthority_level() == 1){
                    Intent intent = new Intent(getActivity(), ReportDisplayActivity.class);
                    intent.putExtra("username", mainViewModel.getMainCustomer().getUser_nickName());
                    startActivity(intent);
                }else {
                    Intent intent = new Intent(getActivity(), DeviceDisplayActivity.class);
                    intent.putExtra("username", mainViewModel.getMainCustomer().getUser_nickName());
                    startActivity(intent);
                }
            }
        });
    }

    private void initUIContent() {
        AppCustomer mainCustomer = mainViewModel.getMainCustomer();
        if(mainCustomer.isMale()){
            settingFragment_customer_picture.setImageResource(R.mipmap.ic_chat_item_boy);
        }else {
            settingFragment_customer_picture.setImageResource(R.mipmap.ic_chat_item_girl);
        }
        settingFragment_top_nickname.setText(mainCustomer.getUser_nickName());
        settingFragment_top_ordernum.setText(String.valueOf(mainCustomer.getNumberForFinishedOrders() + mainCustomer.getNumberForProvideOrders()));
        if(mainCustomer.getAuthority_level() == 2){
            String[] related_devices = mainCustomer.getRelated_device_id().split(";");
            settingFragment_top_balanceordevicenum.setText(String.valueOf(related_devices.length));
            settingFragment_top_balanceordevicenum_text.setText("仪器数");
            settingFragment_filelistordevicelist_icon.setImageResource(R.mipmap.ic_settingfragment_devicelist);
            settingFragment_filelistordevicelist_text.setText("我的仪器");
            settingFragment_deviceOrbalance_icon.setImageResource(R.mipmap.ic_settingfragment_devices);
            settingFragment_deviceOrbalance_text.setText("注册仪器");
        }else if(mainCustomer.getAuthority_level() == 1){
            settingFragment_top_balanceordevicenum.setText(String.valueOf(mainCustomer.getUser_balance()));
            settingFragment_top_balanceordevicenum_text.setText("余额（￥）");
            settingFragment_filelistordevicelist_icon.setImageResource(R.mipmap.ic_settingfragment_files);
            settingFragment_filelistordevicelist_text.setText("实验报告");
            settingFragment_deviceOrbalance_icon.setImageResource(R.mipmap.ic_settingfragment_balance);
            settingFragment_deviceOrbalance_text.setText("账户充值");
        }
    }

    private void initUI(View view) {
        settingFragment_customer_picture = view.findViewById(R.id.settingfragment_customer_picture);
        settingFragment_top_nickname = view.findViewById(R.id.settingfragment_top_nickname);
        settingFragment_top_ordernum = view.findViewById(R.id.settingfragment_top_ordernum);
        settingFragment_top_balanceordevicenum = view.findViewById(R.id.settingfragment_top_balanceordevicenum);
        settingFragment_top_balanceordevicenum_text = view.findViewById(R.id.settingfragment_top_balanceordevicenum_text);
        settingFragment_changepassword = view.findViewById(R.id.settingfragment_changepassword);
        settingFragment_changeinfo = view.findViewById(R.id.settingfragment_changeinfo);
        settingFragment_filelistordevicelist = view.findViewById(R.id.settingfragment_filelistordevicelist);
        settingFragment_filelistordevicelist_icon = view.findViewById(R.id.settingfragment_filelistordevicelist_icon);
        settingFragment_filelistordevicelist_text = view.findViewById(R.id.settingfragment_filelistordevicelist_text);
        settingFragment_deviceOrbalance = view.findViewById(R.id.settingfragment_deviceOrbalance);
        settingFragment_deviceOrbalance_icon = view.findViewById(R.id.settingfragment_deviceOrbalance_icon);
        settingFragment_deviceOrbalance_text = view.findViewById(R.id.settingfragment_deviceOrbalance_text);
        settingFragment_clearChatCache = view.findViewById(R.id.settingfragment_clearChatCache);
        setting_fragment_logout = view.findViewById(R.id.setting_fragment_logout);
    }

    public SettingViewModel getSettingViewModel() {
        return settingViewModel;
    }

    public void setSettingViewModel(SettingViewModel settingViewModel) {
        this.settingViewModel = settingViewModel;
    }

    public void refresh() {
        initUIContent();
    }
}