package com.breadcrumbs;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.ImageView;

public class ViewPictureActivity extends Activity {
	ImageView imageView;

	public GestureDetector gestureDetector;
	public ScaleGestureDetector scaleGestureDetector;
	private Matrix matrix = new Matrix();
	private float scale = 1f;
	int picW, picH;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		gestureDetector = new GestureDetector(this, new GestureListener());
		scaleGestureDetector = new ScaleGestureDetector(this, new ScaleGestureListener());
		
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
		picW = bitmap.getWidth();
		picH = bitmap.getHeight();
		/* Associate the Bitmap to the ImageView */
		imageView.setImageBitmap(bitmap);
		
		
		
	}
	
public boolean onTouchEvent(MotionEvent ev) {
	scaleGestureDetector.onTouchEvent(ev);
	return true;
}
private class GestureListener extends GestureDetector.SimpleOnGestureListener {
		
		@Override
		public boolean onScroll(MotionEvent e1,MotionEvent e2, float distanceX, float distanceY) {
			
			
			return true;
		}
	}
	
	private class ScaleGestureListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
		//private PointF scaleFocus;
		
		@Override
		public boolean onScaleBegin(ScaleGestureDetector detector) {
			//scaleFocus = new PointF(detector.getFocusX(), detector.getFocusY());
		
			return true;
		}
		
		public boolean onScale(ScaleGestureDetector detector) {
			//float factor = detector.getScaleFactor();
			
			scale *= detector.getScaleFactor();
		    scale = Math.max(1f, Math.min(scale, 5.0f));
		    matrix.setScale(scale, scale,detector.getFocusX(),detector.getFocusY());
		    float scaleWidth = ((float) imageView.getWidth()) / picW;
	        float scaleHeight = ((float) imageView.getHeight()) / picH;
	        matrix.postScale(scaleWidth, scaleHeight);
		    imageView.setImageMatrix(matrix);
			
			return true;
		}
		
		public void onScaleEnd(ScaleGestureDetector detector) {
			
		}
	}

}
