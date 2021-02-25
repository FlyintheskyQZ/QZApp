package seu.qz.qzapp.activity.viewmodel;

import android.view.Gravity;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModel;

import com.ashokvarma.bottomnavigation.BottomNavigationBar;
import com.ashokvarma.bottomnavigation.BottomNavigationItem;
import com.ashokvarma.bottomnavigation.TextBadgeItem;

import java.util.ArrayList;
import java.util.List;

import seu.qz.qzapp.R;
import seu.qz.qzapp.activity.BaseApplication;
import seu.qz.qzapp.entity.AppCustomer;

public class MainViewModel extends ViewModel {


    private List<String> bottomMenus;
    private List<String> textChange1 = new ArrayList<>();
    private List<String> textChange2 = new ArrayList<>();
    private AppCustomer mainCustomer;





    @Override
    protected void onCleared() {
        super.onCleared();
//        fragments.clear();
//        bottomMenus.clear();
    }

    public AppCustomer getMainCustomer() {
        return mainCustomer;
    }

    public void setMainCustomer(AppCustomer mainCustomer) {
        this.mainCustomer = mainCustomer;
    }

    public List<String> getBottomMenus() {
        return bottomMenus;
    }

    public void setBottomMenus(List<String> bottomMenus) {
        this.bottomMenus = bottomMenus;
    }

    public List<String> getTextChange1() {
        return textChange1;
    }

    public void setTextChange1(List<String> textChange1) {
        this.textChange1 = textChange1;
    }

    public List<String> getTextChange2() {
        return textChange2;
    }

    public void setTextChaned2(List<String> textChaned2) {
        this.textChange2 = textChaned2;
    }
}
