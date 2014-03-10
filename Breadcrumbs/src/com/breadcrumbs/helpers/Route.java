package com.breadcrumbs.helpers;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Iterator;
import java.util.Vector;

import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.location.Location;

public class Route {
	
	private boolean isEmpty;
	
	private Path path;
	
	private Vector<PointF> routeVector;
	
	public PointF start;
	
	protected Paint paint;
	
	private Matrix transform;
	
	
	public Route() {
		isEmpty = true;
		routeVector = new Vector<PointF>();
		transform = new Matrix();
		transform.reset();
		path = new Path();
	}
	
	public Route(SerializableRoute sRoute) {
		this();
		for (int i = 0; i < sRoute.vector.length; i = i + 2) {
			addLocation(sRoute.vector[i], sRoute.vector[i+1]);
		}
		transform.setValues(sRoute.matrix);
		recalculatePath();

	}
	
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		
		path = new Path();
		recalculatePath();
	}
	
	public void addLocation(Location location) {
		PointF point = getPointFFromLocation(location);
		addLocation(point);
	}
	
	public void addLocation(float x, float y) {
		PointF point = new PointF(x,y);
		addLocation(point);
	}
		
	public void addLocation (PointF point) {
		
		routeVector.add(point);
		float[] arr = new float[2];
		arr[0]=point.x;
		arr[1]=point.y;
		
		transform.mapPoints(arr);
		if (isEmpty) {
			start = point;
			isEmpty = false;
			path.moveTo(arr[0],arr[1]);
			path.lineTo(arr[0],arr[1]);
			return;
		}
		
		path.lineTo(arr[0],arr[1]);
	}
	
	public boolean isEmpty() {
		return isEmpty;
	}
	
	public void recalculatePath() {
		path.rewind();
		if (routeVector.isEmpty())
			return;
		
		float[] arr = getPointArrayFromVector(routeVector);
		
		transform.mapPoints(arr);
		
		path.moveTo(arr[0], arr[1]);
		for (int j = 0; j < arr.length; j=j+2) {
			path.lineTo(arr[j],arr[j+1]);
		}
    }
	
	private float[] getPointArrayFromVector(Vector<PointF> vector) {
		float[] arr = new float[vector.size()*2];
		int i=0;
		Iterator<PointF> iter = vector.iterator();
		while (iter.hasNext()) {
			PointF point = iter.next();
			arr[i]=point.x;
			arr[i+1]=point.y;
			i=i+2;
		}
		return arr;
	}
	
	public float[] getArrayFromVector() {
		return getPointArrayFromVector(routeVector);
	}
	
	public float[] getArrayFromMatrix() {
		float[] values = new float[9];
		transform.getValues(values);
		return values;
	}
	
	public Path getPath(){
		return path;
	}
	
	public void offset(float _x, float _y) {
		transform.postTranslate(_x, _y);
	}
	
	public void scale(float _scale) {
		transform.postScale(_scale, _scale);
	}
	
	public void scale(float _scale,float _x,float _y) {
		transform.postScale(_scale, _scale, _x, _y);
	}

	
	private PointF getPointFFromLocation(Location location) {
		return new PointF((float) location.getLatitude(), (float) location.getLongitude());
	}
	
}
