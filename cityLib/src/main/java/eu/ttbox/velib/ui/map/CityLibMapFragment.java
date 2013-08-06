package eu.ttbox.velib.ui.map;


import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.widget.ToggleButton;

import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicBoolean;

import eu.ttbox.osm.ui.map.OsmMapFragment;
import eu.ttbox.osm.ui.map.mylocation.dialog.GpsActivateAskDialog;
import eu.ttbox.velib.R;
import eu.ttbox.velib.core.AppConstants;
import eu.ttbox.velib.map.provider.VeloProviderItemizedOverlay;
import eu.ttbox.velib.map.station.StationDispoOverlay;
import eu.ttbox.velib.model.Station;
import eu.ttbox.velib.model.VelibProvider;
import eu.ttbox.velib.service.VelibService;

public abstract class CityLibMapFragment extends OsmMapFragment
        implements SharedPreferences.OnSharedPreferenceChangeListener, VelibProviderContainer {

    private static final String TAG = "CityLibMapFragment";


    private ToggleButton swtichMode;

    // Prefs
    private SharedPreferences sharedPreferences;
    private SharedPreferences privateSharedPreferences;


    // Service
    private VelibService velibService;
    private ScheduledThreadPoolExecutor timer;


    // Binding
    private Drawable packing;
    private Drawable cycle;
    private Drawable all;

    // Instance Value
    private VelibServiceConnection velibServiceConnection;
    private VelibProvider velibProvider;
    private StationDispoOverlay stationOverlay;

    // Config
    boolean askToEnableGps = true;
    private StationDispoModeSwitch stationDispoModeSwitch;


    // Manage Thread
    /**
     * * Le AtomicBoolean pour lancer et stopper la Thread :
     * http://mathias-seguy
     * .developpez.com/cours/android/handler_async_memleak/#L4-1
     */
    private AtomicBoolean isThreadRunnning = new AtomicBoolean();
    private AtomicBoolean isThreadPausing = new AtomicBoolean();


    // ===========================================================
    // Message Handler
    // ===========================================================

    // Message Handler
    protected static final int UI_MSG_ANIMATE_TO_GEOPOINT = 0;
    protected static final int UI_MSG_TOAST = 1;
    protected static final int UI_MSG_TOAST_ERROR = 2;

    private Handler uiHandler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (isThreadRunnning.get()) {
                switch (msg.what) {
                    case UI_MSG_ANIMATE_TO_GEOPOINT: {
                        GeoPoint geoPoint = (GeoPoint) msg.obj;
                        if (geoPoint != null) {
                            if (myLocation != null) {
                                myLocation.disableFollowLocation();
                            }
                            Log.d(TAG, "uiHandler mapController : animateTo " + geoPoint);
                            mapController.setCenter(geoPoint);
                            mapController.setZoom(17);
                        }
                        break;
                    }
                    case UI_MSG_TOAST: {
                        String msgToast = (String) msg.obj;
                        Toast.makeText(getActivity(), msgToast, Toast.LENGTH_SHORT).show();
                        break;
                    }
                    case UI_MSG_TOAST_ERROR: {
                        String msgToastError = (String) msg.obj;
                        Toast.makeText(getActivity(), msgToastError, Toast.LENGTH_SHORT).show();
                        break;
                    }
                }
            }
        }
    };


    // ===========================================================
    // Constructors
    // ===========================================================


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.map, container, false);
        // Map
        initMap();

        ViewGroup mapViewContainer = (ViewGroup) v.findViewById(R.id.mapViewContainer);
        mapViewContainer.addView((View) mapView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));


        // Service
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
        privateSharedPreferences = getActivity().getSharedPreferences(MapConstants.PREFS_NAME, Context.MODE_PRIVATE);
        timer = new ScheduledThreadPoolExecutor(1);

        // Binding
        // -------------
        swtichMode = (ToggleButton) v.findViewById(R.id.map_button_parking_cycle_switch);
        packing = getResources().getDrawable(R.drawable.panneau_parking);
        cycle = getResources().getDrawable(R.drawable.panneau_obligation_cycles);
        all = getResources().getDrawable(R.drawable.panneau_parking_cycle);
        stationDispoModeSwitch = new StationDispoModeSwitch();
        if (swtichMode != null) {
            stationDispoModeSwitch.displayToMode(0);
            swtichMode.setOnClickListener(stationDispoModeSwitch);
        }
        // Action bar and compatibility
        // -----------------------------
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            // No Action bar => Activate Map Button Overlay
            ToggleButton myPositionButton = (ToggleButton) v.findViewById(R.id.map_button_myposition);
            if (myPositionButton != null) {
                myPositionButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        centerOnMyLocationFix();
                    }
                });
                myPositionButton.setVisibility(View.VISIBLE);
            }
        }



        // Bind CityLib Service
        // ---------------------
        velibServiceConnection = new VelibServiceConnection();
        getActivity().bindService(new Intent(getActivity(), VelibService.class), velibServiceConnection, Context.BIND_AUTO_CREATE);

        GeoPoint lastKnownLocationAsGeoPoint = myLocation.getLastKnownLocationAsGeoPoint();
        velibProvider = computeConditionVelibProvider(lastKnownLocationAsGeoPoint);


        // Check For Gps
        // --------------
        boolean enableGPS = isGpsLocationProviderIsEnable();
        if (askToEnableGps && !enableGPS) {
            new GpsActivateAskDialog(getActivity()).show();
        }
        // Initialiser le booléen isThreadRunning
        isThreadRunnning.set(true);
        return v;
    }


    private VelibProvider computeConditionVelibProvider(GeoPoint lastKnownLocationAsGeoPoint) {
        return VelibProviderHelper.computeConditionVelibProvider(sharedPreferences, lastKnownLocationAsGeoPoint);
    }


    // ===========================================================
    // Overwide
    // ===========================================================

    public ITileSource getPreferenceMapViewTileSource() {
        return getPreferenceMapViewTileSource(privateSharedPreferences);
    }


    // ===========================================================
    // Life Cycle
    // ===========================================================

    @Override
    public void onDestroy() {
        Log.d(TAG, "### ### ### ### ### onDestroy call ### ### ### ### ###");
        // Tuer la Thread
        isThreadRunnning.set(false);
        timer.shutdownNow();
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
        // Unbind service
        getActivity().unbindService(velibServiceConnection);
        // Super
        super.onDestroy();
    }


    @Override
    public void onPause() {
        Log.d(TAG, "### ### ### ### ### onPause call ### ### ### ### ###");
        // Mettre la Thread en pause
        isThreadPausing.set(true);
        // Desactivated
        if (stationOverlay != null) {
            stationOverlay.disableTimer();
        }
        super.onPause();
        // timer.cancel();
    }


    @Override
    public void onResume() {
        // Super
        super.onResume();
        Log.d(TAG, "### ### ### ### ### onResume call ### ### ### ### ###");
        // Relancer la Thread
        isThreadPausing.set(false);

        // Enable Status
        if (stationOverlay != null) {
            stationOverlay.enableTimer();
        }
    }
    // ===========================================================
    // Listener
    // ===========================================================

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        // Log.i(TAG, "------------------------------------------------");
        // Log.i(TAG, "Preference change for key " + key);
        // Log.i(TAG, "------------------------------------------------");
        // Preference Providers
        if (AppConstants.PREFS_KEY_PROVIDER_SELECT.equals(key)) {
            String providerName = sharedPreferences.getString(AppConstants.PREFS_KEY_PROVIDER_SELECT, VelibProvider.FR_PARIS.getProviderName());
            VelibProvider newVelibProvider = VelibProvider.getVelibProvider(providerName);
            if (Log.isLoggable(TAG, Log.INFO))
                Log.i(TAG, String.format("Preference change for key %s = %s", key, newVelibProvider));
            if (velibProvider == null || !velibProvider.equals(newVelibProvider)) {
                // Center on new Velib Provider
                Log.d(TAG, "onSharedPreferenceChanged mapController : setCenter " + newVelibProvider.asGeoPoint());
                mapController.setCenter(newVelibProvider.asGeoPoint());
                if (myLocation != null) {
                    myLocation.enableFollowLocation();
                }
                // Download Stations
                DownloadVeloStationsTask downloadVeloStationsTask = new DownloadVeloStationsTask();
                downloadVeloStationsTask.execute(newVelibProvider);
            }
        }
        // Other Preference

    }

    // ===========================================================
    // Accessor
    // ===========================================================

    public VelibProvider getVelibProvider() {
        return velibProvider;
    }

    // ===========================================================
    // Service
    // ===========================================================

    private class VelibServiceBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
        }

    }

    private class VelibServiceConnection implements ServiceConnection {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            velibService = null;

        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            velibService = ((VelibService.LocalBinder) service).getService();
            // downloadVeloStationsTask = new DownloadVeloStationsTask();
            // --- Init Velib Provider List ---
            // --------------------------------
            Drawable providerOverlaytMarker = getResources().getDrawable(R.drawable.marker_velib_circle);
            VeloProviderItemizedOverlay providerOverlay = new VeloProviderItemizedOverlay(getActivity(), providerOverlaytMarker, velibService);
            mapView.getOverlays().add(providerOverlay);

            // --- Init station Dispo ---
            // ----------------------------
            if (velibProvider != null) {
                DownloadVeloStationsTask downloadVeloStationsTask = new DownloadVeloStationsTask();
                downloadVeloStationsTask.execute(velibProvider);
            }
        }
    };

    private class DownloadVeloStationsTask extends AsyncTask<VelibProvider, Void, ArrayList<Station>> {

        @Override
        protected ArrayList<Station> doInBackground(VelibProvider... params) {
            // Looper.prepare();
            // Do the Job
            VelibProvider newVelibProvider = params[0];
            if (Log.isLoggable(TAG, Log.INFO))
                Log.i(TAG, String.format("DownloadVeloStationsTask for provider %s", newVelibProvider));
            velibProvider = newVelibProvider;
            ArrayList<Station> stations = null;
            try {
                stations = velibService.getStationsByProvider(newVelibProvider);
            } catch (final Exception e) {
                String errorMsg = String.format("Error in getStationsByProvider : %s", e.getMessage());
                Log.e(TAG, errorMsg, e);
                uiHandler.sendMessage(uiHandler.obtainMessage(UI_MSG_TOAST_ERROR, errorMsg));
            }
            // TODO Toast.makeText(VelibMapActivity.this,
            // "Download Stations size " + stations.size(),
            // Toast.LENGTH_SHORT).show();
            return stations;

        }

        @Override
        protected void onPostExecute(ArrayList<Station> stations) {
            if (stations != null && !stations.isEmpty() && isThreadRunnning.get()) {
                if (Log.isLoggable(TAG, Log.INFO))
                    Log.i(TAG, String.format("DownloadVeloStationsTask result of stations count %s", stations.size()));
                int stationSize = stations.size();
                String displayStationInfos = getResources().getQuantityString(R.plurals.numberOfStationAvailable, stationSize, stationSize);
                uiHandler.sendMessage(uiHandler.obtainMessage(UI_MSG_TOAST, displayStationInfos));
                // Add Station Overlay
                if (stationOverlay != null) {
                    velibService.removeOnStationDispoUpdatedListener(stationOverlay);
                    mapView.getOverlays().remove(stationOverlay);
                }
                stationOverlay = new StationDispoOverlay(getActivity(), mapView, stations, velibService, uiHandler, timer);
                stationOverlay.enableDisplayDispoText();
                // Register listener for update
                velibService.addOnStationDispoUpdatedListener(stationOverlay);
                // Register Overlay
                mapView.getOverlays().add(stationOverlay);
            }

        }
    }

    // ===========================================================
    // UI Listener
    // ===========================================================

    private class StationDispoModeSwitch implements View.OnClickListener {
        private int status = 0;

        @Override
        public void onClick(View v) {
            if (stationOverlay != null) {
                displayToMode(status + 1);
            }
        }

        public void displayToMode(int pMode) {
            int mode = pMode % 3;// pMode > 3 ? pMode % 3 : pMode;
            switch (mode) {
                case 0:
                    swtichMode.setBackgroundDrawable(all);
                    if (stationOverlay != null) {
                        stationOverlay.setDrawDisplayCycleParking(true, true);
                    }
                    break;
                case 1:
                    swtichMode.setBackgroundDrawable(cycle);
                    if (stationOverlay != null) {
                        stationOverlay.setDrawDisplayCycleParking(true, false);
                    }
                    break;
                case 2:
                    swtichMode.setBackgroundDrawable(packing);
                    if (stationOverlay != null) {
                        stationOverlay.setDrawDisplayCycleParking(false, true);
                    }
                    break;
                default:
                    break;
            }
            this.status = mode;
        }
    }

    // ===========================================================
    // Other
    // ===========================================================

    public boolean isGpsLocationProviderIsEnable() {
        boolean result = false;
        if (myLocation != null) {
            result = myLocation.isGpsLocationProviderIsEnable();
        }
        return result;
    }

    public void centerOnMyLocationFix() {
//		mapView.getScroller().forceFinished(true);
        myLocation.enableFollowLocation();
        myLocation.runOnFirstFix(new Runnable() {

            @Override
            public void run() {
//				myLocation.animateToLastFix();
                mapController.setZoom(17);
            }
        });
    }


}
