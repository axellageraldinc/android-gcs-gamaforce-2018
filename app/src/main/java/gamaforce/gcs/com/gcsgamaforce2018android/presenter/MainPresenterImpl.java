package gamaforce.gcs.com.gcsgamaforce2018android.presenter;

import android.content.Context;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.util.Log;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.hoho.android.usbserial.util.SerialInputOutputManager;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
                beginRetrievingData();
            } catch (IOException e) {
                Log.e(TAG, "Error connecting to usb : " + e.getMessage());
            }
        }
    }

    @Override
    public void disconnectFromUsb() {
        try {
            usbSerialPort.close();
        } catch (IOException e) {
            Log.e(TAG, "Error disconnecting usb : " + e.getMessage());
        }
    }

    @Override
    public void onNewData(byte[] data) {
        if(isDataValid(Arrays.toString(data))) {
            mainView.showAltitude(Double.parseDouble(parseData(1, Arrays.toString(data))));
            mainView.showYaw(Double.parseDouble(parseData(2, Arrays.toString(data))));
            mainView.showPitch(Double.parseDouble(parseData(3, Arrays.toString(data))));
            mainView.showRoll(Double.parseDouble(parseData(4, Arrays.toString(data))));
            mainView.setDronePositionOnGoogleMaps(
                    Double.parseDouble(parseData(5, Arrays.toString(data))),
                    Double.parseDouble(parseData(6, Arrays.toString(data))));
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
            return null;
        }
        return availableDrivers.get(0);
    }

    private String parseData(int index, String data){
        String[] dataSplit = data.split("#");
        return dataSplit[index];
    }

    private boolean isDataValid(String data) {
        String[] dataSplit = data.split("#");
        return dataSplit.length == 7;
    }

}
