package seu.qz.qzapp.entity;

import java.io.Serializable;
import java.util.Date;

/**
 * 此类为未完成的订单（即没有实验数据产生前的订单）
 */
public class ProvideOrder implements Serializable {


    //未完成订单中的排号
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
    //对应于相关的用户的订单序号（只考虑ProvideOrder用于在服务器中给其排序）
    private Integer orderForRelatedUser;
    //展示价格
    private Integer display_price;
    //租用起始时间（实验最早开始时间）
    private Date rentTime_begin;
    //租用结束时间（实验最迟结束时间）
    private Date rentTime_end;

    //订单状态码：0（订单刚刚发布尚未被选购，此阶段商家可修改订单信息）；1（订单已被用户订购，但尚未被商家确认，此阶段订单可被用户取消）
    // 2（订单被商家驳回，需要重新填写资料）；3（订单被商家确认，合作关系确定）
    private Integer order_confirmed;
    //对应于相关的商家的订单序号（只考虑ProvideOrder用于在服务器中给其排序）
    private Integer orderForRelatedSaler;
    //商家id
    private Integer saler_id;
    //订单下单时时间（指订单成功建立,合作开始时的时间）
    private Date order_placed;
    //材料名称
    private String materialName;
    //材料型号
    private Integer materialType;
    //订单号，建立订单时由总的订单数确定
    private Integer placed_orderNumber;
    //仪器地址
    private String device_address;
    //材料说明，由FinishedOrder的extra_explanation继承，订单创建期间由user提供
    private String material_explanation;
    //额外说明,用于创立期间的审核情况记录，不记录到FinishedOrder中
    private String extra_explanation;


    public ProvideOrder() {
    }

    @Override
    public String toString() {
        return "ProvideOrder{" +
                "order_id=" + order_id +
                ", device_id=" + device_id +
                ", user_id=" + user_id +
                ", user_name='" + user_name + '\'' +
                ", saler_name='" + saler_name + '\'' +
                ", factory_name='" + factory_name + '\'' +
                ", device_orderForSaler=" + device_orderForSaler +
                ", orderForRelatedUser=" + orderForRelatedUser +
                ", display_price=" + display_price +
                ", rentTime_begin=" + rentTime_begin +
                ", rentTime_end=" + rentTime_end +
                ", order_confirmed=" + order_confirmed +
                ", orderForRelatedSaler=" + orderForRelatedSaler +
                ", saler_id=" + saler_id +
                ", order_placed=" + order_placed +
                ", materialName='" + materialName + '\'' +
                ", materialType=" + materialType +
                ", placed_orderNumber=" + placed_orderNumber +
                ", device_address='" + device_address + '\'' +
                ", material_explanation='" + material_explanation + '\'' +
                ", extra_explanation='" + extra_explanation + '\'' +
                '}';
    }

    public String getExtra_explanation() {
        return extra_explanation;
    }

    public void setExtra_explanation(String extra_explanation) {
        this.extra_explanation = extra_explanation;
    }

    public String getMaterial_explanation() {
        return material_explanation;
    }

    public void setMaterial_explanation(String material_explanation) {
        this.material_explanation = material_explanation;
    }

    public String getSaler_name() {
        return saler_name;
    }

    public void setSaler_name(String saler_name) {
        this.saler_name = saler_name;
    }

    public String getDevice_address() {
        return device_address;
    }

    public void setDevice_address(String device_address) {
        this.device_address = device_address;
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

    public Integer getDisplay_price() {
        return display_price;
    }

    public void setDisplay_price(Integer display_price) {
        this.display_price = display_price;
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

    public Integer getOrder_confirmed() {
        return order_confirmed;
    }

    public void setOrder_confirmed(Integer order_confirmed) {
        this.order_confirmed = order_confirmed;
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
