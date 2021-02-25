package seu.qz.qzapp.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProviders;

import android.os.Bundle;
import android.text.InputType;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import seu.qz.qzapp.R;
import seu.qz.qzapp.activity.viewmodel.PasswordSettingViewModel;
import seu.qz.qzapp.activity.viewmodel.RechargeViewModel;
import seu.qz.qzapp.database.LitePalUtils;
import seu.qz.qzapp.entity.AppCustomer;

public class RechargeActivity extends AppCompatActivity {

    //viewmodel
    RechargeViewModel rechargeViewModel;

    //UI控件
    Toolbar recharge_toolbar;
    ConstraintLayout recharge_top;
    EditText recharge_count;
    TextView recharge_count_error;
    Button recharge_confirm_btn;
    ProgressBar recharge_loading;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_recharge);
        rechargeViewModel = ViewModelProviders.of(this).get(RechargeViewModel.class);
        rechargeViewModel.setMainCustomer(LitePalUtils.getSingleCustomer(getIntent().getStringExtra("username")));
        initUI();
        initToolbar();
        initUIContent();
        registerListener();
    }

    private void initUIContent() {
        //设置键盘只能输入数字和小数点
        recharge_count.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
    }

    private void registerListener() {
        final AppCustomer mainCustomer = rechargeViewModel.getMainCustomer();
        recharge_confirm_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String recharge_input = recharge_count.getText().toString();
                if(recharge_input.isEmpty()){
                    recharge_count_error.setTextColor(ContextCompat.getColor(RechargeActivity.this,R.color.recharge_error_notice));
                    recharge_count_error.setText("充值金额为空，请输入有效金额！");
                }
                int recharge_money = Integer.parseInt(recharge_input);
                if(recharge_money < 1){
                    recharge_count_error.setTextColor(ContextCompat.getColor(RechargeActivity.this,R.color.recharge_error_notice));
                    recharge_count_error.setText("充值金额不得低于1元，请输入有效金额！");
                }
                if(recharge_money > 10000){
                    recharge_count_error.setTextColor(ContextCompat.getColor(RechargeActivity.this,R.color.recharge_error_notice));
                    recharge_count_error.setText("单次充值不得高于10000元");
                }
                recharge_count_error.setTextColor(ContextCompat.getColor(RechargeActivity.this,R.color.recharge_notice));
                recharge_count_error.setText("单次充值不得高于10000元");
                AppCustomer new_customer = new AppCustomer(mainCustomer);
                new_customer.setUser_balance(new_customer.getUser_balance() + recharge_money);
                rechargeViewModel.updateNewPassword(new_customer, recharge_loading, RechargeActivity.this);
            }
        });
    }


    private void initToolbar() {
        setSupportActionBar(recharge_toolbar);
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
        recharge_toolbar = findViewById(R.id.recharge_toolbar);
        recharge_top = findViewById(R.id.recharge_top);
        recharge_count = findViewById(R.id.recharge_count);
        recharge_count_error = findViewById(R.id.recharge_count_error);
        recharge_confirm_btn = findViewById(R.id.recharge_confirm_btn);
        recharge_loading = findViewById(R.id.recharge_loading);
    }
}