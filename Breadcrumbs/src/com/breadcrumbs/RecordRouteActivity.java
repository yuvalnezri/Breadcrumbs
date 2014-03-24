package com.breadcrumbs;


import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.Bundle;
import android.provider.MediaStore;
//import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBarActivity;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
//import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
//import android.widget.Button;
import android.widget.EditText;

import com.breadcrumbs.compass.CompassManager;
import com.breadcrumbs.compass.CompassManagerListener;
import com.breadcrumbs.helpers.GoogleServicesManager;
import com.breadcrumbs.helpers.MapItem.Type;
import com.breadcrumbs.location.LocationManager;
import com.breadcrumbs.location.LocationManagerListener;
import com.breadcrumbs.map.RecordMapView;

public class RecordRouteActivity extends ActionBarActivity implements LocationManagerListener, OnClickListener, CompassManagerListener {
	
	private final static int CAMERA_REQUEST = 100;
	
	private LocationManager locationManager;
	private GoogleServicesManager gsManager;
	private CompassManager compassManager;
	
	
	private String routeName;
	
	private RecordMapView mapView;
	private Button focusBtn;
//	private Button  drawButton, startLocationButton, stopLocationButton;
	
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);
        
        routeName = getIntent().getExtras().getString("name");
        
        locationManager = new LocationManager(this);
        
        gsManager = new GoogleServicesManager(this);
        
        compassManager = new CompassManager(this);
        
        mapView = (RecordMapView) findViewById(R.id.mapView);
        focusBtn = (Button) findViewById(R.id.focus_btn); // NEW
		focusBtn.setOnClickListener(this); //NEW
		
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu){
	    getMenuInflater().inflate(R.layout.record_menu, menu);
	    getMenuInflater().inflate(R.menu.action_bar, menu);
	    return true;
	}
		 
	public boolean onOptionsItemSelected(MenuItem item){
         switch (item.getItemId()){
        	case R.id.focus_btn:
	        	mapView.nextViewMode();
	    		return true;
        	case R.id.new_btn :
        		mapView.reset();
        		return true;
        	case R.id.save_btn :
        		mapView.addMapItem(null, Type.FLAG);
        		saveRouteToDB();
        		
        		return true;
        	case R.id.picture_btn:
        		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        		startActivityForResult(intent, CAMERA_REQUEST);
        		return true;
        	case R.id.note_btn :
        		takeNote();
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
    public void onClick(View view) {
    	switch (view.getId()) {
    	case R.id.focus_btn :
    		mapView.nextViewMode();
    		break;
//    	case R.id.start_location_btn :
//    		locationManager.start();
//    		break;
//    	
//    	case R.id.stop_location_btn :
//    		locationManager.stop();
//    		break;
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
    	if(mapView.newLocationUpdate(location) == 0){
    		mapView.addMapItem(null, Type.HOUSE); //TODO ?
    	}
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
    		mapView.addMapItem(path, Type.PICTURE);
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
    
    
    private void takeNote() {

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Take Note");

		// Set up the input
		final EditText input = new EditText(this);
		// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
		input.setInputType(InputType.TYPE_CLASS_TEXT);
		builder.setView(input);

		// Set up the buttons
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() { 
		    @Override
		    public void onClick(DialogInterface dialog, int which) {
        		String note = input.getText().toString();
        		mapView.addMapItem(note, Type.NOTE);		
		    }
		});
		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
		    @Override
		    public void onClick(DialogInterface dialog, int which) {
		    	dialog.cancel();
		        
		    }
		});

		builder.show();
    }

    @Override
    public void onCompassUpdate(float azimut) {
    	mapView.newCompassUpdate(azimut);
    	
    }
  
}
