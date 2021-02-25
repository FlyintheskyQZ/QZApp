package seu.qz.qzapp.entity;

import org.litepal.crud.LitePalSupport;

import java.io.Serializable;
import java.util.Date;


/**
 * 用户类，继承LitePalSupport以适配LitePal框架，所有字段与服务器的用户模型对应
 */
public class AppCustomer extends LitePalSupport implements Serializable {

    //用户Id
    private Integer user_id;

    //用户昵称
    private String user_nickName;
    //用户密码
    private String user_password;
    //用户余额
    private Integer user_balance;
    //用户注册真实姓名
    private String user_registerName;
    //用户身份证id
    private String user_identityId;
    //用户注册时间
    private Date register_time;
    //是否是男性
    private boolean isMale;

    //普通用户等级为1，商家等级为2
    private Integer authority_level;
    //如果是商家，则其相关的仪器id,以";"分隔
    private String related_device_id;
    //用户手机号，或者座机号
    private String phoneNumber;
    //用户Email
    private String email;
    //用户持有的待完成订单数
    private Integer numberForProvideOrders;
    //用户持有的已完成订单数
    private Integer numberForFinishedOrders;

    public AppCustomer() {
    }

    public AppCustomer(Integer user_id, String user_nickName, String user_password, Integer user_balance,
                       String user_registerName, String user_identityId, Date register_time, boolean isMale,
                       Integer authority_level, String related_device_id, String phoneNumber, String email,
                       Integer numberForProvideOrders, Integer numberForFinishedOrders) {
        this.user_id = user_id;
        this.user_nickName = user_nickName;
        this.user_password = user_password;
        this.user_balance = user_balance;
        this.user_registerName = user_registerName;
        this.user_identityId = user_identityId;
        this.register_time = register_time;
        this.isMale = isMale;
        this.authority_level = authority_level;
        this.related_device_id = related_device_id;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.numberForProvideOrders = numberForProvideOrders;
        this.numberForFinishedOrders = numberForFinishedOrders;
    }

    public AppCustomer(AppCustomer customer) {
        this.user_id = customer.user_id;
        this.user_nickName = customer.user_nickName;
        this.user_password = customer.user_password;
        this.user_balance = customer.user_balance;
        this.user_registerName = customer.user_registerName;
        this.user_identityId = customer.user_identityId;
        this.register_time = customer.register_time;
        this.isMale = customer.isMale;
        this.authority_level = customer.authority_level;
        this.related_device_id = customer.related_device_id;
        this.phoneNumber = customer.phoneNumber;
        this.email = customer.email;
        this.numberForProvideOrders = customer.numberForProvideOrders;
        this.numberForFinishedOrders = customer.numberForFinishedOrders;
    }

    public AppCustomer(String user_nickName, String user_password) {
        this.user_nickName = user_nickName;
        this.user_password = user_password;
    }


    public void adjustSelf(AppCustomer mainCustomer) {
        this.user_password = mainCustomer.getUser_password();
        this.user_balance = mainCustomer.getUser_balance();
        this.related_device_id = mainCustomer.getRelated_device_id();
        this.phoneNumber = mainCustomer.getPhoneNumber();
        this.email = mainCustomer.getEmail();
        this.numberForProvideOrders = mainCustomer.getNumberForProvideOrders();
        this.numberForFinishedOrders = mainCustomer.getNumberForFinishedOrders();
    }

    @Override
    public String toString() {
        return "AppCustomer{" +
                "user_id=" + user_id +
                ", user_nickName='" + user_nickName + '\'' +
                ", user_password='" + user_password + '\'' +
                ", user_balance=" + user_balance +
                ", user_registerName='" + user_registerName + '\'' +
                ", user_identityId='" + user_identityId + '\'' +
                ", register_time=" + register_time +
                ", authority_level=" + authority_level +
                ", related_device_id='" + related_device_id + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", email='" + email + '\'' +
                ", numberForProvideOrders=" + numberForProvideOrders +
                ", numberForFinishedOrders=" + numberForFinishedOrders +
                '}';
    }

    public Integer getNumberForProvideOrders() {
        return numberForProvideOrders;
    }

    public void setNumberForProvideOrders(Integer numberForProvideOrders) {
        this.numberForProvideOrders = numberForProvideOrders;
    }

    public Integer getNumberForFinishedOrders() {
        return numberForFinishedOrders;
    }

    public void setNumberForFinishedOrders(Integer numberForFinishedOrders) {
        this.numberForFinishedOrders = numberForFinishedOrders;
    }

    public Integer getUser_id() {
        return user_id;
    }

    public void setUser_id(Integer user_id) {
        this.user_id = user_id;
    }

    public String getUser_nickName() {
        return user_nickName;
    }

    public void setUser_nickName(String user_nickName) {
        this.user_nickName = user_nickName;
    }

    public String getUser_password() {
        return user_password;
    }

    public void setUser_password(String user_password) {
        this.user_password = user_password;
    }

    public Integer getUser_balance() {
        return user_balance;
    }

    public void setUser_balance(Integer user_balance) {
        this.user_balance = user_balance;
    }

    public String getUser_registerName() {
        return user_registerName;
    }

    public void setUser_registerName(String user_registerName) {
        this.user_registerName = user_registerName;
    }

    public String getUser_identityId() {
        return user_identityId;
    }

    public void setUser_identityId(String user_identityId) {
        this.user_identityId = user_identityId;
    }

    public Date getRegister_time() {
        return register_time;
    }

    public void setRegister_time(Date register_time) {
        this.register_time = register_time;
    }

    public Integer getAuthority_level() {
        return authority_level;
    }

    public void setAuthority_level(Integer authority_level) {
        this.authority_level = authority_level;
    }

    public String getRelated_device_id() {
        return related_device_id;
    }

    public void setRelated_device_id(String related_device_id) {
        this.related_device_id = related_device_id;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isMale() {
        return isMale;
    }

    public void setMale(boolean male) {
        isMale = male;
    }
}
