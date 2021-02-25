package seu.qz.qzapp.entity;

import java.io.Serializable;

public class BriefReportItem implements Serializable {

    //报告的名称（pdf名称）
    String report_name;
    //报告的日期（实验日期）
    String report_date;
    //报告对应的材料名称
    String report_material;
    //报告的结果
    String report_result;
    //报告对应的商家name
    String saler_name;
    //报告对应的FinishedOrder的编号
    String order_id;
    //报告对应的ExperimentResult的编号
    String result_id;
    //报告是否已下载
    boolean isDownloaded = false;

    public BriefReportItem() {
    }

    public BriefReportItem(String report_name, String report_date, String report_material, String report_result,
                           String saler_name, String order_id, String result_id, boolean isDownloaded) {
        this.report_name = report_name;
        this.report_date = report_date;
        this.report_material = report_material;
        this.report_result = report_result;
        this.saler_name = saler_name;
        this.order_id = order_id;
        this.result_id = result_id;
        this.isDownloaded = isDownloaded;
    }

    public boolean isDownloaded() {
        return isDownloaded;
    }

    public void setDownloaded(boolean downloaded) {
        isDownloaded = downloaded;
    }

    public String getOrder_id() {
        return order_id;
    }

    public void setOrder_id(String order_id) {
        this.order_id = order_id;
    }

    public String getResult_id() {
        return result_id;
    }

    public void setResult_id(String result_id) {
        this.result_id = result_id;
    }

    public String getSaler_name() {
        return saler_name;
    }

    public void setSaler_name(String saler_name) {
        this.saler_name = saler_name;
    }

    public String getReport_name() {
        return report_name;
    }

    public void setReport_name(String report_name) {
        this.report_name = report_name;
    }

    public String getReport_date() {
        return report_date;
    }

    public void setReport_date(String report_date) {
        this.report_date = report_date;
    }

    public String getReport_material() {
        return report_material;
    }

    public void setReport_material(String report_material) {
        this.report_material = report_material;
    }

    public String getReport_result() {
        return report_result;
    }

    public void setReport_result(String report_result) {
        this.report_result = report_result;
    }
}
