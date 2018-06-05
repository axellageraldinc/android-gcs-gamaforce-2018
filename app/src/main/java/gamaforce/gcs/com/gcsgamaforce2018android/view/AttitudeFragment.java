package gamaforce.gcs.com.gcsgamaforce2018android.view;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import gamaforce.gcs.com.gcsgamaforce2018android.R;
import gamaforce.gcs.com.gcsgamaforce2018android.contract.AttitudeContract;
import gamaforce.gcs.com.gcsgamaforce2018android.contract.MainContract;
import gamaforce.gcs.com.gcsgamaforce2018android.presenter.AttitudePresenterImpl;
import gamaforce.gcs.com.gcsgamaforce2018android.presenter.MainPresenterImpl;


public class AttitudeFragment extends Fragment implements AttitudeContract.View {

    View view;

    private AttitudeIndicator attitudeIndicator;
    private TextView txtAltitude, txtYaw, txtPitch, txtRoll;

    public AttitudeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_attitude, container, false);
        attitudeIndicator = new AttitudeIndicator(getActivity());
        txtAltitude = view.findViewById(R.id.txtAltitude);
        txtYaw = view.findViewById(R.id.txtYaw);
        txtPitch = view.findViewById(R.id.txtPitch);
        txtRoll = view.findViewById(R.id.txtRoll);

        return view;
    }

    @Override
    public void setAttitudeIndicator(double pitch, double roll) {
        attitudeIndicator.setAttitude(Float.parseFloat(String.valueOf(pitch)), Float.parseFloat(String.valueOf(roll)));
    }

    @Override
    public void showAltitude(double altitude) {
        //TODO : Implement here...
        txtAltitude.setText(String.valueOf(altitude));
    }

    @Override
    public void showYaw(double yaw) {
        //TODO : Implement here...
        txtYaw.setText(String.valueOf(yaw));
    }

    @Override
    public void showPitch(double pitch) {
        //TODO : Implement here...
        txtPitch.setText(String.valueOf(pitch));
    }

    @Override
    public void showRoll(double roll) {
        //TODO : Implement here...
        txtRoll.setText(String.valueOf(roll));
    }
}
