package com.breadcrumbs;

import java.util.ArrayList;




import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import com.breadcrumbs.db.DbManager;
import com.breadcrumbs.helpers.AdapterItem;
import com.breadcrumbs.helpers.ListAdapter;

public class LoadActivity extends ActionBarActivity implements OnClickListener {

	static final int ROUTE_REQUEST = 1;
	private DbManager dbManager;
	private SimpleCursorAdapter cAdapter;
	String newRouteName;
	private Button open_btn,del_btn;

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_load);
		

		ListView listView = (ListView) findViewById(R.id.list_view);
		dbManager = new DbManager(this);
		dbManager.open();
		
//		del_btn = (Button) findViewById(R.id.delete); // NEW
//		del_btn.setOnClickListener(this); //NEW
		String[] from = new String[]{"name","date"};
		int[] to = new int[] {android.R.id.text1, android.R.id.text2};
		cAdapter = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_2, dbManager.getRoutesCursor(),
												from, to, 0);
		
		listView.setAdapter(cAdapter);
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener()  {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
//				switch (view.getId()) {
//		    	case R.id.delete :
//		    		removeRoute(id);
//		    		break;
//				}
				
				openRoute(id);		
			}
		});
	}
	
	

	@Override
	public void onClick(View v) {

	}
	
//	private void removeRoute(long id){
//		dbManager.deleteRoute(id);
//		cAdapter.changeCursor(dbManager.getRoutesCursor());
//		cAdapter.notifyDataSetChanged();
//	}
	
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
				cAdapter.changeCursor(dbManager.getRoutesCursor());
				cAdapter.notifyDataSetChanged();
				
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
		     		dbManager.deleteAllRoutes();
					cAdapter.changeCursor(dbManager.getRoutesCursor());
					cAdapter.notifyDataSetChanged();
		    		return true;
	     	default:
			return super.onOptionsItemSelected(item);
		 }
	}



	

}
