package com.breadcrumbs.map;

import android.content.Context;
import android.graphics.PointF;
import android.location.Location;
import android.util.AttributeSet;
import android.util.Pair;
import android.widget.Toast;

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

	public void takePicture(String path) {
		if (currentLocation==null) {
			Toast.makeText(context, "Cant take picture, need to get location first...", Toast.LENGTH_SHORT).show();
			return;
		}
		imageArray.add(new Pair<PointF, String>(currentLocation, path));
		PointF point = transformPoint(currentLocation);
		imageLocationArray.add(point);
		invalidate();
		
	}
}
