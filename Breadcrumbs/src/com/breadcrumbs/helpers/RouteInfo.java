package com.breadcrumbs.helpers;

import java.io.Serializable;

public class RouteInfo implements Serializable{

	
	/**
	 * auto generated version UID
	 */
	private static final long serialVersionUID = -6212400522218473821L;
	
	
	public String name;
	public String date;
	public long id;
	
	public RouteInfo(long id,String name, String date) {
		this.id = id;
		this.name = name;
		this.date = date;
	}
	
	
}
