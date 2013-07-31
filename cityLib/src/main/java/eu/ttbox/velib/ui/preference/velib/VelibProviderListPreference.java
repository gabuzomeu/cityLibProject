package eu.ttbox.velib.ui.preference.velib;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.preference.ListPreference;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;

import eu.ttbox.velib.R;
import eu.ttbox.velib.model.VelibProvider;

/**
 * http://blog.isys-labs.com/creating-a-custom-listpreference/
 */
public class VelibProviderListPreference extends ListPreference {

    Context context;

    public VelibProviderListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        initVelibProvidersListValues();
    }


    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {

        int mClickedDialogEntryIndex  =findIndexOfValue(getValue());

        ListAdapter listAdapter =  new VelibProviderArrayAdapter(getContext(),
                R.layout.pref_velibprovider_list_row, this.getEntryValues() ,mClickedDialogEntryIndex, this);
        builder.setAdapter(listAdapter, this);

        super.onPrepareDialogBuilder(builder);

    }

    private void initVelibProvidersListValues() {
        ListPreference provilderList = this;
        VelibProvider[] velibs = VelibProvider.values();
        String[] entries = new String[velibs.length];
        String[] entryValues = new String[velibs.length];
        for (int e = velibs.length - 1; e >= 0; e--) {
            VelibProvider velib = velibs[e];
            entries[e] = velib.getName();
            entryValues[e] = velib.getProviderName();
        }
        provilderList.setEntries(entries);
        provilderList.setEntryValues(entryValues);
    }

}
