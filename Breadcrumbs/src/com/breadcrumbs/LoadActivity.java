package com.breadcrumbs;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import com.breadcrumbs.db.DbContentProvider;
import com.breadcrumbs.db.DbManager;
import com.breadcrumbs.db.DbOpenHelper;
import com.breadcrumbs.helpers.ListAdapter;
import com.breadcrumbs.helpers.RouteInfo;

public class LoadActivity extends ActionBarActivity implements LoaderManager.LoaderCallbacks<Cursor>{

	private static final int ROUTE_LIST_LOADER = 0x01;
	
	
	static final int ROUTE_REQUEST = 1;
	private DbManager dbManager;

	String newRouteName;
	private ListAdapter adapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_load);
		
		dbManager = new DbManager(this);
		dbManager.open();
		
	    
	    adapter = new ListAdapter(this, null);
	    
		ListView listView = (ListView) findViewById(R.id.list_view);
		
		listView.setAdapter(adapter);
	    
		
	    getSupportLoaderManager().initLoader(ROUTE_LIST_LOADER, null, this);
	    

	}
	
	@Override
	public Loader<Cursor> onCreateLoader(int loaderId, Bundle bundle) {
		switch (loaderId) {
		case ROUTE_LIST_LOADER:
			String[] projection = { DbOpenHelper.COL_ID, DbOpenHelper.COL_NAME, DbOpenHelper.COL_DATE };

			return new android.support.v4.content.CursorLoader
					(this, DbContentProvider.CONTENT_URI, projection, null, null, null);
		default:
			return null;
		}
	}
	
	
	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		adapter.changeCursor(null);
		
	}
	
	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		adapter.changeCursor(cursor);
		
	}
	
	public void listItemClickedHandler(View v) {
		final RouteInfo routeinfo = (RouteInfo) v.getTag();
		
		
		switch (v.getId()) {
		case R.id.delete:
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("Delete Route");
			builder.setMessage("Are you sure you want to delete " + routeinfo.name + "?");

			// Set up the buttons
			builder.setPositiveButton("OK", new DialogInterface.OnClickListener() { 
			    @Override
			    public void onClick(DialogInterface dialog, int which) {
					deleteRoute(routeinfo.id);
			    }
			});
			builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			    @Override
			    public void onClick(DialogInterface dialog, int which) {
			        dialog.cancel();
			        
			    }
			});

			builder.show();
			break;
		case R.id.nameLabel:
		case R.id.dateLabel:
			openRoute(routeinfo.id);
			break;
		default:
			break;
		}
	}
	
	private void deleteRoute(long id){
		dbManager.deleteRoute(id);
		getSupportLoaderManager().restartLoader(ROUTE_LIST_LOADER, null, this);
		adapter.notifyDataSetChanged();
	}
	
	private void deleteAllRoutes(){
    	dbManager.deleteAllRoutes();
		getSupportLoaderManager().restartLoader(ROUTE_LIST_LOADER, null, this);
		adapter.notifyDataSetChanged();
	}
	
	public void openRoute(long id) {
		Intent i = new Intent(this,NavigateRouteActivity.class);
		byte[] route = dbManager.getRouteById(id);
		if (route == null) {
			//TODO some error
			return;
		}
		
		i.putExtra("route", route);
		startActivity(i);
		
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if (requestCode == ROUTE_REQUEST) {
			if (resultCode == RESULT_OK) {
				//TODO open name dialog
				
				byte[] route = data.getByteArrayExtra("route");
				dbManager.addRoute(newRouteName, route);
				getSupportLoaderManager().restartLoader(ROUTE_LIST_LOADER, null, this);
				adapter.notifyDataSetChanged();

			}
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
	    getMenuInflater().inflate(R.menu.load_action_bar, menu);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		 switch (item.getItemId()){
	     	case R.id.delete_all_btn:
	     		
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle("Delete All Routes");
				builder.setMessage("Are you sure you want to delete ALL routes?");

				// Set up the buttons
				builder.setPositiveButton("OK", new DialogInterface.OnClickListener() { 
				    @Override
				    public void onClick(DialogInterface dialog, int which) {
				    	deleteAllRoutes();
				    }
				});
				builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				    @Override
				    public void onClick(DialogInterface dialog, int which) {
				        dialog.cancel();
				        
				    }
				});


				builder.show();
		    	return true;
	     	default:
			return super.onOptionsItemSelected(item);
		 }
	}



	

}
