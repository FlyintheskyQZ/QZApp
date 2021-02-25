package seu.qz.qzapp.main;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public abstract class CustomizeRecyclerScrollListener extends RecyclerView.OnScrollListener {

    //标志滑动方向是否向上
    private boolean isScrollUpward = false;

    //滑动状态发生改变时调用的方法
    @Override
    public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
        super.onScrollStateChanged(recyclerView, newState);
        LinearLayoutManager manager = (LinearLayoutManager) recyclerView.getLayoutManager();
        //新状态newState如果为停止滑动
        if(newState == RecyclerView.SCROLL_STATE_IDLE){
            //获取当前完整显示的最后一个视图的position
            int lastItemPosition = manager.findLastCompletelyVisibleItemPosition();
            int itemCount = manager.getItemCount();
           //如果滑动到最后一个Item并且停止前的滑动是向上滑动
            if(lastItemPosition == (itemCount - 1) && isScrollUpward){
                //加载更多的数据
                onLoadData();
            }
        }
    }

    //滑动停止后调用的方法
    @Override
    public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);
        isScrollUpward = dy > 0;
    }

    protected abstract void onLoadData();
}
