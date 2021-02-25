package seu.qz.qzapp.activity;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import org.litepal.LitePal;


import java.util.Date;

import seu.qz.qzapp.R;
import seu.qz.qzapp.entity.AppCustomer;
import seu.qz.qzapp.register.RegisterFormState;
import seu.qz.qzapp.register.RegisterResult;
import seu.qz.qzapp.register.RegisterViewModel;
import seu.qz.qzapp.register.RegisterViewModelFactory;


/**
 * 注册活动：用于注册用户
 */
public class RegisterActivity extends AppCompatActivity {

    private RegisterViewModel registerViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_register);

        //获取各个UI控件对象
        final Button button_register = findViewById(R.id.register_button);
        final ProgressBar bar_register = findViewById(R.id.register_loading);
        final EditText edit_username = findViewById(R.id.reg_username);
        final EditText edit_password = findViewById(R.id.reg_password);
        final EditText edit_realName = findViewById(R.id.reg_realname);
        final EditText edit_realIdentity = findViewById(R.id.reg_realIdentity);
        final EditText edit_phoneNumber = findViewById(R.id.reg_phonenumber);
        final EditText edit_email = findViewById(R.id.reg_email);
        final RadioButton radio_normal = findViewById(R.id.reg_select_normal);
        final RadioButton radio_factory = findViewById(R.id.reg_select_factory);
        final RadioGroup radio_group = findViewById(R.id.reg_select);
        final RadioButton radio_male = findViewById(R.id.reg_select_male);
        final RadioButton radio_female = findViewById(R.id.reg_select_female);

        //创建与本活动关联的ViewModel
        registerViewModel = ViewModelProviders.of(this, new RegisterViewModelFactory())
                .get(RegisterViewModel.class);


        //创建文本监察器，当输入文本发生变化时同步更新RegisterViewModel中的RegisterFormState
        TextWatcher afterTextChangedListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // ignore
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // ignore
            }

            @Override
            public void afterTextChanged(Editable s) {
                registerViewModel.registerDataChanged(edit_username.getText().toString(),
                        edit_password.getText().toString(), edit_realName.getText().toString(),
                        edit_realIdentity.getText().toString(), edit_phoneNumber.getText().toString(),
                        edit_email.getText().toString(), radio_normal.isChecked() || radio_factory.isChecked());
            }
        };
        //给EditText注册文本监察器
        edit_username.addTextChangedListener(afterTextChangedListener);
        edit_password.addTextChangedListener(afterTextChangedListener);
        edit_realName.addTextChangedListener(afterTextChangedListener);
        edit_realIdentity.addTextChangedListener(afterTextChangedListener);
        edit_phoneNumber.addTextChangedListener(afterTextChangedListener);
        edit_email.addTextChangedListener(afterTextChangedListener);


        //给RadioGroup单选按钮组注册监听器，但由于至少有一个RadioButton被选中，且注册启动只在button_register被点击后发生，那时会重新获取选择按钮状态，故无需监听
//        radio_group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(RadioGroup group, int checkedId) {
//                registerViewModel.loginDataChanged(edit_username.getText().toString(),
//                        edit_password.getText().toString(), edit_realName.getText().toString(),
//                        edit_realIdentity.getText().toString(), edit_phoneNumber.getText().toString(),
//                        edit_email.getText().toString(), radio_normal.isChecked() || radio_factory.isChecked());
//            }
//        });


        //给registerViewModel中的MuteLiveData<registerFormState>设置观察者，当因注册界面输入值发生变化导致registerFormState被重新赋值时触发调用OnChanged（）
        registerViewModel.getRegisterFormState().observe(this, new Observer<RegisterFormState>() {
            @Override
            public void onChanged(RegisterFormState registerFormState) {
                if(registerFormState == null){
                    return;
                }
                if(registerFormState.isDataValid()){
                    button_register.setEnabled(true);
                    return;
                }else{
                    button_register.setEnabled(false);
                }
                if (registerFormState.getUser_nickName_error() != null) {
                    edit_username.setError(getString(registerFormState.getUser_nickName_error()));
                }
                if (registerFormState.getUser_password_error() != null) {
                    edit_password.setError(getString(registerFormState.getUser_password_error()));
                }
                if (registerFormState.getUser_registerName_error() != null) {
                    edit_realName.setError(getString(registerFormState.getUser_registerName_error()));
                }
                if (registerFormState.getUser_identityId_error() != null) {
                    edit_realIdentity.setError(getString(registerFormState.getUser_identityId_error()));
                }
                if (registerFormState.getPhoneNumber_error() != null) {
                    edit_phoneNumber.setError(getString(registerFormState.getPhoneNumber_error()));
                }
                if (registerFormState.getEmail_error() != null) {
                    edit_email.setError(getString(registerFormState.getEmail_error()));
                }
            }
        });

        //给registerViewModel中的MuteLiveData<RegisterResult>设置观察者，当启动一次注册导致RegisterResult被重新赋值时触发调用OnChanged（）
        registerViewModel.getRegisterResult().observe(this, new Observer<RegisterResult>() {
            @Override
            public void onChanged(RegisterResult registerResult) {
                if(registerResult == null){
                    return;
                }
                bar_register.setVisibility(View.GONE);
                if (registerResult.isSuccess()) {
                    showRegisterSuccess(registerResult);
                }else{
                    showRegisterFailed(registerResult.getErrorReason());
                }
            }
        });


        //给button_register按钮注册监听器,点击该按钮触发注册程序
        button_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppCustomer customer = new AppCustomer(null, edit_username.getText().toString(), edit_password.getText().toString(),
                        0, edit_realName.getText().toString(), edit_realIdentity.getText().toString(), new Date(System.currentTimeMillis()),
                        radio_male.isChecked() ? true : false, radio_normal.isChecked() ? 1 : 2, null,  edit_phoneNumber.getText().toString(),
                        edit_email.getText().toString(), 0, 0);
                bar_register.setVisibility(View.VISIBLE);
                registerViewModel.register(customer, getApplicationContext());
            }
        });
    }


    //展示注册失败的通知
    private void showRegisterFailed(String errorReason) {
        Toast.makeText(getApplicationContext(), errorReason, Toast.LENGTH_LONG).show();


    }
    //展示注册成功的通知,弹出窗口选择直接登录还是返回登陆窗口
    private void showRegisterSuccess(final RegisterResult registerResult) {
        AlertDialog.Builder trans_dialog = new AlertDialog.Builder(RegisterActivity.this);
        trans_dialog.setTitle(getString(R.string.register_dialog_title));
        trans_dialog.setMessage(R.string.register_dialog_message);
        trans_dialog.setCancelable(false);
        //选择直接登录的话，会返回登陆界面直接登录并将新注册的用户置换掉SQLite中首个用户信息
        trans_dialog.setPositiveButton(R.string.register_dialog_posibutton, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent returnToLogin = new Intent();
                returnToLogin.putExtra("username", registerResult.getSuccessCustomer().getUser_nickName());
                returnToLogin.putExtra("password", registerResult.getSuccessCustomer().getUser_password());
                setResult(2, returnToLogin);
                finish();
            }
        });
        //选择只是返回登陆界面的话，只会返回登陆界面并将新注册的用户信息填写在登录UI中，但是不会置换掉SQLite中首个用户信息
        trans_dialog.setNegativeButton(R.string.register_dialog_negabutton, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent returnToLogin = new Intent();
                returnToLogin.putExtra("username", registerResult.getSuccessCustomer().getUser_nickName());
                returnToLogin.putExtra("password", registerResult.getSuccessCustomer().getUser_password());
                setResult(3, returnToLogin);
                finish();
            }
        });
        trans_dialog.show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        setResult(4);
        finish();
    }
}