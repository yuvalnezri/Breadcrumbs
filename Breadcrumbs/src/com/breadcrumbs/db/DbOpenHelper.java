package com.breadcrumbs.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DbOpenHelper extends SQLiteOpenHelper {

	private final static String databaseName = "BreadcrumbsDB";
	private final static int    databaseVersion = 1;
	 
	public static final String TABLE_ROUTES = "routes";
	public static final String COL_ID = "_id";
	public static final String COL_NAME = "name";
	public static final String COL_DATE = "date";
	public static final String COL_DATA = "data";
	
	private static final String CREATE_ROUTES_TABLE = 
			"CREATE TABLE " + TABLE_ROUTES + " ( " + 
			 COL_ID + " integer primary key autoincrement, " + 
		     COL_NAME + " text not null, " +
		     COL_DATE + " DATETIME DEFAULT CURRENT_TIMESTAMP, " + 
		     COL_DATA + " blob not null);";
	
	
	public DbOpenHelper(Context context) {
	    super(context, databaseName, null, databaseVersion);
	}
	
	@Override
	public void onCreate(SQLiteDatabase sqLiteDB) {
	    sqLiteDB.execSQL(CREATE_ROUTES_TABLE);
	}
	
	@Override
	public void onUpgrade(SQLiteDatabase sqLiteDB, int oldVersion, int newVersion) {
	    sqLiteDB.execSQL("DROP TABLE IF EXISTS BreadcrumbsDB");
	    sqLiteDB.execSQL(CREATE_ROUTES_TABLE);
	}

	public String getDBName() {
		return databaseName;
	}
}
