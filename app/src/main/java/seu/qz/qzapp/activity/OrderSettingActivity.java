package seu.qz.qzapp.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProviders;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.bigkoo.pickerview.builder.TimePickerBuilder;
import com.bigkoo.pickerview.listener.OnTimeSelectListener;
import com.bigkoo.pickerview.view.TimePickerView;
import com.contrarywind.listener.OnItemSelectedListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import seu.qz.qzapp.R;
import seu.qz.qzapp.activity.viewmodel.OrderContentViewModel;
import seu.qz.qzapp.activity.viewmodel.OrderSettingViewModel;
import seu.qz.qzapp.database.LitePalUtils;
import seu.qz.qzapp.entity.AppCustomer;
import seu.qz.qzapp.entity.LOIInstrument;
import seu.qz.qzapp.entity.ProvideOrder;
import seu.qz.qzapp.utils.CommonUtils;
import seu.qz.qzapp.utils.DateRelatedUtils;

public class OrderSettingActivity extends AppCompatActivity {

    private OrderSettingViewModel orderSettingViewModel;

    Toolbar orderSetting_toolbar;

    TextView orderSetting_saler_name;

    Spinner orderSetting_device_spinner;
    TextView orderSetting_device_explanation;
    ArrayAdapter<String> device_adapter;

    TextView orderSetting_factory;

    TextView orderSetting_rentTime_begin;
    TimePickerView orderSetting_timeBegin;

    TextView orderSetting_rentTime_end;
    TimePickerView orderSetting_timeEnd;

    EditText orderSetting_price;

    TextView orderSetting_order_status;

    TextView orderSetting_order_placedtime;

    TextView orderSettng_order_materialname;

    Spinner orderSetting_materialType_spinner;
    ArrayAdapter<String> materialType_adapter;

    EditText orderSetting_order_extra;

    View orderSetting_statusNote_backView;
    TextView orderSetting_statusNote_text;
    EditText orderSetting_statusNote;

    Button orderSetting_button_positive;
    Button orderSetting_button_negative;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_order_setting);
        //创建并初始化viewmodel
        orderSettingViewModel = ViewModelProviders.of(this).get(OrderSettingViewModel.class);
        initViewModel();
    }

    //初始化仪器项，一般用于创建订单，即ambition==1:若当前用户为saler，则立即查询其持有的仪器信息，存入viewModel;若当前用户为saler，则在查询ProvideOrder之后再查询仪器信息
    private void initInstruments() {
        AppCustomer mainCustomer = orderSettingViewModel.getMainCustomer();
        System.out.println("this customer is !!!!!!!!!!!!!!!!!!!!!!!!!:" + mainCustomer);
        if(mainCustomer != null){
            int level = mainCustomer.getAuthority_level();
            if(level == 1){
                return;
            }else if(level == 2){
                int ambition = orderSettingViewModel.getAmbition();
                if(ambition == 1){
                    String devices = mainCustomer.getRelated_device_id();
                    //若saler没有注册的仪器，则直接退出（此种情况对应于创建订单）
                    if(devices == null || devices.isEmpty()){
                        AlertDialog.Builder dialog_InstrumentLack = new AlertDialog.Builder(this);
                        dialog_InstrumentLack.setTitle(R.string.orderSetting_error_InstrumentLack_title);
                        dialog_InstrumentLack.setMessage(R.string.orderSetting_error_InstrumentLack_message);
                        dialog_InstrumentLack.setCancelable(false);
                        dialog_InstrumentLack.setPositiveButton(R.string.orderSetting_error_InstrumentLack_button, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                setResult(1);
                                finish();
                            }
                        });
                        dialog_InstrumentLack.show();
                    }else {
                        orderSettingViewModel.getInstrumentsFromServer(mainCustomer.getUser_id(), this);
                    }
                }
            }
        }
    }

    private void initUIObject() {
        orderSetting_toolbar = findViewById(R.id.order_setting_toolbar);
        orderSetting_saler_name = findViewById(R.id.ordersettng_saler_name);
        orderSetting_device_spinner = findViewById(R.id.ordersetting_device_spinner);
        if(orderSettingViewModel.getMainCustomer().getAuthority_level() == 2){
            initDeviceSpinner(orderSettingViewModel.getMainCustomer().getRelated_device_id());
        }
        orderSetting_device_explanation = findViewById(R.id.ordersetting_device_explanation);
        orderSetting_factory = findViewById(R.id.ordersetting_factory);
        orderSetting_rentTime_begin = findViewById(R.id.ordersettng_renttime_begin);
        orderSetting_rentTime_end = findViewById(R.id.ordersetting_renttime_end);
        orderSetting_price = findViewById(R.id.ordersetting_price);
        orderSetting_order_status = findViewById(R.id.ordersetting_order_status);
        orderSetting_order_placedtime = findViewById(R.id.ordersetting_order_placedtime);
        orderSettng_order_materialname = findViewById(R.id.ordersetting_order_materialname);
        orderSetting_materialType_spinner = findViewById(R.id.ordersetting_materialtype_spinner);
        initMaterialTypeSpinner();
        orderSetting_order_extra = findViewById(R.id.ordersetting_order_extra);
        orderSetting_statusNote_text = findViewById(R.id.ordersetting_statusNote_text);
        orderSetting_statusNote = findViewById(R.id.ordersetting_statusNote);
        orderSetting_button_positive = findViewById(R.id.ordersetting_button_positive);
        orderSetting_button_negative = findViewById(R.id.ordersetting_button_negative);
    }

    //根据给divice的spinner初始化ArrayAdapter
    private void initDeviceSpinner(String related_device_id) {
        if(related_device_id == null || related_device_id.isEmpty()){
            return;
        }
        List<String> device_list = new ArrayList<>();
        device_list.add("未选");
        //如果参数为FromViewModel则表示当前mainCustomer是user，需要在异步查询devices后才能初始化deviceSpinner；
        // 否则直接以saler的related_device_id字符串初始化
        if(related_device_id.equals("FromViewModel")){
            List<LOIInstrument> loiInstruments = orderSettingViewModel.getInstruments();
            for (int i = 0; i < loiInstruments.size(); i++){
                device_list.add(String.valueOf(i + 1));
            }
        }else {
            String[] devices = related_device_id.split(";");
            for(int i = 0; i < devices.length; i++){
                device_list.add(devices[i]);
            }
        }
        //此处不知道会不会覆盖默认背景！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！
        device_adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, device_list);
        device_adapter.setDropDownViewResource(R.layout.ordersetting_spinner_dropdown_item);
        orderSetting_device_spinner.setAdapter(device_adapter);
    }

    //给
    private void initMaterialTypeSpinner() {
        //此处不知道会不会覆盖默认背景！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！
        materialType_adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, orderSettingViewModel.getMaterial_types());
        materialType_adapter.setDropDownViewResource(R.layout.ordersetting_spinner_dropdown_item);
        orderSetting_materialType_spinner.setAdapter(materialType_adapter);
    }

    //配置UI的界面,此函数只用在ProvideOrder已存在的情况下（即创建Order无需调用）,根据已得到的ProvideOrder，
    // 将其数据可视化到UI界面上，并根据customer和订单confirmed的情况设定可编辑状态
    public void showOrderContent() {

        final ProvideOrder order = orderSettingViewModel.getOrder();
        int level = orderSettingViewModel.getMainCustomer().getAuthority_level();
        if(order != null || order == null){
            //如果mainCustomer为user，则需要在异步获取LOIInstruments后在此处初始化deviceSpinner
            if(orderSettingViewModel.getMainCustomer().getAuthority_level() == 1){
                initDeviceSpinner("FromViewModel");
            }
            String username = order.getUser_name();
            orderSetting_saler_name.setText(username != null ? username : "");
            Integer device_order = order.getDevice_orderForSaler();
            orderSetting_device_spinner.setSelection(device_order != null ? device_order : 0, true);
            String device_address = null;
            if(device_order == null){
                device_address = "";
            }else {
                device_address = orderSettingViewModel.getInstruments().get(device_order - 1).getFactory_address();
            }

            //initSalerPart()中"地址："需要与商家修改订单中的device_address同步，以及与showOrderContent()方法中开头展示device_address要同步
            orderSetting_device_explanation.setText("地址：" + device_address);
            orderSetting_factory.setText(order.getFactory_name());
            orderSetting_rentTime_begin.setText(DateRelatedUtils.FormatOutputByDate(order.getRentTime_begin(), DateRelatedUtils.TYPE_CHINESE));
            orderSetting_rentTime_end.setText(DateRelatedUtils.FormatOutputByDate(order.getRentTime_end(), DateRelatedUtils.TYPE_CHINESE));
            Integer order_price = order.getDisplay_price();
            if(order_price == null){
                orderSetting_price.setHint("￥0");
            }else {
                orderSetting_price.setText("￥" + order.getDisplay_price());
            }
            int status_order = order.getOrder_confirmed();
            String status = null;
            switch (status_order){
                case 0:
                    status = "待订购";
                    break;
                case 1:
                    status = "待审核";
                    break;
                case 2:
                    status = "审核驳回，需重审";
                    break;
                case 3:
                    status = "审核通过";
                    break;
                default:break;
            }
            orderSetting_order_status.setText(status);
            orderSetting_order_placedtime.setText(status_order == 3 ?
                    DateRelatedUtils.FormatOutputByDate(order.getOrder_placed(), DateRelatedUtils.TYPE_CHINESE) : "订单未成功下单");
            String material_name = order.getMaterialName();
            orderSettng_order_materialname.setHint("待客户填写");
            if(material_name != null && !material_name.isEmpty()){
                orderSettng_order_materialname.setText(material_name);
            }
            Integer material_type = order.getMaterialType();
            orderSetting_materialType_spinner.setEnabled(true);
            orderSetting_materialType_spinner.setSelection(material_type != null ? material_type : 0, true);
            String order_extra = order.getMaterial_explanation();
            if(order_extra == null || order_extra.isEmpty()){
                orderSetting_order_extra.setHint("待客户填写");
            }else {
                orderSetting_order_extra.setText(order_extra);
            }
            switch (order.getOrder_confirmed()){
                //订单未下单
                case 0:
                    orderSetting_statusNote_text.setVisibility(View.GONE);
                    orderSetting_statusNote.setVisibility(View.GONE);
                    //当前用户为客户
                    if(level == 1){
                        initSalerPart(2);
                        initUserPart(1);
                        orderSetting_button_positive.setText("填写完成");
                        orderSetting_button_positive.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                //获取客户填写的部分，并判断是否是合法输入，同时判断客户余额是否充足
                                int error = 0;
                                String material_name = orderSettng_order_materialname.getText().toString();
                                int material_type = orderSetting_materialType_spinner.getSelectedItemPosition();
                                String order_extra = orderSetting_order_extra.getText().toString().trim();
                                int balance = orderSettingViewModel.getMainCustomer().getUser_balance();
                                if(material_name == null || material_name.isEmpty()){
                                    error = 1;
                                }else if(material_type == 0){
                                    error = 2;
                                }else if(material_name.equals(order.getMaterialName()) && material_type == 0
                                        && order_extra.equals(order.getMaterial_explanation())){
                                    error = 3;
                                }else if(balance < order.getDisplay_price()){
                                    error = 4;
                                }
                                //输入不合法则弹出警告框
                                if(error != 0){
                                    AlertDialog.Builder dialog_wrongContent = new AlertDialog.Builder(OrderSettingActivity.this);
                                    if(error == 1){
                                        dialog_wrongContent.setTitle(R.string.orderSetting_error_EmptymaterialName_title);
                                        dialog_wrongContent.setMessage(R.string.orderSetting_error_EmptymaterialName_message);
                                    }else if(error == 2){
                                        dialog_wrongContent.setTitle(R.string.orderSetting_error_EmptymaterialType_title);
                                        dialog_wrongContent.setMessage(R.string.orderSetting_error_EmptymaterialType_message);
                                    }else if(error == 3){
                                        dialog_wrongContent.setTitle(R.string.orderSetting_error_noChangeModify_title);
                                        dialog_wrongContent.setMessage(R.string.orderSetting_error_noChangeModify_message);
                                    }else if(error == 4){
                                        dialog_wrongContent.setTitle(R.string.orderSetting_error_lackBalance_title);
                                        dialog_wrongContent.setMessage(R.string.orderSetting_error_lackBalance_message);
                                    }
                                    final int final_error = error;
                                    dialog_wrongContent.setCancelable(false);
                                    dialog_wrongContent.setPositiveButton(R.string.orderSetting_error_positive_button, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if(final_error == 4){
                                                //跳转到充值界面！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！




                                            }
                                        }
                                    });
                                    if(final_error == 4){
                                        dialog_wrongContent.setNegativeButton(R.string.orderSetting_error_negative_button, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                            }
                                        });
                                    }
                                    dialog_wrongContent.show();
                                    //输入无误则直接提交到服务器，并在服务器更新成功后更新本地mainCustomer,返回上一Activity
                                }else {
                                    AppCustomer mainCustomer = orderSettingViewModel.getMainCustomer();
                                    order.setUser_id(mainCustomer.getUser_id());
                                    order.setUser_name(mainCustomer.getUser_nickName());
                                    order.setOrderForRelatedUser(mainCustomer.getNumberForProvideOrders() + 1);
                                    order.setOrder_confirmed(1);
                                    order.setMaterialName(material_name);
                                    order.setMaterialType(material_type);
                                    order.setMaterial_explanation(order_extra);
                                    mainCustomer.setNumberForProvideOrders(mainCustomer.getNumberForProvideOrders() + 1);
                                    orderSettingViewModel.updateProvideOrder(order, OrderSettingActivity.this);
                                }
                            }
                        });
                        orderSetting_button_negative.setText("取消订购");
                        orderSetting_button_negative.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                finish();
                            }
                        });
                        //当前用户为商家
                    }else {
                        initSalerPart(1);
                        initUserPart(2);
                        if(orderSettingViewModel.getAmbition() == 2){
                            orderSetting_button_positive.setText("确认修改");
                            orderSetting_button_negative.setText("取消修改");
                        }else {
                            orderSetting_saler_name.setText(orderSettingViewModel.getMainCustomer().getUser_nickName());
                            orderSetting_button_positive.setText("确认发布");
                            orderSetting_button_negative.setText("取消发布");
                        }
                        orderSetting_button_positive.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                int related_device_id = orderSetting_device_spinner.getSelectedItemPosition();
                                int device_id = orderSettingViewModel.getInstruments().get(related_device_id - 1).getDevice_id();
                                //initSalerPart()中"地址："需要与商家修改订单中的device_address同步，以及与showOrderContent()方法中开头展示device_address要同步
                                String device_address = orderSetting_device_explanation.getText().toString().substring("地址：".length());
                                String factory_name = orderSetting_factory.getText().toString();
                                Date date_begin = DateRelatedUtils.formDateByString(orderSetting_rentTime_begin.getText().toString(),
                                        DateRelatedUtils.TYPE_CHINESE);
                                Date date_end = DateRelatedUtils.formDateByString(orderSetting_rentTime_end.getText().toString(),
                                        DateRelatedUtils.TYPE_CHINESE);
                                int price = CommonUtils.getPriceFromString(orderSetting_price.getText().toString());
                                boolean equal_begin = DateRelatedUtils.FormatOutputByDate(date_begin, DateRelatedUtils.TYPE_CHINESE).equals(DateRelatedUtils
                                        .FormatOutputByDate(order.getRentTime_begin(), DateRelatedUtils.TYPE_CHINESE));
                                boolean equal_end = DateRelatedUtils.FormatOutputByDate(date_end, DateRelatedUtils.TYPE_CHINESE).equals(DateRelatedUtils
                                        .FormatOutputByDate(order.getRentTime_end(), DateRelatedUtils.TYPE_CHINESE));
                                if(device_id == order.getDevice_id() && equal_begin && equal_end && price == order.getDisplay_price()){
                                    AlertDialog.Builder dialog = new AlertDialog.Builder(OrderSettingActivity.this);
                                    dialog.setTitle(R.string.orderSetting_error_noChangeModify_title);
                                    dialog.setMessage(R.string.orderSetting_error_noChangeModify_message);
                                    dialog.setCancelable(false);
                                    dialog.setPositiveButton(R.string.orderSetting_error_positive_button, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                        }
                                    });
                                    dialog.show();
                                }else {
                                    order.setDevice_orderForSaler(related_device_id);
                                    order.setDevice_id(device_id);
                                    order.setDevice_address(device_address);
                                    order.setFactory_name(factory_name);
                                    order.setRentTime_begin(date_begin);
                                    order.setRentTime_end(date_end);
                                    order.setDisplay_price(price);
                                    if(orderSettingViewModel.getAmbition() == 2){
                                        orderSettingViewModel.updateProvideOrder(order, OrderSettingActivity.this);
                                    }else {
                                        orderSettingViewModel.addNewProvideOrder(order, OrderSettingActivity.this);
                                    }

                                }
                            }
                        });
                        orderSetting_button_negative.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                finish();
                            }
                        });
                    }
                    break;
                    //订单已下单，等待审核
                case 1:
                    orderSetting_statusNote_text.setVisibility(View.VISIBLE);
                    orderSetting_statusNote.setVisibility(View.VISIBLE);
                    //当前用户为客户
                    if(level == 1){
                        initSalerPart(2);
                        initUserPart(1);
                        orderSetting_statusNote.setText("暂无审核意见");
                        orderSetting_statusNote.setEnabled(false);
                        orderSetting_button_positive.setText("确认修改");
                        orderSetting_button_positive.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                //此部分类似order_confirmed=0时的客户操作，只是最后整合到order中时只需要更新客户需要填写的部分
                                //获取客户填写的部分，并判断是否是有更改且为合法输入
                                int error = 0;
                                String material_name = orderSettng_order_materialname.getText().toString().trim();
                                int material_type = orderSetting_materialType_spinner.getSelectedItemPosition();
                                String order_extra = orderSetting_order_extra.getText().toString().trim();
                                if(material_name == null || material_name.isEmpty()){
                                    error = 1;
                                }else if(material_type == 0){
                                    error = 2;
                                    //material_explanation由于通过文本控件获取的String不会为null，会为""，故在此处将order中为null的部分转化为空字符串
                                    // 而material_name则判断过null和空，无需担心
                                }else if(material_name.equals(order.getMaterialName()) && material_type == order.getMaterialType()
                                        && order_extra.equals(order.getMaterial_explanation() == null ? "" : order.getMaterial_explanation())){
                                    error = 3;
                                }
                                //输入不合法或无更改则弹出警告框
                                if(error != 0){
                                    AlertDialog.Builder dialog_wrongContent = new AlertDialog.Builder(OrderSettingActivity.this);
                                    if(error == 1){
                                        dialog_wrongContent.setTitle(R.string.orderSetting_error_EmptymaterialName_title);
                                        dialog_wrongContent.setMessage(R.string.orderSetting_error_EmptymaterialName_message);
                                    }else if(error == 2){
                                        dialog_wrongContent.setTitle(R.string.orderSetting_error_EmptymaterialType_title);
                                        dialog_wrongContent.setMessage(R.string.orderSetting_error_EmptymaterialType_message);
                                    }else if(error == 3){
                                        dialog_wrongContent.setTitle(R.string.orderSetting_error_noChangeModify_title);
                                        dialog_wrongContent.setMessage(R.string.orderSetting_error_noChangeModify_message);
                                    }
                                    dialog_wrongContent.setCancelable(false);
                                    dialog_wrongContent.setPositiveButton(R.string.orderSetting_error_positive_button, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                        }
                                    });
                                    dialog_wrongContent.show();
                                    //输入无误则直接提交到服务器，并在服务器更新成功后更新本地mainCustomer,返回上一Activity
                                }else {
                                    //此处修改只涉及User可修改的三个文本域
                                    order.setMaterialName(material_name);
                                    order.setMaterialType(material_type);
                                    order.setMaterial_explanation(order_extra);
                                    orderSettingViewModel.updateProvideOrder(order, OrderSettingActivity.this);
                                }
                            }
                        });
                        orderSetting_button_negative.setText("取消修改");
                        orderSetting_button_negative.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                finish();
                            }
                        });
                        //当前用户为商家
                    }else {
                        initSalerPart(1);
                        initUserPart(2);
                        orderSetting_statusNote.setHint("请审核客户填写材料，并在此处填写意见。");
                        orderSetting_statusNote.setEnabled(true);
                        if(orderSettingViewModel.getModifyorCheck() == 1){
                            orderSetting_button_positive.setText("确认修改");
                            orderSetting_button_positive.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    int related_device_id = orderSetting_device_spinner.getSelectedItemPosition();
                                    int device_id = orderSettingViewModel.getInstruments().get(related_device_id - 1).getDevice_id();
                                    String device_address = orderSetting_device_explanation.getText().toString().substring("地址：".length());
                                    String factory_name = orderSetting_factory.getText().toString();
                                    Date date_begin = DateRelatedUtils.formDateByString(orderSetting_rentTime_begin.getText().toString(),
                                            DateRelatedUtils.TYPE_CHINESE);
                                    Date date_end = DateRelatedUtils.formDateByString(orderSetting_rentTime_end.getText().toString(),
                                            DateRelatedUtils.TYPE_CHINESE);
                                    int price = CommonUtils.getPriceFromString(orderSetting_price.getText().toString());
                                    boolean equal_begin = DateRelatedUtils.FormatOutputByDate(date_begin, DateRelatedUtils.TYPE_CHINESE).equals(DateRelatedUtils
                                            .FormatOutputByDate(order.getRentTime_begin(), DateRelatedUtils.TYPE_CHINESE));
                                    boolean equal_end = DateRelatedUtils.FormatOutputByDate(date_end, DateRelatedUtils.TYPE_CHINESE).equals(DateRelatedUtils
                                            .FormatOutputByDate(order.getRentTime_end(), DateRelatedUtils.TYPE_CHINESE));
                                    if(device_id == order.getDevice_id() && equal_begin && equal_end && price == order.getDisplay_price()){
                                        AlertDialog.Builder dialog = new AlertDialog.Builder(OrderSettingActivity.this);
                                        dialog.setTitle(R.string.orderSetting_error_noChangeModify_title);
                                        dialog.setMessage(R.string.orderSetting_error_noChangeModify_message);
                                        dialog.setCancelable(false);
                                        dialog.setPositiveButton(R.string.orderSetting_error_positive_button, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                            }
                                        });
                                        dialog.show();
                                    }else {
                                        order.setDevice_orderForSaler(related_device_id);
                                        order.setDevice_id(device_id);
                                        order.setDevice_address(device_address);
                                        order.setFactory_name(factory_name);
                                        order.setRentTime_begin(date_begin);
                                        order.setRentTime_end(date_end);
                                        order.setDisplay_price(price);
                                        orderSettingViewModel.updateProvideOrder(order, OrderSettingActivity.this);
                                    }
                                }
                            });
                            orderSetting_button_negative.setText("取消修改");
                            orderSetting_button_negative.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    finish();
                                }
                            });
                        }else if(orderSettingViewModel.getModifyorCheck() == 2){
                            orderSetting_button_positive.setText("审核通过");
                            orderSetting_button_positive.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    AppCustomer mainCustomer = orderSettingViewModel.getMainCustomer();
                                    mainCustomer.setUser_balance(mainCustomer.getUser_balance() - order.getDisplay_price());
                                    //设置订单号在服务器中设置
                                    order.setOrder_confirmed(3);
                                    order.setExtra_explanation(null);
                                    order.setOrder_placed(new Date(System.currentTimeMillis()));
                                    orderSettingViewModel.updateProvideOrder(order, OrderSettingActivity.this);
                                }
                            });
                            orderSetting_button_negative.setText("退回修改");
                            orderSetting_button_negative.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    String modify_opinion = orderSetting_statusNote.getText().toString();
                                    if(modify_opinion == null || modify_opinion.isEmpty()){
                                        AlertDialog.Builder dialog = new AlertDialog.Builder(OrderSettingActivity.this);
                                        dialog.setTitle(R.string.orderSetting_error_lackModifyOpinion_title);
                                        dialog.setMessage(R.string.orderSetting_error_lackModifyOpinion_message);
                                        dialog.setCancelable(false);
                                        dialog.setPositiveButton(R.string.orderSetting_error_positive_button, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                            }
                                        });
                                        dialog.show();
                                    }else {
                                        order.setOrder_confirmed(2);
                                        order.setExtra_explanation(modify_opinion);
                                        orderSettingViewModel.updateProvideOrder(order, OrderSettingActivity.this);
                                    }
                                }
                            });
                        }
                    }
                    break;
                    //订单审核未通过
                case 2:
                    orderSetting_statusNote_text.setVisibility(View.VISIBLE);
                    orderSetting_statusNote.setVisibility(View.VISIBLE);
                    //当前用户为客户
                    if(level == 1){
                        initSalerPart(2);
                        initUserPart(1);
                        orderSetting_statusNote.setText("审核未通过，原因是：" + order.getExtra_explanation());
                        orderSetting_statusNote.setEnabled(false);
                        orderSetting_button_positive.setText("确认修改");
                        orderSetting_button_positive.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                //此部分类似order_confirmed=0时的客户操作，只是最后整合到order中时只需要更新客户需要填写的部分
                                //获取客户填写的部分，并判断是否是有更改且为合法输入
                                int error = 0;
                                String material_name = orderSettng_order_materialname.getText().toString().trim();
                                int material_type = orderSetting_materialType_spinner.getSelectedItemPosition();
                                String order_extra = orderSetting_order_extra.getText().toString().trim();
                                if(material_name == null || material_name.isEmpty()){
                                    error = 1;
                                }else if(material_type == 0){
                                    error = 2;
                                }else if(material_name.equals(order.getMaterialName()) && material_type == order.getMaterialType()
                                        && order_extra.equals(order.getMaterial_explanation() == null ? "" : order.getMaterial_explanation())){
                                    error = 3;
                                }
                                //输入不合法或无更改则弹出警告框
                                if(error != 0){
                                    AlertDialog.Builder dialog_wrongContent = new AlertDialog.Builder(OrderSettingActivity.this);
                                    if(error == 1){
                                        dialog_wrongContent.setTitle(R.string.orderSetting_error_EmptymaterialName_title);
                                        dialog_wrongContent.setMessage(R.string.orderSetting_error_EmptymaterialName_message);
                                    }else if(error == 2){
                                        dialog_wrongContent.setTitle(R.string.orderSetting_error_EmptymaterialType_title);
                                        dialog_wrongContent.setMessage(R.string.orderSetting_error_EmptymaterialType_message);
                                    }else if(error == 3){
                                        dialog_wrongContent.setTitle(R.string.orderSetting_error_noChangeModify_title);
                                        dialog_wrongContent.setMessage(R.string.orderSetting_error_noChangeModify_message);
                                    }
                                    dialog_wrongContent.setCancelable(false);
                                    dialog_wrongContent.setPositiveButton(R.string.orderSetting_error_positive_button, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                        }
                                    });
                                    dialog_wrongContent.show();
                                    //输入无误则直接提交到服务器，并在服务器更新成功后更新本地mainCustomer,返回上一Activity
                                }else {
                                    //此处修改只涉及User可修改的三个文本域
                                    order.setOrder_confirmed(1);
                                    order.setMaterialName(material_name);
                                    order.setMaterialType(material_type);
                                    order.setMaterial_explanation(order_extra);
                                    orderSettingViewModel.updateProvideOrder(order, OrderSettingActivity.this);
                                }
                            }
                        });
                        orderSetting_button_negative.setText("取消修改");
                        orderSetting_button_negative.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                finish();
                            }
                        });
                        //当前用户为商家
                    }else {
                        initSalerPart(1);
                        initUserPart(2);
                        orderSetting_statusNote.setText(order.getExtra_explanation());
                        orderSetting_statusNote.setEnabled(true);
                        if(orderSettingViewModel.getModifyorCheck() == 1){
                            orderSetting_button_positive.setText("确认修改");
                            orderSetting_button_positive.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    int related_device_id = orderSetting_device_spinner.getSelectedItemPosition();
                                    int device_id = orderSettingViewModel.getInstruments().get(related_device_id - 1).getDevice_id();
                                    String device_address = orderSetting_device_explanation.getText().toString().substring("地址：".length());
                                    String factory_name = orderSetting_factory.getText().toString();
                                    Date date_begin = DateRelatedUtils.formDateByString(orderSetting_rentTime_begin.getText().toString(),
                                            DateRelatedUtils.TYPE_CHINESE);
                                    Date date_end = DateRelatedUtils.formDateByString(orderSetting_rentTime_end.getText().toString(),
                                            DateRelatedUtils.TYPE_CHINESE);
                                    int price = CommonUtils.getPriceFromString(orderSetting_price.getText().toString());
                                    boolean equal_begin = DateRelatedUtils.FormatOutputByDate(date_begin, DateRelatedUtils.TYPE_CHINESE).equals(DateRelatedUtils
                                            .FormatOutputByDate(order.getRentTime_begin(), DateRelatedUtils.TYPE_CHINESE));
                                    boolean equal_end = DateRelatedUtils.FormatOutputByDate(date_end, DateRelatedUtils.TYPE_CHINESE).equals(DateRelatedUtils
                                            .FormatOutputByDate(order.getRentTime_end(), DateRelatedUtils.TYPE_CHINESE));
                                    if(device_id == order.getDevice_id() && equal_begin && equal_end && price == order.getDisplay_price()){
                                        AlertDialog.Builder dialog = new AlertDialog.Builder(OrderSettingActivity.this);
                                        dialog.setTitle(R.string.orderSetting_error_noChangeModify_title);
                                        dialog.setMessage(R.string.orderSetting_error_noChangeModify_message);
                                        dialog.setCancelable(false);
                                        dialog.setPositiveButton(R.string.orderSetting_error_positive_button, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                            }
                                        });
                                        dialog.show();
                                    }else {
                                        order.setDevice_orderForSaler(related_device_id);
                                        order.setDevice_id(device_id);
                                        order.setDevice_address(device_address);
                                        order.setFactory_name(factory_name);
                                        order.setRentTime_begin(date_begin);
                                        order.setRentTime_end(date_end);
                                        order.setDisplay_price(price);
                                        orderSettingViewModel.updateProvideOrder(order, OrderSettingActivity.this);
                                    }
                                }
                            });
                            orderSetting_button_negative.setText("取消修改");
                            orderSetting_button_negative.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    finish();
                                }
                            });
                        }else if(orderSettingViewModel.getModifyorCheck() == 2){
                            orderSetting_button_positive.setText("审核通过");
                            orderSetting_button_positive.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    AppCustomer mainCustomer = orderSettingViewModel.getMainCustomer();
                                    mainCustomer.setUser_balance(mainCustomer.getUser_balance() - order.getDisplay_price());
                                    order.setOrder_confirmed(3);
                                    order.setExtra_explanation(null);
                                    order.setOrder_placed(new Date(System.currentTimeMillis()));
                                    orderSettingViewModel.updateProvideOrder(order, OrderSettingActivity.this);
                                }
                            });
                            orderSetting_button_negative.setText("修改意见");
                            orderSetting_button_negative.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    String modify_opinion = orderSetting_statusNote.getText().toString();
                                    int error = 0;
                                    if(modify_opinion == null || modify_opinion.isEmpty()){
                                        error = 1;
                                    }else if(modify_opinion.equals(order.getExtra_explanation())){
                                        error = 2;
                                    }
                                    if(error != 0){
                                        AlertDialog.Builder dialog = new AlertDialog.Builder(OrderSettingActivity.this);
                                        if(error == 1){
                                            dialog.setTitle(R.string.orderSetting_error_lackModifyOpinion_title);
                                            dialog.setMessage(R.string.orderSetting_error_lackModifyOpinion_message);
                                            //在不改变审批结果的前提下需修改审批意见
                                        }else if(error == 2){
                                            dialog.setTitle(R.string.orderSetting_error_ModifyOpinionnoChange_title);
                                            dialog.setMessage(R.string.orderSetting_error_ModifyOpinionnoChange_message);
                                        }
                                        dialog.setCancelable(false);
                                        dialog.setPositiveButton(R.string.orderSetting_error_positive_button, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                            }
                                        });
                                        dialog.show();
                                    }else {
                                        order.setOrder_confirmed(2);
                                        order.setExtra_explanation(modify_opinion);
                                        orderSettingViewModel.updateProvideOrder(order, OrderSettingActivity.this);
                                    }
                                }
                            });
                        }
                    }
                    break;
                    //订单审核通过，user和saler都无法修改订单
                case 3:
                    orderSetting_statusNote_text.setVisibility(View.GONE);
                    orderSetting_statusNote.setVisibility(View.GONE);
                    initSalerPart(1);
                    initUserPart(1);
                    orderSetting_button_positive.setVisibility(View.GONE);
                    orderSetting_button_negative.setVisibility(View.GONE);
                    break;
                default:break;
            }
        }else {
            //创建订单

        }
    }



    //status:1,saler部分可编辑;2,saler部分不可编辑
    private void initUserPart(int status) {
        if(status == 1){
            //使能EditText控件orderSettng_order_materialname
            orderSettng_order_materialname.setEnabled(true);

            //使能orderSetting_device_spinner并设置监听器
            orderSetting_materialType_spinner.setEnabled(true);

            //使能EditText控件orderSetting_order_extra
            orderSetting_order_extra.setEnabled(true);
        }else if(status == 2){
            //使下面三个控件失效
            orderSettng_order_materialname.setEnabled(false);
            orderSetting_materialType_spinner.setEnabled(false);
            orderSetting_order_extra.setEnabled(false);
        }else{
            return;
        }
    }

    //status:1,saler部分可编辑;2,saler部分不可编辑
    private void initSalerPart(int status) {
        if(status == 1){
            //使能orderSetting_device_spinner并设置监听器
            orderSetting_device_spinner.setEnabled(true);
            orderSetting_device_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    //initSalerPart()中"地址："需要与商家修改订单中的device_address同步，以及与showOrderContent()方法中开头展示device_address要同步
                    orderSetting_device_explanation.setText(position != 0 ?
                            "地址：" + orderSettingViewModel.getInstruments().get(position - 1).getFactory_address() : "");
                    orderSetting_factory.setText(position != 0 ? orderSettingViewModel.getInstruments().get(position - 1).getFactory_name() : "");
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

            //使能orderSetting_rentTime_begin并设置监听器
            orderSetting_timeBegin = new TimePickerBuilder(this, new OnTimeSelectListener() {
                @Override
                public void onTimeSelect(Date date, View v) {//选中事件回调
                    orderSetting_rentTime_begin.setText(DateRelatedUtils.FormatOutputByDate(date, DateRelatedUtils.TYPE_CHINESE));
                }
            }).setType(new boolean[]{true, true, true, true, true, false})
                    .setLabel("年","月","日","时","分", "")
                    .build();
            orderSetting_rentTime_begin.setEnabled(true);
            orderSetting_rentTime_begin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    orderSetting_timeBegin.show();
                }
            });

            //使能orderSetting_rentTime_end并设置监听器
            orderSetting_timeEnd = new TimePickerBuilder(this, new OnTimeSelectListener() {
                @Override
                public void onTimeSelect(Date date, View v) {//选中事件回调
                    orderSetting_rentTime_end.setText(DateRelatedUtils.FormatOutputByDate(date, DateRelatedUtils.TYPE_CHINESE));
                }
            }).setType(new boolean[]{true, true, true, true, true, false})
                    .setLabel("年","月","日","时","分", "")
                    .build();
            orderSetting_rentTime_end.setEnabled(true);
            orderSetting_rentTime_end.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    orderSetting_timeEnd.show();
                }
            });
            //使能EditText控件orderSetting_price
            orderSetting_price.setEnabled(true);

        }else if(status == 2){
            //使下面四个控件失效
            orderSetting_device_spinner.setEnabled(false);
            orderSetting_rentTime_begin.setEnabled(false);
            orderSetting_rentTime_end.setEnabled(false);
            orderSetting_price.setEnabled(false);
        }else{
            return;
        }
    }

    private void initToolbar() {

    }

    public void backToLastActivity(int resultCode){
        setResult(resultCode);
        finish();
    }


    private void initViewModel() {
        orderSettingViewModel.setMainCustomer(LitePalUtils.getSingleCustomer(getIntent().getStringExtra("username")));
        int ambition = getIntent().getIntExtra("ambition", 0);
        orderSettingViewModel.setAmbition(ambition);
        int modifyorCheck = getIntent().getIntExtra("modifyorCheck", 0);
        orderSettingViewModel.setModifyorCheck(modifyorCheck);
        initUIObject();
        initInstruments();
        switch (ambition){
            //1表示来自创建订单,尚未规划UI显示的布局，可以考虑在showOrderContent（）中复写!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
            case 1:
                ProvideOrder new_order = new ProvideOrder();
                AppCustomer mainCustomer = orderSettingViewModel.getMainCustomer();
                new_order.setSaler_name(mainCustomer.getUser_nickName());
                new_order.setSaler_id(mainCustomer.getUser_id());
                new_order.setOrder_confirmed(0);
                orderSettingViewModel.setOrder(new_order);
                orderSettingViewModel.getInstrumentsFromServer(new_order.getSaler_id(), this);
                break;
                //2表示修改、查看、审核订单
            case 2:
                //获取传递的order_i，从服务器获取ProvideOrder
                int order_id = getIntent().getIntExtra("order_id", 0);
                if(order_id != 0){
                    orderSettingViewModel.getAmbitionOrder(order_id, this);
                }else {
                    AlertDialog.Builder dialog_wrongId = new AlertDialog.Builder(this);
                    dialog_wrongId.setTitle(R.string.orderSetting_error_wrongOrderId_title);
                    dialog_wrongId.setMessage(R.string.orderSetting_error_wrongOrderId_message);
                    dialog_wrongId.setCancelable(false);
                    dialog_wrongId.setPositiveButton(R.string.orderSetting_error_wrongOrderId_button, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            setResult(5);
                            finish();
                        }
                    });
                    dialog_wrongId.show();
                }
            default:break;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
 //       AppCustomer mainCustomer = LitePalUtils.getSingleCustomer(orderSettingViewModel.getMainCustomer().getUser_nickName());
  //      orderSettingViewModel.getMainCustomer().adjustSelf(mainCustomer);
    }



    public OrderSettingViewModel getOrderSettingViewModel() {
        return orderSettingViewModel;
    }

    public void setOrderSettingViewModel(OrderSettingViewModel orderSettingViewModel) {
        this.orderSettingViewModel = orderSettingViewModel;
    }



}