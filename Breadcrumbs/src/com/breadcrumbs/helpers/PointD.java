package com.breadcrumbs.helpers;

import android.graphics.PointF;

public class PointD {
	public double x,y;
	
	public PointD(double _x, double _y) {
		x = _x;
		y = _y;
		
	}
	
	public PointF toPointF() {
		return new PointF((float) x, (float) y);
	}
	
}
