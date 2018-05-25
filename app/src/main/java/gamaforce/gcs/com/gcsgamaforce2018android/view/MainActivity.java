package gamaforce.gcs.com.gcsgamaforce2018android.view;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import javax.inject.Inject;

import gamaforce.gcs.com.gcsgamaforce2018android.R;
import gamaforce.gcs.com.gcsgamaforce2018android.contract.MainContract;
import gamaforce.gcs.com.gcsgamaforce2018android.presenter.MainPresenterImpl;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, MainContract.View {

    private MainContract.Presenter mainPresenter;

    private Spinner spinnerBaudRate;
    private Button btnConnect;
    private TextView txtLogData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initializeWidget();
        // TODO : Migrate to dagger2 injection
        mainPresenter = new MainPresenterImpl(this, this);
    }

    private void initializeWidget() {
        spinnerBaudRate = findViewById(R.id.spinner_baud_rate);
        setSpinnerBaudRateContent();
        btnConnect = findViewById(R.id.button_connect);
        txtLogData = findViewById(R.id.text_log);
        btnConnect.setOnClickListener(this);
    }
    private void setSpinnerBaudRateContent() {
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.baud_rate_list, android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBaudRate.setAdapter(spinnerAdapter);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_connect:
                if(btnConnect.getText().toString().equals("CONNECT"))
                    mainPresenter.connectToUsb(spinnerBaudRate.getSelectedItem().toString());
                else
                    mainPresenter.disconnectFromUsb();
                break;
        }
    }

    @Override
    public void showLogData(String log) {
        txtLogData.append(log);
    }

    @Override
    public void showAltitude(double altitude) {
        //TODO : Implement here...
    }

    @Override
    public void showYaw(double yaw) {
        //TODO : Implement here...
    }

    @Override
    public void showPitch(double pitch) {
        //TODO : Implement here...
    }

    @Override
    public void showRoll(double roll) {
        //TODO : Implement here...
    }

    @Override
    public void setDronePositionOnGoogleMaps(double latitude, double longitude) {
        //TODO : Implement here...
    }

    @Override
    public void setBtnConnectText(String text) {
        btnConnect.setText(text);
    }
}
