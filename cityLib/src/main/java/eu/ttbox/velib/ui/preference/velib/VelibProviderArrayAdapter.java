package eu.ttbox.velib.ui.preference.velib;


import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import eu.ttbox.velib.R;
import eu.ttbox.velib.model.Station;
import eu.ttbox.velib.model.VelibProvider;
import eu.ttbox.velib.service.VelibService;

public class VelibProviderArrayAdapter extends ArrayAdapter<CharSequence> implements View.OnClickListener {

    private int index;
    private int textViewResourceId;
    private Context mContext;

    VelibProviderListPreference prefSrc;

    // Service
    private SharedPreferences prefs;
    private LayoutInflater mInflater;


    public VelibProviderArrayAdapter(Context context, int textViewResourceId, CharSequence[] objects, int selected
            , VelibProviderListPreference prefSrc)  {
        super(context, textViewResourceId, objects);
        this.mContext = context;
        this.index = selected;
        this.textViewResourceId = textViewResourceId;
        this.prefSrc  = prefSrc;

        // Service
        this.mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.prefs = PreferenceManager.getDefaultSharedPreferences(context);
    }



    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v;
        if (convertView == null) {
            v = newView(position, parent);
        } else {
            v = convertView;
        }
        bindView(v, position);
        return v;
    }

    public void bindView(final View row, int position) {
        ViewHolder holder = (ViewHolder) row.getTag();
        //get themeId
        String providerKey = this.getItem(position).toString();
         final VelibProvider velibProvider = VelibProvider.getVelibProvider(providerKey);
        row.setId(velibProvider.getProvider());

        //set name
        holder.tv.setText(velibProvider.getName());

        //set checkbox
        boolean isSelected = (position == index);
        holder.tb.setChecked(isSelected);
        holder.tb.setClickable(false);

        // Last Update time
        String updateKey = VelibProviderLastUpdateHelper.getProviderStationsLastUpdateKey(velibProvider);
        long lastUpdate = prefs.getLong(updateKey, Long.MIN_VALUE);
        CharSequence lastUpdateTime = null;
        if (lastUpdate > Long.MIN_VALUE) {
            lastUpdateTime = DateUtils.getRelativeDateTimeString(mContext, lastUpdate, DateUtils.MINUTE_IN_MILLIS, DateUtils.MINUTE_IN_MILLIS,
                    DateUtils.FORMAT_ABBREV_RELATIVE );
        }
        holder.lastUpdateTime.setText(lastUpdateTime);

        // Update Button
        holder.updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
           //      ArrayList<Station> stations = velibService.getStationsByProviderWithCheckUpdateDate(velibProvider, true);
           //     Toast.makeText(mContext, "Download Stations size " + stations.size(), Toast.LENGTH_SHORT).show();
           //     row.postInvalidate();
            }
        });

    }

    public View newView(int position, ViewGroup parent) {

        View view = mInflater.inflate(textViewResourceId, parent, false);
        view.setFocusable(false);
        view.setFocusableInTouchMode(false);

        // Then populate the ViewHolder
        ViewHolder holder = new ViewHolder();
        holder.tv = (TextView) view.findViewById(R.id.themename);
        holder.lastUpdateTime =(TextView) view.findViewById(R.id.prefs_velibprovider_lastupdatetime);

        holder.tb = (RadioButton) view.findViewById(R.id.ckbox);
        holder.tb.setFocusable(false);
        holder.tb.setFocusableInTouchMode(false);

        holder.updateButton = (Button) view.findViewById(R.id.pref_velibprovider_update_button);

        // and store it inside the layout.
        view.setTag(holder);
        // Listener
        view.setOnClickListener(this);
        return view;

    }

    static class ViewHolder {
        TextView tv;
        TextView lastUpdateTime;
        RadioButton tb;
        Button updateButton;
    }


     @Override
    public void onClick(View v) {
        int clicked = v.getId();
        VelibProvider provider = VelibProvider.getVelibProvider(clicked);
        this.prefSrc.setOnVelibProviderClick(clicked, provider);

     }

}
