package eu.ttbox.velib.ui.help;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import eu.ttbox.velib.R;

public class HelpCircleFragment extends Fragment{

    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.help_circle, container, false);
        return v;
    }
    
}
