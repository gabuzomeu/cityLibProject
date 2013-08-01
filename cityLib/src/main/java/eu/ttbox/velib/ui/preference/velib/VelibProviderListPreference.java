package eu.ttbox.velib.ui.preference.velib;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.preference.ListPreference;
import android.util.AttributeSet;
import android.widget.ListAdapter;
import android.widget.Toast;

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

        int mClickedDialogEntryIndex = findIndexOfValue(getValue());

        ListAdapter listAdapter = new VelibProviderArrayAdapter(getContext(),
                R.layout.pref_velibprovider_list_row, this.getEntryValues(), mClickedDialogEntryIndex, this);
        builder.setAdapter(listAdapter, this);

        builder.setPositiveButton(null, null);
        //  super.onPrepareDialogBuilder(builder);

    }

    public void setOnVelibProviderClick(int clicked, VelibProvider provider) {
        Dialog dialog = getDialog();
        if (this.callChangeListener("" + clicked)) {
            Toast.makeText(context, "click on " + provider.getProviderName() + " shoudPerist : " + shouldPersist(), Toast.LENGTH_SHORT).show();
            // Close Dialog
            setValue(provider.getProviderName());
            onClick(dialog, DialogInterface.BUTTON_POSITIVE);
            onDialogClosed(true);
        }
        dialog.dismiss();
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
