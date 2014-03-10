package com.breadcrumbs.location;

import java.util.Enumeration;
import java.util.Vector;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;

public class LocationManager implements 
LocationListener,
GooglePlayServicesClient.ConnectionCallbacks,
GooglePlayServicesClient.OnConnectionFailedListener
{

    // Global constants
	
    // Milliseconds per second
    private static final int MILLISECONDS_PER_SECOND = 1000;
    // Update frequency in seconds
    public static final int UPDATE_INTERVAL_IN_SECONDS = 5;
    // Update frequency in milliseconds
    private static final long UPDATE_INTERVAL =
            MILLISECONDS_PER_SECOND * UPDATE_INTERVAL_IN_SECONDS;
    // The fastest update frequency, in seconds
    private static final int FASTEST_INTERVAL_IN_SECONDS = 1;
    // A fast frequency ceiling in milliseconds
    private static final long FASTEST_INTERVAL =
            MILLISECONDS_PER_SECOND * FASTEST_INTERVAL_IN_SECONDS;
    //distance const to fire a new location event in Meters
    private static final float MINIMUM_DISTANCE_BETWEEN_UPDATES = 1;
	
    Location lastLocationRecieved;
	
    protected Vector<LocationManagerListener> listeners;
	
	LocationClient locationClient;
	boolean updatesRequested;
	
	SharedPreferences sharedPrefrences;
	SharedPreferences.Editor prefEditor;
	
	
    // Define an object that holds accuracy and frequency parameters
    LocationRequest locationRequest;
	
	Context context;
	
	public LocationManager(Context _context){
		this.context = _context;
		
		sharedPrefrences = context.getSharedPreferences("SharedPreferences", Context.MODE_PRIVATE);
		//Get a shared preferences editor
		prefEditor = sharedPrefrences.edit();
        //create the LocationRequest object
        locationClient = new LocationClient(context, this, this);
        
        //start with updates turned off
        updatesRequested = false;
        
        // Create the LocationRequest object
        locationRequest = LocationRequest.create();
        // Use high accuracy
        locationRequest.setPriority(
                LocationRequest.PRIORITY_HIGH_ACCURACY);
        // Set the update interval to 5 seconds
        locationRequest.setInterval(UPDATE_INTERVAL);
        // Set the fastest update interval to 1 second
        locationRequest.setFastestInterval(FASTEST_INTERVAL);
        
        
	}
	
	public void start() {
		updatesRequested = true;
		locationClient.connect();
	}
	
	public void pause() {
		// Save the current setting for updates
        prefEditor.putBoolean("KEY_UPDATES_ON", updatesRequested);
        prefEditor.commit();
		locationClient.disconnect();
	}
	
	public void resume() {
        /*
         * Get any previous setting for location updates
         * Gets "false" if an error occurs
         */
        if (sharedPrefrences.contains("KEY_UPDATES_ON")) {
            updatesRequested =
                    sharedPrefrences.getBoolean("KEY_UPDATES_ON", false);

        // Otherwise, turn off location updates
        } else {
            prefEditor.putBoolean("KEY_UPDATES_ON", false);
            prefEditor.commit();
        }
	}
	
	public void stop() {
        // If the client is connected
        if (locationClient.isConnected()) {
            /*
             * Remove location updates for a listener.
             * The current Activity is the listener, so
             * the argument is "this".
             */
            locationClient.removeLocationUpdates(this);
        }
        /*
         * After disconnect() is called, the client is
         * considered "dead".
         */
        locationClient.disconnect();
	}
	
    @Override
    public void onConnected(Bundle dateBundle) {
    	Toast.makeText(context, "Connected", Toast.LENGTH_SHORT).show();
        // If already requested, start periodic updates
        if (updatesRequested) {
            locationClient.requestLocationUpdates(locationRequest, this);
        }
    }
    
    @Override
    public void onDisconnected() {
    	Toast.makeText(context, "Disconnected. Please re-connect.", Toast.LENGTH_SHORT).show();
    }
    
    // Define the callback method that receives location updates
    @Override
    public void onLocationChanged(Location location) {
    	// if distance is bigger than min fire event 
    	if (lastLocationRecieved==null) {
    		lastLocationRecieved = location;
    		fireLocationUpdateEvent(location);
    		return;
    	}
    	else {
    		if (lastLocationRecieved.distanceTo(location) >= MINIMUM_DISTANCE_BETWEEN_UPDATES) { 
    			lastLocationRecieved = location;
    			fireLocationUpdateEvent(location);
    		}
    		return;
    	}
    	
    }
    
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    	//TODO: raise event to activity.
    }
    
    
    public void addLocationManagerListener(LocationManagerListener listener) {
    	if (listeners == null)
    		listeners = new Vector<LocationManagerListener>();
    	
    	listeners.addElement(listener);
    }
    
    //TODO: implement removeListener
    
    protected void fireLocationUpdateEvent(Location location) {
    	if (listeners != null && !listeners.isEmpty()) {
    		Enumeration<LocationManagerListener> e = listeners.elements();
    		while (e.hasMoreElements()) {
    			LocationManagerListener listener = e.nextElement();
    			listener.onLocationUpdate(location);
    		}
    			
    	}
    }
    
    
    
}

