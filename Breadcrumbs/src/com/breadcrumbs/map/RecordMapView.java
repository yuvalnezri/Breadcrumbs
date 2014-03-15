package com.breadcrumbs.map;

import android.content.Context;
import android.graphics.PointF;
import android.location.Location;
import android.util.AttributeSet;
import android.widget.Toast;

import com.breadcrumbs.helpers.MapItem;
import com.breadcrumbs.helpers.MapItem.Type;

public class RecordMapView extends MapView {
	
	
	
	
	public RecordMapView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public void newLocationUpdate(Location location) {
		super.newLocationUpdate(location);
		PointF point = getPointFFromLocation(location);
		if (locationArray.isEmpty()) {
			initTransform(point);
		}
		addPointToPath(point);
		locationArray.add(point);
		currentLocation = point;
		invalidate();
	}

	public void addMapItem(String data,Type type) {
		if (currentLocation==null) {
			Toast.makeText(context, "Cant add item, need to get location first...", Toast.LENGTH_SHORT).show();
			return;
		}
		mapItemsArray.add(new MapItem(currentLocation, data, type));
		PointF point = transformPoint(currentLocation);
		mapItemsLocationArray.add(point);
		invalidate();
		
	}
		
}
