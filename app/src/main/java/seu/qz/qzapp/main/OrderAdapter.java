package seu.qz.qzapp.main;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import seu.qz.qzapp.R;
import seu.qz.qzapp.activity.MainActivity;
import seu.qz.qzapp.activity.OrderContentActivity;
import seu.qz.qzapp.entity.BriefOrderItem;

public class OrderAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private OnItemClickListener listener;
    private List<BriefOrderItem> orderList;

    private final int TYPE_NORMAL = 1;
    private final int TYPE_BOTTOM = 2;

    //加载状态：分别为正在加载中、单次加载完成、全部加载完成
    private int loadingState = 1;
    public final int STATE_LOADING = 1;
    public final int STATE_FINISHED = 2;
    public final int STATE_END = 3;

    //加载错误信息：用于记录每次加载时遇到的错误情况
    private int loadingErrorType = 0;

    static class NormalViewHolder extends RecyclerView.ViewHolder{

        ImageView item_order_isOk_icon;
        TextView item_order_isOk_text;
        TextView item_order_number;
        TextView item_order_instrument;
        TextView item_order_date;
        TextView item_order_expense;
        public NormalViewHolder(@NonNull View itemView) {
            super(itemView);
            item_order_isOk_icon = itemView.findViewById(R.id.item_order_isOk_icon);
            item_order_isOk_text = itemView.findViewById(R.id.item_order_isOk_text);
            item_order_number = itemView.findViewById(R.id.item_order_number);
            item_order_instrument = itemView.findViewById(R.id.item_order_instrument);
            item_order_date = itemView.findViewById(R.id.item_order_date);
            item_order_expense = itemView.findViewById(R.id.item_order_expense);
        }
    }

    static class BottomViewHolder extends RecyclerView.ViewHolder{

        ProgressBar item_order_bottom_process;
        TextView item_order_bottom_note;
        ConstraintLayout item_order_bottom_bottomNotice;


        public BottomViewHolder(@NonNull View itemView) {
            super(itemView);
            item_order_bottom_process = itemView.findViewById(R.id.item_order_bottom_process);
            item_order_bottom_note = itemView.findViewById(R.id.item_order_bottom_note);
            item_order_bottom_bottomNotice = itemView.findViewById(R.id.item_order_bottom_bottomNotice);
        }
    }



    public OrderAdapter(List<BriefOrderItem> orderList, OnItemClickListener listener) {
        this.orderList = orderList;
        this.listener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        if(position + 1 == getItemCount()){
            return TYPE_BOTTOM;
        }else {
            return TYPE_NORMAL;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, int viewType) {
        if(viewType == TYPE_NORMAL) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order, parent, false);
            return new NormalViewHolder(view);
        }else if(viewType == TYPE_BOTTOM){
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order_foot, parent, false);
            return new BottomViewHolder(view);
        }

        return null;
    }

    //position从0~size-1
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(holder instanceof NormalViewHolder){
            NormalViewHolder normalViewHolder = (NormalViewHolder)holder;
            final BriefOrderItem item = orderList.get(position);
            if(item.isOk()){
                normalViewHolder.item_order_isOk_icon.setImageResource(R.mipmap.ic_order_ok);
                normalViewHolder.item_order_isOk_text.setText(R.string.item_order_isFinished);
            }else {
                normalViewHolder.item_order_isOk_icon.setImageResource(R.mipmap.ic_order_wait);
                normalViewHolder.item_order_isOk_text.setText(R.string.item_order_isNotFinished);
            }
            normalViewHolder.item_order_instrument.setText(item.getInstrument());
            //订单的显示序号，由position决定，后期开发加上只显示已完成或未完成的订单时可以由orderNumber决定
//            normalViewHolder.item_order_number.setText(String.valueOf(item.getOrderNumber()));
            normalViewHolder.item_order_number.setText(String.valueOf(position + 1));
            normalViewHolder.item_order_date.setText(item.getDate());
            normalViewHolder.item_order_expense.setText(item.getCost());
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(listener != null){
                        listener.onItemClick(v, item);
                    }
                }
            });
        } else if(holder instanceof BottomViewHolder){
            BottomViewHolder bottomViewHolder = (BottomViewHolder)holder;
            switch (loadingState){
                //错误情况
                case 0:
                    //此处作错误判断，，也用于在recyclerView进行下拉刷新时避免显示“加载中..."的bottomItem（会与SwipeFresheLayout的下拉刷新重复）,并设置相应的内容显示！！！！！！！！！！！！！！！！！！！！
                    bottomViewHolder.item_order_bottom_bottomNotice.setVisibility(View.GONE);
                    bottomViewHolder.item_order_bottom_note.setVisibility(View.GONE);
                    bottomViewHolder.item_order_bottom_process.setVisibility(View.GONE);
                    break;
                    //正在加载
                case 1:
                    bottomViewHolder.item_order_bottom_bottomNotice.setVisibility(View.GONE);
                    bottomViewHolder.item_order_bottom_note.setVisibility(View.VISIBLE);
                    bottomViewHolder.item_order_bottom_process.setVisibility(View.VISIBLE);
                    break;
                    //单次加载完成
                case 2:
                    bottomViewHolder.item_order_bottom_bottomNotice.setVisibility(View.GONE);
                    bottomViewHolder.item_order_bottom_note.setVisibility(View.INVISIBLE);
                    bottomViewHolder.item_order_bottom_process.setVisibility(View.INVISIBLE);
                    break;
                    //全部加载完成
                case 3:
                    bottomViewHolder.item_order_bottom_bottomNotice.setVisibility(View.VISIBLE);
                    bottomViewHolder.item_order_bottom_note.setVisibility(View.GONE);
                    bottomViewHolder.item_order_bottom_process.setVisibility(View.GONE);
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public int getItemCount() {
        return orderList.size() + 1;
    }

    public int getLoadingState() {
        return loadingState;
    }

    public void setLoadingState(int loadingState) {
        this.loadingState = loadingState;
        notifyDataSetChanged();
    }

    public interface OnItemClickListener{
        void onItemClick(View view, BriefOrderItem item);
    }

    public int getLoadingErrorType() {
        return loadingErrorType;
    }

    public void setLoadingErrorType(int loadingErrorType) {
        this.loadingErrorType = loadingErrorType;
    }

}
