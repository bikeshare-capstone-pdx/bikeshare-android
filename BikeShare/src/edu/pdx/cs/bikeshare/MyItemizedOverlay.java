package edu.pdx.cs.bikeshare;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
	private Thread bikeThread;
	private BikeRider bike;
	public Drawable bikeMarker;

	public static boolean haveBike = false;
	public static int checkoutStationId = 0;
	public static int checkinStationId = 0;

	public MyItemizedOverlay(Drawable pDefaultMarker, ResourceProxy pResourceProxy) {
		super(pDefaultMarker, pResourceProxy);
		mHandler = new Handler(Looper.getMainLooper()) {
			@Override
			public void handleMessage(Message inputMessage) {
				bike = (BikeRider) inputMessage.obj;
				removeBike();
				if (inputMessage.what != 1) {
					addBike(bike.getPoint(), "Bike", "Bike");
				}
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
				if (inputMessage.what != 1) {
					addBike(bike.getPoint(), "Bike", "Bike");
				}
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
				if (inputMessage.what != 1) {
					addBike(bike.getPoint(), "Bike", "Bike");
				}
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
		overlayItemList.remove(bikeOverlayItem);
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
					String station_name = jStation.getString("STATION_NAME");
					String street_address = jStation.getString("STREET_ADDRESS");
					int current_bikes = jStation.getInt("CURRENT_BIKES");
					int current_docks = jStation.getInt("CURRENT_DOCKS");
					int current_discount = jStation.getInt("CURRENT_DISCOUNT");
					final BikeInfo bikeInfo = new BikeInfo(station_id, current_bikes);
					if (!haveBike) {
						// We don't have a bike, offer to check one out.
						String checkoutMsg = "%s\n";
						checkoutMsg += "Station ID: %d\n";
						checkoutMsg += "Address: %s\n";
						checkoutMsg += "Number of bikes available: %d\n\n";
						checkoutMsg += "Check out bike?";
						checkoutStationId = station_id;
						// Display dialog box asking if the user wants to check out a bike.
						AlertDialog.Builder checkOut = new AlertDialog.Builder(mContext);
						checkOut.setMessage(String.format(checkoutMsg, station_name, station_id, street_address, current_bikes)).setTitle("Check out bike")
						.setPositiveButton(R.string.check_out, new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								new CheckoutBike().execute(checkoutStationId, UserSignUp.user_id);
								if (bikeInfo.bike_count > 0) {
									bikeThread = new Thread(new BikeRider(mContext, mHandler, bikeInfo.st_id, UserSignUp.user_id));
									bikeThread.start();
									//dialog.dismiss();
								}
								dialog.dismiss();
							}
						})
						.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								// User cancelled the dialog
							}
						});
						checkOut.show();
					} else {
						// We have a bike, offer to check it in.
						String checkinMsg = "%s\n";
						checkinMsg += "Station ID: %d\n";
						checkinMsg += "Address: %s\n";
						checkinMsg += "Number of docks available: %d\n\n";
						checkinMsg += "Check in bike?";
						checkinStationId = station_id;
						// Display dialog box asking if the user wants to check out a bike.
						AlertDialog.Builder checkIn = new AlertDialog.Builder(mContext);
						checkIn.setMessage(String.format(checkinMsg, station_name, station_id, street_address, current_docks)).setTitle("Check in bike")
						.setPositiveButton(R.string.check_in, new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								new CheckinBike().execute(checkinStationId, UserSignUp.user_id);
								bike.terminate = true;
							}
						})
						.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								// User cancelled the dialog
							}
						});
						checkIn.show();
					}
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

	// When invoked, should be passed two int arguments: first a station_id, second a user_id.
	private class CheckoutBike extends AsyncTask<Integer, Void, Integer> {
		private final static String route = "/REST/1.0/bikes/checkout";
		private final static String tag = "CheckoutBike";

		@Override
		protected Integer doInBackground(Integer... params) {
			// Call REST API to checkout a bike.
			HttpClient web = new DefaultHttpClient();
			int station_id = params[0];
			int user_id = params[1];
			int apiData = -1;
			try {
				HttpPost request = new HttpPost(apiUrl + route);
				List<NameValuePair> formData = new ArrayList<NameValuePair>(2);
				formData.add(new BasicNameValuePair("station_id", String.valueOf(station_id)));
				formData.add(new BasicNameValuePair("user_id", String.valueOf(user_id)));
				request.setEntity(new UrlEncodedFormEntity(formData, "UTF-8"));
				HttpResponse resp = web.execute(request);
				StatusLine status = resp.getStatusLine();
				apiData = status.getStatusCode();
				resp.getEntity().getContent().close();
			} catch (ClientProtocolException e) {
				// HTTP barfed.
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return apiData;
		}

		protected void onPostExecute(Integer result) {
			if (result == HttpStatus.SC_OK) {
				// REST API returned success.
				haveBike = true;
			} else if (result == HttpStatus.SC_UNAUTHORIZED) {
				// Failure (401) - User does not exist
				// TODO: Handle this better.
				AlertDialog.Builder err = new AlertDialog.Builder(mContext);
				err.setMessage("Your user ID does not exist. Please restart app.").setTitle("Error")
				.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						// Do nothing.
					}
				});
				err.show();
				Log.e(tag,"REST API returned error: User does not exist");
			} else if (result == HttpStatus.SC_FORBIDDEN) {
				// Failure (403) - User already has a bike checked out
				AlertDialog.Builder err = new AlertDialog.Builder(mContext);
				err.setMessage("You already have a bike checked out!").setTitle("Error")
				.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						// Do nothing.
					}
				});
				err.show();
				Log.e(tag,"REST API returned error. User already has a bike checked out");
				haveBike = true;
			} else if (result == HttpStatus.SC_SERVICE_UNAVAILABLE) {
				// Failure (503) - No bikes available at station
				AlertDialog.Builder err = new AlertDialog.Builder(mContext);
				err.setMessage("No bikes available at this station.").setTitle("Error")
				.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						// Do nothing.
					}
				});
				err.show();
				Log.e(tag,"REST API returned error: No bikes available at station");
			} else {
				// REST API returned an error.
				Log.e(tag,"REST API returned error: " + result.toString());
			}
			return;
		}
	}

	// When invoked, should be passed two int arguments: first a station_id, second a user_id.
	private class CheckinBike extends AsyncTask<Integer, Void, Integer> {
		private final static String route = "/REST/1.0/bikes/checkin";
		private final static String tag = "CheckinBike";

		@Override
		protected Integer doInBackground(Integer... params) {
			// Call REST API to checkout a bike.
			HttpClient web = new DefaultHttpClient();
			int station_id = params[0];
			int user_id = params[1];
			int apiData = -1;
			try {
				HttpPost request = new HttpPost(apiUrl + route);
				List<NameValuePair> formData = new ArrayList<NameValuePair>(2);
				formData.add(new BasicNameValuePair("station_id", String.valueOf(station_id)));
				formData.add(new BasicNameValuePair("user_id", String.valueOf(user_id)));
				request.setEntity(new UrlEncodedFormEntity(formData, "UTF-8"));
				HttpResponse resp = web.execute(request);
				StatusLine status = resp.getStatusLine();
				apiData = status.getStatusCode();
				resp.getEntity().getContent().close();
			} catch (ClientProtocolException e) {
				// HTTP barfed.
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return apiData;
		}

		protected void onPostExecute(Integer result) {
			if (result == HttpStatus.SC_OK) {
				// REST API returned success.
				haveBike = false;
			} else if (result == HttpStatus.SC_UNAUTHORIZED) {
				// Failure (401) - User does not exist
				// TODO: Handle this better.
				AlertDialog.Builder err = new AlertDialog.Builder(mContext);
				err.setMessage("Your user ID does not exist. Please restart app.").setTitle("Error")
				.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						// Do nothing.
					}
				});
				err.show();
				Log.e(tag,"REST API returned error: User does not exist");
			} else if (result == HttpStatus.SC_FORBIDDEN) {
				// Failure (403) - User does not have a bike to checkin
				AlertDialog.Builder err = new AlertDialog.Builder(mContext);
				err.setMessage("No bike to checkin!").setTitle("Error")
				.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						// Do nothing.
					}
				});
				err.show();
				haveBike = false;
				Log.e(tag,"REST API returned error: User does not have a bike to checkin");
			} else if (result == HttpStatus.SC_SERVICE_UNAVAILABLE) {
				// Failure (503) - No docks available at station
				AlertDialog.Builder err = new AlertDialog.Builder(mContext);
				err.setMessage("No docks available at this station.").setTitle("Error")
				.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						// Do nothing.
					}
				});
				err.show();
				Log.e(tag,"REST API returned error: No docks available at station");
			} else {
				// REST API returned an error.
				Log.e(tag,"REST API returned error: " + result.toString());
			}
			return;
		}
	}

	class BikeInfo {
		public int st_id; //Station ID for where a bike is being checked out from
		public int bike_count; //Number of bikes at that station

		BikeInfo(int st_id, int bike_count) {
			this.st_id = st_id;
			this.bike_count = bike_count;
		}
	}
}


