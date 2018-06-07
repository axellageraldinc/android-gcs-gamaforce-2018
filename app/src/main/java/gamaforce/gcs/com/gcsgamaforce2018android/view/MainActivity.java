package gamaforce.gcs.com.gcsgamaforce2018android.view;

import android.Manifest;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;

import gamaforce.gcs.com.gcsgamaforce2018android.R;
import gamaforce.gcs.com.gcsgamaforce2018android.contract.MainContract;
import gamaforce.gcs.com.gcsgamaforce2018android.presenter.MainPresenterImpl;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, View.OnClickListener, MainContract.View {

    private static final String TAG = MainActivity.class.getSimpleName();

    private MainContract.Presenter mainPresenter;

    private Dialog dialog;
    private Spinner spinnerBaudRate;
    private Button btnConnect;
    private FloatingActionButton btnShowDialogConnect;
    private GoogleMap googleMap;
    private AttitudeIndicator attitudeIndicator;
    private TextView txtAltitude, txtYaw, txtPitch, txtRoll;

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
    public void setDronePositionOnGoogleMaps(double latitude, double longitude) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //TODO : Implement here...
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
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 001);
            }
        } else {
            googleMap.setMyLocationEnabled(true);
        }
    }
}
