package eu.ttbox.velib.service.provider;

import android.content.Context;

import eu.ttbox.velib.service.download.VeloServiceParser;
import eu.ttbox.velib.service.download.bixi.BixiServiceParser;
import eu.ttbox.velib.service.download.cyclocity.CyclocityServiceParser;
import eu.ttbox.velib.service.download.strasbourg.VelhopServiceParser;

public enum VelibServiceProviderAdpater {

	CycloCity("http://%s/service/carto", "http://%s/service/stationdetails/%s") {
		@Override
		public VeloServiceParser createVeloServiceParser(Context context) {
			return new CyclocityServiceParser();
		}
	},

	Bixi("http://%s/data/bikeStations.xml", null) {
		@Override
		public VeloServiceParser createVeloServiceParser(Context context) {
			return new BixiServiceParser();
		}
	},

    Velhop(null, null) {
        @Override
        public VeloServiceParser createVeloServiceParser(Context context) {
            return new VelhopServiceParser(context);
        }
    },

	VeloBleu("http://%s/oybike/stands.nsf/getsite?openagent&site=nice&format=json&key=diolev", "http://%s/service/stationdetails/%s") {
		@Override
		public VeloServiceParser createVeloServiceParser(Context context) {
			return new CyclocityServiceParser();
		}
	};

	private final String serviceCartoUrlPattern;
	private final String serviceDispoUrlPattern;

	private VeloServiceParser veloServiceParser;

	VelibServiceProviderAdpater(String serviceCartoUrlPattern, String serviceDispoUrlPattern) {
		this.serviceCartoUrlPattern = serviceCartoUrlPattern;
		this.serviceDispoUrlPattern = serviceDispoUrlPattern;
	}

	// public DefaultHandler getHandler(int provider) {
	// throw new RuntimeException("Not implemented XML Service Handler");
	// }

	public String getUrlCartoWithHostname(String hostName) {
		String url = String.format(serviceCartoUrlPattern, hostName);
		return url;
	}

	public String getUrlCartoWithUrl(String urlDispoUrl) {
		return urlDispoUrl;
	}

	public String getUrlDipsoWithHostname(String hostName, String stationId) {
		String url = String.format(serviceDispoUrlPattern, hostName, stationId);
		return url;
	}

	public String getUrlDipsoWithUrlPattern(String urlDispoUrlPattern, String stationId) {
		String url = String.format(urlDispoUrlPattern, stationId);
		return url;
	}

	public VeloServiceParser getVeloServiceParser(Context context) {
		if (veloServiceParser == null) {
			veloServiceParser = createVeloServiceParser(  context);
		}
		return veloServiceParser;
	}

	protected VeloServiceParser createVeloServiceParser(Context context) {
		throw new RuntimeException("Not implemented");
	}
}
