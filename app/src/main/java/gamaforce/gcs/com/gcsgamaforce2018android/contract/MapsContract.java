package gamaforce.gcs.com.gcsgamaforce2018android.contract;

public interface MapsContract {
    interface View {
        void setDronePositionOnGoogleMaps(double latitude, double longitude);
    }
    interface Presenter {
        void setView(String data);
    }
}
