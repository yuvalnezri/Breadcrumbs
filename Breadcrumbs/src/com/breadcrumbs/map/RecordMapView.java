package com.breadcrumbs.map;

import android.content.Context;
import android.graphics.PointF;
import android.location.Location;
import android.util.AttributeSet;
import android.widget.Toast;

import com.breadcrumbs.helpers.MapItem;
import com.breadcrumbs.helpers.MapItem.Type;

public class RecordMapView extends MapView {
	
	private final static int  INITIAL_PIX_TO_METER_SCALE = 50;
	
	
	public RecordMapView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public int newLocationUpdate(Location location) {

		PointF point = getPointFFromLocation(location);
		currentLocation = point;
		
		if (locationArray.isEmpty()) {
			//initialze scale meter
			Location newL = new Location(location);
			newL.setLatitude(location.getLatitude()+1);
			initPixToMeter = newL.distanceTo(location);
			
			//initialize transformation matrix
			//invert y axis
			transform.postScale(1,-1);
			//set initial scale to 20 pix = 5 meter
			float initScale = initPixToMeter * SCALE_METER_LENGTH_PIX / INITIAL_PIX_TO_METER_SCALE;
			transform.postScale(initScale, initScale);
			
			
			locationArray.add(point);
			focus();
			super.newLocationUpdate(location);
			return 0;
			
		}
		
		locationArray.add(point);
		
		if (mode == MapViewMode.NORMAL) 
			addPointToPath(point);

		super.newLocationUpdate(location);

		return 1;
	}

	public void addMapItem(String data,Type type) {
		if (currentLocation==null) {
			Toast.makeText(context, "Cant add item, need to get location first...", Toast.LENGTH_SHORT).show();
			return;
		}
//		if(type == MapItem.Type.FOCUS){
//			PointF p = new PointF(getWidth()/2 - 40,getHeight()-130);
//			mapItemsArray.add(new MapItem(p, data, type));
//			//PointF point = transformPoint(p,transform);
//			mapItemsLocationArray.add(p);
//		} else {
			mapItemsArray.add(new MapItem(currentLocation, data, type));
			PointF point = transformPoint(currentLocation,transform);
			if (mode == MapViewMode.ORIENTIATED_FOCUS)
				point = transformPoint(currentLocation,pathRotation);
			mapItemsLocationArray.add(point);
	//	}
		
		invalidate();
		
	}
		
}
