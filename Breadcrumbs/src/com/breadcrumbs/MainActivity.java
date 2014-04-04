package com.breadcrumbs;

//import android.support.v7.app.ActionBar;
//import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
//import android.support.v4.widget.SimpleCursorAdapter;
import android.text.InputType;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;

import com.breadcrumbs.db.DbManager;
import com.breadcrumbs.helpers.LoadActivity;


public class MainActivity extends ActionBarActivity implements OnClickListener{
	
	static final int ROUTE_REQUEST = 1;
	private SimpleCursorAdapter cAdapter;
	private DbManager dbManager;
	private Button newRoute,loadBtn;
	private Button resumeBtn;
	String newRouteName;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		newRoute = (Button) findViewById(R.id.new_route_btn);
		loadBtn = (Button) findViewById(R.id.load_btn);
		//resumeBtn = (Button) findViewById(R.id.resume_btn);
		newRoute.setOnClickListener(this);
		loadBtn.setOnClickListener(this);
		//resumeBtn.setOnClickListener(this);
		
		dbManager = new DbManager(this);
		dbManager.open();
		
		String[] from = new String[]{"name","date"};
		int[] to = new int[] {android.R.id.text1, android.R.id.text2};
		cAdapter = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_multiple_choice, dbManager.getRoutesCursor(),
											from, to, 0);

	}
	

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case (R.id.new_route_btn):
			newRoute();
			break;
		case (R.id.load_btn):
			loadRoute();
			break;
//		case (R.id.resume_btn):
//			Intent i= new Intent(this,RecordRouteActivity.class);
//	        i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
//	        startActivity(i);
//			break;
		}
		
	}
	

	
	private void loadRoute() {
		Intent intent = new Intent(this, LoadActivity.class);
		startActivity(intent);
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
				i.putExtra("name", newRouteName);
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
	
//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//	    // Inflate the menu items for use in the action bar
//	    
//	    getMenuInflater().inflate(R.menu.action_bar, menu);
//	    return super.onCreateOptionsMenu(menu);
//	}
//	
//	@Override
//	public boolean onOptionsItemSelected(MenuItem item) {
//	    // Handle presses on the action bar items
//	    switch (item.getItemId()) {
//	        case R.id.picture_btn:
//	        	//Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        		//startActivityForResult(intent, CAMERA_REQUEST);
//	            return true;
//	        case R.id.action_settings:
//	            openSettings();
//	            return true;
//	        default:
//	            return super.onOptionsItemSelected(item);
//	    }
//	}
}
