package com.breadcrumbs;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import com.breadcrumbs.gestureImageview.GestureImageView;

public class ViewPictureActivity extends Activity {
	GestureImageView imageView;

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		

		
		setContentView(R.layout.activity_view_picture);
		
		imageView = (GestureImageView) findViewById(R.id.gestureImageView);

		Intent i = getIntent();
		String path = i.getExtras().getString("imagepath");
		
		Bitmap bitmap = BitmapFactory.decodeFile(path);
		
		imageView.setImageBitmap(bitmap);
		

		
		
	}
}
