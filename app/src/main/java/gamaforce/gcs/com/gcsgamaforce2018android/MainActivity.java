package gamaforce.gcs.com.gcsgamaforce2018android;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.hoho.android.usbserial.util.HexDump;
import com.hoho.android.usbserial.util.SerialInputOutputManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String USB_TAG = "UsbAndroidDriver";
    private static final String CLASS_TAG = MainActivity.class.getSimpleName();

    private static UsbSerialPort sSerialPort = null;

    private List<UsbSerialPort> ports = new ArrayList<>();

    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();

    private Spinner mSpinnerBaudRate, mSpinnerUsbPort;
    private Button mBtnConnect, mBtnRefresh;
    private TextView mTxtLog;

    private SerialInputOutputManager.Listener serialListener =
            new SerialInputOutputManager.Listener() {
                @Override
                public void onNewData(final byte[] data) {
                    try {
                        Thread.sleep(500);
                        MainActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                MainActivity.this.updateData(data);
                            }
                        });
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onRunError(Exception e) {

                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initializeWidget();
        setSpinnerBaudRateContent();
//        setSpinnerUsbPortContent();
    }

    private void initializeWidget() {
        mSpinnerBaudRate = findViewById(R.id.spinner_baud_rate);
//        mSpinnerUsbPort = findViewById(R.id.spinner_usb_port);
//        mBtnRefresh = findViewById(R.id.button_refresh_usb);
        mBtnConnect = findViewById(R.id.button_connect);
        mTxtLog = findViewById(R.id.text_log);

//        mBtnRefresh.setOnClickListener(this);
        mBtnConnect.setOnClickListener(this);
    }

    private void setSpinnerBaudRateContent() {
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.baud_rate_list, android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinnerBaudRate.setAdapter(spinnerAdapter);
    }

//    private void setSpinnerUsbPortContent() {
//        refreshUsb();
//    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
//            case R.id.button_refresh_usb:
//                refreshUsb();
//                break;
            case R.id.button_connect:
                if (sSerialPort == null) {
                    connectToUsb();
                } else {
                    closeConnection();
                }
                break;
        }
    }

//    @SuppressLint("StaticFieldLeak")
//    private void refreshUsb() {
//        new AsyncTask<Void, Void, List<UsbSerialPort>>() {
//            @Override
//            protected List<UsbSerialPort> doInBackground(Void... params) {
//                Log.d(USB_TAG, "Refreshing device list ...");
//                SystemClock.sleep(1000);
//
//                // Get UsbManager from Android.
//                UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);
//                final List<UsbSerialDriver> drivers =
//                        UsbSerialProber.getDefaultProber().findAllDrivers(manager);
//
//                final List<UsbSerialPort> result = new ArrayList<>();
//                for (final UsbSerialDriver driver : drivers) {
//                    final List<UsbSerialPort> ports = driver.getPorts();
//                    Log.d(USB_TAG, String.format("+ %s: %s port%s",
//                            driver, ports.size(), ports.size() == 1 ? "" : "s"));
//                    result.addAll(ports);
//                }
//
//                return result;
//            }
//
//            @Override
//            protected void onPostExecute(List<UsbSerialPort> result) {
//                ports.clear();
//                ports.addAll(result);
//                Log.d(USB_TAG, "Done refreshing, " + ports.size() + " entries found.");
//            }
//
//        }.execute((Void) null);
//    }

    private void connectToUsb() {
        int baudRate = Integer.parseInt(mSpinnerBaudRate.getSelectedItem().toString());

        // Find all available drivers from attached devices.
        UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);
        List<UsbSerialDriver> availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(manager);
        if (availableDrivers.isEmpty()) {
            return;
        }

        // Open a connection to the first available driver.
        UsbSerialDriver driver = availableDrivers.get(0);
        UsbDeviceConnection connection = manager.openDevice(driver.getDevice());
        if (connection == null) {
            // You probably need to call UsbManager.requestPermission(driver.getDevice(), ..)
            return;
        }

        // Read some data! Most have just one port (port 0).
        sSerialPort = driver.getPorts().get(0);
        try {
            sSerialPort.open(connection);
            sSerialPort.setParameters(baudRate, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);
            SerialInputOutputManager serialManager =
                    new SerialInputOutputManager(sSerialPort, serialListener);
            mExecutor.submit(serialManager);
            byte buffer[] = new byte[16];
            int numBytesRead = sSerialPort.read(buffer, 1000);
            Log.d(USB_TAG, "Read " + numBytesRead + " bytes.");
            Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show();
            mBtnConnect.setText("STOP");
        } catch (IOException e) {
            Log.d(USB_TAG, e.getMessage());
            Toast.makeText(this, "Gagal: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            sSerialPort = null;
            mBtnConnect.setText("Connect");
            return;
        }
    }

    private void closeConnection() {
        if (sSerialPort != null) {
            try {
                sSerialPort.close();
                mBtnConnect.setText("Connect");
            } catch (IOException e) {
                Toast.makeText(this, "Gagal: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void updateData(byte[] data) {
        String message = new String(data) + "\n\n";
//        Toast.makeText(this, "Terkoneksi" + message, Toast.LENGTH_SHORT).show();
        mTxtLog.append(message);
        mTxtLog.setMovementMethod(new ScrollingMovementMethod());
    }
}
