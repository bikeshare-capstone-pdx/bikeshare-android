package edu.pdx.cs.bikeshare;

import java.io.IOException;
import java.io.InputStream;

import org.json.JSONArray;
import org.json.JSONException;
import org.osmdroid.util.GeoPoint;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

public class BikeRider implements Runnable {
	private double lat;
	private double lon;
	private GeoPoint icon;
	private Context mContext;
	//private MyItemizedOverlay mio;
	private Handler mHandler;
	
	public BikeRider(double lat, double lon, Context mContext) {
		this.lat = lat;
		this.lon = lon;
		this.icon = new GeoPoint(lat, lon);
		this.mContext = mContext;
	}
	
	public BikeRider(GeoPoint p, Context mContext, Handler mHandler) {
		this.icon = p;
		this.lat = p.getLatitude();
		this.lon = p.getLongitude();
		this.mContext = mContext;
		this.mHandler = mHandler;
	}
	
	public GeoPoint getPoint() {
		return icon;
	}
	
	public double getLatitude() {
		return lat;
	}
	
	public double getLongitude() {
		return lon;
	}
	
	public void setPoint(GeoPoint p) {
		icon = p;
	}
	
	public JSONArray bikeRoute() {
		String jsonFile = null;
		try {
			InputStream is = mContext.getAssets().open("routes/OHSU_South_Waterfront_to_Civic_Stadium.json");
			int size = is.available();
			byte [] buffer = new byte[size];
			is.read(buffer);
			jsonFile = new String(buffer, "UTF-8");
			JSONArray coord = new JSONArray(jsonFile);
			return coord;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	/*public void ride() {
		String jsonFile = null;
		try {
			InputStream is = mContext.getAssets().open("/BikeShare/res/routes/OHSU_South_Waterfront_to_Civic_Stadium.json");
			int size = is.available();
			byte [] buffer = new byte[size];
			is.read(buffer);
			jsonFile = new String(buffer, "UTF-8");
			JSONArray coord = new JSONArray(jsonFile);
			for (int i = 0; i < coord.length(); ++i) {
				icon = new GeoPoint(coord.getJSONArray(i).getDouble(0), coord.getJSONArray(i).getDouble(1));
				mio.removeBike();
				mio.addBike(icon, "Bike", "Bike");
				Thread.sleep(3000);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}*/
	
	public GeoPoint ride(double lat, double lon) {
		return icon = new GeoPoint(lat, lon);
	}
	
	@Override
	public void run() {
		android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
		synchronized (this) {
			JSONArray route = this.bikeRoute();
			//Handler mHandler = mio.getHandler();
			for (int i = 0; i < route.length() - 1; ++i) {
				try {
					icon = new GeoPoint(route.getJSONArray(i).getDouble(1), route.getJSONArray(i).getDouble(0));
					//mio.removeBike();
					Message msg = mHandler.obtainMessage(0, this);
					mHandler.sendMessage(msg);
					//System.out.println("Sent Message");
					wait(1000);
					//mio.addBike(icon, "Bike", "Bike");
					//for (int j = 0; j < 10000000; j++) {}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			icon = null;
		}
	}
}