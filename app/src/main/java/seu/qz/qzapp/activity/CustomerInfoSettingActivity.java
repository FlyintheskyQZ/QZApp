package seu.qz.qzapp.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProviders;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import seu.qz.qzapp.R;
import seu.qz.qzapp.activity.viewmodel.CustomerInfoSettingViewModel;
import seu.qz.qzapp.activity.viewmodel.PasswordSettingViewModel;
import seu.qz.qzapp.database.LitePalUtils;
import seu.qz.qzapp.entity.AppCustomer;
import seu.qz.qzapp.utils.StringFormatUtil;

public class CustomerInfoSettingActivity extends AppCompatActivity {

    //viewmodel
    CustomerInfoSettingViewModel customerInfoSettingViewModel;

    //UI
    Toolbar customerinfo_toolbar;
    TextView infosetting_userid;
    TextView infosetting_username;
    TextView infosetting_authority;
    TextView infosetting_realname;
    TextView infosetting_identityid;
    TextView infosetting_gender;
    EditText infosetting_phonenumber;
    EditText infosetting_email;
    TextView infosetting_error;
    Button customerinfo_confirm_btn;
    ProgressBar customerinfo_loading;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_customer_info_setting);
        customerInfoSettingViewModel = ViewModelProviders.of(this).get(CustomerInfoSettingViewModel.class);
        customerInfoSettingViewModel.setMainCustomer(LitePalUtils.getSingleCustomer(getIntent().getStringExtra("username")));
        initUI();
        initToolbar();
        initUIContent();
        registerListener();
    }

    private void registerListener() {
        final AppCustomer mainCustomer = customerInfoSettingViewModel.getMainCustomer();
        customerinfo_confirm_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String new_phonenumber = infosetting_phonenumber.getText().toString();
                String new_email = infosetting_email.getText().toString();
                int modified = 0;
                if(!new_phonenumber.isEmpty() && !new_phonenumber.equals(mainCustomer.getPhoneNumber())){
                    modified++;
                }
                if(!new_email.isEmpty() && !new_email.equals(mainCustomer.getEmail())){
                    modified++;
                }
                if(modified == 0){
                    infosetting_error.setVisibility(View.VISIBLE);
                    infosetting_error.setText("未进行任何修改！");
                    return;
                }
                if(!StringFormatUtil.isValid(new_phonenumber, StringFormatUtil.REGULAR_CELLPHONE) && !StringFormatUtil.isValid(new_phonenumber, StringFormatUtil.REGULAR_PHONE)){
                    infosetting_error.setVisibility(View.VISIBLE);
                    infosetting_error.setText("电话号码格式不对！");
                    return;
                }
                if(StringFormatUtil.isValid(new_email, StringFormatUtil.REGULAR_EMAIL)){
                    infosetting_error.setVisibility(View.VISIBLE);
                    infosetting_error.setText("邮件地址格式不对！");
                    return;
                }
                infosetting_error.setVisibility(View.GONE);
                customerinfo_loading.setVisibility(View.VISIBLE);
                AppCustomer new_customer = new AppCustomer(mainCustomer);
                new_customer.setPhoneNumber(new_phonenumber);
                new_customer.setEmail(new_email);
                customerInfoSettingViewModel.updateNewCustomer(new_customer, customerinfo_loading, CustomerInfoSettingActivity.this);
            }
        });

    }

    private void initUIContent() {
        AppCustomer mainCustomer = customerInfoSettingViewModel.getMainCustomer();
        infosetting_userid.setText(mainCustomer.getUser_id().toString());
        infosetting_username.setText(mainCustomer.getUser_nickName());
        infosetting_authority.setText(mainCustomer.getAuthority_level() == 1 ? "客户" : "商家");
        infosetting_realname.setText(mainCustomer.getUser_registerName());
        infosetting_identityid.setText(mainCustomer.getUser_identityId());
        infosetting_gender.setText(mainCustomer.isMale() ? "男" : "女");
        infosetting_phonenumber.setHint(mainCustomer.getPhoneNumber());
        infosetting_email.setHint(mainCustomer.getEmail());
        infosetting_error.setVisibility(View.GONE);
    }

    private void initToolbar() {
        setSupportActionBar(customerinfo_toolbar);
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
        customerinfo_toolbar = findViewById(R.id.customerinfo_toolbar);
        infosetting_userid = findViewById(R.id.infosetting_userid);
        infosetting_username = findViewById(R.id.infosetting_username);
        infosetting_authority = findViewById(R.id.infosetting_authority);
        infosetting_realname = findViewById(R.id.infosetting_realname);
        infosetting_identityid = findViewById(R.id.infosetting_identityid);
        infosetting_gender = findViewById(R.id.infosetting_gender);
        infosetting_phonenumber = findViewById(R.id.infosetting_phonenumber);
        infosetting_email = findViewById(R.id.infosetting_email);
        customerinfo_confirm_btn = findViewById(R.id.customerinfo_confirm_btn);
        customerinfo_loading = findViewById(R.id.customerinfo_loading);
        infosetting_error = findViewById(R.id.infosetting_error);
    }
}