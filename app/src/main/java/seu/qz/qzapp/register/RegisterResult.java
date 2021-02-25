package seu.qz.qzapp.register;

import seu.qz.qzapp.entity.AppCustomer;

/**
 * 注册结果类：
 */
public class RegisterResult {
    //注册是否成功
    private boolean isSuccess;
    //注册失败的原因
    private String errorReason;

    private AppCustomer successCustomer;

    public RegisterResult(String errorReason) {
        this.isSuccess = false;
        this.errorReason = errorReason;
    }

    public RegisterResult() {
        this.isSuccess = false;
        this.errorReason = null;
    }

    public RegisterResult(AppCustomer customer){
        this.isSuccess = true;
        this.errorReason = null;
        this.successCustomer = customer;
    }

    public AppCustomer getSuccessCustomer() {
        return successCustomer;
    }

    public void setSuccessCustomer(AppCustomer successCustomer) {
        this.successCustomer = successCustomer;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public void setSuccess(boolean success) {
        isSuccess = success;
    }

    public String getErrorReason() {
        return errorReason;
    }

    public void setErrorReason(String errorReason) {
        this.errorReason = errorReason;
    }
}
