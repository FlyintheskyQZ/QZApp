package seu.qz.qzapp.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.DialogInterface;
import android.content.Intent;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.Overlay;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeOption;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiCitySearchOption;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiDetailSearchResult;
import com.baidu.mapapi.search.poi.PoiIndoorResult;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import seu.qz.qzapp.R;
import seu.qz.qzapp.entity.BriefOrderItem;

public class DeviceLocationDisplayActivity extends AppCompatActivity {


    //UI
    Toolbar locationdisplay_toolbar;
    MapView locationdisplay_mapview;
    ConstraintLayout locationdisplay_searchpart;
    TextView locationdisplay_search_text;
    EditText locationdisplay_search_input;
    TextView locationdisplay_error;
    Button locationdisplay_search_btn;
    ProgressBar locationdisplay_loading;
    FloatingActionButton locationdisplay_floatbtn;

    //BaiduMap
    LocationClient mLocationClient;
    BaiduMap map;
    BDLocation bdlocation;
    //地图上点击生成的mark标记
    Overlay mark;

    //订单信息
    BriefOrderItem item;

    //用于开始定位和恢复原位时地图显示为以当前定位为中心的模式
    boolean isFirstLocated = false;

    //用于判断当前进入地图的动作是查看位置还是注册仪器时的获取位置（true表示注册仪器）
    boolean isRegisterdDevice = false;

    private static final String TAG = "DeviceLocationDisplayAc";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_device_location_display);
        setRegisterdDevice(getIntent().getBooleanExtra("registerdevice", false));
        if(!isRegisterdDevice){
            item = (BriefOrderItem) getIntent().getSerializableExtra("item");
        }
        initUI();
        initToolbar();
        initMapView();
        registerListener();
        if(!isRegisterdDevice){
            displayDeviceLocation();
            locationdisplay_searchpart.setVisibility(View.GONE);
            locationdisplay_floatbtn.hide();
        }
        startLocation();
    }



    private void registerListener() {
        locationdisplay_search_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                locationdisplay_error.setVisibility(View.GONE);
                String address = locationdisplay_search_input.getText().toString();
                if(address.isEmpty()){
                    locationdisplay_error.setVisibility(View.VISIBLE);
                    locationdisplay_error.setText("未输入地址！");
                    return;
                }
                GeoCoder geoCoder = GeoCoder.newInstance();
                geoCoder.setOnGetGeoCodeResultListener(new OnGetGeoCoderResultListener() {
                    @Override
                    public void onGetGeoCodeResult(GeoCodeResult geoCodeResult) {
                        if(geoCodeResult == null || geoCodeResult.error!= SearchResult.ERRORNO.NO_ERROR){
                            locationdisplay_error.setVisibility(View.VISIBLE);
                            locationdisplay_error.setText("输入地址有误！");
                            return;
                        }
                        MapStatus mapStatus = new MapStatus.Builder().target(geoCodeResult.getLocation()).zoom(18.0f).build();
                        map.setMapStatus(MapStatusUpdateFactory.newMapStatus(mapStatus));
                    }

                    @Override
                    public void onGetReverseGeoCodeResult(ReverseGeoCodeResult reverseGeoCodeResult) {

                    }
                });
                geoCoder.geocode(new GeoCodeOption().city(bdlocation.getCity()).address(address));
            }
        });
        if(isRegisterdDevice){
            BaiduMap.OnMapClickListener clickListener = new BaiduMap.OnMapClickListener() {
                @Override
                public void onMapClick(final LatLng latLng) {
                    if(mark != null){
                        mark.remove();
                    }
                    //构建Marker图标
                    BitmapDescriptor bitmap = BitmapDescriptorFactory
                            .fromResource(R.drawable.icon_marka);
                    //构建MarkerOption，用于在地图上添加Marker
                    OverlayOptions option = new MarkerOptions()
                            .position(latLng)
                            .anchor(0.5f, 0.5f)
                            .icon(bitmap);
                    //在地图上添加Marker，并显示
                    mark = map.addOverlay(option);
                    AlertDialog.Builder dialog = new AlertDialog.Builder(DeviceLocationDisplayActivity.this);
                    dialog.setTitle(R.string.devicelocationdisplay_mark_title);
                    dialog.setMessage(R.string.devicelocationdisplay_mark_message);
                    dialog.setPositiveButton(R.string.devicelocationdisplay_mark_positive, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent();
                            String location = String.valueOf(latLng.longitude) + ":" + String.valueOf(latLng.latitude);
                            intent.putExtra("location", location);
                            setResult(1, intent);
                            finish();
                        }
                    });
                    dialog.setNegativeButton(R.string.devicelocationdisplay_mark_negative, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
                    dialog.setCancelable(false);
                    dialog.show();
                }

                @Override
                public void onMapPoiClick(MapPoi mapPoi) {

                }
            };
            map.setOnMapClickListener(clickListener);
        }

        locationdisplay_floatbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isFirstLocated = false;
            }
        });
    }

    private void displayDeviceLocation() {
        String[] location = item.getLocation().split(":");
        double longitude = Double.parseDouble(location[0]);
        double latitude = Double.parseDouble(location[1]);
        LatLng point = new LatLng(latitude, longitude);
        //构建Marker图标
        BitmapDescriptor bitmap = BitmapDescriptorFactory
                .fromResource(R.drawable.icon_marka);
        //构建MarkerOption，用于在地图上添加Marker
        OverlayOptions option = new MarkerOptions()
                .position(point)
                .icon(bitmap);
        //在地图上添加Marker，并显示
        map.addOverlay(option);
        MapStatusUpdate status_self = MapStatusUpdateFactory.newLatLng(point);
        map.setMapStatus(status_self);
    }

    private void startLocation() {
        //定位初始化
        mLocationClient = new LocationClient(this);

        //通过LocationClientOption设置LocationClient相关参数
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true); // 打开gps
        option.setCoorType("bd09ll"); // 设置坐标类型
        option.setScanSpan(1000);//设置扫描周期
        option.setIsNeedAddress(true);//设置是否获取地址名称，设置后getCity等方法才有作用

        //设置locationClientOption
        mLocationClient.setLocOption(option);

        //注册LocationListener监听器
        MyLocationListener myLocationListener = new MyLocationListener();
        mLocationClient.registerLocationListener(myLocationListener);
        //开启地图定位图层
        mLocationClient.start();
        Log.d(TAG, "startLocation: ");
    }

    private void initMapView() {
        MapStatus.Builder builder = new MapStatus.Builder();
        builder.zoom(21.0f);
        map = locationdisplay_mapview.getMap();
        map.setMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
        map.setMyLocationEnabled(true);
    }

    private void initToolbar() {
        setSupportActionBar(locationdisplay_toolbar);
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
        locationdisplay_toolbar = findViewById(R.id.locationdisplay_toolbar);
        locationdisplay_mapview = findViewById(R.id.locationdisplay_mapview);
        locationdisplay_searchpart = findViewById(R.id.locationdisplay_searchpart);
        locationdisplay_loading = findViewById(R.id.locationdisplay_loading);
        locationdisplay_search_text = findViewById(R.id.locationdisplay_search_text);
        locationdisplay_search_input = findViewById(R.id.locationdisplay_search_input);
        locationdisplay_search_btn = findViewById(R.id.locationdisplay_search_btn);
        locationdisplay_error = findViewById(R.id.locationdisplay_error);
        locationdisplay_floatbtn = findViewById(R.id.locationdisplay_floatbtn);
    }

    @Override
    protected void onResume() {
        super.onResume();
        locationdisplay_mapview.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        locationdisplay_mapview.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLocationClient.stop();
        map.setMyLocationEnabled(false);
        locationdisplay_mapview.onDestroy();
        locationdisplay_mapview = null;
    }

    public class MyLocationListener extends BDAbstractLocationListener {
        @Override
        public void onReceiveLocation(final BDLocation location) {
            //mapView 销毁后不在处理新接收的位置
            if (location == null || locationdisplay_mapview == null){
                return;
            }
            if(bdlocation != null){
                if(!bdlocation.getCity().equals(location.getCity())){
                    locationdisplay_search_text.setText("在" + location.getCity() + "查找");
                }
            }else {
                locationdisplay_search_text.setText("在" + location.getCity() + "查找");
            }
            bdlocation = location;
            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(location.getRadius())
                    // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(location.getDirection()).latitude(location.getLatitude())
                    .longitude(location.getLongitude()).build();
            map.setMyLocationData(locData);
            if(isRegisterdDevice){
                if(!isFirstLocated){
                    LatLng self_location = new LatLng(location.getLatitude(), location.getLongitude());
                    MapStatusUpdate status_self = MapStatusUpdateFactory.newLatLng(self_location);
                    map.setMapStatus(status_self);
                    isFirstLocated = true;
                }
            }
            Log.d(TAG, "经度+纬度：" + location.getLongitude() + ";" + location.getLatitude());
        }
    }

    public boolean isFirstLocated() {
        return isFirstLocated;
    }

    public void setFirstLocated(boolean firstLocated) {
        isFirstLocated = firstLocated;
    }

    public boolean isRegisterdDevice() {
        return isRegisterdDevice;
    }

    public void setRegisterdDevice(boolean registerdDevice) {
        isRegisterdDevice = registerdDevice;
    }
}