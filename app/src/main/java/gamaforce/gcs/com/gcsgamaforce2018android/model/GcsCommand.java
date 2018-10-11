package gamaforce.gcs.com.gcsgamaforce2018android.model;

public class GcsCommand {
    private String commandToSend;
    private String commandShownToUi;

    public GcsCommand(String commandToSend, String commandShownToUi) {
        this.commandToSend = commandToSend;
        this.commandShownToUi = commandShownToUi;
    }

    public String getCommandToSend() {
        return commandToSend;
    }

    public void setCommandToSend(String commandToSend) {
        this.commandToSend = commandToSend;
    }

    public String getCommandShownToUi() {
        return commandShownToUi;
    }

    public void setCommandShownToUi(String commandShownToUi) {
        this.commandShownToUi = commandShownToUi;
    }
}
