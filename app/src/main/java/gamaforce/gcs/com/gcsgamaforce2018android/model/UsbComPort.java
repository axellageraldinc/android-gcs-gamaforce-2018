package gamaforce.gcs.com.gcsgamaforce2018android.model;

public class UsbComPort {
    private int comPort;
    private String comPortText;

    public UsbComPort(int comPort, String comPortText) {
        this.comPort = comPort;
        this.comPortText = comPortText;
    }

    public int getComPort() {
        return comPort;
    }

    public void setComPort(int comPort) {
        this.comPort = comPort;
    }

    public String getComPortText() {
        return comPortText;
    }

    public void setComPortText(String comPortText) {
        this.comPortText = comPortText;
    }
}
