package seu.qz.qzapp.fragment.viewmodel;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.ArrayList;
import java.util.List;

import seu.qz.qzapp.R;
import seu.qz.qzapp.activity.MainActivity;
import seu.qz.qzapp.activity.operation.SearchFragmentOperation;
import seu.qz.qzapp.entity.BriefOrderItem;
import seu.qz.qzapp.fragment.SearchFragment;
import seu.qz.qzapp.main.SearchAdapter;

public class SearchViewModel extends BaseViewModel {

    private SearchFragmentOperation operation = new SearchFragmentOperation();

    //加载待订购的订单，参数分别为：
    //1.RecyclerView的Adapter
    //2.加载状态：1表示初始化时加载，2表示点击TabLayout的子项时加载（伴随有过滤器的动作发生），3表示swipefreshLayout的刷新动作
    public void loadProvideOrders(final SearchAdapter adapter, final int status, final int position, final SwipeRefreshLayout layout, final Activity activity, final FilterOrderListener listener) {
        layout.setRefreshing(true);
        Handler handler = new Handler(){
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                switch (msg.what){
                        //网络连接出错
                    case 0:
                        if(layout != null){
                            layout.setRefreshing(false);
                        }
                        AlertDialog.Builder dialog = new AlertDialog.Builder(activity);
                        dialog.setTitle(R.string.order_error);
                        dialog.setMessage(R.string.order_netError_content);
                        dialog.setCancelable(false);
                        dialog.setPositiveButton(R.string.order_netError_dialog_ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                adapter.setLoadingState(2);
                                Intent intent = new Intent(Settings.ACTION_SETTINGS);
                                activity.startActivity(intent);
                            }
                        });
                        dialog.setNegativeButton(R.string.order_netError_dialog_cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                adapter.setLoadingState(2);
                            }
                        });
                        dialog.show();
                        break;
                        //未加载到任何BriefOrderItem
                    case 1:
                        if(layout != null){
                            layout.setRefreshing(false);
                        }
                        if(status != 1){
                            AlertDialog.Builder dialog_empty = new AlertDialog.Builder(activity);
                            dialog_empty.setTitle(R.string.search_loadItemsError_empty_title);
                            dialog_empty.setMessage(R.string.search_loadItemsError_empty_message);
                            dialog_empty.setCancelable(false);
                            dialog_empty.setPositiveButton(R.string.order_netError_dialog_ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    adapter.setOrderList(new ArrayList<BriefOrderItem>());
                                    adapter.setSearch_list(new ArrayList<BriefOrderItem>());
                                    adapter.setOriginal_items(new ArrayList<BriefOrderItem>());
                                    adapter.notifyDataSetChanged();
                                }
                            });
                            dialog_empty.show();
                        }else {
                            adapter.setOrderList(new ArrayList<BriefOrderItem>());
                            adapter.setSearch_list(new ArrayList<BriefOrderItem>());
                            adapter.setOriginal_items(new ArrayList<BriefOrderItem>());
                            adapter.notifyDataSetChanged();
                        }
                        break;
                        //加载到有效orders
                    case 2:
                        if(layout != null){
                            layout.setRefreshing(false);
                        }
                        List<BriefOrderItem> new_list = (List<BriefOrderItem>) msg.obj;
                        adapter.setOrderList(new_list);
                        adapter.setSearch_list(new ArrayList<BriefOrderItem>(new_list));
                        adapter.setOriginal_items(new ArrayList<BriefOrderItem>(new_list));
                        if(status == 2){
                            if(listener != null){
                                listener.filterOrder(position);
                            }
                        }
                        adapter.notifyDataSetChanged();
                        break;
                    case 3:
                        //以通知的方法告知查询时出现错误
                        AlertDialog.Builder dialog_error = new AlertDialog.Builder(activity);
                        dialog_error.setTitle(R.string.order_error);
                        dialog_error.setMessage(R.string.order_unknownError);
                        dialog_error.setCancelable(false);
                        dialog_error.setPositiveButton(R.string.order_netError_dialog_ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                adapter.setLoadingState(2);
                            }
                        });
                        dialog_error.show();
                        break;
                    default:break;
                }
            }
        };
        operation.loadOriginalItems(handler, activity);
    }

    public interface FilterOrderListener{
        void filterOrder(int position);
    }
}