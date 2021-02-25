package seu.qz.qzapp.entity;

import java.io.Serializable;

public class BriefOrderItem implements Serializable {

    //是否已经完成
    private boolean ok;
    //订单序号（对应provideOrder or finishedOrder数据库的id）
    private Integer orderNumber;
    //机器名称（包括商家和机器序号）
    private String instrument;
    //租用起始时间
    private String date;
    //消费金额
    private String cost;
    //仪器位置（经纬度）
    private String location;

    public BriefOrderItem(boolean Ok, Integer orderNumber, String instrument, String date, String cost) {
        this.ok = Ok;
        this.orderNumber = orderNumber;
        this.instrument = instrument;
        this.date = date;
        this.cost = cost;
        this.location = "";
    }

    public BriefOrderItem(boolean ok, Integer orderNumber, String instrument, String date, String cost, String location) {
        this.ok = ok;
        this.orderNumber = orderNumber;
        this.instrument = instrument;
        this.date = date;
        this.cost = cost;
        this.location = location;
    }

    public BriefOrderItem() {
    }

    @Override
    public String toString() {
        return "BriefOrderItem{" +
                "isOk=" + ok +
                ", orderNumber=" + orderNumber +
                ", instrument='" + instrument + '\'' +
                ", date='" + date + '\'' +
                ", cost='" + cost + '\'' +
                '}';
    }

    public boolean isOk() {
        return ok;
    }

    public void setOk(boolean ok) {
        this.ok = ok;
    }

    public Integer getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(Integer orderNumber) {
        this.orderNumber = orderNumber;
    }

    public String getInstrument() {
        return instrument;
    }

    public void setInstrument(String instrument) {
        this.instrument = instrument;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getCost() {
        return cost;
    }

    public void setCost(String cost) {
        this.cost = cost;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
