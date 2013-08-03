package eu.ttbox.velib.service.ws.direction.model;

import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;

public class GoogleDirectionRoute {

	public ArrayList<GoogleDirectionLeg> legs = new ArrayList<GoogleDirectionLeg>();

	public String summary;

	public String copyrights;

	public ArrayList<GeoPoint> polyline;

	public void addLegs(GoogleDirectionLeg leg) {
		legs.add(leg);
	}

}
