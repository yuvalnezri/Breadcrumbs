package com.breadcrumbs;


import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
	
	private final static int CAMERA_REQUEST = 100;
	
	LocationManager locationManager;
	GoogleServicesManager gsManager;
	
	String routeName;
	
	RecordMapView mapView;
	DbManager dbManager;

	Button  drawButton, startLocationButton, stopLocationButton;
	
	
	Uri imageUri;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);
        
        routeName = getIntent().getExtras().getString("name");
        
        locationManager = new LocationManager(this);
        
        gsManager = new GoogleServicesManager(this);
        
        mapView = (RecordMapView) findViewById(R.id.mapView);
        dbManager = new DbManager(this);
        
        drawButton = (Button) findViewById(R.id.draw_btn);
        startLocationButton = (Button) findViewById(R.id.start_location_btn);
        stopLocationButton = (Button) findViewById(R.id.stop_location_btn);
        drawButton.setOnClickListener(this);
        startLocationButton.setOnClickListener(this);
        stopLocationButton.setOnClickListener(this);

    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu){
	    MenuInflater menuInflater = getMenuInflater();
	    menuInflater.inflate(R.layout.record_menu, menu);
	    return true;
	}
		 
	public boolean onOptionsItemSelected(MenuItem item){
         switch (item.getItemId()){
        	case R.id.focus_btn:
	        	mapView.focus();
	    		return true;
        	case R.id.new_btn :
        		mapView.reset();
        		return true;
        	case R.id.save_btn :
        		saveRouteToDB();
        		return true;
        	case R.id.picture_btn:
        		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        		startActivityForResult(intent, CAMERA_REQUEST);
        		return true;
        
        	default:
        		return super.onOptionsItemSelected(item);
        }
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
    public void onClick(View view) {
    	switch (view.getId()) {
    	case R.id.draw_btn :
    		mapView.drawDebugRoute();
    		break;
    	case R.id.start_location_btn :
    		locationManager.start();
    		break;
    	
    	case R.id.stop_location_btn :
    		locationManager.stop();
    		break;
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	switch (requestCode) {
    	case GoogleServicesManager.CONNECTION_FAILURE_RESOLUTION_REQUEST :
    		gsManager.onActivityResult(requestCode, resultCode, data);
    		break;
    		
    	case CAMERA_REQUEST:
    		if (resultCode == RESULT_CANCELED)
    			return;
    		Bitmap pic = (Bitmap) data.getExtras().get("data");
    		String path = saveNewPic(pic);
    		mapView.takePicture(path);
    	}
    	
    	
    }
    
    
    private String saveNewPic(Bitmap pic)  {
    	File appDir = getDir("imageDir", MODE_PRIVATE);
        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",java.util.Locale.getDefault())
                .format(new Date());
        File imgFile = new File(appDir.getPath() + File.separator + routeName + File.separator , "IMG_" 
        		+ timeStamp + ".jpg");

        
        FileOutputStream fos = null;
        try {           
            if (imgFile.exists() == false) {
                imgFile.getParentFile().mkdirs();
                imgFile.createNewFile();
            }
            fos = new FileOutputStream(imgFile);

            // Use the compress method on the BitMap object to write image to the OutputStream
            pic.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
        } catch (Exception e) {
            Log.e("breadcrumbs", "ERROR", e);
        }
        return imgFile.getAbsolutePath();
    }
    

  
}
