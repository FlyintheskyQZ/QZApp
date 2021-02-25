package seu.qz.qzapp.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;

import seu.qz.qzapp.R;
import seu.qz.qzapp.objectfactory.HttpConnectionFactory;

/**
 * 本类用于做HttpURLConnection的网络请求测试，没有用到
 */
public class HttpTest extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_http_test);
        Button button = findViewById(R.id.start_http);
        final TextView textView = findViewById(R.id.text_view);
  //      final WebView webView = findViewById(R.id.web_view);
 //       webView.getSettings().setJavaScriptEnabled(true);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                webView.setWebViewClient(new WebViewClient());
//                webView.loadUrl("http://10.208.10.227:8080/test");
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        HttpURLConnection connection = HttpConnectionFactory.getHttpConnectionByURLStr("test", getApplicationContext());
                        if(connection != null){
                            try {
                                connection.setRequestMethod("GET");
                                connection.setConnectTimeout(8000);
                                connection.setReadTimeout(8000);
                                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                                final StringBuilder builder = new StringBuilder();
                                String line = null;
                                while((line = reader.readLine()) != null){
                                    builder.append(line);
                                }
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        textView.setText(builder.toString());
                                    }
                                });


                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        }
                    }
                }).start();
            }
        });
    }


}