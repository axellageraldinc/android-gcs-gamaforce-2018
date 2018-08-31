package gamaforce.gcs.com.gcsgamaforce2018android.contract;

public interface MainContract {
    interface View {
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
        void showMode(String mode);
        void showArmStatus(String armStatus);

        void setDronePositionOnGoogleMaps(double latitude, double longitude);

        void showToastMessage(String message);
    }
    interface Presenter {
        void connectToUsb(String baudRate);
        void disconnectFromUsb();
    }
}
