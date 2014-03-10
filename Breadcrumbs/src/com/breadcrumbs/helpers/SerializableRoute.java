package com.breadcrumbs.helpers;

import java.io.Serializable;

public class SerializableRoute implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public float[] vector;
	public float[] matrix;
	
	public SerializableRoute(Route route) {
		vector = route.getArrayFromVector();
		matrix = route.getArrayFromMatrix();
	}
	
	public void removeViewOffset(int viewWidth ,int viewHeight) {
		matrix[2] -= viewWidth;
		matrix[5] -= viewHeight;
	}
	
	public void addViewOffset(int viewWidth ,int viewHeight) {
		removeViewOffset(-viewWidth, -viewHeight);
	}
	
}
