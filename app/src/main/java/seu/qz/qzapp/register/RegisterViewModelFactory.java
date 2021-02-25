package seu.qz.qzapp.register;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class RegisterViewModelFactory implements ViewModelProvider.Factory{

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (modelClass.isAssignableFrom(RegisterViewModel.class)) {
                return (T) new RegisterViewModel(RegisterOptions.getInstance());
            } else {
                throw new IllegalArgumentException("Unknown ViewModel class");
            }
    }
}
