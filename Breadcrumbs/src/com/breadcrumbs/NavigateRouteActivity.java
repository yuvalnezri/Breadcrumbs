package com.breadcrumbs;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;

import com.breadcrumbs.compass.CompassManager;
import com.breadcrumbs.compass.CompassManagerListener;
import com.breadcrumbs.helpers.GoogleServicesManager;
import com.breadcrumbs.location.LocationManager;
import com.breadcrumbs.location.LocationManagerListener;
import com.breadcrumbs.map.NavigationMapView;
//import android.support.v4.app.FragmentActivity;
//import android.widget.Button;
//import android.support.v7.app.ActionBar;

public class NavigateRouteActivity extends ActionBarActivity implements LocationManagerListener, OnClickListener, CompassManagerListener  {
	NavigationMapView mapView;
	
	LocationManager locationManager;
	GoogleServicesManager gsManager;
	CompassManager compassManager;
	private Button focusBtn;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_navigate);
		
		//dont turn off screen
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		
		//finding view by id
		mapView = (NavigationMapView) findViewById(R.id.mapView);
		focusBtn = (Button) findViewById(R.id.focus_btn); 
		focusBtn.setOnClickListener(this); 
		
		byte[] route  = getIntent().getByteArrayExtra("route");
		if (route == null) {
			//TODO some error
			finish();
		}
		
		mapView.loadRouteFromByteArray(route);
		
        locationManager = new LocationManager(this);
        gsManager = new GoogleServicesManager(this);
        compassManager = new CompassManager(this);
		
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
    	case R.id.focus_btn :
    		mapView.nextViewMode();
    		break;
		}
		
	}
	@Override
	protected void onStart(){
		super.onStart();
		if (gsManager.isGooglePlayServicesConnected){
			locationManager.start();
			locationManager.addLocationManagerListener(this);
		}
		compassManager.addCompassManagerListener(this);
	}

	@Override
	protected void onResume(){
		super.onResume();
		if (gsManager.isGooglePlayServicesConnected){
			locationManager.resume();
		}
		compassManager.onResume();
	}
	
	@Override
	protected void onPause(){
		super.onPause();
		if (gsManager.isGooglePlayServicesConnected){
			locationManager.pause();
		}
		compassManager.onPause();
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
    	mapView.newLocationUpdate(location);
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
    
    @Override
    public void onCompassUpdate(float azimut) {
    	mapView.newCompassUpdate(azimut);
    	
    }
    
    
}
