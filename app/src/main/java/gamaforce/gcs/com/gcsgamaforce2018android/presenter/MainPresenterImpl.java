package gamaforce.gcs.com.gcsgamaforce2018android.presenter;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.util.Log;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.hoho.android.usbserial.util.SerialInputOutputManager;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import gamaforce.gcs.com.gcsgamaforce2018android.contract.MainContract;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class MainPresenterImpl implements MainContract.Presenter, SerialInputOutputManager.Listener {

    private static final String TAG = MainPresenterImpl.class.getSimpleName();

    private UsbManager usbManager;
    private SerialInputOutputManager serialInputOutputManager;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private UsbSerialPort usbSerialPort = null;
    private UsbSerialDriver usbSerialDriver;

    private MainContract.View mainView;
    private Observable<String> dataObservable = null;
    private Observer<String> dataObserver = null;
    private Disposable disposable = null;
    private Context context;

    private PendingIntent usbPermissionIntent;
    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";

    private IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
    private String baudRate;

    public MainPresenterImpl(Context context, MainContract.View mainView) {
        this.mainView = mainView;
        this.context = context;
        usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
    }

    @Override
    public void refreshDeviceList() {
        mainView.showToastMessage("Refreshing device list");
        List<UsbSerialDriver> availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(usbManager);
        mainView.populateSpinnerComPort(availableDrivers);
        if (availableDrivers.isEmpty()) {
            mainView.enableButtonConnect(false);
        } else {
            mainView.enableButtonConnect(true);
        }
    }

    @Override
    public void connectToUsb(String baudRate) {
        usbSerialDriver = getFirstAvailableUsb();
        this.baudRate = baudRate;
        if (usbSerialDriver != null) {
            usbPermissionIntent = PendingIntent.getBroadcast(context, 0,
                    new Intent(ACTION_USB_PERMISSION), 0);
            context.registerReceiver(usbBroadcast, filter);
            usbManager.requestPermission(usbSerialDriver.getDevice(), usbPermissionIntent);
        }
    }

    @Override
    public void disconnectFromUsb() {
        try {
            mainView.setDisposable(null);
            serialInputOutputManager.stop();
            context.unregisterReceiver(usbBroadcast);
            usbSerialPort.close();
            mainView.dismissDialogConnect();
            mainView.changeBtnConnectTextToConnect();
            mainView.showToastMessage("Disconnected from USB...");
            refreshDeviceList();
        } catch (IOException e) {
            Log.e(TAG, "Error disconnecting usb : " + e.getMessage());
        }
    }

    @Override
    public void setArmStatus(int armStatus) {
        try {
            String fullCommand = "a#" + armStatus + "*";
            usbSerialPort.write(fullCommand.getBytes(), 3000);
        } catch (IOException e) {
            Log.e(TAG, "Error write to usb : " + e.getMessage());
        }
    }

    @Override
    public void setControlMode(int controlMode) {
        try {
            String fullCommand = "u#" + controlMode + "*";
            usbSerialPort.write(fullCommand.getBytes(), 3000);
        } catch (IOException e) {
            Log.e(TAG, "Error write to usb : " + e.getMessage());
        }
    }

    @Override
    public void sendAutoTakeOff() {
        try {
            String fullCommand = "t";
            usbSerialPort.write(fullCommand.getBytes(), 3000);
        } catch (IOException e) {
            Log.e(TAG, "Error write to usb : " + e.getMessage());
        }
    }

    @Override
    public void sendAutoLanding() {
        try {
            String fullCommand = "l";
            usbSerialPort.write(fullCommand.getBytes(), 3000);
        } catch (IOException e) {
            Log.e(TAG, "Error write to usb : " + e.getMessage());
        }
    }

    @Override
    public void onNewData(byte[] data) {
        String retrievedData = new String(data);
        if (isDataValid(retrievedData)) {
            // @#alt#yaw#pitch#roll#lat#lng#air_speed#battery#plane_mode(vtol atau plane)#gcs_command#control_mode(manual atau auto)#arming(0 atau 1)#*
            dataObservable = getDataObservable(retrievedData);
            dataObserver = getDataObserver();
            dataObservable.observeOn(Schedulers.io())
                    .subscribeOn(AndroidSchedulers.mainThread())
                    .subscribe(dataObserver);
        } else {
            Log.e(TAG, "Invalid data : " + retrievedData);
        }
    }

    @Override
    public void onRunError(Exception e) {
        Log.e(TAG, "Error listening to USB : " + e.getMessage());
        mainView.showToastMessage("Error listening to USB : " + e.getMessage());
    }

    private void beginRetrievingData() {
        //TODO : Consider migrating this serialInputOutputManager initiation using Dagger2
        serialInputOutputManager =
                new SerialInputOutputManager(usbSerialPort, this);
        executorService.submit(serialInputOutputManager);
    }

    private UsbSerialDriver getFirstAvailableUsb() {
        // Find all available drivers from attached devices.
        List<UsbSerialDriver> availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(usbManager);
        if (availableDrivers.isEmpty()) {
            mainView.showToastMessage("No connected USB found!");
            return null;
        }
        return availableDrivers.get(0);
    }

    private boolean isDataValid(String data) {
        if (data.split("\\*").length > 1) {
            String validData = data.split("\\*")[0];
            String[] dataSplit = validData.split("#");
            return dataSplit.length == 13 &&
                    !dataSplit[0].contains("\\*");
        } else {
            String[] splittedData = data.split("#");
            return splittedData.length == 14 && !splittedData[0].equals("*");
        }
    }

    private double parseData(int index, String data) {
        try {
            String[] dataSplit = data.split("#");
            return Double.parseDouble(dataSplit[index]);
        } catch (Exception ex) {
            return 0;
        }
    }

    private Observer<String> getDataObserver() {
        return new Observer<String>() {
            @Override
            public void onSubscribe(Disposable d) {
                Log.d(TAG, "onSubscribe");
                disposable = d;
                mainView.setDisposable(disposable);
            }

            @Override
            public void onNext(String retrievedData) {
                Log.d(TAG, retrievedData);
                mainView.showAltitude(parseData(1, retrievedData));
                mainView.showYaw(parseData(2, retrievedData));
                mainView.showPitch(parseData(3, retrievedData));
                mainView.showRoll(parseData(4, retrievedData));
                mainView.setAttitudeIndicator(parseData(3, retrievedData), parseData(4, retrievedData));
                mainView.setDronePositionOnGoogleMaps(
                        parseData(5, retrievedData),
                        parseData(6, retrievedData),
                        parseData(2, retrievedData)
                );
                mainView.showAirSpeed(parseData(7, retrievedData));
                mainView.showBattery(parseData(8, retrievedData));

                int planeMode = (int) parseData(9, retrievedData);
                if (planeMode == 0)
                    mainView.showPlaneMode("VTOL");
                else
                    mainView.showPlaneMode("PLANE");

                int gcsCommand = (int) parseData(10, retrievedData);
//                mainView.showGcsCommand(gcsCommand);

                int controlMode = (int) parseData(11, retrievedData);
                mainView.showControlMode(controlMode);

                int armStatus = (int) parseData(12, retrievedData);
                if (armStatus == 0)
                    mainView.showArmStatus("DISARMED");
                else
                    mainView.showArmStatus("ARMED");
            }

            @Override
            public void onError(Throwable e) {
                Log.e(TAG, e.getMessage());
            }

            @Override
            public void onComplete() {
                Log.d(TAG, "onComplete");
            }
        };
    }

    private io.reactivex.Observable<String> getDataObservable(String receivedData) {
        return io.reactivex.Observable.just(receivedData);
    }

    private final BroadcastReceiver usbBroadcast = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbDevice device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if(device != null) {
                            Log.d(TAG, "permission granted for device " + device);
                            UsbDeviceConnection connection = usbManager.openDevice(usbSerialDriver.getDevice());
                            usbSerialPort = usbSerialDriver.getPorts().get(0);
                            try {
                                usbSerialPort.open(connection);
                                usbSerialPort.setParameters(Integer.parseInt(baudRate), 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);
                                usbSerialPort.setDTR(true);
                                mainView.dismissDialogConnect();
                                mainView.changeBtnConnectTextToDisconnect();
                                mainView.showToastMessage("Successfully connected to USB...");
                                beginRetrievingData();
                            } catch (IOException e) {
                                Log.e(TAG, "Error connecting to usb : " + e.getMessage());
                                mainView.showToastMessage("Error connecting to USB : " + e.getMessage());
                            }
                        }
                    }
                    else {
                        Log.d(TAG, "permission denied for device " + device);
                        mainView.showToastMessage("Please allow USB permission");
                    }
                }
            } else if (UsbManager.ACTION_USB_ACCESSORY_DETACHED.equals(action)) {
                UsbDevice device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if (device != null) {
                    Log.d(TAG, "usb disconnected");
                    disconnectFromUsb();
                }
            }
        }
    };
}
