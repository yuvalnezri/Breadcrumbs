package com.breadcrumbs.helpers;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.breadcrumbs.R;

public class ListAdapter extends CursorAdapter {

	Activity context;
	
	public ListAdapter(Activity context, Cursor cursor) {
		super(context, cursor, FLAG_REGISTER_CONTENT_OBSERVER);
		
		this.context = context;
	}
	
	
	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		// TODO Auto-generated method stub
		RouteInfo routeInfo = new RouteInfo(cursor.getLong(0), 
				cursor.getString(1), cursor.getString(2));
		
		
		TextView nameTextView = (TextView) view.findViewById(R.id.nameLabel);
		nameTextView.setText(routeInfo.name);
		nameTextView.setTag(routeInfo);
		
		TextView dateTextView = (TextView) view.findViewById(R.id.dateLabel);
		dateTextView.setText(routeInfo.date);
		dateTextView.setTag(routeInfo); 
		
		ImageButton deleteButton  = (ImageButton) view.findViewById(R.id.delete);
		deleteButton.setTag(routeInfo);
		
	}
	
	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		// TODO Auto-generated method stub
		return this.context.getLayoutInflater().inflate(R.layout.list_layout_item, parent, false);
	}
	
	
	

}
