package seu.qz.qzapp.activity.viewmodel;

import androidx.lifecycle.ViewModel;

import seu.qz.qzapp.activity.operation.DeviceContentOperation;
import seu.qz.qzapp.entity.AppCustomer;
import seu.qz.qzapp.entity.LOIInstrument;

public class DeviceContentViewModel extends ViewModel {

    DeviceContentOperation operation = new DeviceContentOperation();

    AppCustomer mainCustomer;

    LOIInstrument instrument;

    public LOIInstrument getInstrument() {
        return instrument;
    }

    public void setInstrument(LOIInstrument instrument) {
        this.instrument = instrument;
    }

    public AppCustomer getMainCustomer() {
        return mainCustomer;
    }

    public void setMainCustomer(AppCustomer mainCustomer) {
        this.mainCustomer = mainCustomer;
    }
}
