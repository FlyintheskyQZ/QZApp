package seu.qz.qzapp.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import seu.qz.qzapp.R;
import seu.qz.qzapp.activity.MainActivity;
import seu.qz.qzapp.activity.OrderContentActivity;
import seu.qz.qzapp.entity.AppCustomer;
import seu.qz.qzapp.entity.BriefOrderItem;
import seu.qz.qzapp.fragment.viewmodel.BaseViewModel;
import seu.qz.qzapp.fragment.viewmodel.OrderViewModel;
import seu.qz.qzapp.main.CustomizeRecyclerScrollListener;
import seu.qz.qzapp.activity.viewmodel.MainViewModel;
import seu.qz.qzapp.main.OrderAdapter;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link OrderFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class OrderFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
//    private static final String ARG_PARAM1 = "param1";
//    private static final String ARG_PARAM2 = "param2";

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment OrderFragment.
     */
    // TODO: Rename and change types and number of parameters
//    public static OrderFragment newInstance(String param1, String param2) {
//        OrderFragment fragment = new OrderFragment();
//        Bundle args = new Bundle();
//        args.putString(ARG_PARAM1, param1);
//        args.putString(ARG_PARAM2, param2);
//        fragment.setArguments(args);
//        return fragment;
//    }



    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public OrderFragment() {
        // Required empty public constructor
    }

    public static OrderFragment newInstance() {
        return new OrderFragment();
    }

    MainViewModel mainViewModel;
    private OrderViewModel orderViewModel;
    private OrderAdapter adapter;
    SwipeRefreshLayout swipeRefreshLayout;
    RecyclerView recyclerView;


    public OrderViewModel getViewModel(){
        return orderViewModel;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainViewModel = ViewModelProviders.of(getActivity()).get(MainViewModel.class);
        orderViewModel = ViewModelProviders.of(this).get(OrderViewModel.class);
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_order, container, false);
        swipeRefreshLayout = view.findViewById(R.id.order_swipe_fresh);
        recyclerView = view.findViewById(R.id.order_recycle);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        adapter = new OrderAdapter(orderViewModel.getBriefOrderItems(), new OrderAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, BriefOrderItem item) {
                MainActivity activity = (MainActivity) getActivity();
                Intent intent = new Intent(activity, OrderContentActivity.class);
                intent.putExtra("username", mainViewModel.getMainCustomer().getUser_nickName());
                intent.putExtra("briefOrderItem", item);
                startActivityForResult(intent, 1);
                //开启活动内容界面，考虑到可能会有返回值
                //根据返回值做出相应操作！！！！！！！！！！！！！！！！！！！！！！！
            }
        });
        orderViewModel.initBriefItems(mainViewModel.getMainCustomer().getUser_nickName(), adapter, getActivity(), swipeRefreshLayout);
        recyclerView.setAdapter(adapter);
        recyclerView.addOnScrollListener(new CustomizeRecyclerScrollListener() {
            @Override
            protected void onLoadData() {
                if(orderViewModel.getRequestType() !=  orderViewModel.TYPE_BOTH_OVER){
                    //采用异步处理方式，耗时不能确定，故可加载状态下（即非TYPE_BOTH_OVER)loadingState的状态设置在异步方法里完成
                    orderViewModel.getBriefItems(mainViewModel.getMainCustomer().getUser_nickName(), adapter, getActivity(), swipeRefreshLayout);
                }else{
                    adapter.setLoadingState(3);
                }
            }
        });
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //设置adapter的loadingState为0是为了避免下拉刷新的过程中显示adapter上拉刷新的加载提示项（原因是当recycler下拉刷新时会清空子项的缓存集合，导致adapter安排显示时会把加载提示项当成唯一存在的项）
                adapter.setLoadingState(0);
                orderViewModel.refresh(mainViewModel.getMainCustomer().getUser_nickName(), adapter, getActivity(), swipeRefreshLayout);
            }
        });
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
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
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        adapter = null;
        Log.i("MainActivity", "order_onDestroyView: 启动！！！！！！！！！！！！！！！！！！！！！！！！！！！");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("MainActivity", "order_onDestroy: 启动!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
    }

    public OrderViewModel getOrderViewModel() {
        return orderViewModel;
    }

    public void setOrderViewModel(OrderViewModel orderViewModel) {
        this.orderViewModel = orderViewModel;
    }

    public OrderAdapter getAdapter() {
        return adapter;
    }

    public void setAdapter(OrderAdapter adapter) {
        this.adapter = adapter;
    }

    public SwipeRefreshLayout getSwipeRefreshLayout() {
        return swipeRefreshLayout;
    }

    public void setSwipeRefreshLayout(SwipeRefreshLayout swipeRefreshLayout) {
        this.swipeRefreshLayout = swipeRefreshLayout;
    }
}