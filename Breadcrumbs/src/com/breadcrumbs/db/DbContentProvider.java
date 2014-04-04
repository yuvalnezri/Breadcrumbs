package com.breadcrumbs.db;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

public class DbContentProvider extends ContentProvider {

	DbOpenHelper routesDb;
	
	private static final String AUTHORITY = "com.breadcrumbs.db.dbcontentprovider";
	public static final int ROUTES = 100;
	public static final int ROUTE_ID = 110;
	 
	private static final String ROUTES_BASE_PATH = "routes";
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
	        + "/" + ROUTES_BASE_PATH);
	
	private static final UriMatcher sURIMatcher = new UriMatcher(
	        UriMatcher.NO_MATCH);
	static {
	    sURIMatcher.addURI(AUTHORITY, ROUTES_BASE_PATH, ROUTES);
	    sURIMatcher.addURI(AUTHORITY, ROUTES_BASE_PATH + "/#", ROUTE_ID);
	}
	
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		int uriType = sURIMatcher.match(uri);
		SQLiteDatabase sqlDB = routesDb.getWritableDatabase();
		int rowsAffected = 0;
		switch (uriType) {
		
		case ROUTES:
			rowsAffected = sqlDB.delete(DbOpenHelper.TABLE_ROUTES, selection, selectionArgs);
			break;
		case ROUTE_ID:
			String id = uri.getLastPathSegment();
			if (TextUtils.isEmpty(selection)) {
				rowsAffected = sqlDB.delete(DbOpenHelper.TABLE_ROUTES, 
						DbOpenHelper.COL_ID+"="+id , null); 
			} else {
				rowsAffected = sqlDB.delete(DbOpenHelper.TABLE_ROUTES,
						selection + " AND " + DbOpenHelper.COL_ID + "=" + id, selectionArgs);
			}
			break;
		default:
			throw new IllegalArgumentException("Unknown or Invalid URI " + uri);
		}			
		getContext().getContentResolver().notifyChange(uri, null);
		return rowsAffected;
	}

	@Override
	public String getType(Uri arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Uri insert(Uri arg0, ContentValues arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean onCreate() {
		routesDb = new DbOpenHelper(getContext());
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
			String sortOrder) {
		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
	    queryBuilder.setTables(DbOpenHelper.TABLE_ROUTES);
	 
	    int uriType = sURIMatcher.match(uri);
	    switch (uriType) {
	    case ROUTE_ID:
	        queryBuilder.appendWhere(DbOpenHelper.COL_ID + "="
	                + uri.getLastPathSegment());
	        break;
	    case ROUTES:
	        // no filter
	        break;
	    default:
	        throw new IllegalArgumentException("Unknown URI");
	    }
	 
	    Cursor cursor = queryBuilder.query(routesDb.getReadableDatabase(),
	            projection, selection, selectionArgs, null, null, sortOrder);
	    cursor.setNotificationUri(getContext().getContentResolver(), uri);
	    return cursor;
	}

	@Override
	public int update(Uri arg0, ContentValues arg1, String arg2, String[] arg3) {
		// TODO Auto-generated method stub
		return 0;
	}

}
