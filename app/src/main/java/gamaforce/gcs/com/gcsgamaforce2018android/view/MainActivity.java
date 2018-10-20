package gamaforce.gcs.com.gcsgamaforce2018android.view;

import android.Manifest;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;
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
import io.reactivex.disposables.Disposable;

public class MainActivity extends AppCompatActivity implements
        OnMapReadyCallback,
        Button.OnClickListener,
        MainContract.View, Switch.OnCheckedChangeListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final Integer GMAPS_REQUEST_PERMISSION = 1;

    private List<LatLng> uavLocationList = new ArrayList<>();

    private MainContract.Presenter mainPresenter;

    private Dialog dialogConnectToUav;
    private Dialog dialogSendCommandToUav;
    private Spinner spinnerBaudRate;
    private Spinner spinnerCommand;
    private Button btnConnect;
    private AttitudeIndicator attitudeIndicator;
    private TextView txtAltitude, txtYaw, txtPitch, txtRoll, txtAirSpeed, txtBattery, txtMode, txtArmStatus;
    private Switch switchArm;
    private Switch switchControlMode;
    private Button btnTakeoffLanding;

    private GoogleMap googleMap;
    private Marker marker;
    private BitmapDescriptor uavMarker;
    private Polyline uavPathPolyline;
    private float oldYaw;

    private Disposable disposable = null;

    private int planeMode=0, controlMode=0, armStatus=0;
    private boolean isInitialArmStatusHasBeenSet = false;
    private boolean isInitialControlModeHasBeenSet = false;

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

        attitudeIndicator = findViewById(R.id.attitude_indicator);
        txtAltitude = findViewById(R.id.txtAltitude);
        txtYaw = findViewById(R.id.txtYaw);
        txtPitch = findViewById(R.id.txtPitch);
        txtRoll = findViewById(R.id.txtRoll);
        txtAirSpeed = findViewById(R.id.txtAirSpeed);
        txtBattery = findViewById(R.id.txtBattery);
        txtMode = findViewById(R.id.txtMode);
        txtArmStatus = findViewById(R.id.txtArming);
        FloatingActionButton btnShowDialogConnect = findViewById(R.id.btnShowDialogConnect);
        btnShowDialogConnect.setOnClickListener(this);
        FloatingActionButton btnRefreshUsb = findViewById(R.id.btnRefreshUsb);
        btnRefreshUsb.setOnClickListener(this);

        dialogConnectToUav = new Dialog(MainActivity.this);
        dialogConnectToUav.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogConnectToUav.setContentView(R.layout.dialog_connect);
        int width = (int)(getResources().getDisplayMetrics().widthPixels*0.5);
        dialogConnectToUav.getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
        spinnerBaudRate = dialogConnectToUav.findViewById(R.id.spinnerCommand);
        setSpinnerBaudRateContent();
        btnConnect = dialogConnectToUav.findViewById(R.id.button_connect);
        btnConnect.setOnClickListener(this);

        switchArm = findViewById(R.id.switchArm);
        switchArm.setChecked(false);
        switchArm.setOnCheckedChangeListener(this);

        switchControlMode = findViewById(R.id.switchControlMode);
        switchControlMode.setChecked(false);
        switchControlMode.setOnCheckedChangeListener(this);

        btnTakeoffLanding = findViewById(R.id.btnTakeoffLanding);
        btnTakeoffLanding.setOnClickListener(this);
    }

    private void setSpinnerBaudRateContent() {
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.baud_rate_list, android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBaudRate.setAdapter(spinnerAdapter);
    }

    @Override
    public void enableButtonConnect(boolean isEnabled) {
        btnConnect.setEnabled(isEnabled);
    }

    @Override
    public void dismissDialogConnect() {
        dialogConnectToUav.dismiss();
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
                attitudeIndicator.setAttitude(
                        Float.parseFloat(String.valueOf(pitch)),
                        Float.parseFloat(String.valueOf(roll))
                );

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
    public void showAirSpeed(final double airSpeed) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                txtAirSpeed.setText(String.valueOf(airSpeed));
            }
        });
    }

    @Override
    public void showBattery(final double battery) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                txtBattery.setText(String.valueOf(battery) + "V");
            }
        });
    }

    @Override
    public void showPlaneMode(final String mode) {
        if (mode.equals(getApplicationContext().getResources().getStringArray(R.array.plane_mode_list)[0]))
            this.planeMode = 0;
        else
            this.planeMode = 1;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                txtMode.setText(mode);
            }
        });
    }

    @Override
    public void showControlMode(final int controlMode) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String controlModeText;
                if (controlMode == 0) {
                    controlModeText = "MANUAL";
                } else {
                    controlModeText = "AUTO";
                }
                switchControlMode.setText(controlModeText);
                if (!isInitialControlModeHasBeenSet) {
                    if (controlMode == 0){
                        switchArm.setChecked(false);
                    }
                    else {
                        switchArm.setChecked(true);
                    }
                    isInitialControlModeHasBeenSet = true;
                }
            }
        });
    }

    @Override
    public void showArmStatus(final String armStatus) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                txtArmStatus.setText(armStatus);
                switchArm.setText(armStatus);
                if (!isInitialArmStatusHasBeenSet) {
                    if (armStatus.equals(getApplicationContext().getResources().getStringArray(R.array.arm_status_list)[0])){
                        switchArm.setChecked(false);
                    }
                    else {
                        switchArm.setChecked(true);
                    }
                    isInitialArmStatusHasBeenSet = true;
                }
            }
        });
    }

    @Override
    public void setTakeOffLanding(boolean isTakeOff) {
        if (isTakeOff) {
            btnTakeoffLanding.setText("TAKE OFF");
        } else {
            btnTakeoffLanding.setText("LANDING");
        }
    }

    @Override
    public void setDronePositionOnGoogleMaps(final double latitude, final double longitude,
                                             final double yaw) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                moveMarker(new LatLng(latitude, longitude), yaw);
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
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        createMarker();
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, GMAPS_REQUEST_PERMISSION);
            }
        } else {
            this.googleMap.setMyLocationEnabled(false);
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

    private void moveMarker(LatLng latLng, double yaw) {
        float rotationDegree = (float) yaw - oldYaw;
        oldYaw = (float) yaw;
        if(this.uavMarker == null){
            uavMarker = BitmapDescriptorFactory.fromResource(R.mipmap.ic_plane);
        }
        marker.remove();
        marker = googleMap.addMarker(new MarkerOptions()
                .position(latLng)
                .flat(true)
                .anchor(0.5f, 0.5f)
                .rotation(rotationDegree)
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
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.switchArm:
                if (btnConnect.getText().equals("CONNECT")) {
                    showToastMessage("You have to connect to UAV first!");
                    switchArm.setChecked(false);
                } else {
                    if (isChecked) {
                        mainPresenter.setArmStatus(1);
                    } else {
                        mainPresenter.setArmStatus(0);
                    }
                }
                break;
            case R.id.switchControlMode:
                if (btnConnect.getText().equals("CONNECT")) {
                    showToastMessage("You have to connect to UAV first!");
                    switchControlMode.setChecked(false);
                } else {
                    if (isChecked) {
                        mainPresenter.setControlMode(1);
                    } else {
                        mainPresenter.setControlMode(0);
                    }
                }
                break;
        }
    }

    @Override
    public void setDisposable(Disposable disposable) {
        this.disposable = disposable;
    }

    @Override
    protected void onDestroy() {
        if (disposable != null) {
            disposable.dispose();
        }
        super.onDestroy();
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
                dialogConnectToUav.show();
                break;
            case R.id.btnRefreshUsb:
                mainPresenter.refreshDeviceList();
                break;
            case R.id.btnTakeoffLanding:
                if (btnTakeoffLanding.getText().toString().equals("TAKE OFF")) {
                    mainPresenter.sendAutoTakeOff();
                    btnTakeoffLanding.setText("LANDING");
                } else {
                    mainPresenter.sendAutoLanding();
                    btnTakeoffLanding.setText("TAKE OFF");
                }
                break;
        }
    }
}
