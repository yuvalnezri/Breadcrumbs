package com.breadcrumbs.helpers;

import java.io.Serializable;

import android.graphics.PointF;

public class MapItem implements Serializable{

	private static final long serialVersionUID = 5283304951856447332L;
	
	
	String data;
	float locationX;
	float locationY;
	Type type;
	
	public MapItem(PointF location, String data, Type type) {
		this.locationX = location.x;
		this.locationY = location.y;
		this.data = data;
		this.type = type;
	}
	
	public PointF getLocation() {
		return new PointF(locationX,locationY);
	}
	
	public Type getType() {
		return type;
	}
	
	public String getData() {
		return data;
	}
	public enum Type {PICTURE, NOTE};
}
