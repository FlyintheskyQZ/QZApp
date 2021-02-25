package seu.qz.qzapp.main;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import seu.qz.qzapp.R;
import seu.qz.qzapp.entity.AppCustomer;
import seu.qz.qzapp.entity.BriefReportItem;

public class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.ViewHolder> {

    AppCustomer mainCustomer;

    List<BriefReportItem> reportItems;
    OnItemClickListener listener;

    public ReportAdapter(List<BriefReportItem> reportItems, OnItemClickListener listener) {
        this.reportItems = reportItems;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_brief_report, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final BriefReportItem item = reportItems.get(position);
        holder.report_pdf_name.setText(String.valueOf(position + 1) + ".  " + mainCustomer.getUser_nickName() + "-" + item.getOrder_id() + "-" + item.getSaler_name() + ".pdf");
        holder.report_pdf_result.setText(item.getReport_result() + "%");
        holder.report_pdf_material.setText(item.getReport_material());
        holder.report_pdf_date.setText(item.getReport_date());
        if(item.isDownloaded()){
            holder.report_pdf_download.setImageResource(R.mipmap.ic_reportdisplay_downloaded);
        }else {
            holder.report_pdf_download.setImageResource(R.mipmap.ic_reportdisplay_undownload);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onClick(item);
            }
        });
        if(position == getItemCount() - 1){
            holder.report_pdf_splitline.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return reportItems.size();
    }

    static class  ViewHolder extends RecyclerView.ViewHolder{

        TextView report_pdf_name;
        TextView report_pdf_material;
        TextView report_pdf_result;
        TextView report_pdf_date;
        ImageView report_pdf_download;
        View report_pdf_splitline;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            report_pdf_name = itemView.findViewById(R.id.report_pdf_name);
            report_pdf_material = itemView.findViewById(R.id.report_pdf_material);
            report_pdf_result = itemView.findViewById(R.id.report_pdf_result);
            report_pdf_date = itemView.findViewById(R.id.report_pdf_date);
            report_pdf_download = itemView.findViewById(R.id.report_pdf_download);
            report_pdf_splitline = itemView.findViewById(R.id.report_pdf_splitline);
        }
    }

    public interface OnItemClickListener{
        void onClick(BriefReportItem item);
    }

    public List<BriefReportItem> getReportItems() {
        return reportItems;
    }

    public void setReportItems(List<BriefReportItem> reportItems) {
        this.reportItems = reportItems;
    }

    public AppCustomer getMainCustomer() {
        return mainCustomer;
    }

    public void setMainCustomer(AppCustomer mainCustomer) {
        this.mainCustomer = mainCustomer;
    }
}
