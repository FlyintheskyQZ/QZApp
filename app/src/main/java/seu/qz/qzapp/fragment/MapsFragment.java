package seu.qz.qzapp.fragment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import seu.qz.qzapp.R;
import seu.qz.qzapp.fragment.viewmodel.BaseViewModel;
import seu.qz.qzapp.fragment.viewmodel.MapsViewModel;
import seu.qz.qzapp.activity.viewmodel.MainViewModel;

public class MapsFragment extends Fragment {

//    private OnMapReadyCallback callback = new OnMapReadyCallback() {
//        /**
//         * Manipulates the map once available.
//         * This callback is triggered when the map is ready to be used.
//         * This is where we can add markers or lines, add listeners or move the camera.
//         * In this case, we just add a marker near Sydney, Australia.
//         * If Google Play services is not installed on the device, the user will be prompted to
//         * install it inside the SupportMapFragment. This method will only be triggered once the
//         * user has installed Google Play services and returned to the app.
//         */
//        @Override
//        public void onMapReady(GoogleMap googleMap) {
//            LatLng sydney = new LatLng(-34, 151);
//            googleMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//            googleMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
//        }
//    };

    public static MapsFragment newInstance(){
        return new MapsFragment();
    }

    MainViewModel mainViewModel;
    TextView maps_text;
    MapsViewModel mapsViewModel;

    public MapsFragment() {
    }

    public MapsViewModel getViewModel(){
        return mapsViewModel;
    }

    public TextView getMaps_text() {
        return maps_text;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainViewModel = ViewModelProviders.of(getActivity()).get(MainViewModel.class);
        mapsViewModel = ViewModelProviders.of(this).get(MapsViewModel.class);
//        mapsViewModel.setTest_text1(mainViewModel.getTextChange1());
//        mapsViewModel.setTest_text2(mainViewModel.getTextChange2());
    }



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_maps, container, false);
        maps_text = view.findViewById(R.id.maps_text1);
        return view;

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
//        SupportMapFragment mapFragment =
//                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
//        if (mapFragment != null) {
//            mapFragment.getMapAsync(callback);
//        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.i("MainActivity", "maps_onDestroyView: 启动！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("MainActivity", "onDestroy: 启动！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！");
    }
}