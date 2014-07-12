package edu.pdx.cs.bikeshare;


import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.ResourceProxy;
import org.osmdroid.api.IMapController;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import android.os.Bundle;
import android.graphics.drawable.Drawable;
import edu.pdx.cs.bikeshare.R;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class MainActivity extends ActionBarActivity {
	MyItemizedOverlay myItemizedOverlay = null;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MapView mapView = (MapView) findViewById(R.id.mapview);
        mapView.setBuiltInZoomControls(true);
        
        mapView.setMinZoomLevel(11);
        mapView.setMaxZoomLevel(19);
        IMapController myMapController = mapView.getController();
        myMapController.setZoom(13);
        myMapController.setCenter(new GeoPoint(45.55, -122.70));
         
        Drawable marker=getResources().getDrawable(R.drawable.ic_location_marker);
        int markerWidth = marker.getIntrinsicWidth();
        int markerHeight = marker.getIntrinsicHeight();
        marker.setBounds(0, markerHeight, markerWidth, 0);
         
        ResourceProxy resourceProxy = new DefaultResourceProxyImpl(getApplicationContext());
        
        myItemizedOverlay = new MyItemizedOverlay(marker, resourceProxy);
        mapView.getOverlays().add(myItemizedOverlay);
        
        GeoPoint OHSUSouthWaterfront = new GeoPoint(45.4992785100733, -122.670743465424);
        myItemizedOverlay.addItem(OHSUSouthWaterfront, "OHSU South Waterfront", "OHSU South Waterfront");
        
        GeoPoint waterfrontPark = new GeoPoint(45.5153465357174, -122.673382759094);
        myItemizedOverlay.addItem(waterfrontPark, "Waterfront Park", "Waterfront Park");
        
        GeoPoint eastbankEsplanade = new GeoPoint(45.5182333316815, -122.66716003418);
        myItemizedOverlay.addItem(eastbankEsplanade, "Eastbank Esplanade", "Eastbank Esplanade");
        
        GeoPoint modaCenter = new GeoPoint(45.5309439966742, -122.667524814606);
        myItemizedOverlay.addItem(modaCenter, "Moda Center", "Moda Center");
        
        GeoPoint portlandStateUniversity = new GeoPoint(45.5093168644112, -122.681311368942);
        myItemizedOverlay.addItem(portlandStateUniversity, "Portland State University", "Portland State University");
        
        GeoPoint overlookPark  = new GeoPoint(45.5491969282445, -122.681010961533);
        myItemizedOverlay.addItem(overlookPark, "Overlook Park", "Overlook Park");
        
        GeoPoint civicStadium  = new GeoPoint(45.5220708871078, -122.690554261208);
        myItemizedOverlay.addItem(civicStadium, "Civic Stadium ", "Civic Stadium ");
         
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
    	MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_actions, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
    	switch (item.getItemId()) {
        case R.id.action_search:
        	return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
