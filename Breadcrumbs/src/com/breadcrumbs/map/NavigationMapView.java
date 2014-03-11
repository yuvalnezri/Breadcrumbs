package com.breadcrumbs.map;

import android.content.Context;
import android.location.Location;
import android.util.AttributeSet;

public class NavigationMapView extends MapView {
	public NavigationMapView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	
	@Override
	public void newLocationUpdate(Location location) {
		
	}
	
}
