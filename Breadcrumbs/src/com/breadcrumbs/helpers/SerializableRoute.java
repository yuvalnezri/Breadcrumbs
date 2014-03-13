package com.breadcrumbs.helpers;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;

import android.graphics.Matrix;
import android.graphics.PointF;
import android.util.Pair;

public class SerializableRoute implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private float[] loactionArray;
	private float[] matrix;
	private float[] imageLocationArray;
	private String[] imagePathArray;
	
	public SerializableRoute(ArrayList<PointF> locationArray, Matrix matrix, ArrayList<Pair<PointF,String>> imageArray) {
		this.loactionArray = getPointArrayFromArrayList(locationArray);
		this.matrix = new float[9];
		serializeImages(imageArray);
		matrix.getValues(this.matrix);
	}
	
	public void removeViewOffset(int viewWidth ,int viewHeight) {
		matrix[2] -= viewWidth;
		matrix[5] -= viewHeight;
	}
	
	public void addViewOffset(int viewWidth ,int viewHeight) {
		removeViewOffset(-viewWidth, -viewHeight);
	}
	
	private void serializeImages(ArrayList<Pair<PointF,String>> imageArray){
		imageLocationArray = new float[imageArray.size()*2];
		imagePathArray = new String[imageArray.size()];
		int i=0;
		for (Iterator<Pair<PointF, String>> iterator = imageArray.iterator(); iterator.hasNext();) {
			Pair<PointF, String> pair = (Pair<PointF, String>) iterator.next();
			imageLocationArray[i*2] = pair.first.x;
			imageLocationArray[i*2+1] = pair.first.y;
			imagePathArray[i] = pair.second;
			i++;
		}
	}
	
	private ArrayList<Pair<PointF,String>> deserializeImages(float[] imageLocationArray,String[] imagePathArray) {
		ArrayList<Pair<PointF,String>> imageArray = new ArrayList<Pair<PointF,String>>();
		for (int i = 0; i < imagePathArray.length; i++) {
			imageArray.add(new Pair<PointF,String>(new PointF(imageLocationArray[2*i],imageLocationArray[2*i+1]),
					imagePathArray[i]));
		}
		return imageArray;
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
	
	public ArrayList<Pair<PointF,String>> getImageArray() {
		return deserializeImages(imageLocationArray, imagePathArray);
	}
}
