package seu.qz.qzapp.entity;

import java.util.Date;

/**
 * 此类为完成的订单（即有实验数据产生前的订单）
 */
public class FinishedOrder {

    //已成订单中的排号
    private Integer order_id;
    //仪器序号
    private Integer device_id;
    //用户id
    private Integer user_id;
    //用户昵称
    private String user_name;
    //卖家名称(昵称）
    private String saler_name;
    //商家名称
    private String factory_name;

    //仪器对应于商家的序号
    private Integer device_orderForSaler;
    //对应于相关的用户的订单序号（用于在服务器中给其排序）
    private Integer orderForRelatedUser;
    //展示价格
    private Integer deal_price;
    //租用起始时间（实验最早开始时间）
    private Date rentTime_begin;
    //租用结束时间（实验最迟结束时间）
    private Date rentTime_end;
    //操作人员
    private String operator_name;

    //结果id
    private Integer result_id;
    //对应于相关的商家的订单序号（用于在服务器中给其排序）
    private Integer orderForRelatedSaler;
    //商家id
    private Integer saler_id;
    //订单下单时时间
    private Date order_placed;
    //材料名称
    private String materialName;
    //材料型号
    private Integer materialType;
    //实验结果数据，精确到小数点后一位
    private long result_data;
    //订单号，建立订单时由总的订单数确定
    private Integer placed_orderNumber;
    //仪器地址
    private String device_address;
    //额外说明，对应ProvideOrder的material_explanation，最终对应到PDF的特别说明中
    private String extra_explanation;

    public FinishedOrder() {
        super();
    }

    public String getExtra_explanation() {
        return extra_explanation;
    }

    public void setExtra_explanation(String extra_explanation) {
        this.extra_explanation = extra_explanation;
    }

    public String getSaler_name() {
        return saler_name;
    }

    public void setSaler_name(String saler_name) {
        this.saler_name = saler_name;
    }

    public long getResult_data() {
        return result_data;
    }

    public String getDevice_address() {
        return device_address;
    }

    public void setDevice_address(String device_address) {
        this.device_address = device_address;
    }

    public void setResult_data(long result_data) {
        this.result_data = result_data;
    }

    public Integer getPlaced_orderNumber() {
        return placed_orderNumber;
    }

    public void setPlaced_orderNumber(Integer placed_orderNumber) {
        this.placed_orderNumber = placed_orderNumber;
    }

    public Integer getOrder_id() {
        return order_id;
    }

    public void setOrder_id(Integer order_id) {
        this.order_id = order_id;
    }

    public Integer getDevice_id() {
        return device_id;
    }

    public void setDevice_id(Integer device_id) {
        this.device_id = device_id;
    }

    public Integer getUser_id() {
        return user_id;
    }

    public void setUser_id(Integer user_id) {
        this.user_id = user_id;
    }

    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public String getFactory_name() {
        return factory_name;
    }

    public void setFactory_name(String factory_name) {
        this.factory_name = factory_name;
    }

    public Integer getDevice_orderForSaler() {
        return device_orderForSaler;
    }

    public void setDevice_orderForSaler(Integer device_orderForSaler) {
        this.device_orderForSaler = device_orderForSaler;
    }

    public Integer getOrderForRelatedUser() {
        return orderForRelatedUser;
    }

    public void setOrderForRelatedUser(Integer orderForRelatedUser) {
        this.orderForRelatedUser = orderForRelatedUser;
    }

    public Integer getDeal_price() {
        return deal_price;
    }

    public void setDeal_price(Integer deal_price) {
        this.deal_price = deal_price;
    }

    public Date getRentTime_begin() {
        return rentTime_begin;
    }

    public void setRentTime_begin(Date rentTime_begin) {
        this.rentTime_begin = rentTime_begin;
    }

    public Date getRentTime_end() {
        return rentTime_end;
    }

    public void setRentTime_end(Date rentTime_end) {
        this.rentTime_end = rentTime_end;
    }

    public String getOperator_name() {
        return operator_name;
    }

    public void setOperator_name(String operator_name) {
        this.operator_name = operator_name;
    }

    public Integer getResult_id() {
        return result_id;
    }

    public void setResult_id(Integer result_id) {
        this.result_id = result_id;
    }

    public Integer getOrderForRelatedSaler() {
        return orderForRelatedSaler;
    }

    public void setOrderForRelatedSaler(Integer orderForRelatedSaler) {
        this.orderForRelatedSaler = orderForRelatedSaler;
    }

    public Integer getSaler_id() {
        return saler_id;
    }

    public void setSaler_id(Integer saler_id) {
        this.saler_id = saler_id;
    }

    public Date getOrder_placed() {
        return order_placed;
    }

    public void setOrder_placed(Date order_placed) {
        this.order_placed = order_placed;
    }

    public String getMaterialName() {
        return materialName;
    }

    public void setMaterialName(String materialName) {
        this.materialName = materialName;
    }

    public Integer getMaterialType() {
        return materialType;
    }

    public void setMaterialType(Integer materialType) {
        this.materialType = materialType;
    }
}
