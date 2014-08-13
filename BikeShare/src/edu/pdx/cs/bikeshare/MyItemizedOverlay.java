package edu.pdx.cs.bikeshare;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.ResourceProxy;
import org.osmdroid.api.IMapView;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedOverlay;
import org.osmdroid.views.overlay.OverlayItem;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.AsyncTask;
import android.util.Log;
 
public class MyItemizedOverlay extends ItemizedOverlay<OverlayItem> {
      
	private static final String apiUrl = MainActivity.apiUrl;
	private ArrayList<OverlayItem> overlayItemList = new ArrayList<OverlayItem>();
	private OverlayItem bikeOverlayItem;
	private Context mContext;
	private MapView mMapView;
	private Handler mHandler;
	private BikeRider bike;
	public Drawable bikeMarker;
 
	public MyItemizedOverlay(Drawable pDefaultMarker, ResourceProxy pResourceProxy) {
		super(pDefaultMarker, pResourceProxy);
		mHandler = new Handler(Looper.getMainLooper()) {
			@Override
			public void handleMessage(Message inputMessage) {
				bike = (BikeRider) inputMessage.obj;
				removeBike();
				addBike(bike.getPoint(), "Bike", "Bike");
			}
		};
	}
	
	public Handler getHandler() {
		return mHandler;
	}
 
	public MyItemizedOverlay(Drawable pDefaultMarker, ResourceProxy pResourceProxy, Context context) {
		super(pDefaultMarker, pResourceProxy);
		mContext = context;
		mHandler = new Handler(Looper.getMainLooper()) {
			@Override
			public void handleMessage(Message inputMessage) {
				bike = (BikeRider) inputMessage.obj;
				removeBike();
				addBike(bike.getPoint(), "Bike", "Bike");
			}
		};
	}
	
	public MyItemizedOverlay(Drawable pDefaultMarker, ResourceProxy pResourceProxy, Context context, MapView mapView) {
		super(pDefaultMarker, pResourceProxy);
		mContext = context;
		mMapView = mapView;
		mHandler = new Handler(Looper.getMainLooper()) {
			@Override
			public void handleMessage(Message inputMessage) {
				bike = (BikeRider) inputMessage.obj;
				//System.out.println(bike.getPoint().toString());
				removeBike();
				addBike(bike.getPoint(), "Bike", "Bike");
			}
		};
		
		bikeMarker = mapView.getResources().getDrawable(R.drawable.ic_launcher);
        int bikeMarkerWidth = bikeMarker.getIntrinsicWidth();
        int bikeMarkerHeight = bikeMarker.getIntrinsicHeight();
        bikeMarker.setBounds(0, bikeMarkerHeight, bikeMarkerWidth, 0);
	}
  
	public void addItem(GeoPoint p, String title, String snippet){
		OverlayItem newItem = new OverlayItem(title, snippet, p);
		overlayItemList.add(newItem);
		populate();
	}
	
	public void addBike(GeoPoint p, String title, String snippet) {
		bikeOverlayItem = new OverlayItem(title, snippet, p);
		bikeOverlayItem.setMarker(bikeMarker);
		overlayItemList.add(bikeOverlayItem);
		populate();
	}
	
	public void removeBike() {
		if (!overlayItemList.remove(bikeOverlayItem))
			System.out.println("Couldn't find it");
		populate();
		mMapView.invalidate();
	}
 
	public void addItem(Station s){
		overlayItemList.add(s);
		populate();
	}
	
	@Override
	public boolean onSnapToItem(int arg0, int arg1, Point arg2, IMapView arg3) {
		return false;
	}
 
	@Override
	protected OverlayItem createItem(int arg0) {
		return overlayItemList.get(arg0);
	}
 
	@Override
	public int size() {
		return overlayItemList.size();
	}
	@Override
	protected boolean onTap(int index) {
		OverlayItem item = overlayItemList.get(index);
		Station s = (Station)item;
		new ShowStationInfo().execute(s.station_id);
		return true;
	}
	
	private class ShowStationInfo extends AsyncTask<Integer, Void, String> {
		private final static String route = "/REST/1.0/stations/info/%d";
		private final static String tag = "ShowAllStations";

		@Override
		protected String doInBackground(Integer... params) {
			// Call REST API to find BikeShare station info.
			HttpClient web = new DefaultHttpClient();
			String apiData = null;
			try {
				HttpResponse resp = web.execute(new HttpGet(apiUrl + String.format(route, params[0])));
				StatusLine status = resp.getStatusLine();
				if (status.getStatusCode() == HttpStatus.SC_OK) {
					// REST API returned success.
					ByteArrayOutputStream out = new ByteArrayOutputStream();
					resp.getEntity().writeTo(out);
					out.close();
					apiData = out.toString();
				} else {
					// REST API returned an error.
					resp.getEntity().getContent().close();
					Log.e(tag,"REST API returned error: " + status.getReasonPhrase());
				}
			} catch (ClientProtocolException e) {
				// HTTP barfed.
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return apiData;
		}

		@Override
		protected void onPostExecute(String result) {
			// If we received data from the API, parse the JSON.
			if (result != null) {
				try {
					JSONObject jStation = new JSONObject(result);
					// Construct a station object from the data.
					int station_id = jStation.getInt("STATION_ID");
					final int st_id = station_id;
					String station_name = jStation.getString("STATION_NAME");
					String street_address = jStation.getString("STREET_ADDRESS");
					int current_bikes = jStation.getInt("CURRENT_BIKES");
					int current_docks = jStation.getInt("CURRENT_DOCKS");
					int current_discount = jStation.getInt("CURRENT_DISCOUNT");
					String checkoutMsg = "Station ID: %d\n";
					checkoutMsg += "Address: %s\n";
					checkoutMsg += "Number of bikes available: %d\n";
					checkoutMsg += "Check out bike?";
					// Display dialog box asking if the user wants to check out a bike.
					AlertDialog.Builder checkOut = new AlertDialog.Builder(mContext);
					checkOut.setMessage(String.format(checkoutMsg, station_id, street_address, current_bikes)).setTitle(station_name)
					.setPositiveButton(R.string.check_out, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							GeoPoint p = new GeoPoint(45.4995785100733, -122.670543465424);
							Thread th = new Thread(new BikeRider(p, mContext, mHandler, st_id));
							th.start();
							dialog.dismiss();
						}
					})
					.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							// User cancelled the dialog
						}
					});
					checkOut.show();
					// Update the data structure containing the map pins with the additional API info.
					for (OverlayItem item : overlayItemList) {
						if (item instanceof Station) {
							Station s = (Station)item;
							if (s.station_id == station_id) {
								s.current_bikes = current_bikes;
								s.current_docks = current_docks;
								s.current_discount = current_discount;
								break;
							}
						}
					}
				} catch (JSONException e) {
					// Failed to parse the JSON.
					e.printStackTrace();
				}
			} else {
				// Failed to get data from the API.
			}
		}
	}
}


