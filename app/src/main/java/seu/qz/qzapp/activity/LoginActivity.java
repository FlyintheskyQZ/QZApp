package seu.qz.qzapp.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.litepal.LitePal;

import seu.qz.qzapp.R;
import seu.qz.qzapp.entity.AppCustomer;
import seu.qz.qzapp.login.ui.LoggedInUserView;
import seu.qz.qzapp.login.ui.LoginFormState;
import seu.qz.qzapp.login.ui.LoginResult;
import seu.qz.qzapp.login.ui.LoginViewModel;
import seu.qz.qzapp.login.ui.LoginViewModelFactory;
import seu.qz.qzapp.utils.SharedPreferencesUtils;


/**
 * 注册活动
 */
public class LoginActivity extends AppCompatActivity {

    private LoginViewModel loginViewModel;
    EditText usernameEditText;
    EditText passwordEditText;
    ProgressBar loadingProgressBar;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_login);
        //给视图添加Toolbar,并初始化
        Toolbar login_toolbar = findViewById(R.id.login_toolbar);
        initToolbar(login_toolbar);
        //创建LoginViewModel，用于存储登录信息数据
        loginViewModel = ViewModelProviders.of(this, new LoginViewModelFactory())
                .get(LoginViewModel.class);
        //获取UI界面控件
        usernameEditText = findViewById(R.id.username);
        passwordEditText = findViewById(R.id.password);
        final Button loginButton = findViewById(R.id.login);
        loadingProgressBar = findViewById(R.id.login_loading);
        final CheckBox remember_uandp = findViewById(R.id.remember_uandp);
        final Button registerButton = findViewById(R.id.register);




        //注册观察者，当登陆数据格式状态发生变化时，通知观察者做如下动作：
        //  1.设置登录按钮的使能
        //  2.设置用户名和密码的错误提示
        loginViewModel.getLoginFormState().observe(this, new Observer<LoginFormState>() {
            @Override
            public void onChanged(@Nullable LoginFormState loginFormState) {
                if (loginFormState == null) {
                    return;
                }
                loginButton.setEnabled(loginFormState.isDataValid());
                if (loginFormState.getUsernameError() != null) {
                    usernameEditText.setError(getString(loginFormState.getUsernameError()));
                }
                if (loginFormState.getPasswordError() != null) {
                    passwordEditText.setError(getString(loginFormState.getPasswordError()));
                }
            }
        });
        //注册观察者，当登陆登录结果发生变化时，通知观察者做如下动作：
        //  1.设置加载bar的显示与否
        //  2.设置成功登录或登陆失败后的UI显示
        //  3.设置result用于标识当前Activity
        loginViewModel.getLoginResult().observe(this, new Observer<LoginResult>() {
            @Override
            public void onChanged(@Nullable LoginResult loginResult) {
                if (loginResult == null) {
                    return;
                }
                loadingProgressBar.setVisibility(View.GONE);
                if (loginResult.getError() != null) {
                    showLoginFailed("登陆失败，请检查账户密码或进行注册");
                }
                if (loginResult.getSuccess() != null) {
                    updateUiWithUser(loginResult.getSuccess());
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.putExtra("username", loginResult.getCustomer().getUser_nickName());
//                    intent.putExtra("password", loginResult.getCustomer().getUser_password());
//                    intent.putExtra("customer", loginResult.getCustomer());
                    startActivity(intent);
                    finish();
                }
            }
        });



        //创建文本输入监测器，当登录信息重新填写时，更新LoginViewModel中的MuteLiveData，从而通知对应观察者做出反应
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
                loginViewModel.loginDataChanged(usernameEditText.getText().toString(),
                        passwordEditText.getText().toString());
            }
        };
        //注册文本监测器
        usernameEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.addTextChangedListener(afterTextChangedListener);
        //设置文本编辑器动作监听器，当点击“完成”按键时调用LoginViewModel的登录方法
        passwordEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    loginViewModel.login(usernameEditText.getText().toString(),
                            passwordEditText.getText().toString(), getApplicationContext(), remember_uandp.isChecked());
                }
                return false;
            }
        });



        //设置按键监听器，点击按键调用LoginViewModel方法登录
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadingProgressBar.setVisibility(View.VISIBLE);
                loginViewModel.login(usernameEditText.getText().toString(),
                        passwordEditText.getText().toString(), getApplicationContext(), remember_uandp.isChecked());
            }
        });
        //设置按键监听器，点击按键调用方法注册
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivityForResult(intent, 1);
                //finish();
            }
        });
        //尝试从SharedPreferences中获取缓存用户名和密码填充到UI中
        String usernameFromCache = SharedPreferencesUtils.getLoginData(getApplicationContext(), "username");
        String passwordFromCache = SharedPreferencesUtils.getLoginData(getApplicationContext(), "password");
        if(!usernameFromCache.isEmpty() && passwordFromCache.isEmpty()){
            usernameEditText.setText(usernameFromCache);
            passwordEditText.setText(passwordFromCache);
        }
    }

    //添加并初始化Toolbar
    private void initToolbar(Toolbar login_toolbar) {
        setSupportActionBar(login_toolbar);
        //添加返回按钮
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }


    //登陆成功后更新显示界面和浮条提示
    private void updateUiWithUser(LoggedInUserView model) {
        String welcome = getString(R.string.welcome) + model.getDisplayName();
        Toast.makeText(getApplicationContext(), welcome, Toast.LENGTH_LONG).show();
    }
    //浮条提示登录失败信息
    private void showLoginFailed(String errorNote) {
        Toast.makeText(getApplicationContext(), errorNote, Toast.LENGTH_SHORT).show();
    }



    //根据register活动的返回值来选择是否直接登录还是等待操作
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1 && resultCode == 2){
            String username = data.getStringExtra("username");
            String password = data.getStringExtra("password");
            loadingProgressBar.setVisibility(View.VISIBLE);
            loginViewModel.login(username, password, getApplicationContext(), true);
        }else if(requestCode == 1 && resultCode == 3){
            String username = data.getStringExtra("username");
            String password = data.getStringExtra("password");
            usernameEditText.setText(username);
            passwordEditText.setText(password);
        }
    }

    //设置toolbar的视图和按键
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.login_toolbar, menu);
        return true;
    }
    //设置菜单按键的监听机制，也可以通过toolbar.setOnMenuItemClickListener()实现
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