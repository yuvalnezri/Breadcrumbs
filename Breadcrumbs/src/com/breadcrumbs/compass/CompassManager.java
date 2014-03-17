package com.breadcrumbs.compass;

import java.util.Enumeration;
import java.util.Vector;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class CompassManager implements SensorEventListener{
	private final static int MINIMUM_DELTA_BETWEEN_EVENTS_DEGREES = 2;
	
	private Context context;
	
	private SensorManager sensorManager;
	private Sensor accelerometer;
	private Sensor magnetometer;
	
	float[] mGravity;
	float[] mGeomagnetic;
	
	float currentAzimut = 0f;
	  
	protected Vector<CompassManagerListener> listeners;

	
	public CompassManager(Context context) {
		this.context = context;
		
		
		sensorManager = (SensorManager) this.context.getSystemService(Context.SENSOR_SERVICE);
		accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
	}
	
	
	public void onResume() {
		sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
		sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI);
	}
	
	public void onPause() {
		sensorManager.unregisterListener(this);
	}
	
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		
		
	}
	
	@Override
	public void onSensorChanged(SensorEvent event) {
	    if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
	        mGravity = event.values;
	      if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
	        mGeomagnetic = event.values;
	      if (mGravity != null && mGeomagnetic != null) {
	        float R[] = new float[9];
	        float I[] = new float[9];
	        boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);
	        if (success) {
	          float orientation[] = new float[3];
	          SensorManager.getOrientation(R, orientation);
	          float azimut = orientation[0]; // orientation contains: azimut, pitch and roll
	          float delta = Math.abs(radToDeg(currentAzimut)-radToDeg(azimut));
	          if ( delta > MINIMUM_DELTA_BETWEEN_EVENTS_DEGREES) {
	        	  currentAzimut = azimut;
	        	  fireCompassUpdateEvent(azimut);
	          }
	        }
	      }
	}
	
    public void addCompassManagerListener(CompassManagerListener listener) {
    	if (listeners == null)
    		listeners = new Vector<CompassManagerListener>();
    	
    	listeners.addElement(listener);
    }
    
    //TODO: implement removeListener
    
    protected void fireCompassUpdateEvent(float azimut) {
    	if (listeners != null && !listeners.isEmpty()) {
    		Enumeration<CompassManagerListener> e = listeners.elements();
    		while (e.hasMoreElements()) {
    			CompassManagerListener listener = e.nextElement();
    			listener.onCompassUpdate(azimut);
    		}
    			
    	}
    }
	
    private float radToDeg(float rad) {
    	return rad*360/(2*3.14159f);
    }
}
