package gamaforce.gcs.com.gcsgamaforce2018android.model;

public class GcsCommand {
    private int commandToSend;
    private String commandShownToUi;

    public GcsCommand(int commandToSend, String commandShownToUi) {
        this.commandToSend = commandToSend;
        this.commandShownToUi = commandShownToUi;
    }

    public int getCommandToSend() {
        return commandToSend;
    }

    public void setCommandToSend(int commandToSend) {
        this.commandToSend = commandToSend;
    }

    public String getCommandShownToUi() {
        return commandShownToUi;
    }

    public void setCommandShownToUi(String commandShownToUi) {
        this.commandShownToUi = commandShownToUi;
    }
}
