package eu.ttbox.velib.ui.preference.velib;


import eu.ttbox.velib.core.AppConstants;
import eu.ttbox.velib.model.VelibProvider;

public class VelibProviderLastUpdateHelper {


    public static String getProviderStationsLastUpdateKey(VelibProvider velibProvider) {
        String key = new StringBuilder(AppConstants.PREFS_KEY_PROVIDER_LAST_UPDATE_BASE).append(velibProvider.name()).toString();
        return key;
    }


}
