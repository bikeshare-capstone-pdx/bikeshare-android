package edu.pdx.cs.bikeshare;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.osmdroid.util.GeoPoint;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class BikeRider implements Runnable {
	private double lat;
	private double lon;
	private GeoPoint point;
	private Context mContext;
	private Handler mHandler;
	private int station_id;
	private int user_id;
	private final String apiUrl = MainActivity.apiUrl;
	private final String tag = "Ride Bike";
	private final String path = "/REST/1.0/bikes/pos";
	
	public BikeRider(Context mContext, Handler mHandler, int station_id, int user_id) {
		this.mContext = mContext;
		this.mHandler = mHandler;
		this.station_id = station_id;
		this.user_id = user_id;
	}
	
	public GeoPoint getPoint() {
		return point;
	}
	
	public double getLatitude() {
		return lat;
	}
	
	public double getLongitude() {
		return lon;
	}
	
	public void setPoint(GeoPoint p) {
		point = p;
	}
	
	public JSONArray bikeRoute() {
		String jsonFile = null;
		try {
			InputStream is;
			if (station_id == 0)
				is = mContext.getAssets().open("routes/OHSU_South_Waterfront_to_Civic_Stadium.json");
			else if (station_id == 1)
				is = mContext.getAssets().open("routes/Waterfront_Park_to_Portland_State_University.json");
			else if (station_id == 2)
				is = mContext.getAssets().open("routes/Eastbank_Esplanade_to_Portland_State_University.json");
			else if (station_id == 3)
				is = mContext.getAssets().open("routes/Moda_Center_to_Portland_State_University.json");
			else if (station_id == 4)
				is = mContext.getAssets().open("routes/PSU_to_Moda_Center.json");
			else if (station_id == 5)
				is = mContext.getAssets().open("routes/Overlook_Park_to_Portland_State_University.json");
			else if (station_id == 6)
				is = mContext.getAssets().open("routes/Civic_Stadium_to_Portland_State_University.json");
			else //If there's an unaccounted for station, we're just gonna pretend they were at ohsu
				is = mContext.getAssets().open("routes/OHSU_South_Waterfront_to_Civic_Stadium.json");
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
	
	public void sendPoints(int user_id, double lat, double lon) {
		String rider_id = user_id + "";
		String latitude = lat + "";
		String longitude = lon + "";
		HttpClient web = new DefaultHttpClient();
		HttpPost httppost = new HttpPost(apiUrl + path);
		List<NameValuePair> params = new ArrayList<NameValuePair>(3);
		params.add(new BasicNameValuePair("user_id", rider_id));
		params.add(new BasicNameValuePair("lat", latitude));
		params.add(new BasicNameValuePair("lon", longitude));
		try {
			httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
			HttpResponse response = web.execute(httppost);
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				StatusLine status = response.getStatusLine();
				if (status.getStatusCode() == HttpStatus.SC_OK) {
					// REST API returned success.
					ByteArrayOutputStream out = new ByteArrayOutputStream();
					response.getEntity().writeTo(out);
					out.close();
				} else {
					// REST API returned an error.
					response.getEntity().getContent().close();
					Log.e(tag,"REST API returned error: " + status.getReasonPhrase());
				}
			}
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
		synchronized (this) {
			JSONArray route = this.bikeRoute();
			for (int i = 0; i < route.length() - 1; ++i) {
				try {
					lat = route.getJSONArray(i).getDouble(1);
					lon = route.getJSONArray(i).getDouble(0);
					sendPoints(user_id, lat, lon);
					point = new GeoPoint(lat, lon);
					Message msg = mHandler.obtainMessage(0, this);
					mHandler.sendMessage(msg);
					wait(1000);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			point = null;
		}
	}
}