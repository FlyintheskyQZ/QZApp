package seu.qz.qzapp.activity.viewmodel;

import androidx.lifecycle.ViewModel;

import seu.qz.qzapp.entity.AppCustomer;
import seu.qz.qzapp.entity.BriefChatItem;

public class AgoraChatViewModel extends ViewModel {

    AppCustomer mainCustomer;
    BriefChatItem chatItem;


    public AppCustomer getMainCustomer() {
        return mainCustomer;
    }

    public void setMainCustomer(AppCustomer mainCustomer) {
        this.mainCustomer = mainCustomer;
    }

    public BriefChatItem getChatItem() {
        return chatItem;
    }

    public void setChatItem(BriefChatItem chatItem) {
        this.chatItem = chatItem;
    }
}
