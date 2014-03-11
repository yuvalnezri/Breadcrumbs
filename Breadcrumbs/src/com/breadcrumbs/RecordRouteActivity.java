package com.breadcrumbs;


import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.telephony.gsm.GsmCellLocation;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.breadcrumbs.db.DbManager;
import com.breadcrumbs.helpers.GoogleServicesManager;
import com.breadcrumbs.location.LocationManager;
import com.breadcrumbs.location.LocationManagerListener;
import com.breadcrumbs.map.RecordMapView;

public class RecordRouteActivity extends FragmentActivity implements LocationManagerListener, OnClickListener {
	
	LocationManager locationManager;
	GoogleServicesManager gsManager;
	
	RecordMapView mapView;
	DbManager dbManager;
	
	Button newButton, drawButton, startLocationButton, stopLocationButton,
			pictureButton, noteButton, saveButton;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);
        
        locationManager = new LocationManager(this);
        
        gsManager = new GoogleServicesManager(this);
        
        mapView = (RecordMapView) findViewById(R.id.mapView);
        dbManager = new DbManager(this);
        
        newButton = (Button) findViewById(R.id.new_btn);
        drawButton = (Button) findViewById(R.id.draw_btn);
        startLocationButton = (Button) findViewById(R.id.start_location_btn);
        stopLocationButton = (Button) findViewById(R.id.stop_location_btn);
        saveButton = (Button) findViewById(R.id.save_btn);
        pictureButton = (Button) findViewById(R.id.picture_btn);
        
        newButton.setOnClickListener(this);
        drawButton.setOnClickListener(this);
        startLocationButton.setOnClickListener(this);
        stopLocationButton.setOnClickListener(this);
        saveButton.setOnClickListener(this);
        pictureButton.setOnClickListener(this);
        
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    @Override
    public void onClick(View view) {
    	switch (view.getId()) {
    	case R.id.new_btn :
    		mapView.reset();
    		break;
    	case R.id.draw_btn :
    		mapView.drawDebugRoute();
    		break;
    	case R.id.start_location_btn :
    		locationManager.start();
    		break;
    	
    	case R.id.stop_location_btn :
    		locationManager.stop();
    		break;
    		
    	case R.id.save_btn :
    		saveRouteToDB();
    		break;
    		
    	case R.id.picture_btn:
    		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
    		startActivityForResult(intent, 100);
    	}
    }
    
    public void saveRouteToDB() {
    	
    	byte[] buf = mapView.serializeRoute();
    
    	Intent i = new Intent();
    	i.putExtra("route",	buf);
    	setResult(RESULT_OK, i);
    	finish();
    	
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
    	switch (requestCode) {
    	case GoogleServicesManager.CONNECTION_FAILURE_RESOLUTION_REQUEST :
    		gsManager.onActivityResult(requestCode, resultCode, data);
    		break;
    		
    	//case MediaStore.ACTION_IMAGE_CAPTURE:
    		
    		
    	}
    	
    	
    }
    
  
}
