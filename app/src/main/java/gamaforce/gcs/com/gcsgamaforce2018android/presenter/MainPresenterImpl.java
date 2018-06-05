package gamaforce.gcs.com.gcsgamaforce2018android.presenter;

import android.content.Context;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.util.Log;
import android.widget.Toast;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.hoho.android.usbserial.util.SerialInputOutputManager;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import gamaforce.gcs.com.gcsgamaforce2018android.contract.AttitudeContract;
import gamaforce.gcs.com.gcsgamaforce2018android.contract.MainContract;

public class MainPresenterImpl implements MainContract.Presenter, SerialInputOutputManager.Listener {

    private static final String TAG = MainPresenterImpl.class.getSimpleName();

    private Context context;
    private UsbManager usbManager;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private static UsbSerialPort usbSerialPort = null;

    private MainContract.View mainView;

    public MainPresenterImpl(Context context, MainContract.View mainView) {
        this.context = context;
        this.mainView = mainView;
        usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
    }

    @Override
    public void connectToUsb(String baudRate) {
        UsbSerialDriver usbSerialDriver = getFirstAvailableUsb();
        if (usbSerialDriver != null) {
            UsbDeviceConnection connection = usbManager.openDevice(usbSerialDriver.getDevice());
            usbSerialPort = usbSerialDriver.getPorts().get(0);
            try {
                usbSerialPort.open(connection);
                usbSerialPort.setParameters(Integer.parseInt(baudRate), 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);
                Toast.makeText(context, "Successfully connected to USB...", Toast.LENGTH_SHORT).show();
                mainView.dismissDialogConnect();
                beginRetrievingData();
            } catch (IOException e) {
                Toast.makeText(context, "Failed connecting to usb : " + e.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Error connecting to usb : " + e.getMessage());
            }
        }
    }

    @Override
    public void disconnectFromUsb() {
        try {
            usbSerialPort.close();
            mainView.dismissDialogConnect();
            mainView.changeBtnConnectTextToConnect();
        } catch (IOException e) {
            Toast.makeText(context, "Error disconnecting usb : " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Error disconnecting usb : " + e.getMessage());
        }
    }

    @Override
    public void onNewData(byte[] data) {
        String sData = new String(data);
        Log.d(TAG, sData);
        if(isDataValid(sData)) {
            // TODO: Set data to maps and attitude fragment
        }
    }

    @Override
    public void onRunError(Exception e) {
        Log.e(TAG, "Error listening to usb port data : " + e.getMessage());
    }

    private void beginRetrievingData(){
        //TODO : Consider migrating this serialInputOutputManager initiation using Dagger2
        SerialInputOutputManager serialInputOutputManager =
                new SerialInputOutputManager(usbSerialPort, this);
        executorService.submit(serialInputOutputManager);
    }

    private UsbSerialDriver getFirstAvailableUsb(){
        // Find all available drivers from attached devices.
        List<UsbSerialDriver> availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(usbManager);
        if (availableDrivers.isEmpty()) {
            Toast.makeText(context, "No connected USB found...", Toast.LENGTH_SHORT).show();
            return null;
        }
        return availableDrivers.get(0);
    }

    private boolean isDataValid(String data) {
        String[] dataSplit = data.split("#");
        if (dataSplit.length == 7) {
            return dataSplit[6].contains("*");
        } else {
            return false;
        }
    }

}
