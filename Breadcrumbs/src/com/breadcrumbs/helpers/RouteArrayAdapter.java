package com.breadcrumbs.helpers;


import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class RouteArrayAdapter extends ArrayAdapter<RouteInfo> {
	private Context context;
	private ArrayList<RouteInfo> routes;
	
	public RouteArrayAdapter(Context context, ArrayList<RouteInfo> routes) {
		super(context, android.R.layout.simple_list_item_2,routes);
		this.context = context;
		this.routes = routes;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(android.R.layout.simple_list_item_2, parent, false);
		TextView text1 = (TextView) view.findViewById(android.R.id.text1);
		text1.setText(routes.get(position).name);
		TextView text2 = (TextView) view.findViewById(android.R.id.text2);
		text2.setText(routes.get(position).date);
		return view;
	}
}
