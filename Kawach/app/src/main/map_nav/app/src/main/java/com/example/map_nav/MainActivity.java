package com.example.map_nav;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;

import android.os.Bundle;
import android.widget.Button;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.SupportStreetViewPanoramaFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback {

    GoogleMap map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;

        LatLng KIET = new LatLng(28.754108, 77.495866);
        map.addMarker(new MarkerOptions().position(KIET).title("KIET"));
        map.moveCamera(CameraUpdateFactory.newLatLng(KIET));

        LatLng JFC = new LatLng(28.701808, 77.418668);
        map.addMarker(new MarkerOptions().position(JFC).title("JFC Family Restaurant"));
        map.moveCamera(CameraUpdateFactory.newLatLng(JFC));

        LatLng EasyDay = new LatLng(28.701280, 77.419465);
        map.addMarker(new MarkerOptions().position(EasyDay).title("Easyday Club"));
        map.moveCamera(CameraUpdateFactory.newLatLng(EasyDay));
    }
}