package gamaforce.gcs.com.gcsgamaforce2018android.contract;

public interface AttitudeContract {
    interface View {
        void setAttitudeIndicator(double pitch, double roll);
        void showAltitude(double altitude);
        void showYaw(double yaw);
        void showPitch(double pitch);
        void showRoll(double roll);
    }
    interface Presenter {
        void setView(String data);
    }
}
