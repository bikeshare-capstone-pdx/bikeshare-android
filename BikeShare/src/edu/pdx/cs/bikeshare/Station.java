package edu.pdx.cs.bikeshare;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.OverlayItem;

public class Station extends OverlayItem {
    public Station(String aTitle, String aSnippet, GeoPoint aGeoPoint) {
		super(aTitle, aSnippet, aGeoPoint);
		// TODO Auto-generated constructor stub
	}
	public int station_id;
    public String station_name;
    public String street_address;
    public double latitude;
    public double longitude;
    public int current_bikes;
    public int current_discount;
}
