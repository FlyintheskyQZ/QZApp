package seu.qz.qzapp.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.MenuItem;
import android.view.Window;
import android.widget.ScrollView;

import java.io.File;
import java.util.ArrayList;

import seu.qz.qzapp.R;
import seu.qz.qzapp.activity.viewmodel.DeviceDisplayViewModel;
import seu.qz.qzapp.activity.viewmodel.ReportDisplayViewModel;
import seu.qz.qzapp.database.LitePalUtils;
import seu.qz.qzapp.entity.AppCustomer;
import seu.qz.qzapp.entity.BriefReportItem;
import seu.qz.qzapp.entity.LOIInstrument;
import seu.qz.qzapp.main.DeviceAdapter;
import seu.qz.qzapp.main.ReportAdapter;

public class DeviceDisplayActivity extends AppCompatActivity {

    //viewModel
    DeviceDisplayViewModel deviceDisplayViewModel;

    //UI
    Toolbar devicetdisplay_toolbar;
    SwipeRefreshLayout devicetdisplay_freshlayout;
    ScrollView devicetdisplay_scrollview;
    RecyclerView devicetdisplay_recyclerview;
    DeviceAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_device_display);
        deviceDisplayViewModel = ViewModelProviders.of(this).get(DeviceDisplayViewModel.class);
        deviceDisplayViewModel.setMainCustomer(LitePalUtils.getSingleCustomer(getIntent().getStringExtra("username")));
        initUI();
        initToolbar();
        initUIContent();
        registerListener();
    }

    private void registerListener() {
        devicetdisplay_freshlayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //设置adapter的loadingState为0是为了避免下拉刷新的过程中显示adapter上拉刷新的加载提示项（原因是当recycler下拉刷新时会清空子项的缓存集合，导致adapter安排显示时会把加载提示项当成唯一存在的项）
                devicetdisplay_freshlayout.setRefreshing(true);
                refreshList();
            }
        });
    }

    private void refreshList() {
        getInstrumentsFromServer();
    }

    private void initUIContent() {
        final AppCustomer mainCustomer = deviceDisplayViewModel.getMainCustomer();
        deviceDisplayViewModel.setLoiInstruments(new ArrayList<LOIInstrument>());
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        devicetdisplay_recyclerview.setLayoutManager(layoutManager);
        adapter = new DeviceAdapter(deviceDisplayViewModel.getLoiInstruments(), new DeviceAdapter.OnItemClickListener() {
            @Override
            public void onClick(LOIInstrument item) {
                Intent intent = new Intent(DeviceDisplayActivity.this, DeviceContentActivity.class);
                intent.putExtra("username", mainCustomer.getUser_nickName());
                intent.putExtra("instrument", item);
                startActivity(intent);
            }
        });
        devicetdisplay_recyclerview.setAdapter(adapter);
        getInstrumentsFromServer();
    }

    private void getInstrumentsFromServer() {
        deviceDisplayViewModel.getInstrumentsFromServer(adapter, devicetdisplay_freshlayout, this);
    }

    private void initToolbar() {
        setSupportActionBar(devicetdisplay_toolbar);
        //添加返回按钮
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()){
            case R.id.toolbar_login_settings:
                break;
            //设置返回按钮
            case android.R.id.home:
                onBackPressed();
            default:
                break;
        }
        return true;
    }

    private void initUI() {
        devicetdisplay_toolbar = findViewById(R.id.devicetdisplay_toolbar);
        devicetdisplay_freshlayout = findViewById(R.id.devicetdisplay_freshlayout);
        devicetdisplay_scrollview = findViewById(R.id.devicetdisplay_scrollview);
        devicetdisplay_recyclerview = findViewById(R.id.devicetdisplay_recyclerview);
    }
}