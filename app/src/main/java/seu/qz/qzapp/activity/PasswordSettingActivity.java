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
import seu.qz.qzapp.activity.viewmodel.MainViewModel;
import seu.qz.qzapp.activity.viewmodel.PasswordSettingViewModel;
import seu.qz.qzapp.database.LitePalUtils;
import seu.qz.qzapp.entity.AppCustomer;
import seu.qz.qzapp.utils.StringFormatUtil;

public class PasswordSettingActivity extends AppCompatActivity {

    //viewmodel
    PasswordSettingViewModel passwordSettingViewModel;

    //UI控件
    Toolbar password_toolbar;
    EditText password_old;
    TextView password_old_error;
    EditText password_new;
    TextView password_new_error;
    Button password_confirm_btn;
    ProgressBar password_loading;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_password_setting);
        passwordSettingViewModel = ViewModelProviders.of(this).get(PasswordSettingViewModel.class);
        passwordSettingViewModel.setMainCustomer(LitePalUtils.getSingleCustomer(getIntent().getStringExtra("username")));
        initUI();
        initToolbar();
        initUIContent();
        registerListener();
    }

    private void registerListener() {
        password_confirm_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String old_password = password_old.getText().toString();
                if(old_password == null || old_password.isEmpty()){
                    password_old_error.setVisibility(View.VISIBLE);
                    password_old_error.setText("尚未输入旧密码！");
                    return;
                }else if(!old_password.equals(passwordSettingViewModel.getMainCustomer().getUser_password())){
                    password_old_error.setVisibility(View.VISIBLE);
                    password_old_error.setText("旧密码输入错误，请重试！");
                    return;
                }
                String new_password = password_new.getText().toString();
                if(new_password == null || new_password.isEmpty()){
                    password_old_error.setVisibility(View.GONE);
                    password_new_error.setVisibility(View.VISIBLE);
                    password_new_error.setText("尚未输入新密码！");
                    return;
                }else if(!StringFormatUtil.isValid(new_password, StringFormatUtil.REGULAR_PASSWORD)){
                    password_new_error.setVisibility(View.VISIBLE);
                    password_new_error.setText("新密码格式输入错误，请按照要求重新输入！");
                    return;
                }
                password_old_error.setVisibility(View.GONE);
                password_new_error.setVisibility(View.GONE);
                password_loading.setVisibility(View.VISIBLE);
                AppCustomer new_customer = new AppCustomer(passwordSettingViewModel.getMainCustomer());
                new_customer.setUser_password(new_password);
                passwordSettingViewModel.updateNewPassword(new_customer, password_loading, PasswordSettingActivity.this);
            }
        });
    }

    private void initUIContent() {
        password_old_error.setVisibility(View.GONE);
        password_new_error.setVisibility(View.GONE);
        password_loading.setVisibility(View.GONE);
    }

    private void initUI() {
        password_toolbar = findViewById(R.id.password_toolbar);
        password_old = findViewById(R.id.password_old);
        password_old_error = findViewById(R.id.password_old_error);
        password_new = findViewById(R.id.password_new);
        password_new_error = findViewById(R.id.password_new_error);
        password_confirm_btn = findViewById(R.id.password_confirm_btn);
        password_loading = findViewById(R.id.password_loading);
    }

    private void initToolbar() {
        setSupportActionBar(password_toolbar);
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
}