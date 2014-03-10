package com.breadcrumbs.db;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.breadcrumbs.helpers.RouteInfo;

public class DbManager {
	private Context context;
	private SQLiteDatabase database;
	private DbOpenHelper dbHelper;
	 
	public DbManager(Context context) {
	    this.context = context;
	}
	
	public DbManager open() throws SQLException {
	    dbHelper = new DbOpenHelper(context);
	    database = dbHelper.getWritableDatabase();
	    return this;
	}
	 
	public void close() {
	    dbHelper.close();
	}
	
	public long addRoute(String name, byte[] buf) {
	    ContentValues contentValue = new ContentValues();        
	    contentValue.put("name", name);
	    contentValue.put("data", buf);
	    return database.insert("Routes", null, contentValue);
	}
	
	public ArrayList<RouteInfo> getRoutesInfo() {
		Cursor cursor = database.query("Routes", new String[] {"_id",  "name", "date"},
				null, null, null ,null, "_id");
		
		ArrayList<RouteInfo> routes = new ArrayList<RouteInfo>();
		cursor.moveToFirst();
		for (int i = 0; i < cursor.getCount(); i++) {
			routes.add(new RouteInfo(cursor.getString(1), cursor.getString(2)));
			cursor.moveToNext();
		}
		cursor.close();
		return routes;
	}
	
	public Cursor getRoutesCursor(){
		return database.query("Routes", new String[] {"_id",  "name", "date"},
				null, null, null ,null, "_id");
	}
	
	public void deleteAllRoutes() {
		
		database.delete("Routes", null, null);
	}
	
	
	public byte[] getRouteById(long rowId) throws SQLException {
	    Cursor mCursor = database.query(true, "Routes", new String[] { "_id",
	            "data" }, "_id ="+ rowId, null, null, null, null, null);
	    if (mCursor != null) {
	        mCursor.moveToFirst();
	        return (mCursor.getBlob(1));
	    }
	    return null;
	}
}
