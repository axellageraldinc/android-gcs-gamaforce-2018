package gamaforce.gcs.com.gcsgamaforce2018android.contract;

import com.hoho.android.usbserial.driver.UsbSerialDriver;

import java.util.List;

import gamaforce.gcs.com.gcsgamaforce2018android.model.UsbComPort;
import io.reactivex.disposables.Disposable;

public interface MainContract {
    interface View {
        void enableButtonConnect(boolean isEnabled);
        void dismissDialogConnect();
        void changeBtnConnectTextToConnect();
        void changeBtnConnectTextToDisconnect();
        void populateSpinnerComPort(List<UsbSerialDriver> usbSerialDrivers);

        void setAttitudeIndicator(double pitch, double roll);
        void showAltitude(double altitude);
        void showYaw(double yaw);
        void showPitch(double pitch);
        void showRoll(double roll);
        void showAirSpeed(double airSpeed);
        void showBattery(double battery);
        void showPlaneMode(String mode);
        void showControlMode(int controlMode);
        void showArmStatus(String armStatus);
        void setTakeOffLanding(boolean isTakeOff);
        void showMissionButton();
        void hideMissionButton();

        void setDronePositionOnGoogleMaps(double latitude, double longitude, double yaw);

        void showToastMessage(String message);

        void setDisposable(Disposable disposable);
    }
    interface Presenter {
        void refreshDeviceList();
        void connectToUsb(String baudRate);
        void disconnectFromUsb();
        void setArmStatus(int armStatus);
        void setControlMode(int controlMode);
        void sendAutoTakeOff();
        void sendAutoLanding();
        void sendMission(int mission);
    }
}
