package eu.ttbox.velib.service.ws.direction.model;

import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;

public class GoogleDirectionStep {

	public double[] latLngStart;
	public double[] latLngEnd;
	public String htmlInstructions;
	public long distanceInM;
	public String distanceText;
	public long durationInS;
	public String durationText;
	public ArrayList<GeoPoint> polyline;

}
