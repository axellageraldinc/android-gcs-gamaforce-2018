package gamaforce.gcs.com.gcsgamaforce2018android.view;

import android.Manifest;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

import gamaforce.gcs.com.gcsgamaforce2018android.R;
import gamaforce.gcs.com.gcsgamaforce2018android.contract.MainContract;
import gamaforce.gcs.com.gcsgamaforce2018android.presenter.MainPresenterImpl;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, View.OnClickListener, MainContract.View {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final Integer GMAPS_REQUEST_PERMISSION = 1;

    private List<LatLng> uavLocationList = new ArrayList<>();

    private MainContract.Presenter mainPresenter;

    private Dialog dialog;
    private Spinner spinnerBaudRate;
    private Button btnConnect;
    private FloatingActionButton btnShowDialogConnect;
    // TODO : AttitudeIndicator still not working
    private AttitudeIndicator attitudeIndicator;
    private TextView txtAltitude, txtYaw, txtPitch, txtRoll;

    private GoogleMap googleMap;
    private Marker marker;
    private BitmapDescriptor uavMarker;
    private Polyline uavPathPolyline;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initializeWidget();
        // TODO : Migrate to dagger2 injection
        mainPresenter = new MainPresenterImpl(this, this);
    }

    private void initializeWidget() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        attitudeIndicator = new AttitudeIndicator(getApplicationContext());
        txtAltitude = findViewById(R.id.txtAltitude);
        txtYaw = findViewById(R.id.txtYaw);
        txtPitch = findViewById(R.id.txtPitch);
        txtRoll = findViewById(R.id.txtRoll);

        btnShowDialogConnect = findViewById(R.id.btnShowDialogConnect);
        btnShowDialogConnect.setOnClickListener(this);

        dialog = new Dialog(MainActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_connect);
        int width = (int)(getResources().getDisplayMetrics().widthPixels*0.5);
        int height = (int)(getResources().getDisplayMetrics().heightPixels*0.5);
        dialog.getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
        spinnerBaudRate = dialog.findViewById(R.id.spinnerBaudRate);
        setSpinnerBaudRateContent();
        btnConnect = dialog.findViewById(R.id.button_connect);
        btnConnect.setOnClickListener(this);
    }

    private void setSpinnerBaudRateContent() {
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.baud_rate_list, android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBaudRate.setAdapter(spinnerAdapter);
    }

    @Override
    public void dismissDialogConnect() {
        dialog.dismiss();
        btnConnect.setText("DISCONNECT");
    }

    @Override
    public void changeBtnConnectTextToConnect() {
        btnConnect.setText("CONNECT");
    }

    @Override
    public void changeBtnConnectTextToDisconnect() {
        btnConnect.setText("DISCONNECT");
    }

    @Override
    public void setAttitudeIndicator(final double pitch, final double roll) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                attitudeIndicator.setAttitude(Float.parseFloat(String.valueOf(pitch)), Float.parseFloat(String.valueOf(roll)));
            }
        });
    }

    @Override
    public void showAltitude(final double altitude) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                txtAltitude.setText(String.valueOf(altitude));
            }
        });
    }

    @Override
    public void showYaw(final double yaw) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                txtYaw.setText(String.valueOf(yaw));
            }
        });
    }

    @Override
    public void showPitch(final double pitch) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                txtPitch.setText(String.valueOf(pitch));
            }
        });
    }

    @Override
    public void showRoll(final double roll) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                txtRoll.setText(String.valueOf(roll));
            }
        });
    }

    @Override
    public void setDronePositionOnGoogleMaps(final double latitude, final double longitude) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                moveMarker(new LatLng(latitude, longitude));
                drawPolyline(new LatLng(latitude, longitude));
            }
        });
    }

    @Override
    public void showToastMessage(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.button_connect:
                if(btnConnect.getText().toString().equals("CONNECT"))
                    mainPresenter.connectToUsb(spinnerBaudRate.getSelectedItem().toString());
                else
                    mainPresenter.disconnectFromUsb();
                break;
            case R.id.btnShowDialogConnect:
                dialog.show();
                break;
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        createMarker();
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, GMAPS_REQUEST_PERMISSION);
            }
        } else {
            this.googleMap.setMyLocationEnabled(true);
        }
    }

    private void createMarker() {
        if(this.uavMarker == null){
            uavMarker = BitmapDescriptorFactory.fromResource(R.mipmap.ic_plane);
        }
        if(marker ==null) {
            LatLng ugm = new LatLng(-7.7713847, 110.3774998);
            marker = googleMap.addMarker(new MarkerOptions()
                    .position(ugm)
                    .flat(true)
                    .anchor(0.5f, 0.5f)
                    .icon(this.uavMarker)
                    .title("Lokasi Pesawat"));
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(ugm));
            googleMap.animateCamera(CameraUpdateFactory.zoomTo(17));
        }
    }

    private void moveMarker(LatLng latLng) {
        if(this.uavMarker == null){
            uavMarker = BitmapDescriptorFactory.fromResource(R.mipmap.ic_plane);
        }
        marker.remove();
        marker = googleMap.addMarker(new MarkerOptions()
                .position(latLng)
                .flat(true)
                .anchor(0.5f, 0.5f)
                .icon(this.uavMarker)
                .title("Lokasi Pesawat"));
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
    }

    private void drawPolyline(LatLng latLng){
        uavLocationList.add(latLng);
        if (uavLocationList.size() == 2) {
            LatLng firstLocation = uavLocationList.get(0);
            LatLng lastLocation = uavLocationList.get(1);

            this.uavPathPolyline = googleMap.addPolyline(new PolylineOptions()
                    .add(firstLocation, lastLocation)
                    .width(30)
                    .color(R.color.colorAccent)
                    .geodesic(true));

        } else if (uavLocationList.size() > 2) {
            LatLng toLocation = uavLocationList.get(uavLocationList.size() - 1);

            List<LatLng> points = uavPathPolyline.getPoints();
            points.add(toLocation);

            uavPathPolyline.setPoints(points);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
