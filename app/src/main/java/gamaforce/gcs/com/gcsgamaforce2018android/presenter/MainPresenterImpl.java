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

import gamaforce.gcs.com.gcsgamaforce2018android.R;
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

    @Override
    public void disconnectFromUsb() {
        try {
            usbSerialPort.close();
            mainView.dismissDialogConnect();
            mainView.changeBtnConnectTextToConnect();
            mainView.showToastMessage("Disconnected from USB...");
        } catch (IOException e) {
            Log.e(TAG, "Error disconnecting usb : " + e.getMessage());
//            mainView.showToastMessage("Error disconnecting usb : " + e.getMessage());
        }
    }

    @Override
    public void writeToUsb(String payload) {
        try {
            usbSerialPort.write(payload.getBytes(), 3000);
        } catch (IOException e) {
            Log.e(TAG, "Error write to usb : " + e.getMessage());
        }
    }

    @Override
    public void onNewData(byte[] data) {
        String retrievedData = new String(data);
        if(isDataValid(retrievedData)) {
            Log.d(TAG, "Valid data : " + retrievedData);
            mainView.showAltitude(parseData(1, retrievedData));
            mainView.showYaw(parseData(2, retrievedData));
            mainView.showPitch(parseData(3, retrievedData));
            mainView.showRoll(parseData(4, retrievedData));
            mainView.setAttitudeIndicator(parseData(3, retrievedData), parseData(4, retrievedData));
            mainView.setDronePositionOnGoogleMaps(parseData(5, retrievedData), Double.parseDouble(removeLastChar(String.valueOf(parseData(6, retrievedData)))));
            mainView.showAirSpeed(parseData(7, retrievedData));
            mainView.showBattery(parseData(8, retrievedData));
            int mode = (int) parseData(9, retrievedData);
            Log.d(TAG, "MODE : " + mode + " | " + context.getResources().getStringArray(R.array.mode_list)[mode]);
            mainView.showMode(context.getResources().getStringArray(R.array.mode_list)[mode]);
            int armStatus = (int) parseData(11, retrievedData);
            Log.d(TAG, "ARM STATUS : " + armStatus + " | " + context.getResources().getStringArray(R.array.arm_status_list)[armStatus]);
            mainView.showArmStatus(context.getResources().getStringArray(R.array.arm_status_list)[armStatus]);
        } else{
            Log.e(TAG, "Invalid data : " + retrievedData);
        }
    }

    @Override
    public void onRunError(Exception e) {
        Log.e(TAG, "Error listening to USB : " + e.getMessage());
        mainView.showToastMessage("Error listening to USB : " + e.getMessage());
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
            mainView.showToastMessage("No connected USB found!");
            return null;
        }
        return availableDrivers.get(0);
    }

    private boolean isDataValid(String data) {
        String validData = data.split("\\*")[0];
        String[] dataSplit = validData.split("#");
        return dataSplit.length == 12 &&
                !dataSplit[0].contains("\\*");
    }

    private double parseData(int index, String data){
        try {
            String[] dataSplit = data.split("#");
            return Double.parseDouble(dataSplit[index]);
        } catch (Exception ex){
            return 0;
        }
    }

    private String removeLastChar(String str) {
        return str.substring(0, str.length() - 1);
    }

}
