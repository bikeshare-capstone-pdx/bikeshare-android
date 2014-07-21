package edu.pdx.cs.bikeshare;

import java.util.ArrayList;

import org.osmdroid.ResourceProxy;
import org.osmdroid.api.IMapView;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.ItemizedOverlay;
import org.osmdroid.views.overlay.OverlayItem;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.widget.Toast;
 
public class MyItemizedOverlay extends ItemizedOverlay<OverlayItem> {
  
 private ArrayList<OverlayItem> overlayItemList = new ArrayList<OverlayItem>();
 Context mContext;
 
 public MyItemizedOverlay(Drawable pDefaultMarker, ResourceProxy pResourceProxy) {
  super(pDefaultMarker, pResourceProxy);
 }
 
 public MyItemizedOverlay(Drawable pDefaultMarker, ResourceProxy pResourceProxy, Context context) {
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
	 OverlayItem item = overlayItemList.get(index);
//     AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
//     dialog.setTitle(item.getTitle());
//     dialog.setMessage(item.getSnippet());
//     dialog.show();
	 AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
     builder.setMessage(R.string.dialog_check_out).setTitle(item.getTitle())
            .setPositiveButton(R.string.check_out, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                }
            })
            .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // User cancelled the dialog
                }
            });
     builder.show();
  return true;
 }
}
