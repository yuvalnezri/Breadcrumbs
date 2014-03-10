package com.breadcrumbs;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.MotionEvent;

import com.breadcrumbs.helpers.GoogleServicesManager;
import com.breadcrumbs.helpers.MapView;
import com.breadcrumbs.location.LocationManager;
import com.breadcrumbs.location.LocationManagerListener;

public class NavigateRouteActivity extends FragmentActivity implements LocationManagerListener {
	MapView mapView;
	
	LocationManager locationManager;
	GoogleServicesManager gsManager;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_navigate);
		
		mapView = (MapView) findViewById(R.id.mapView);
		
		byte[] route  = getIntent().getByteArrayExtra("route");
		if (route == null) {
			//TODO some error
			finish();
		}
		
		mapView.loadRouteFromByteArray(route);
		
        locationManager = new LocationManager(this);
        gsManager = new GoogleServicesManager(this);
		
	}
	
	
	@Override
	protected void onStart(){
		super.onStart();
		if (gsManager.isGooglePlayServicesConnected){
			locationManager.start();
			locationManager.addLocationManagerListener(this);
		}
	}

	@Override
	protected void onResume(){
		super.onResume();
		if (gsManager.isGooglePlayServicesConnected){
			locationManager.resume();
		}
	}
	
	@Override
	protected void onPause(){
		super.onPause();
		if (gsManager.isGooglePlayServicesConnected){
			locationManager.pause();
		}
	}
	
	@Override
	protected void onStop(){
		super.onStop();
		if (gsManager.isGooglePlayServicesConnected){
			locationManager.stop();
		}
	}
	
    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        //method onTouchEvent of GestureDetector class Analyzes the given motion event 
        //and if applicable triggers the appropriate callbacks on the GestureDetector.OnGestureListener supplied.
        //Returns true if the GestureDetector.OnGestureListener consumed the event, else false.
    	mapView.scaleGestureDetector.onTouchEvent(event);
    	
        boolean result = mapView.scaleGestureDetector.isInProgress();

        if (!result)
        	result = mapView.gestureDetector.onTouchEvent(event);

        return result ? result : super.onTouchEvent(event);
    }
    
    public void onLocationUpdate(Location location) {
    	//mapView.newLocationUpdate(location);
    }
    
    /*
     * Handle results returned to the FragmentActivity
     * by Google Play services
     */
    @Override
    protected void onActivityResult(
            int requestCode, int resultCode, Intent data) {
    	gsManager.onActivityResult(requestCode, resultCode, data);
    }
    
    
}
