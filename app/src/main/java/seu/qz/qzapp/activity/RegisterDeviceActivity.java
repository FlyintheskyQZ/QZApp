package seu.qz.qzapp.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProviders;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.baidu.mapapi.model.LatLng;

import seu.qz.qzapp.R;
import seu.qz.qzapp.activity.operation.RegisterDeviceOperation;
import seu.qz.qzapp.activity.viewmodel.RechargeViewModel;
import seu.qz.qzapp.activity.viewmodel.RegisterDeviceViewModel;
import seu.qz.qzapp.database.LitePalUtils;
import seu.qz.qzapp.entity.AppCustomer;
import seu.qz.qzapp.entity.LOIInstrument;

public class RegisterDeviceActivity extends AppCompatActivity {

    //viewmodel
    RegisterDeviceViewModel rechargeViewModel;

    //UI
    Toolbar registerdevice_toolbar;
    TextView registerdevice_userid;
    TextView registerdevice_username;
    EditText registerdevice_factoryname;
    EditText registerdevice_factoryaddress;
    EditText registerdevice_factorylocation_longitude;
    EditText registerdevice_factorylocation_latitude;
    Button registerdevice_getlocation;
    EditText registerdevice_deviceexplanation;
    TextView registerdevice_error;
    Button registerdevice_confirm_btn;
    ProgressBar registerdevice_loading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_register_device);
        rechargeViewModel = ViewModelProviders.of(this).get(RegisterDeviceViewModel.class);
        rechargeViewModel.setMainCustomer(LitePalUtils.getSingleCustomer(getIntent().getStringExtra("username")));
        initUI();
        initToolbar();
        initUIContent();
        registerListener();
    }

    private void registerListener() {
        final AppCustomer mainCustomer = rechargeViewModel.getMainCustomer();
        registerdevice_getlocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //启动至另一个可选择获取仪器定位的Activity
                Intent intent = new Intent(RegisterDeviceActivity.this, DeviceLocationDisplayActivity.class);
                intent.putExtra("registerdevice", true);
                startActivityForResult(intent, 1);

            }
        });
        registerdevice_confirm_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String factory_name = registerdevice_factoryname.getText().toString();
                if(factory_name.isEmpty()){
                    registerdevice_error.setVisibility(View.VISIBLE);
                    registerdevice_error.setText("公司名称不能为空");
                    return;
                }
                String factory_address = registerdevice_factoryaddress.getText().toString();
                if(factory_address.isEmpty()){
                    registerdevice_error.setVisibility(View.VISIBLE);
                    registerdevice_error.setText("公司地址不能为空");
                    return;
                }
                String device_longitude = registerdevice_factorylocation_longitude.getText().toString();
                String device_latitude = registerdevice_factorylocation_latitude.getText().toString();
                if(device_longitude.isEmpty() || device_latitude.isEmpty()){
                    registerdevice_error.setVisibility(View.VISIBLE);
                    registerdevice_error.setText("仪器定位不能为空");
                    return;
                }
                registerdevice_error.setText("");
                registerdevice_error.setVisibility(View.GONE);
                String device_explanation = registerdevice_deviceexplanation.getText().toString();
                LOIInstrument new_device = new LOIInstrument(null, factory_name, factory_address,
                        device_longitude, device_latitude, mainCustomer.getPhoneNumber(), device_explanation, null,
                        null, mainCustomer.getUser_id());
                rechargeViewModel.registerDevice(new_device, registerdevice_loading, RegisterDeviceActivity.this);
            }
        });
    }

    private void initUIContent() {
        AppCustomer mainCustomer = rechargeViewModel.getMainCustomer();
        registerdevice_userid.setText(mainCustomer.getUser_id().toString());
        registerdevice_username.setText(mainCustomer.getUser_nickName());
        registerdevice_error.setVisibility(View.GONE);
        registerdevice_loading.setVisibility(View.GONE);
        registerdevice_factorylocation_longitude.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        registerdevice_factorylocation_latitude.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
    }

    private void initToolbar() {
        setSupportActionBar(registerdevice_toolbar);
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
        registerdevice_toolbar = findViewById(R.id.registerdevice_toolbar);
        registerdevice_userid = findViewById(R.id.registerdevice_userid);
        registerdevice_username = findViewById(R.id.registerdevice_username);
        registerdevice_factoryname = findViewById(R.id.registerdevice_factoryname);
        registerdevice_factoryaddress = findViewById(R.id.registerdevice_factoryaddress);
        registerdevice_factorylocation_longitude = findViewById(R.id.registerdevice_factorylocation_longitude);
        registerdevice_factorylocation_latitude = findViewById(R.id.registerdevice_factorylocation_latitude);
        registerdevice_getlocation = findViewById(R.id.registerdevice_getlocation);
        registerdevice_deviceexplanation = findViewById(R.id.registerdevice_deviceexplanation);
        registerdevice_error = findViewById(R.id.registerdevice_error);
        registerdevice_confirm_btn = findViewById(R.id.registerdevice_confirm_btn);
        registerdevice_loading = findViewById(R.id.registerdevice_loading);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case 1:
                String[] location = data.getStringExtra("location").split(":");
                registerdevice_factorylocation_longitude.setText(location[0]);
                registerdevice_factorylocation_latitude.setText(location[1]);
                break;
            default:break;
        }
    }
}