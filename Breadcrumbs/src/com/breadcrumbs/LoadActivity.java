package com.breadcrumbs;

import com.breadcrumbs.db.DbManager;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class LoadActivity extends ActionBarActivity {

	static final int ROUTE_REQUEST = 1;
	private DbManager dbManager;
	private SimpleCursorAdapter cAdapter;
	String newRouteName;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_load);

//		if (savedInstanceState == null) {
//			getSupportFragmentManager().beginTransaction()
//					.add(R.id.container, new PlaceholderFragment()).commit();
//		}
		ListView listView = (ListView) findViewById(R.id.list_view);
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
		getMenuInflater().inflate(R.menu.load, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_load, container,
					false);
			return rootView;
		}
	}

}
