package gamaforce.gcs.com.gcsgamaforce2018android.contract;

import io.reactivex.disposables.Disposable;

public interface MainContract {
    interface View {
        void enableButtonConnect(boolean isEnabled);
        void dismissDialogConnect();
        void changeBtnConnectTextToConnect();
        void changeBtnConnectTextToDisconnect();

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
    }
}
