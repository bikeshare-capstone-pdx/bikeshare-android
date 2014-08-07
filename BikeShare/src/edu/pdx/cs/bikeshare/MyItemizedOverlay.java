package edu.pdx.cs.bikeshare;

import java.util.ArrayList;

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
 
public class MyItemizedOverlay extends ItemizedOverlay<OverlayItem> {
      
	private ArrayList<OverlayItem> overlayItemList = new ArrayList<OverlayItem>();
	private OverlayItem bikeOverlayItem;
	private Context mContext;
	private MapView mMapView;
	private Handler mHandler;
	private BikeRider bike;
 
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
	}
  
	public void addItem(GeoPoint p, String title, String snippet){
		OverlayItem newItem = new OverlayItem(title, snippet, p);
		overlayItemList.add(newItem);
		populate();
	}
	
	public void addBike(GeoPoint p, String title, String snippet) {
		bikeOverlayItem = new OverlayItem(title, snippet, p);
		overlayItemList.add(bikeOverlayItem);
		populate();
	}
	
	public void removeBike() {
		if (!overlayItemList.remove(bikeOverlayItem))
			System.out.println("Couldn't find it");
		populate();
		mMapView.invalidate();
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
		AlertDialog.Builder checkOut = new AlertDialog.Builder(mContext);
        		
		checkOut.setMessage(R.string.dialog_station_id).setTitle(item.getTitle())
            .setPositiveButton(R.string.check_out, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                	GeoPoint p = new GeoPoint(45.4995785100733, -122.670543465424);
                	/*Runnable runnable =  new Runnable() {
                		public void run() {
                			synchronized (this) {
                				try {
                					wait(5000);
                					System.out.println("Done waiting");
                				} catch (InterruptedException e) {
                					// TODO Auto-generated catch block
                					e.printStackTrace();
                				}
                			}
                		}
                	};*/
                	Thread th = new Thread(new BikeRider(p, mContext, mHandler));
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
    return true;
	}
	
	public void updateRide() {
		GeoPoint p = new GeoPoint(45.4995785100733, -122.670543465424);
    	//bike = new BikeRider(p, mContext, this);
    	Thread loop = new Thread(bike);
    	loop.start();
    	//mHandler.post(bike);
	}
}
