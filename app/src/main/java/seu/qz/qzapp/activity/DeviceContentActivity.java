package seu.qz.qzapp.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProviders;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.Window;
import android.widget.TextView;

import seu.qz.qzapp.R;
import seu.qz.qzapp.activity.viewmodel.DeviceContentViewModel;
import seu.qz.qzapp.activity.viewmodel.DeviceDisplayViewModel;
import seu.qz.qzapp.database.LitePalUtils;
import seu.qz.qzapp.entity.AppCustomer;
import seu.qz.qzapp.entity.LOIInstrument;

public class DeviceContentActivity extends AppCompatActivity {

    //viewmodel
    DeviceContentViewModel deviceDisplayViewModel;

    //UI
    Toolbar devicecontent_toolbar;
    TextView devicecontent_userid;
    TextView devicecontent_username;
    TextView devicecontent_factoryname;
    TextView devicecontent_factoryaddress;
    TextView devicecontent_deviceid;
    TextView devicecontent_order;
    TextView devicecontent_factorylocation_longitude;
    TextView devicecontent_factorylocation_latitude;
    TextView devicecontent_deviceexplanation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_device_content);
        deviceDisplayViewModel = ViewModelProviders.of(this).get(DeviceContentViewModel.class);
        deviceDisplayViewModel.setMainCustomer(LitePalUtils.getSingleCustomer(getIntent().getStringExtra("username")));
        deviceDisplayViewModel.setInstrument((LOIInstrument) getIntent().getSerializableExtra("instrument"));
        initUI();
        initToolbar();
        initUIContent();
    }

    private void initUIContent() {
        AppCustomer mainCustomer = deviceDisplayViewModel.getMainCustomer();
        LOIInstrument instrument = deviceDisplayViewModel.getInstrument();
        devicecontent_userid.setText(mainCustomer.getUser_id().toString());
        devicecontent_username.setText(mainCustomer.getUser_nickName());
        devicecontent_factoryname.setText(instrument.getFactory_name());
        devicecontent_factoryaddress.setText(instrument.getFactory_address());
        devicecontent_deviceid.setText(instrument.getDevice_id().toString());
        String provide_orders = instrument.getP_orders_string();
        String finished_orders = instrument.getF_orders_string();
        int provides = (provide_orders == null || provide_orders.isEmpty()) ? 0 : provide_orders.split(";").length;
        int finishes = (finished_orders == null || finished_orders.isEmpty()) ? 0 : finished_orders.split(";").length;
        devicecontent_order.setText("待完成" + provides + "单   ;  已完成" + finishes + "单");
        devicecontent_factorylocation_longitude.setText(instrument.getFactory_longitude());
        devicecontent_factorylocation_latitude.setText(instrument.getFactory_latitude());
        String explanation = instrument.getExtra_description();
        if(explanation == null || explanation.isEmpty()){
            explanation = "暂无说明";
        }
        devicecontent_deviceexplanation.setText(explanation);
    }

    private void initToolbar() {
        setSupportActionBar(devicecontent_toolbar);
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
        devicecontent_toolbar = findViewById(R.id.devicecontent_toolbar);
        devicecontent_userid = findViewById(R.id.devicecontent_userid);
        devicecontent_username = findViewById(R.id.devicecontent_username);
        devicecontent_factoryname = findViewById(R.id.devicecontent_factoryname);
        devicecontent_factoryaddress = findViewById(R.id.devicecontent_factoryaddress);
        devicecontent_deviceid = findViewById(R.id.devicecontent_deviceid);
        devicecontent_order = findViewById(R.id.devicecontent_order);
        devicecontent_factorylocation_longitude = findViewById(R.id.devicecontent_factorylocation_longitude);
        devicecontent_factorylocation_latitude = findViewById(R.id.devicecontent_factorylocation_latitude);
        devicecontent_deviceexplanation = findViewById(R.id.devicecontent_deviceexplanation);
    }
}