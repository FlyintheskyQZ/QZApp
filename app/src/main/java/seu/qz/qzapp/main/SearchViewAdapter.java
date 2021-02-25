package seu.qz.qzapp.main;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import seu.qz.qzapp.R;
import seu.qz.qzapp.entity.SearchHistoryItem;

public class SearchViewAdapter extends ArrayAdapter<SearchHistoryItem> {
    int resourceId;
    //临时列表，存放当前用户输入状态下经过筛选的记录
    List<SearchHistoryItem> items;
    //初始列表，存放所有当前用户的搜索记录
    List<SearchHistoryItem> original_items;
    private static final String TAG = "SearchFragment";

    public SearchViewAdapter(@NonNull Context context, int resource, int textViewResourceId, @NonNull List<SearchHistoryItem> objects) {
        super(context, resource, textViewResourceId, objects);
        this.resourceId = resource;
        this.items = objects;
    }

    /**
     * 创建更新子项视图的方法，需要注意的是，该方法依据的数据源是Arrayadapter的内部list，需要通过add()或remove()方法手动删减
     * @param position
     * @param convertView
     * @param parent
     * @return
     */
    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final SearchHistoryItem item = getItem(position);
        View view;
        ViewHolder viewHolder;
        Context context = getContext();
        //效率化
        if(convertView == null){
            view = LayoutInflater.from(context).inflate(resourceId, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.search_history_item_icon = view.findViewById(R.id.search_history_item_icon);
            viewHolder.search_history_item_text = view.findViewById(R.id.search_history_item_text);
            viewHolder.search_history_item_delete = view.findViewById(R.id.search_history_item_delete);
            view.setTag(viewHolder);
        }else {
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();
        }
        viewHolder.search_history_item_text.setText(item.getSearch_item());
        viewHolder.search_history_item_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //点击删除按钮则删除聊天记录，更新视图
                items.remove(position);
                original_items.remove(item);
                item.delete();
                remove(item);
                notifyDataSetChanged();
            }
        });
        final SearchView searchView = ((Activity)context).findViewById(R.id.searchfragment_searchView);
        viewHolder.search_history_item_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //点击记录部分，则直接将当前搜索记录提交到输入框中
                searchView.setQuery(item.getSearch_item(), true);
            }
        });
        Log.d(TAG, "getView:" + item.getSearch_item());
        return view;
    }

    /**
     * 筛选的过滤器,复写的两个方法在autoCompleteTextView的输入框的内容每次变化时都会调用
     * @return
     */
    @NonNull
    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                if(original_items == null){
                    //首次启动将数据库中对应用户的所有搜索记录存入初始列表original_items中
                    original_items = new ArrayList<>(items);
                }
                //输入框中无输入，故无限制字符，显示所有搜索记录
                if(constraint == null || constraint.length() == 0){
                    results.count = original_items.size();
                    results.values = original_items;
                }else {
                    //输入框中有限制字符，依据该字符匹配相关记录存入results中
                    List<SearchHistoryItem> filteredArrayList = new ArrayList<>();
                    constraint = constraint.toString();
                    for (int i = 0; i < original_items.size(); i++) {
                        SearchHistoryItem data = original_items.get(i);
                        if(data.getSearch_item().contains(constraint.toString())) {
                            filteredArrayList.add(data);
                        }
                    }
                    //返回得到的筛选列表
                    results.count = filteredArrayList.size();
                    results.values = filteredArrayList;
                }
                Log.d(TAG, "performFiltering:" + results.count);
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                //更新items，存入将要展示的子项
                items = (List<SearchHistoryItem>) results.values;
                //每次输入框发生改变时清空视图，重新生成并更新视图
                clear();
                if(items.size() > 0){
                    for(int i = 0; i < items.size(); i++){
                        add(items.get(i));
                    }
                    notifyDataSetChanged();
                }
                Log.d(TAG, "publishResults:" + items.size());
            }
        };
        return filter;
    }

    class ViewHolder{
        ImageView search_history_item_icon;
        TextView search_history_item_text;
        ImageButton search_history_item_delete;
    }


    public int getResourceId() {
        return resourceId;
    }

    public void setResourceId(int resourceId) {
        this.resourceId = resourceId;
    }

    public List<SearchHistoryItem> getItems() {
        return items;
    }

    public void setItems(List<SearchHistoryItem> items) {
        this.items = items;
    }

    public List<SearchHistoryItem> getOriginal_items() {
        return original_items;
    }

    public void setOriginal_items(List<SearchHistoryItem> original_items) {
        this.original_items = original_items;
    }
}
