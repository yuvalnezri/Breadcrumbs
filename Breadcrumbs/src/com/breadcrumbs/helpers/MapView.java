package com.breadcrumbs.helpers;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.location.Location;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;


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
	
	
	protected Paint paint;
	
	private Path path;
	
	//route
	private Route route;
	
	public GestureDetector gestureDetector;
	public ScaleGestureDetector scaleGestureDetector;
	
	public MapView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		this.context = context;
		route = new Route();
		gestureDetector = new GestureDetector(context, new GestureListener());
		scaleGestureDetector = new ScaleGestureDetector(context, new ScaleGestureListener());
		initPaint();
	}

	public void newLocationUpdate(Location location) {
		if (route.isEmpty()) { 
			PointF start = getPointFFromLocation(location);
			route.offset(-start.x, -start.y);
			route.scale(INITIAL_SCALE);
			route.offset(viewWidth/2, viewHeight/2);
			route.recalculatePath();
		}
		
		route.addLocation(location);
		invalidate();
	}

	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
	//view given size
		
		super.onSizeChanged(w, h, oldw, oldh);
		
		viewWidth = w;
		viewHeight = h;
		
		if (!route.isEmpty()){
			route.offset(w/2-oldw/2,h/2-oldh/2);
			route.recalculatePath();
		}
		
		canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
		invalidate();
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
	    super.onDraw(canvas);
	    canvas.drawBitmap(canvasBitmap, 0, 0, canvasPaint);
	    path = route.getPath();
	    canvas.drawPath(path, paint);
	}
	
	private void initPaint(){
		paint = new Paint();
		
		paint.setColor(Color.BLACK);
		paint.setAntiAlias(true);
		paint.setStrokeWidth(20);
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeJoin(Paint.Join.ROUND);
		paint.setStrokeCap(Paint.Cap.ROUND);
	}
	
	public PointF getPointFFromLocation(Location location) {
		return new PointF((float) location.getLatitude(), (float) location.getLongitude());
	}
	
	public void reset() {
		route = new Route();	
		invalidate();
	}
	
	public void drawDebugRoute() { 
		route = new Route();
		route.addLocation(viewWidth/2, viewHeight/2);
		route.addLocation(viewWidth/2-50, viewHeight/2+50);
		route.addLocation(viewWidth/2+50, viewHeight/2+50);
		route.addLocation(viewWidth/2, viewHeight/2);
		invalidate();
	}
	
	private class GestureListener extends GestureDetector.SimpleOnGestureListener {
	
		@Override
		public boolean onScroll(MotionEvent e1,MotionEvent e2, float distanceX, float distanceY) {
			route.offset(-distanceX, -distanceY);
			route.recalculatePath();
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
			route.scale(factor,scaleFocus.x,scaleFocus.y);
			route.recalculatePath();
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
			
			SerializableRoute sroute = new SerializableRoute(route);
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
			route = new Route(sroute);
			invalidate();
			
			ois.close();
			bais.close();
			
		} catch (IOException ex) {
			Log.e(VIEW_LOG_TAG, "error", ex);
		} catch (ClassNotFoundException ex) {
			Log.e(VIEW_LOG_TAG, "error", ex);
		}
		
	}
}