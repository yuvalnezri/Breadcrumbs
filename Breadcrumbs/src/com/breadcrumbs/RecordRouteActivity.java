package com.breadcrumbs;


import java.io.File;
import java.io.IOException;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.breadcrumbs.compass.CompassManager;
import com.breadcrumbs.compass.CompassManagerListener;
import com.breadcrumbs.helpers.IntentCodes;
import com.breadcrumbs.helpers.AlbumStorageDirFactory;
import com.breadcrumbs.helpers.BaseAlbumDirFactory;
import com.breadcrumbs.helpers.FroyoAlbumDirFactory;
import com.breadcrumbs.helpers.GoogleServicesManager;
import com.breadcrumbs.helpers.MapItem.Type;
import com.breadcrumbs.location.LocationManager;
import com.breadcrumbs.location.LocationManagerListener;
import com.breadcrumbs.map.RecordMapView;




public class RecordRouteActivity extends ActionBarActivity implements LocationManagerListener, OnClickListener, CompassManagerListener {
	
	private final static int CAMERA_REQUEST = 100;
	
	/* fields for enabling high res picture */
	static final int REQUEST_TAKE_PHOTO = 1;
	String mCurrentPhotoPath;
	private static final String JPEG_FILE_PREFIX = "IMG";
	private static final String JPEG_FILE_SUFFIX = ".jpg";
	private AlbumStorageDirFactory mAlbumStorageDirFactory = null;

	/* fields for enabling location management */
	private LocationManager locationManager;
	private GoogleServicesManager gsManager;
	private CompassManager compassManager;
	
//	private String routeName;
	
	private RecordMapView mapView;
	private Button focusBtn;

	
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);
       
        
        locationManager = new LocationManager(this);
        gsManager = new GoogleServicesManager(this);
        compassManager = new CompassManager(this);
        mapView = (RecordMapView) findViewById(R.id.mapView);
        focusBtn = (Button) findViewById(R.id.focus_btn); 
		focusBtn.setOnClickListener(this); 
        

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
			mAlbumStorageDirFactory = new FroyoAlbumDirFactory();
		} else {
			mAlbumStorageDirFactory = new BaseAlbumDirFactory();
		}

		Intent intent = getIntent();
		if (intent.getBooleanExtra("continued", false)) {
			byte[] route = intent.getByteArrayExtra("route");
			mapView.loadRouteFromByteArray(route);
		}
		
		
		
    }


	
	/* methods to restore state in case of screen orientation */
	@Override
	protected void onSaveInstanceState(Bundle state) {
		super.onSaveInstanceState(state);
		byte[] buf = mapView.serializeRoute();
		state.putSerializable("currRoute", buf);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		byte[] route = (byte[])savedInstanceState.getSerializable("currRoute");
		if (route == null) {
			//TODO some error
			finish();
		}
		mapView.loadRouteFromByteArray(route);
	}
	/* methods to restore state in case of screen orientation */


	
	/* methods to enable high res picture saving */
	private String getAlbumName() {
		return getString(R.string.album_name);
	}
	private File getAlbumDir() {
		File storageDir = null;

		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
			
			storageDir = mAlbumStorageDirFactory.getAlbumStorageDir(getAlbumName());

			if (storageDir != null) {
				if (! storageDir.mkdirs()) {
					if (! storageDir.exists()){
						Log.d("BreadCrumbs", "failed to create directory");
						return null;
					}
				}
			}
			
		} else {
			Log.v(getString(R.string.app_name), "External storage is not mounted READ/WRITE.");
		}
		
		return storageDir;
	}
	private File createImageFile() throws IOException {
		// Create an image file name special characters not allowed
		File albumF = getAlbumDir();
		File imageF = File.createTempFile(JPEG_FILE_PREFIX, JPEG_FILE_SUFFIX, albumF);
		return imageF;
	}
	private File setUpPhotoFile() throws IOException {
		File f = createImageFile();
		mCurrentPhotoPath = f.getAbsolutePath();
		return f;
	}
	private void galleryAddPic() {
	    Intent mediaScanIntent = new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE");
		File f = new File(mCurrentPhotoPath);
	    Uri contentUri = Uri.fromFile(f);
	    mediaScanIntent.setData(contentUri);
	    this.sendBroadcast(mediaScanIntent);
}
	private void takePicture(){
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		/* enables high res picture saving */ 
		if (intent.resolveActivity(getPackageManager()) != null) {
	        // Create the File where the photo should go
	        File photoFile = null;
	        try {
	            photoFile = setUpPhotoFile();
	            mCurrentPhotoPath = photoFile.getAbsolutePath();
	        } catch (IOException e) {
	            // Error occurred while creating the File
	        	e.printStackTrace();
				photoFile = null;
				mCurrentPhotoPath = null;
	            
	        }
	        // Continue only if the File was successfully created
	        if (photoFile != null) {
	            intent.putExtra(MediaStore.EXTRA_OUTPUT,
	                    Uri.fromFile(photoFile));
	            startActivityForResult(intent, CAMERA_REQUEST);
	        }
	    }
	}
	
	/* methods responsible for action bar inflation and menu inflation */
	@Override
	public boolean onCreateOptionsMenu(Menu menu){
	    getMenuInflater().inflate(R.menu.action_bar, menu);
	    return true;
	} 
	public boolean onOptionsItemSelected(MenuItem item){
         switch (item.getItemId()){

        	case R.id.save_btn :
        		mapView.addMapItem(null, Type.FLAG);
        		saveRouteToDB();
        		
        		return true;
        	case R.id.picture_btn:
        		takePicture();

        		return true;
        	case R.id.note_btn :
        		takeNote();
        		return true;
        		
        	case android.R.id.home:
        		onBackPressed();
        		return true;
        
        	default:
        		return super.onOptionsItemSelected(item);
        }
    }
	
	@Override
	public void onBackPressed() {
    	byte[] buf = mapView.serializeRoute();
        
    	Intent i = new Intent();
    	i.putExtra("route",	buf);
    	setResult(IntentCodes.RESULT_PAUSED, i);
    	finish();
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
    		/* new */
    		if (mCurrentPhotoPath != null) {
    			mapView.addMapItem(mCurrentPhotoPath, Type.PICTURE);
    			galleryAddPic();
    			mCurrentPhotoPath = null;
    		}
    		break;
    		/* new */
    	}
    	
    	
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
