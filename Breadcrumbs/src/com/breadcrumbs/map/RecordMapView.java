package com.breadcrumbs.map;

import android.content.Context;
import android.graphics.PointF;
import android.location.Location;
import android.util.AttributeSet;

public class RecordMapView extends MapView {

	public RecordMapView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public void newLocationUpdate(Location location) {
		PointF point = getPointFFromLocation(location);
		if (locationArray.isEmpty()) {
			initTransform(point);
		}
		addPointToPath(point);
		locationArray.add(point);
		invalidate();
	}

}
