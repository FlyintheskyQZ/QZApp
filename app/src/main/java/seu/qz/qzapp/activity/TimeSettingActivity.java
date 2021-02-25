package seu.qz.qzapp.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.bigkoo.pickerview.builder.TimePickerBuilder;
import com.bigkoo.pickerview.listener.OnTimeSelectListener;
import com.bigkoo.pickerview.view.TimePickerView;

import java.util.Date;
import java.util.List;

import seu.qz.qzapp.R;
import seu.qz.qzapp.entity.BriefOrderItem;
import seu.qz.qzapp.utils.DateRelatedUtils;
import seu.qz.qzapp.utils.ListOperationUtils;

public class TimeSettingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.item_search_filter_time);
        final TextView searchFragment_dialog_time_end_filter = findViewById(R.id.searchfragment_dialog_time_end_filter);
        final TextView searchFragment_dialog_time_start_filter = findViewById(R.id.searchfragment_dialog_time_start_filter);
        //使能orderSetting_rentTime_begin并设置监听器
        final TimePickerView orderFilter_timeBegin = new TimePickerBuilder(this, new OnTimeSelectListener() {
            @Override
            public void onTimeSelect(Date date, View v) {//选中事件回调
                searchFragment_dialog_time_start_filter.setText(DateRelatedUtils.FormatOutputByDate(date, DateRelatedUtils.TYPE_CHINESE));
            }
        }).setType(new boolean[]{true, true, true, true, true, false})
                .setLabel("年", "月", "日", "时", "分", "")
                .build();
        searchFragment_dialog_time_start_filter.setEnabled(true);
        searchFragment_dialog_time_start_filter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                orderFilter_timeBegin.show();
            }
        });
        //使能orderSetting_rentTime_begin并设置监听器
        final TimePickerView orderFilter_timeEnd = new TimePickerBuilder(this, new OnTimeSelectListener() {
            @Override
            public void onTimeSelect(Date date, View v) {//选中事件回调
                searchFragment_dialog_time_end_filter.setText(DateRelatedUtils.FormatOutputByDate(date, DateRelatedUtils.TYPE_CHINESE));
            }
        }).setType(new boolean[]{true, true, true, true, true, false})
                .setLabel("年", "月", "日", "时", "分", "")
                .build();
        searchFragment_dialog_time_end_filter.setEnabled(true);
        searchFragment_dialog_time_end_filter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                orderFilter_timeEnd.show();
            }
        });
//                Date date_begin = DateRelatedUtils.formDateByString(orderSetting_rentTime_begin.getText().toString(),
//                        DateRelatedUtils.TYPE_CHINESE);
        Button searchFragment_dialog_time_order = findViewById(R.id.searchfragment_dialog_time_order);
        Button searchFragment_dialog_time_reverseOrder = findViewById(R.id.searchfragment_dialog_time_reverseorder);
        searchFragment_dialog_time_order.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Date date_begin = DateRelatedUtils.formDateByString(searchFragment_dialog_time_start_filter.getText().toString(),
                        DateRelatedUtils.TYPE_CHINESE);
                Date date_end = DateRelatedUtils.formDateByString(searchFragment_dialog_time_end_filter.getText().toString(),
                        DateRelatedUtils.TYPE_CHINESE);
                Intent intent = new Intent();
                intent.putExtra("date_begin", date_begin);
                intent.putExtra("date_end", date_end);
                setResult(1, intent);
                finish();
            }
        });
        searchFragment_dialog_time_reverseOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Date date_begin = DateRelatedUtils.formDateByString(searchFragment_dialog_time_start_filter.getText().toString(),
                        DateRelatedUtils.TYPE_CHINESE);
                Date date_end = DateRelatedUtils.formDateByString(searchFragment_dialog_time_end_filter.getText().toString(),
                        DateRelatedUtils.TYPE_CHINESE);
                Intent intent = new Intent();
                intent.putExtra("date_begin", date_begin);
                intent.putExtra("date_end", date_end);
                setResult(2, intent);
                finish();
            }
        });

    }
}