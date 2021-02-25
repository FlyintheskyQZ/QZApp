package seu.qz.qzapp.entity;

import org.litepal.crud.LitePalSupport;

import java.io.Serializable;
import java.util.Map;

/**
 * 仪器类：与服务器仪器类的字段统一，继承LitePalSupport以适配LitePal框架
 */
public class LOIInstrument extends LitePalSupport implements Serializable {

    //仪器id
    private Integer device_id;

    //商家名称
    private String factory_name;
    //商家地址
    private String factory_address;
    //商家所在经度
    private String factory_longitude;
    //商家所在纬度
    private String factory_latitude;
    //商家电话号码
    private String factory_phoneNumber;
    //该仪器的特别说明
    private String extra_description;
    //该仪器提供的待选择订单
    private String p_orders_string;
    //该仪器的完成订单
    private String f_orders_string;
    //该仪器相关的用户id（商家id）
    private Integer related_user_id;

    public LOIInstrument() {
    }

    public LOIInstrument(Integer device_id, String factory_name, String factory_address, String factory_longitude,
                         String factory_latitude, String factory_phoneNumber, String extra_description, String p_orders_string,
                         String f_orders_string, Integer related_user_id) {
        this.device_id = device_id;
        this.factory_name = factory_name;
        this.factory_address = factory_address;
        this.factory_longitude = factory_longitude;
        this.factory_latitude = factory_latitude;
        this.factory_phoneNumber = factory_phoneNumber;
        this.extra_description = extra_description;
        this.p_orders_string = p_orders_string;
        this.f_orders_string = f_orders_string;
        this.related_user_id = related_user_id;
    }

    public Integer getDevice_id() {
        return device_id;
    }

    public void setDevice_id(Integer device_id) {
        this.device_id = device_id;
    }

    public String getFactory_name() {
        return factory_name;
    }

    public void setFactory_name(String factory_name) {
        this.factory_name = factory_name;
    }

    public String getFactory_address() {
        return factory_address;
    }

    public void setFactory_address(String factory_address) {
        this.factory_address = factory_address;
    }

    public String getFactory_longitude() {
        return factory_longitude;
    }

    public void setFactory_longitude(String factory_longitude) {
        this.factory_longitude = factory_longitude;
    }

    public String getFactory_latitude() {
        return factory_latitude;
    }

    public void setFactory_latitude(String factory_latitude) {
        this.factory_latitude = factory_latitude;
    }

    public String getFactory_phoneNumber() {
        return factory_phoneNumber;
    }

    public void setFactory_phoneNumber(String factory_phoneNumber) {
        this.factory_phoneNumber = factory_phoneNumber;
    }

    public String getExtra_description() {
        return extra_description;
    }

    public void setExtra_description(String extra_description) {
        this.extra_description = extra_description;
    }

    public String getP_orders_string() {
        return p_orders_string;
    }

    public void setP_orders_string(String p_orders_string) {
        this.p_orders_string = p_orders_string;
    }

    public String getF_orders_string() {
        return f_orders_string;
    }

    public void setF_orders_string(String f_orders_string) {
        this.f_orders_string = f_orders_string;
    }

    public Integer getRelated_user_id() {
        return related_user_id;
    }

    public void setRelated_user_id(Integer related_user_id) {
        this.related_user_id = related_user_id;
    }
}
