package com.breadcrumbs;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.InputType;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.breadcrumbs.db.DbManager;


public class MainActivity extends Activity implements OnClickListener{
	
	
	static final int ROUTE_REQUEST = 1;
	
	private SimpleCursorAdapter cAdapter;
	
	private DbManager dbManager;
	
	private Button newRoute, deleteAllButton, addDebugRecordsButton;
	
	String newRouteName;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		ListView listView = (ListView) findViewById(R.id.list_view);
		
		newRoute = (Button) findViewById(R.id.new_route_btn);
		deleteAllButton = (Button) findViewById(R.id.delete_all_btn);
		addDebugRecordsButton = (Button) findViewById(R.id.debug_btn);
		
		newRoute.setOnClickListener(this);
		deleteAllButton.setOnClickListener(this);
		addDebugRecordsButton.setOnClickListener(this);
		
		dbManager = new DbManager(this);
		dbManager.open();
		
		String[] from = new String[]{"name","date"};
		int[] to = new int[] {android.R.id.text1, android.R.id.text2};
		cAdapter = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_2, dbManager.getRoutesCursor(),
												from, to, 0);
		
		listView.setAdapter(cAdapter);
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener()  {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				openRoute(id);		
			}
		});
	}
	
	private void openRoute(long id) {
		Intent i = new Intent(this,NavigateRouteActivity.class);
		byte[] route = dbManager.getRouteById(id);
		if (route == null) {
			//TODO some error
			return;
		}
		
		i.putExtra("route", route);
		startActivity(i);
		
	}
	private void addDebugRecords() {
		dbManager.addRoute("route1", new byte[] {});
		dbManager.addRoute("route2", new byte[] {});
		dbManager.addRoute("route3", new byte[] {});
		dbManager.addRoute("route4", new byte[] {});
		cAdapter.changeCursor(dbManager.getRoutesCursor());
		cAdapter.notifyDataSetChanged();
	}

	
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case (R.id.new_route_btn):
			newRoute();
			break;
		case (R.id.delete_all_btn):
			dbManager.deleteAllRoutes();
			cAdapter.changeCursor(dbManager.getRoutesCursor());
			cAdapter.notifyDataSetChanged();
			break;
		case (R.id.debug_btn):
			addDebugRecords();
			break;
		}
		
	}
	
	private void newRoute() {
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("New Route");
		builder.setMessage("Name:");

		// Set up the input
		final EditText input = new EditText(this);
		// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
		input.setInputType(InputType.TYPE_CLASS_TEXT);
		builder.setView(input);

		// Set up the buttons
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() { 
		    @Override
		    public void onClick(DialogInterface dialog, int which) {
		        newRouteName = input.getText().toString();
		        Context context = getApplicationContext();
				Intent i = new Intent(context, RecordRouteActivity.class);
				startActivityForResult(i, ROUTE_REQUEST);
		    }
		});
		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
		    @Override
		    public void onClick(DialogInterface dialog, int which) {
		        dialog.cancel();
		        
		    }
		});

		builder.show();

	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if (requestCode == ROUTE_REQUEST) {
			if (resultCode == RESULT_OK) {
				//TODO open name dialog
				
				byte[] route = data.getByteArrayExtra("route");
				dbManager.addRoute(newRouteName, route);
				cAdapter.changeCursor(dbManager.getRoutesCursor());
				cAdapter.notifyDataSetChanged();
			}
		}
	}
}
