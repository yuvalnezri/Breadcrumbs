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
import android.view.View;
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
		/* new */
		
		int targetW = imageView.getWidth();
		int targetH = imageView.getHeight();

		/* Get the size of the image */
		BitmapFactory.Options bmOptions = new BitmapFactory.Options();
		bmOptions.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(path, bmOptions);
		int photoW = bmOptions.outWidth;
		int photoH = bmOptions.outHeight;
		
		/* Figure out which way needs to be reduced less */
		int scaleFactor = 1;
		if ((targetW > 0) || (targetH > 0)) {
			scaleFactor = Math.min(photoW/targetW, photoH/targetH);	
		}

		/* Set bitmap options to scale the image decode target */
		bmOptions.inJustDecodeBounds = false;
		bmOptions.inSampleSize = scaleFactor;
		bmOptions.inPurgeable = true;

		/* Decode the JPEG file into a Bitmap */
		Bitmap bitmap = BitmapFactory.decodeFile(path, bmOptions);
		
		/* Associate the Bitmap to the ImageView */
		imageView.setImageBitmap(bitmap);
//		mVideoUri = null;
//		mImageView.setVisibility(View.VISIBLE);
//		mVideoView.setVisibility(View.INVISIBLE);
		/* new */
		
	    
	    
//		Intent i = getIntent();
//		String path = i.getExtras().getString("imagepath");
//	    try {
//	        File f=new File(path);
//	        Bitmap bm = BitmapFactory.decodeStream(new FileInputStream(f));
//	        imageView.setImageBitmap(bm);
//	    } 
//	    catch (FileNotFoundException e) 
//	    {
//	        Log.e("breadcrumbs", "error", e);
//	    }
	}

}
