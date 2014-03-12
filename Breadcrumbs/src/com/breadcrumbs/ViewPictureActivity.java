package com.breadcrumbs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

public class ViewPictureActivity extends Activity {
	ImageView imageView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_view_picture);
		
		imageView = (ImageView) findViewById(R.id.imageView);
		Intent i = getIntent();
		String path = i.getExtras().getString("imagepath");
	    try {
	        File f=new File(path);
	        Bitmap bm = BitmapFactory.decodeStream(new FileInputStream(f));
	        imageView.setImageBitmap(bm);
	    } 
	    catch (FileNotFoundException e) 
	    {
	        Log.e("breadcrumbs", "error", e);
	    }
	}

}
