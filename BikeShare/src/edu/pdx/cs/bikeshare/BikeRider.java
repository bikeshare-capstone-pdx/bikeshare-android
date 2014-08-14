package edu.pdx.cs.bikeshare;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
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
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.util.GeoPoint;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class BikeRider implements Runnable {
	private double lat;
	private double lon;
	private GeoPoint icon;
	private Context mContext;
	private Handler mHandler;
	private int station_id;
	private final String apiUrl = MainActivity.apiUrl;
	private final String tag = "Ride Bike";
	private final String path = "/REST/1.0/bikes/pos";
	private final String signUpPath = "/REST/1.0/login/signup";
	
	public BikeRider(double lat, double lon, Context mContext) {
		this.lat = lat;
		this.lon = lon;
		this.icon = new GeoPoint(lat, lon);
		this.mContext = mContext;
	}
	
	public BikeRider(GeoPoint p, Context mContext, Handler mHandler, int station_id) {
		this.icon = p;
		this.lat = p.getLatitude();
		this.lon = p.getLongitude();
		this.mContext = mContext;
		this.mHandler = mHandler;
		this.station_id = station_id;
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
		System.out.println(station_id);
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
			else
				is = mContext.getAssets().open("routes/OHSU_South_Waterfront_to_Civic_Stadium.json");
			//InputStream is = mContext.getAssets().open("routes/OHSU_South_Waterfront_to_Civic_Stadium.json");
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
	
	public GeoPoint ride(double lat, double lon) {
		return icon = new GeoPoint(lat, lon);
	}
	
	public int testSignUp() {
		System.out.println("Starting to sign up");
		JSONObject result;
		HttpClient web = new DefaultHttpClient();
		HttpPost httppost = new HttpPost(apiUrl + signUpPath);
		List<NameValuePair> params = new ArrayList<NameValuePair>(3);
		params.add(new BasicNameValuePair("first_name", "Boba"));
		params.add(new BasicNameValuePair("last_name", "Fett"));
		try {
			httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
			HttpResponse response = web.execute(httppost);
			HttpEntity entity = response.getEntity();
			
			if (entity != null) {
				InputStream instream = entity.getContent();
				String resp = "";
				String line;
				BufferedReader rd = new BufferedReader(new InputStreamReader(instream));
				while ((line = rd.readLine()) != null) { 
			        resp += line; 
			    }
				result = new JSONObject(resp);
			    System.out.println(resp);
			    // Return full string
			    return result.getInt("USER_ID");
			} else {
				System.out.println("Didn't get anything back");
				return -1;
			}
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return -1;
	}
	
	public int testCheckout(int user_id) {
		String rider_id = user_id + "";
		HttpClient web = new DefaultHttpClient();
		HttpPost httppost = new HttpPost(apiUrl + path);
		System.out.println(apiUrl + path);
		List<NameValuePair> params = new ArrayList<NameValuePair>(3);
		params.add(new BasicNameValuePair("station_id", "5"));
		params.add(new BasicNameValuePair("user_id", rider_id));
		try {
			httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));

			HttpResponse response = web.execute(httppost);
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				StatusLine status = response.getStatusLine();
				System.out.println("Code " + status.getStatusCode());
			}
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return -1;
	}
	
	public String sendPoints(int user_id, double lat, double lon) {
		String rider_id = user_id + ""; //This feels so dumb
		String latitude = lat + "";
		String longitude = lon + "";
		HttpClient web = new DefaultHttpClient();
		HttpPost httppost = new HttpPost(apiUrl + path);
		System.out.println(apiUrl + path);
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
				System.out.println("Code " + status.getStatusCode());
			}
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
	@Override
	public void run() {
		android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
		synchronized (this) {
			JSONArray route = this.bikeRoute();
			int rider_id = testSignUp();
			//Handler mHandler = mio.getHandler();
			for (int i = 0; i < route.length() - 1; ++i) {
				try {
					lat = route.getJSONArray(i).getDouble(1);
					lon = route.getJSONArray(i).getDouble(0);
					System.out.println(rider_id + "lat = " + lat + "lon = " + lon);
					sendPoints(8480, lat, lon);
					icon = new GeoPoint(route.getJSONArray(i).getDouble(1), route.getJSONArray(i).getDouble(0));
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
			icon = null;
		}
	}
}