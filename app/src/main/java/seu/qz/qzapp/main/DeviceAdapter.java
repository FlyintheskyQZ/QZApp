package seu.qz.qzapp.main;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import seu.qz.qzapp.R;
import seu.qz.qzapp.entity.LOIInstrument;

public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.ViewHolder> {

    List<LOIInstrument> instruments;
    OnItemClickListener listener;

    public DeviceAdapter(List<LOIInstrument> instruments, OnItemClickListener listener) {
        this.instruments = instruments;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_brief_device, parent, false);
        DeviceAdapter.ViewHolder holder = new DeviceAdapter.ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final LOIInstrument instrument = instruments.get(position);
        holder.device_item_count.setText(String.valueOf(position + 1) + ".");
        holder.device_item_order.setText(instrument.getDevice_id().toString());
        holder.device_item_factory.setText(instrument.getFactory_name());
        holder.device_item_address.setText(instrument.getFactory_address());
        String related_finished_orders = instrument.getF_orders_string();
        if(related_finished_orders == null || related_finished_orders.isEmpty()){
            holder.device_item_finished_text.setText("已完成0单");
        }else {
            holder.device_item_finished_text.setText("已完成" + String.valueOf(related_finished_orders.split(";").length) + "单");
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onClick(instrument);
            }
        });
    }

    @Override
    public int getItemCount() {
        return instruments.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder{

        TextView device_item_count;
        TextView device_item_order;
        TextView device_item_factory;
        TextView device_item_address;
        TextView device_item_finished_text;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            device_item_count = itemView.findViewById(R.id.device_item_count);
            device_item_order = itemView.findViewById(R.id.device_item_order);
            device_item_factory = itemView.findViewById(R.id.device_item_factory);
            device_item_address = itemView.findViewById(R.id.device_item_address);
            device_item_finished_text = itemView.findViewById(R.id.device_item_finished_text);
        }
    }

    public interface OnItemClickListener{
        void onClick(LOIInstrument instrument);
    }

    public List<LOIInstrument> getInstruments() {
        return instruments;
    }

    public void setInstruments(List<LOIInstrument> instruments) {
        this.instruments = instruments;
    }
}
