package com.breadcrumbs.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DbOpenHelper extends SQLiteOpenHelper {

	private final static String databaseName = "BreadcrumbsDB";
	private final static int    databaseVersion = 1;
	 
	public DbOpenHelper(Context context) {
	    super(context, databaseName, null, databaseVersion);
	}
	
	@Override
	public void onCreate(SQLiteDatabase sqLiteDB) {
	    String createSql = "CREATE TABLE Routes " +
	            "(_id integer primary key autoincrement, " + 
	    		"name text not null, " +
	            "date DATETIME DEFAULT CURRENT_TIMESTAMP, " + 
	    		"data blob not null);";
	    sqLiteDB.execSQL(createSql);
	}
	
	@Override
	public void onUpgrade(SQLiteDatabase sqLiteDB, int oldVersion, int newVersion) {
	    sqLiteDB.execSQL("DROP TABLE IF EXISTS BreadcrumbsDB");
	    onCreate(sqLiteDB);
	}

	public String getDBName() {
		return databaseName;
	}
}
