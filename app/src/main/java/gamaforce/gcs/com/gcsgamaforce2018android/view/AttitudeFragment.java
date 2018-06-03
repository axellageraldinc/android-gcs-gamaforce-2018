package gamaforce.gcs.com.gcsgamaforce2018android.view;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import gamaforce.gcs.com.gcsgamaforce2018android.R;


public class AttitudeFragment extends Fragment { ;
    View view;
    public AttitudeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        System.out.println("Inside on Create View Attitude Fragment");
        view = inflater.inflate(R.layout.fragment_attitude, container, false);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        System.out.println("Inside on Resume Attitude Fragment");
    }

}
