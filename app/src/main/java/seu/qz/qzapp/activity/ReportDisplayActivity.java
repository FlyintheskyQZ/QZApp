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
import seu.qz.qzapp.activity.viewmodel.RegisterDeviceViewModel;
import seu.qz.qzapp.activity.viewmodel.ReportDisplayViewModel;
import seu.qz.qzapp.database.LitePalUtils;
import seu.qz.qzapp.entity.AppCustomer;
import seu.qz.qzapp.entity.BriefReportItem;
import seu.qz.qzapp.main.ReportAdapter;

public class ReportDisplayActivity extends AppCompatActivity {

    //ViewModel
    ReportDisplayViewModel reportDisplayViewModel;

    //UI
    Toolbar reportdisplay_toolbar;
    SwipeRefreshLayout reportdisplay_freshlayout;
    ScrollView reportdisplay_scrollview;
    RecyclerView reportdisplay_recyclerview;
    ReportAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_report_display);
        reportDisplayViewModel = ViewModelProviders.of(this).get(ReportDisplayViewModel.class);
        reportDisplayViewModel.setMainCustomer(LitePalUtils.getSingleCustomer(getIntent().getStringExtra("username")));
        initUI();
        initToolbar();
        initUIContent();
        registerListener();
    }

    private void registerListener() {
        reportdisplay_freshlayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //设置adapter的loadingState为0是为了避免下拉刷新的过程中显示adapter上拉刷新的加载提示项（原因是当recycler下拉刷新时会清空子项的缓存集合，导致adapter安排显示时会把加载提示项当成唯一存在的项）
                reportdisplay_freshlayout.setRefreshing(true);
                refreshList();
            }
        });
    }

    private void refreshList(){
        getReportItemsFromServer();
    }

    private void initUIContent() {
        final AppCustomer mainCustomer = reportDisplayViewModel.getMainCustomer();
        reportDisplayViewModel.setBriefReportItems(new ArrayList<BriefReportItem>());
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        reportdisplay_recyclerview.setLayoutManager(layoutManager);
        adapter = new ReportAdapter(reportDisplayViewModel.getBriefReportItems(), new ReportAdapter.OnItemClickListener() {
            @Override
            public void onClick(BriefReportItem item) {
            //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!添加点击监听，下载或打开（跳转到WPS）
                if(item.isDownloaded()){
                    String fileName = mainCustomer.getUser_nickName() + "-" + item.getOrder_id();
                    String directory =getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).getPath();
                    File file = new File(directory + "/" + fileName);
                    Intent intent_PDFReader = new Intent(Intent.ACTION_VIEW);
                    //判断Android版本，Android7.0以后应用文件提供给外部程序需要进行URI映射和临时权限的赋予
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        intent_PDFReader.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        Uri contentUri = FileProvider.getUriForFile(ReportDisplayActivity.this, "seu.qz.qzapp.fileprovider",
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
                }else {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(ReportDisplayActivity.this);
                    dialog.setTitle(R.string.report_pdf_noDownload_title);
                    dialog.setMessage(R.string.report_pdf_noDownload_message);
                    dialog.setPositiveButton(R.string.report_pdf_noDownload_positive, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
                    dialog.show();
                }
            }
        });
        adapter.setMainCustomer(reportDisplayViewModel.getMainCustomer());
        reportdisplay_recyclerview.setAdapter(adapter);
        getReportItemsFromServer();
    }

    private void getReportItemsFromServer() {
        reportDisplayViewModel.getItemsFromServer(adapter, reportdisplay_freshlayout, this);
    }

    private void initToolbar() {
        setSupportActionBar(reportdisplay_toolbar);
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
        reportdisplay_toolbar = findViewById(R.id.reportdisplay_toolbar);
        reportdisplay_freshlayout = findViewById(R.id.reportdisplay_freshlayout);
        reportdisplay_scrollview = findViewById(R.id.reportdisplay_scrollview);
        reportdisplay_recyclerview = findViewById(R.id.reportdisplay_recyclerview);
    }
}