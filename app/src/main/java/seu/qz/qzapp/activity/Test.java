package seu.qz.qzapp.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;

import seu.qz.qzapp.R;

/**
 * 测试主活动，目前主要用于测试各个活动
 */
public class Test extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        Button button = findViewById(R.id.main_to_agora);
        String directory =getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).getPath();
        System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:" + directory);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Test.this, AdoraDisplayActivity.class);
                startActivity(intent);
            }
        });
        Button button_http = findViewById(R.id.http_test);
        button_http.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Test.this, HttpTest.class);
                startActivity(intent);
            }
        });

        Button button_login = findViewById(R.id.login_test);
        button_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Test.this, LoginActivity.class);
                startActivity(intent);
            }
        });

        Button button_main = findViewById(R.id.main_test);
        button_main.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Test.this, MainActivity.class);
                startActivity(intent);
            }
        });

        Button button_orderSetting = findViewById(R.id.test_orderSetting);
        button_orderSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Test.this, OrderSettingActivity.class);
                startActivity(intent);
            }
        });
    }

}