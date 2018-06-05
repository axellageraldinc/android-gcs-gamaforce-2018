package gamaforce.gcs.com.gcsgamaforce2018android.contract;

public interface MainContract {
    interface View {
        void dismissDialogConnect();
        void changeBtnConnectTextToConnect();
    }
    interface Presenter {
        void connectToUsb(String baudRate);
        void disconnectFromUsb();
    }
}
