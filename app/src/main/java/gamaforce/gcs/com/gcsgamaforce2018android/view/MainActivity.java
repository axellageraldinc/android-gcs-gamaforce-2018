package gamaforce.gcs.com.gcsgamaforce2018android.view;

import android.app.Dialog;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;

import gamaforce.gcs.com.gcsgamaforce2018android.R;
import gamaforce.gcs.com.gcsgamaforce2018android.contract.AttitudeContract;
import gamaforce.gcs.com.gcsgamaforce2018android.contract.MainContract;
import gamaforce.gcs.com.gcsgamaforce2018android.presenter.MainPresenterImpl;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, BottomNavigationView.OnNavigationItemSelectedListener, MainContract.View {

    private static final String TAG = MainActivity.class.getSimpleName();

    private MainContract.Presenter mainPresenter;

    private BottomNavigationView bottomNavigationView;
    private Spinner spinnerBaudRate;
    private Button btnConnect;
    private Toolbar toolbar;
    private Dialog dialog;
    private AttitudeFragment attitudeFragment;
    private PlaneMapsFragment planeMapsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initializeWidget();
        attitudeFragment = new AttitudeFragment();
        planeMapsFragment = new PlaneMapsFragment();
        // TODO : Migrate to dagger2 injection
        mainPresenter = new MainPresenterImpl(this, this,
                attitudeFragment.getAttitudePresenter(), planeMapsFragment.getMapsPresenter());
        loadFragment(planeMapsFragment);
    }

    private void initializeWidget() {
        bottomNavigationView = findViewById(R.id.navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayShowHomeEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        initializeDialogConnectWidget();
    }

    private void initializeDialogConnectWidget(){
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

    private void loadFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.frame_container, fragment);
        fragmentTransaction.setCustomAnimations(android.R.anim.fade_in,
                android.R.anim.fade_out);
        fragmentTransaction.disallowAddToBackStack();
        fragmentTransaction.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_connectx) {
            dialog.show();
            Window window = dialog.getWindow();
            window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment fragment;
        switch (item.getItemId()) {
            case R.id.navigation_map:
                fragment = planeMapsFragment;
                loadFragment(fragment);
                return true;
            case R.id.navigation_attitude:
                fragment = attitudeFragment;
                loadFragment(fragment);
                return true;
        }
        return false;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_connect:
                if(btnConnect.getText().equals("CONNECT"))
                    mainPresenter.connectToUsb(spinnerBaudRate.getSelectedItem().toString());
                else
                    mainPresenter.disconnectFromUsb();
                break;
        }
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

}
