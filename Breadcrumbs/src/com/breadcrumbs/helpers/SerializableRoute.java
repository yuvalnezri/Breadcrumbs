package com.breadcrumbs.helpers;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;

import android.graphics.Matrix;
import android.graphics.PointF;

public class SerializableRoute implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public float[] loactionArray;
	public float[] matrix;
	
	public SerializableRoute(ArrayList<PointF> locationArray, Matrix matrix) {
		this.loactionArray = getPointArrayFromArrayList(locationArray);
		this.matrix = new float[9];
		matrix.getValues(this.matrix);
	}
	
	public void removeViewOffset(int viewWidth ,int viewHeight) {
		matrix[2] -= viewWidth;
		matrix[5] -= viewHeight;
	}
	
	public void addViewOffset(int viewWidth ,int viewHeight) {
		removeViewOffset(-viewWidth, -viewHeight);
	}
	
	private float[] getPointArrayFromArrayList(ArrayList<PointF> arraylist) {
		float[] arr = new float[arraylist.size()*2];
		int i=0;
		Iterator<PointF> iter = arraylist.iterator();
		while (iter.hasNext()) {
			PointF point = iter.next();
			arr[i]=point.x;
			arr[i+1]=point.y;
			i=i+2;
		}
		return arr;
	}
	
	private ArrayList<PointF> getArrayListFromPointArray(float[] pointArray) {
		ArrayList<PointF> arrlist = new ArrayList<PointF>();
		for (int i = 0; i < pointArray.length; i=i+2) {
			PointF point = new PointF(pointArray[i],pointArray[i+1]);
			arrlist.add(point);
		}
		return arrlist;
	}
	
	public ArrayList<PointF> getLocationArray() {
		return getArrayListFromPointArray(loactionArray);
	}
	
	public Matrix getTransform() {
		Matrix transform = new Matrix();
		transform.setValues(matrix);
		return transform;
	}
}
