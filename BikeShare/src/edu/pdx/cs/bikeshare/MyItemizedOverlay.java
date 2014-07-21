package edu.pdx.cs.bikeshare;

import java.io.Console;
import java.util.ArrayList;

import org.osmdroid.ResourceProxy;
import org.osmdroid.api.IMapView;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.ItemizedOverlay;
import org.osmdroid.views.overlay.OverlayItem;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.widget.Toast;
 
public class MyItemizedOverlay extends ItemizedOverlay<OverlayItem> {
  
 private ArrayList<OverlayItem> overlayItemList = new ArrayList<OverlayItem>();
 Context mContext;
 
 public MyItemizedOverlay(Drawable pDefaultMarker, ResourceProxy pResourceProxy) {
  super(pDefaultMarker, pResourceProxy);
 }
 
 public MyItemizedOverlay(Drawable pDefaultMarker,
   ResourceProxy pResourceProxy, Context context) {
   super(pDefaultMarker, pResourceProxy);
   mContext = context;
 }
  
 public void addItem(GeoPoint p, String title, String snippet){
  OverlayItem newItem = new OverlayItem(title, snippet, p);
  overlayItemList.add(newItem);
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
//	 OverlayItem item = overlayItemList.get(index);
//     AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
//     dialog.setTitle(item.getTitle());
//     dialog.setMessage(item.getSnippet());
//     dialog.show();
	 OverlayItem item = overlayItemList.get(index);
	 CharSequence text = item.getTitle();
	 int duration = Toast.LENGTH_SHORT;

	 Toast toast = Toast.makeText(mContext, text, duration);
	 toast.show();
  return true;
 }
}
