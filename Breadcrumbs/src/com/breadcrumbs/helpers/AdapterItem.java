package com.breadcrumbs.helpers;

import java.io.Serializable;

public class AdapterItem implements Serializable {
	private static final long serialVersionUID = -5435670920302756945L;
	
	private String name = "";
//	private double value = 0;

	public AdapterItem(String name) {
		this.setName(name);
//		this.setValue(value);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

//	public double getValue() {
//		return value;
//	}
//
//	public void setValue(double value) {
//		this.value = value;
//	}
}
