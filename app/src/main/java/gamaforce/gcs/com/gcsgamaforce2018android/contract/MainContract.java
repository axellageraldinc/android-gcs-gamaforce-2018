package gamaforce.gcs.com.gcsgamaforce2018android.contract;

public interface MainContract {
    interface View{
        void setAttitudeIndicator(double pitch, double roll);
        void showAltitude(double altitude);
        void showYaw(double yaw);
        void showPitch(double pitch);
        void showRoll(double roll);
        void setDronePositionOnGoogleMaps(double latitude, double longitude);
        void dismissDialogConnect();
        void changeBtnConnectTextToConnect();
    }
    interface Presenter{
        void connectToUsb(String baudRate);
        void disconnectFromUsb();
    }
}
