package seu.qz.qzapp.fragment;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProviders;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;


import com.bigkoo.pickerview.builder.TimePickerBuilder;
import com.bigkoo.pickerview.listener.OnTimeSelectListener;
import com.bigkoo.pickerview.view.TimePickerView;
import com.google.android.material.tabs.TabItem;
import com.google.android.material.tabs.TabLayout;

import org.litepal.LitePal;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import seu.qz.qzapp.R;
import seu.qz.qzapp.activity.MainActivity;
import seu.qz.qzapp.activity.OrderContentActivity;
import seu.qz.qzapp.activity.OrderSettingActivity;
import seu.qz.qzapp.activity.TimeSettingActivity;
import seu.qz.qzapp.database.LitePalUtils;
import seu.qz.qzapp.entity.AppCustomer;
import seu.qz.qzapp.entity.BriefOrderItem;
import seu.qz.qzapp.entity.SearchHistoryItem;
import seu.qz.qzapp.fragment.viewmodel.SearchViewModel;
import seu.qz.qzapp.activity.viewmodel.MainViewModel;
import seu.qz.qzapp.main.SearchAdapter;
import seu.qz.qzapp.main.SearchViewAdapter;
import seu.qz.qzapp.utils.DateRelatedUtils;
import seu.qz.qzapp.utils.ListOperationUtils;

public class SearchFragment extends Fragment {

    public static SearchFragment newInstance() {
        return new SearchFragment();
    }

    //viewModel保存数据
    MainViewModel mainViewModel;
    SearchViewModel searchViewModel;


    //ui界面控件
    View searchFragment_top_background;

    SearchView searchFragment_searchView;
    AutoCompleteTextView autoCompleteTextView;
    ImageView deleteButton;
    AppCompatImageView searchFragment_searchButton;
    ImageButton searchFragment_addOrder;

    TabLayout searchFragment_selector;
    TabLayout.Tab searchFragment_tab_all;
    TabLayout.Tab searchFragment_tab_time;
    TabLayout.Tab searchFragment_tab_price;
    TabLayout.Tab searchFragment_tab_location;
    //用于记录tablayOut的选中项
    int item_selected = 0;

    SwipeRefreshLayout searchFragment_freshLayout;

    ScrollView searchFragment_scrollview;

    RecyclerView searchFragment_recyclerview;
    SearchAdapter adapter;

    //GPS定位相关
    LocationManager locationManager;
    LocationListener locationListener;
    Criteria criteria;
    String locationProvider;
    Location currentLocation;
    //判断GPS是否初始化完成
    boolean gpsInit= false;


    public SearchFragment() {
    }

    public SearchViewModel getViewModel() {
        return searchViewModel;
    }

    private static final String TAG = "SearchFragment";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainViewModel = ViewModelProviders.of(getActivity()).get(MainViewModel.class);
        searchViewModel = ViewModelProviders.of(this).get(SearchViewModel.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);
        initUI(view);
        initSearchView();
        initListener();
        startGps();
        initTabLayout();
        initRecyclerView();
        initSwipeFreshLayOut();
        return view;
    }

    private void initListener() {
        if(mainViewModel.getMainCustomer().getAuthority_level() == 2){
            searchFragment_addOrder.setVisibility(View.VISIBLE);
            searchFragment_addOrder.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AppCustomer mainCustomer = mainViewModel.getMainCustomer();
                    if(TextUtils.isEmpty(mainCustomer.getRelated_device_id())){
                        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
                        dialog.setTitle("无权限操作！");
                        dialog.setMessage("您当前无可用仪器，请先注册！");
                        dialog.setPositiveButton("好的", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                    }
                    Intent intent = new Intent(getActivity(), OrderSettingActivity.class);
                    intent.putExtra("username", mainCustomer.getUser_nickName());
                    //ambition:1,创建订单;2,修改、查看、审核订单。
                    intent.putExtra("ambition", 1);
                    startActivity(intent);
                }
            });
        }

    }

    private void initSwipeFreshLayOut() {
        searchFragment_freshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //设置adapter的loadingState为0是为了避免下拉刷新的过程中显示adapter上拉刷新的加载提示项（原因是当recycler下拉刷新时会清空子项的缓存集合，导致adapter安排显示时会把加载提示项当成唯一存在的项）
                adapter.setLoadingState(0);
                searchViewModel.loadProvideOrders(adapter, 3, 0, searchFragment_freshLayout, getActivity(), null);
            }
        });
    }

    private void startGps() {
        //申请位置权限
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 2);
        }else {
            initGPS();
        }
    }

    public void initGPS() {
        criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);//设置精度
        criteria.setAltitudeRequired(false);//设置不提供海拔信息
        criteria.setBearingRequired(false);//设置不提供方向信息
        criteria.setCostAllowed(true);
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!gpsEnabled) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
            dialog.setTitle(R.string.search_locationError_title);
            dialog.setMessage(R.string.search_locationError_message);
            dialog.setPositiveButton(R.string.search_locationError_positive, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(Settings.ACTION_SETTINGS);
                    getActivity().startActivity(intent);
                }
            });
            dialog.setNegativeButton(R.string.search_locationError_negative, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
        }else {
            registerGPSListener();
        }
    }

    public void registerGPSListener(){
        locationProvider = locationManager.getBestProvider(criteria, true);
        currentLocation = new Location(locationProvider);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                currentLocation.setLongitude(location.getLongitude());
                currentLocation.setLatitude(location.getLatitude());
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(locationProvider, 1000, 0, locationListener);
            gpsInit = true;
        }
    }


    private void initTabLayout() {
        //清空TabLayout的选择情况
        refreshTabItem();
        searchFragment_selector.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                List<BriefOrderItem> original_items = adapter.getOriginal_items();
                if (original_items == null || original_items.isEmpty()) {
                    //异步获取待订购的订单
                    searchViewModel.loadProvideOrders(adapter, 2, position, searchFragment_freshLayout, getActivity(), new SearchViewModel.FilterOrderListener() {
                        @Override
                        public void filterOrder(int position) {
                            filterOrdersByTabLayout(position);
                        }
                    });
                    return;
                }
                filterOrdersByTabLayout(position);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    public void filterOrdersByTabLayout(int position) {
        final List<BriefOrderItem> search_items = adapter.getSearch_list();
        switch (position) {
            case 0:
                //全部显示
                adapter.setOrderList(new ArrayList<BriefOrderItem>(adapter.getOriginal_items()));
                adapter.notifyDataSetChanged();
                break;
            //按照时间顺序来显示（可规划时间区间）
            case 1:
                AlertDialog.Builder builder_time = new AlertDialog.Builder(getActivity());
                LayoutInflater inflater_time = LayoutInflater.from(getActivity());
                View v_time = inflater_time.inflate(R.layout.item_search_filter_time, null);
                Intent intent = new Intent(getActivity(), TimeSettingActivity.class);
                getActivity().startActivityForResult(intent, 2);
                break;
            //按照价格顺序来规划
            case 2:
                AlertDialog.Builder builder_price = new AlertDialog.Builder(getActivity());
                LayoutInflater inflater_price = LayoutInflater.from(getActivity());
                View v_price = inflater_price.inflate(R.layout.item_search_filter_price, null);
                final Dialog dialog_price = builder_price.create();
                final EditText searchFragment_dialog_price_bottom = v_price.findViewById(R.id.searchfragment_dialog_price_bottom);
                final EditText searchFragment_dialog_price_top = v_price.findViewById(R.id.searchfragment_dialog_price_top);
                Button searchFragment_dialog_price_order = v_price.findViewById(R.id.searchfragment_dialog_price_order);
                Button searchFragment_dialog_price_reverSeorder = v_price.findViewById(R.id.searchfragment_dialog_price_reverseorder);
                searchFragment_dialog_price_order.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int price_bottom = Integer.parseInt(searchFragment_dialog_price_bottom.getText().toString());
                        int price_top = Integer.parseInt(searchFragment_dialog_price_top.getText().toString());
                        Log.d(TAG, "price interval:" + price_bottom + "-" + price_top);
                        List<BriefOrderItem> new_list = ListOperationUtils.filterItemsInPriceInterval(search_items, price_bottom, price_top);
                        if (new_list.size() > 1) {
                            ListOperationUtils.adjustBriefOrderForwardByPrice(new_list);
                        }
                        adapter.setOrderList(new_list);
                        adapter.setLoadingState(3);
                        item_selected = 2;
                        dialog_price.cancel();
                    }
                });
                searchFragment_dialog_price_reverSeorder.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int price_bottom = Integer.parseInt(searchFragment_dialog_price_bottom.getText().toString());
                        int price_top = Integer.parseInt(searchFragment_dialog_price_top.getText().toString());
                        List<BriefOrderItem> new_list = ListOperationUtils.filterItemsInPriceInterval(search_items, price_bottom, price_top);
                        if (new_list.size() > 1) {
                            ListOperationUtils.adjustBriefOrderBackwardByPrice(new_list);
                        }
                        adapter.setOrderList(new_list);
                        adapter.setLoadingState(3);
                        item_selected = 2;
                        dialog_price.cancel();
                    }
                });
                //builer.setView(v);//这里如果使用builer.setView(v)，自定义布局只会覆盖title和button之间的那部分


                dialog_price.show();
                dialog_price.getWindow().setContentView(v_price);//自定义布局应该在这里添加，要在dialog.show()的后面
                dialog_price.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                        WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
                dialog_price.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
                break;
                //按照位置远近进行排序
            case 3:
                boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                if (!gpsEnabled) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
                    dialog.setTitle(R.string.search_locationError_title);
                    dialog.setMessage(R.string.search_locationError_message);
                    dialog.setPositiveButton(R.string.search_locationError_positive, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(Settings.ACTION_SETTINGS);
                            getActivity().startActivity(intent);
                        }
                    });
                    dialog.setNegativeButton(R.string.search_locationError_negative, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
                    //恢复原来的tab选中项
                    switch (item_selected) {
                        case 0:
                            searchFragment_tab_all.select();
                            break;
                        case 1:
                            searchFragment_tab_time.select();
                            break;
                        case 2:
                            searchFragment_tab_price.select();
                            break;
                        default:
                            break;
                    }
                    return;
                } else {
                    //只进行一次注册
                    if(!gpsInit){
                        registerGPSListener();
                    }
                    AlertDialog.Builder builder_location = new AlertDialog.Builder(getActivity());
                    LayoutInflater inflater_location = LayoutInflater.from(getActivity());
                    View v_location = inflater_location.inflate(R.layout.item_search_filter_location, null);
                    final Dialog dialog_location = builder_location.create();
                    Button searchFragment_dialog_location_order = v_location.findViewById(R.id.searchfragment_dialog_location_order);
                    Button searchFragment_dialog_location_reverseOrder = v_location.findViewById(R.id.searchfragment_dialog_location_reverseorder);
                    searchFragment_dialog_location_order.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            double longitude = currentLocation.getLongitude();
                            double latitude = currentLocation.getLatitude();
                            Log.d(TAG, "location:" + longitude + "-" + latitude);
                            List<BriefOrderItem> new_list = new ArrayList<>(search_items);
                            ListOperationUtils.filterBriefOrderForwardByLocation(new_list, longitude, latitude);
                            adapter.setOrderList(new_list);
                            adapter.setLoadingState(3);
                            item_selected = 3;
                            dialog_location.cancel();
                        }
                    });
                    searchFragment_dialog_location_reverseOrder.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            double longitude = currentLocation.getLongitude();
                            double latitude = currentLocation.getLatitude();
                            Log.d(TAG, "location:" + longitude + "-" + latitude);
                            List<BriefOrderItem> new_list = new ArrayList<>(search_items);
                            ListOperationUtils.filterBriefOrderBackwardByLocation(new_list, longitude, latitude);
                            adapter.setOrderList(new_list);
                            adapter.setLoadingState(3);
                            item_selected = 3;
                            dialog_location.cancel();
                        }
                    });

                    dialog_location.show();
                    dialog_location.getWindow().setContentView(v_location);//自定义布局应该在这里添加，要在dialog.show()的后面
                }
                break;
            default:
                break;
        }
    }

    public void showItemsByTimeFilter(List<BriefOrderItem> search_items, Date date_begin, Date date_end, int filterWay) {
        List<BriefOrderItem> new_list = ListOperationUtils.filterItemsInTimeInterval(search_items, date_begin, date_end);

        if (new_list.size() > 1) {
            if(filterWay == 1){
                ListOperationUtils.adjustBriefOrderForwardByDate(new_list);
            }else if(filterWay == 2){
                ListOperationUtils.adjustBriefOrderBackwardByDate(new_list);
            }
        }
        adapter.setOrderList(new_list);
        adapter.notifyDataSetChanged();
        item_selected = 1;
    }


    //每次searchview查询后都重置TabLayout的选择情况
    void refreshTabItem(){
        searchFragment_tab_all.select();
        item_selected = 0;
    }

    private void initRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        searchFragment_recyclerview.setLayoutManager(layoutManager);
        adapter = new SearchAdapter(new ArrayList<BriefOrderItem>(), new SearchAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, BriefOrderItem item) {
                //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!需要填写点击触发事件
                MainActivity activity = (MainActivity) getActivity();
                Intent intent = new Intent(activity, OrderContentActivity.class);
                intent.putExtra("username", mainViewModel.getMainCustomer().getUser_nickName());
                intent.putExtra("briefOrderItem", item);
                startActivityForResult(intent, 1);
            }
        });
        adapter.setLoadingState(3);
        searchFragment_recyclerview.setAdapter(adapter);
        searchViewModel.loadProvideOrders(adapter, 1, 0, searchFragment_freshLayout, getActivity(), null);
    }

    private void initSearchView() {
        searchFragment_searchView.setIconifiedByDefault(false);//搜索图标是否显示在搜索框内
        searchFragment_searchView.setImeOptions(2);//设置输入法中完成输入后enter键的显示文本，如回车、前往、搜索等。
        searchFragment_searchView.setInputType(1);//设置输入类型
//        mSearchView.setMaxWidth(200);//设置最大宽度
        searchFragment_searchView.setQueryHint("请输入查询内容");//设置查询提示字符串
        searchFragment_searchView.setSubmitButtonEnabled(true);//设置是否显示搜索框展开时的提交按钮
        //设置SearchView下划线透明
        setUnderLinetransparent(searchFragment_searchView);
        //设置输入几个字符时出现提示
        initAutoCompleteText();

    }
    //autoCompleteText是搜索记录的核心控件
    private void initAutoCompleteText() {
        final List<SearchHistoryItem> historyItems = LitePal.where("user_id = ?",
                String.valueOf(mainViewModel.getMainCustomer().getUser_id())).find(SearchHistoryItem.class);
        autoCompleteTextView.setThreshold(0);
        autoCompleteTextView.setTextColor(ContextCompat.getColor(getActivity(), R.color.searchFragment_searchText));
        final SearchViewAdapter search_adapter = new SearchViewAdapter(getActivity(), R.layout.item_search_searchview, R.id.searchfragment_searchView, historyItems);
        autoCompleteTextView.setAdapter(search_adapter);
        registerSearchViewListener(search_adapter);
    }

    private void registerSearchViewListener(final SearchViewAdapter search_adapter) {
        // 设置搜索文本监听
        searchFragment_searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            //当点击搜索按钮时触发该方法，将搜索内容存入相关缓存列表中，并注意唯一性
            @Override
            public boolean onQueryTextSubmit(String query) {
                Toast.makeText(getContext(), "输入为：" + query, Toast.LENGTH_SHORT).show();
                SearchHistoryItem new_item = new SearchHistoryItem(String.valueOf(mainViewModel.getMainCustomer().getUser_id()), query, new Date(System.currentTimeMillis()));
                LitePalUtils.saveSingleSearchHistoryItem(new_item);
                SearchHistoryItem.restoreSingleItem(search_adapter.getOriginal_items(), new_item);
                search_adapter.notifyDataSetChanged();
                refreshTabItem();
                List<BriefOrderItem> new_searchList = ListOperationUtils.filterBriefOrderBySearch(adapter.getOriginal_items(), query);
                adapter.setSearch_list(new_searchList);
                adapter.setOrderList(new ArrayList<BriefOrderItem>(new_searchList));
                adapter.notifyDataSetChanged();
                return false;
            }

            //当搜索内容改变时触发该方法，会触发Adapter的filter的方法
            @Override
            public boolean onQueryTextChange(String newText) {
                Toast.makeText(getContext(), "输入改变为：" + newText, Toast.LENGTH_SHORT).show();
                return false;
            }
        });
        searchFragment_searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                return false;
            }
        });

    }

    /**设置SearchView下划线透明**/
    private void setUnderLinetransparent(SearchView searchView){
        try {
            Class<?> argClass = searchView.getClass();
            // mSearchPlate是SearchView父布局的名字
            Field ownField = argClass.getDeclaredField("mSearchPlate");
            ownField.setAccessible(true);
            View mView = (View) ownField.get(searchView);
            mView.setBackgroundColor(Color.TRANSPARENT);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private void initUI(View view) {
        searchFragment_top_background = view.findViewById(R.id.searchfragment_top_background);
        searchFragment_searchView = view.findViewById(R.id.searchfragment_searchView);
        autoCompleteTextView = searchFragment_searchView.findViewById(R.id.search_src_text);
        deleteButton = searchFragment_searchView.findViewById(R.id.search_close_btn);
        searchFragment_selector = view.findViewById(R.id.searchfragment_selector);
        searchFragment_scrollview = view.findViewById(R.id.searchfragment_scrollview);
        searchFragment_recyclerview = view.findViewById(R.id.searchfragment_recyclerview);
        searchFragment_searchButton = view.findViewById(R.id.search_button);
        searchFragment_freshLayout = view.findViewById(R.id.searchfragment_freshlayout);
        searchFragment_addOrder = view.findViewById(R.id.searchfragment_addOrder);
//        searchFragment_tab_time = view.findViewById(R.id.searchfragment_tab_time);
//        searchFragment_tab_price = view.findViewById(R.id.searchfragment_tab_price);
//        searchFragment_tab_location = view.findViewById(R.id.searchfragment_tab_location);
        searchFragment_tab_all = searchFragment_selector.newTab().setIcon(R.mipmap.ic_searchfragment_order_all).setText("全部");
        searchFragment_selector.addTab(searchFragment_tab_all);
        searchFragment_tab_time = searchFragment_selector.newTab().setIcon(R.mipmap.ic_searchfragment_order_time).setText("时间排序");
        searchFragment_selector.addTab(searchFragment_tab_time);
        searchFragment_tab_price = searchFragment_selector.newTab().setIcon(R.mipmap.ic_searchfragment_order_price).setText("价格排序");
        searchFragment_selector.addTab(searchFragment_tab_price);
        searchFragment_tab_location = searchFragment_selector.newTab().setIcon(R.mipmap.ic_searchfragment_order_location).setText("距离排序");
        searchFragment_selector.addTab(searchFragment_tab_location);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // TODO: Use the ViewModel
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.i("MainActivity", "search_onDestroyView: 启动！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("MainActivity", "search_onDestroy: 启动！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！");
    }

    public SearchAdapter getAdapter() {
        return adapter;
    }

    public void setAdapter(SearchAdapter adapter) {
        this.adapter = adapter;
    }
}