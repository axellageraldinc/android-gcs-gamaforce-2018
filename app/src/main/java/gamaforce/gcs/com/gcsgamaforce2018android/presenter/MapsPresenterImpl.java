package gamaforce.gcs.com.gcsgamaforce2018android.presenter;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import gamaforce.gcs.com.gcsgamaforce2018android.contract.MapsContract;

public class MapsPresenterImpl implements MapsContract.Presenter {
    private static final String TAG = MapsPresenterImpl.class.getSimpleName();

    private Context context;
    private MapsContract.View mapsView;

    public MapsPresenterImpl(Context context, MapsContract.View mapsView) {
        this.context = context;
        this.mapsView = mapsView;
    }

    @Override
    public void setView(String data) {
        mapsView.setDronePositionOnGoogleMaps(
                Double.parseDouble(parseData(5, data)),
                Double.parseDouble(parseData(6, removeLastChar(data))));
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

    private String removeLastChar(String str) {
        return str.substring(0, str.length() - 1);
    }
}
