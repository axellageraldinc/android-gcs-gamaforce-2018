package gamaforce.gcs.com.gcsgamaforce2018android.presenter;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import gamaforce.gcs.com.gcsgamaforce2018android.contract.AttitudeContract;

public class AttitudePresenterImpl implements AttitudeContract.Presenter {
    private final static String TAG = AttitudePresenterImpl.class.getSimpleName();

    private Context context;
    private AttitudeContract.View attitudeView;

    public AttitudePresenterImpl(Context context, AttitudeContract.View attitudeView) {
        this.context = context;
        this.attitudeView = attitudeView;
    }

    @Override
    public void setView(String data) {
        attitudeView.showAltitude(Double.parseDouble(parseData(1, data)));
        attitudeView.showYaw(Double.parseDouble(parseData(2, data)));
        attitudeView.showPitch(Double.parseDouble(parseData(3, data)));
        attitudeView.showRoll(Double.parseDouble(parseData(4, data)));
        attitudeView.setAttitudeIndicator(
                Double.parseDouble(parseData(3, data)),
                Double.parseDouble(parseData(4, data)));
    }

    private String parseData(int index, String data){
        try {
            String[] dataSplit = data.split("#");
            Log.d(TAG, dataSplit[index]);
            return dataSplit[index];
        } catch (Exception ex){
            Toast.makeText(context, "Incorrect data format...\n" + data, Toast.LENGTH_SHORT).show();
            return null;
        }
    }
}
