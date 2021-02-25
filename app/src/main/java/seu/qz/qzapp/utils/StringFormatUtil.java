package seu.qz.qzapp.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 字符串格式化工具类
 */
public class StringFormatUtil {

    //昵称验证：只包含中文、数字、英文
    public static String REGULAR_NICKNAME = "[\\u4e00-\\u9fa5]*|\\w*|\\d*|_*";

    //真实姓名验证：可以为中文或者英文但不能同时出现，允许输入空格和点，长度20字符以内
    public static String REGULAR_REALNAME = "^([\\u4e00-\\u9fa5]{1,20}|[a-zA-Z\\.\\s]{1,20})$";

    //密码验证：6-16位字符，只能是英文或者数字，但不能全是英文或者数字
    public static String REGULAR_PASSWORD = "^(?![0-9])(?![0-9]+$)(?![a-zA-Z]+$)[0-9A-Za-z]{6,16}$";

    //手机号验证（国内）
    public static String REGULAR_CELLPHONE = "^((13[0-9])|(14[0,1,4-9])|(15[0-3,5-9])|(16[2,5,6,7])|(17[0-8])|(18[0-9])|(19[0-3,5-9]))\\d{8}$";

    //座机号验证:区号（0开头加两位数字）+"-"（不加这个也行）+号码（7-8位数字）
    public static String REGULAR_PHONE = "0\\d{2,3}[-]?\\d{7,8}|0\\d{2,3}\\s?\\d{7,8}";

    //身份证号验证，15-18位，最后一位可为字母
    public static String REGULAR_REALID = "(^[1-9]\\d{5}(18|19|20)\\d{2}((0[1-9])|(10|11|12))(([0-2][1-9])|10|20|30|31)\\d{3}[0-9Xx]$)|"
                                        + "(^[1-9]\\d{5}\\d{2}((0[1-9])|(10|11|12))(([0-2][1-9])|10|20|30|31)\\d{3}$)";

    //Email验证
    public static String REGULAR_EMAIL = "^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$";

    /**
     * 验证字符串str是否满足正则表达式regularExpression的要求
     * @param str
     * @param regularExpression
     * @return
     */
    public static boolean isValid(String str, String regularExpression){
        if(str.isEmpty()){
            return false;
        }
        Pattern p=Pattern.compile(regularExpression);
        Matcher m=p.matcher(str);
        boolean isMatch=m.matches();
        return isMatch;
    }
}
