package com.breadcrumbs.map;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Iterator;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.location.Location;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Pair;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import com.breadcrumbs.R;
import com.breadcrumbs.helpers.SerializableRoute;


public class MapView extends View

{
	
	private final int INITIAL_SCALE = 10000;
	
	//Needed to pass to View Constructor
	protected Context context;
	
	//MapView dimensions
	protected int  viewWidth, viewHeight;
	
	//canvas bitmap
	private Bitmap canvasBitmap;
	
	//canvas paint
	private Paint canvasPaint;
	
	private Bitmap locationMarker;
	private Bitmap cameraIcon;
	
	protected PointF currentLocation;
	
	protected Paint paint,textPaint,linePaint;

	protected ArrayList<PointF> locationArray;
	protected ArrayList<Pair<PointF,String>> imageArray;
	protected ArrayList<PointF> imageLocationArray;
	
	private Matrix transform;
	
	private Path path;
	
	float initPixToMeter ;

	
	public GestureDetector gestureDetector;
	public ScaleGestureDetector scaleGestureDetector;
	
	public MapView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		this.context = context;

		gestureDetector = new GestureDetector(context, new GestureListener());
		scaleGestureDetector = new ScaleGestureDetector(context, new ScaleGestureListener());
		
		locationArray = new ArrayList<PointF>();
		imageArray = new ArrayList<Pair<PointF,String>>();
		imageLocationArray = new ArrayList<PointF>();
		transform = new Matrix();
		path = new Path();
		
		locationMarker = BitmapFactory.decodeResource(getResources(), R.drawable.location_marker);
		cameraIcon = BitmapFactory.decodeResource(getResources(), R.drawable.camera);
		
		
		initPaint();
	}

	public void newLocationUpdate(Location location) {
		if (locationArray.isEmpty()) {
			Location newL = new Location(location);
			newL.setLatitude(location.getLatitude()+1);
			initPixToMeter = newL.distanceTo(location);
		}
	}
	
	protected PointF transformPoint(PointF point) {
		float[] arr = new float[] {point.x,point.y};
		transform.mapPoints(arr);
		return new PointF(arr[0],arr[1]);
	}
	
	private ArrayList<PointF> transformArray(ArrayList<PointF> array){
		ArrayList<PointF> newArr = new ArrayList<PointF>();
		for (Iterator<PointF> iterator = array.iterator(); iterator.hasNext();) {
			PointF point = (PointF) iterator.next();
			newArr.add(transformPoint(point));
		}
		return newArr;
	}
	
	//initialize transformation matrix
	protected void initTransform(PointF start) {
		//add start location offset
		transform.postTranslate(-start.x, -start.y);
		//add initial scale
		transform.postScale(INITIAL_SCALE, INITIAL_SCALE);
		//add view offset
		transform.postTranslate(viewWidth/2, viewHeight/2);
	}
	
	protected void addPointToPath (PointF point) {
		PointF transformedPoint = transformPoint(point);
		
		if (locationArray.isEmpty()) {
			path.moveTo(transformedPoint.x, transformedPoint.y);
		} else {
			path.lineTo(transformedPoint.x, transformedPoint.y);
		}
	}
	
	protected void recalculatePath() {
		path.rewind();
		
		if (locationArray.isEmpty())
			return;
		
		ArrayList<PointF> transformedArray = transformArray(locationArray);
		
		path.moveTo(transformedArray.get(0).x, transformedArray.get(0).y);
		
		for (Iterator<PointF> iterator = transformedArray.iterator(); iterator
				.hasNext();) {
			PointF point = (PointF) iterator.next();
			path.lineTo(point.x, point.y);
			
		}
		imageLocationArray.clear();
		for (Iterator<Pair<PointF, String>> iterator = imageArray.iterator(); iterator
				.hasNext();) {
			Pair<PointF, String> pair = (Pair<PointF, String>) iterator.next();
			imageLocationArray.add(transformPoint(pair.first));
		}
		
    }
	
	
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
	//view given size
		
		super.onSizeChanged(w, h, oldw, oldh);
		
		viewWidth = w;
		viewHeight = h;
		
		if (!locationArray.isEmpty()){
			transform.postTranslate(w/2-oldw/2,h/2-oldh/2);
			recalculatePath();
		}
		
		canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
		invalidate();
	}
	
	public float calcZoomFactor(){
		float values[] = new float[9];
	    transform.getValues(values);
	    float scaleX = values[Matrix.MSCALE_X];
	    float factor = initPixToMeter / scaleX * 50;
	    return factor;
	}
	@Override
	protected void onDraw(Canvas canvas) {
	    super.onDraw(canvas);
	    canvas.drawBitmap(canvasBitmap, 0, 0, canvasPaint);
	    //draw location marker
	    if (currentLocation!=null) {
	    	PointF transformedCurrent = transformPoint(currentLocation);
	    	canvas.drawBitmap(locationMarker, transformedCurrent.x-locationMarker.getWidth()/2,
	    			transformedCurrent.y-locationMarker.getHeight(), null);
	    }
	    canvas.drawPath(path, paint);
	    
	    //draw camera icons
	    for (Iterator<PointF> iterator = imageLocationArray.iterator(); iterator.hasNext();) {
			PointF point = (PointF) iterator.next();
			canvas.drawBitmap(cameraIcon, point.x-cameraIcon.getHeight()/2, point.y-cameraIcon.getWidth()/2, null);
		}
	    
	    //draw scale
	    canvas.drawText(Float.toString(calcZoomFactor()), 10, 30, textPaint);
	    canvas.drawLine(10, 35, 60, 35, linePaint);
	}
	
	private void initPaint(){
		paint = new Paint();
		
		paint.setColor(Color.BLACK);
		paint.setAntiAlias(true);
		paint.setStrokeWidth(20);
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeJoin(Paint.Join.ROUND);
		paint.setStrokeCap(Paint.Cap.ROUND);
		textPaint = new Paint();
		textPaint.setTextSize(30f);
		textPaint.setColor(Color.BLUE);
		linePaint = new Paint();
		linePaint.setTextSize(30f);
		linePaint.setTextScaleX(4);
		linePaint.setColor(Color.BLUE);
	}
	
	public PointF getPointFFromLocation(Location location) {
		return new PointF((float) location.getLatitude(), (float) location.getLongitude());
	}
	
	public void reset() {
		locationArray = new ArrayList<PointF>();
		transform = new Matrix();
		path = new Path();	
		invalidate();
	}
	
	public void drawDebugRoute() { 
		reset();
		locationArray.add(new PointF(viewWidth/2, viewHeight/2));
		locationArray.add(new PointF(viewWidth/2-50, viewHeight/2+50));
		locationArray.add(new PointF(viewWidth/2+50, viewHeight/2+50));
		locationArray.add(new PointF(viewWidth/2, viewHeight/2));
		recalculatePath();
		invalidate();
	}
	
	private class GestureListener extends GestureDetector.SimpleOnGestureListener {
	
		@Override
		public boolean onScroll(MotionEvent e1,MotionEvent e2, float distanceX, float distanceY) {
			transform.postTranslate(-distanceX, -distanceY);
			recalculatePath();
			invalidate();
			return true;
		}
	}
	
	private class ScaleGestureListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
		private PointF scaleFocus;
		
		@Override
		public boolean onScaleBegin(ScaleGestureDetector detector) {
			scaleFocus = new PointF(detector.getFocusX(), detector.getFocusY());
			return true;
		}
		
		public boolean onScale(ScaleGestureDetector detector) {
			float factor = detector.getScaleFactor();
			transform.postScale(factor,factor,scaleFocus.x,scaleFocus.y);
			recalculatePath();
			invalidate();
			return true;
		}
		
		public void onScaleEnd(ScaleGestureDetector detector) {
			
		}
	}	
	
	public byte[] serializeRoute() {
		ByteArrayOutputStream baos;
		ObjectOutputStream oos;
		try{
			baos = new ByteArrayOutputStream();
			oos = new ObjectOutputStream(baos);
			
			SerializableRoute sroute = new SerializableRoute(locationArray,transform, imageArray);
			sroute.removeViewOffset(viewWidth/2, viewHeight/2);
			oos.writeObject(sroute);
			byte[] buf = baos.toByteArray();
			
			oos.close();
			baos.close();
			
			return buf;
		} catch (IOException ex) {
			
			Log.e(VIEW_LOG_TAG, "error", ex);
			return new byte[] {};
		}
	}
	
	public void loadRouteFromByteArray(byte[] buf) {
		ByteArrayInputStream bais;
		ObjectInputStream ois;
		try {
			bais = new ByteArrayInputStream(buf);
			ois = new ObjectInputStream(bais);
			
			SerializableRoute sroute = (SerializableRoute) ois.readObject();
			sroute.addViewOffset(viewWidth/2, viewHeight/2);
			locationArray = sroute.getLocationArray();
			transform = sroute.getTransform();
			imageArray = sroute.getImageArray();
			recalculatePath();
			invalidate();
			
			ois.close();
			bais.close();
			
		} catch (IOException ex) {
			Log.e(VIEW_LOG_TAG, "error", ex);
		} catch (ClassNotFoundException ex) {
			Log.e(VIEW_LOG_TAG, "error", ex);
		}
		
	}
	
	public void focus() {
		if(currentLocation == null){
			return;
		}
		PointF temp = transformPoint(currentLocation);
		transform.postTranslate(-temp.x, -temp.y);
		//post scaling to defualt
		transform.postTranslate(viewWidth/2, viewHeight/2);
		recalculatePath();
		invalidate();
	}

	
	
	
	
}
