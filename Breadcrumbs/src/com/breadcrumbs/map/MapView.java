package com.breadcrumbs.map;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
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
//import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.Toast;

import com.breadcrumbs.R;
import com.breadcrumbs.ViewPictureActivity;
import com.breadcrumbs.helpers.MapItem;
import com.breadcrumbs.helpers.SerializableRoute;
import com.breadcrumbs.helpers.MapItem.Type;


public class MapView extends View

{
	
	protected final int INITIAL_SCALE = 10000;
	private final int LOCATION_MARKER_SCALE=3;
	protected final int SCALE_METER_LENGTH_PIX = 50;
	
	protected enum MapViewMode { NORMAL , FOCUSED, ORIENTIATED_FOCUS }
	
	public MapViewMode mode;
	
	//Needed to pass to View Constructor
	protected Context context;
	
	//MapView dimensions
	protected int  viewWidth, viewHeight;
	
	//canvas bitmap
	private Bitmap canvasBitmap;
	
	//canvas paint
	private Paint canvasPaint;
	
	private Bitmap locationMarker,noteIcon,cameraIcon,houseIcon,flagIcon;

	//NOT transformed location coordinates
	protected PointF currentLocation;
	
	
	protected Paint paint,textPaint,linePaint,paint2;

	protected ArrayList<PointF> locationArray;
	protected ArrayList<MapItem> mapItemsArray;
	protected ArrayList<PointF> mapItemsLocationArray;

	protected Matrix transform;
	protected Matrix locationMarkerTransform;
	protected Matrix pathRotation;
	
	private float currentAzimut=0f;
	
	private Path path;
	
	
	float initPixToMeter ;
	
	
	public GestureDetector gestureDetector;
	public ScaleGestureDetector scaleGestureDetector;
	
	public MapView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		this.context = context;
		this.setOnTouchListener( new myTouchListener());

		gestureDetector = new GestureDetector(context, new GestureListener());
		scaleGestureDetector = new ScaleGestureDetector(context, new ScaleGestureListener());
		
		locationArray = new ArrayList<PointF>();
		mapItemsArray = new ArrayList<MapItem>();
		mapItemsLocationArray = new ArrayList<PointF>();
		transform = new Matrix();
		locationMarkerTransform = new Matrix();
		
		pathRotation = new Matrix();

		path = new Path();
		mode = MapViewMode.NORMAL;
		
		locationMarker = BitmapFactory.decodeResource(getResources(), R.drawable.marker);
		cameraIcon = BitmapFactory.decodeResource(getResources(), R.drawable.amir_camera127_v2);
		noteIcon =  BitmapFactory.decodeResource(getResources(), R.drawable.amir_comment27_v3);
		houseIcon = BitmapFactory.decodeResource(getResources(), R.drawable.amir_house);
		flagIcon = BitmapFactory.decodeResource(getResources(), R.drawable.amir_flag_56);

		initPaint();
		
		
		
	}
	
	/*************************************************************************************
	*Initialization functions
	**************************************************************************************/
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
	//view given size
		
		super.onSizeChanged(w, h, oldw, oldh);
		
		viewWidth = w;
		viewHeight = h;
		
		if (!locationArray.isEmpty()){
			transform.postTranslate(w/2-oldw/2,h/2-oldh/2);
			recalculateLocationMarkerTransform();
			recalculatePath();
		}
		
		Resources res = getResources();
		Bitmap bitmap = BitmapFactory.decodeResource(res, R.drawable.map_background);
		//Canvas canvas = new Canvas(bitmap.copy(Bitmap.Config.ARGB_8888, true));
		
		canvasBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
		//canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);

		invalidate();
	}
	//returns true if got at least 1 gps location update
	public Boolean isInitialized() {
		return locationArray.size()>0;
	}
	
	private void initPaint(){
		paint = new Paint();
		textPaint = new Paint();
		linePaint = new Paint();
		paint2 = new Paint();
		
		paint.setColor(Color.BLACK);
		paint.setAntiAlias(true);
		paint.setStrokeWidth(20);
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeJoin(Paint.Join.ROUND);
		paint.setStrokeCap(Paint.Cap.ROUND);
		paint2.setColor(Color.parseColor("#785631"));
		paint2.setAntiAlias(true);
		paint2.setStrokeWidth(15);
		paint2.setStyle(Paint.Style.STROKE);
		paint2.setStrokeJoin(Paint.Join.ROUND);
		paint2.setStrokeCap(Paint.Cap.ROUND);
		
		textPaint.setTextSize(35f);
		textPaint.setColor(Color.parseColor("#785631"));
		
		linePaint.setStrokeWidth(5);
		linePaint.setColor(Color.parseColor("#785631"));
	}
	
	/*************************************************************************************
	*Event related functions
	**************************************************************************************/
	public int newLocationUpdate(Location location) {
		if (locationArray.isEmpty())
			return 0; //TODO ?
		switch (mode) {
		case NORMAL:
			recalculateLocationMarkerTransform();
			invalidate();
			break;
		case FOCUSED:
			focus();
			break;
		case ORIENTIATED_FOCUS:
			orientAndFocus();
			break;
		default:
			break;
		}
		return 1;
	}
	
	public void newCompassUpdate(float azimut) {
		currentAzimut = azimut;
		switch (mode) {
		case NORMAL:
			recalculateLocationMarkerTransform();
			invalidate();
			break;
		case FOCUSED:
			recalculateLocationMarkerTransform();
			invalidate();
			break;
		case ORIENTIATED_FOCUS:
			orientAndFocus();
			break;
		default:
			break;
		}
		invalidate();
	}
	
	private class GestureListener extends GestureDetector.SimpleOnGestureListener {
		
		@Override
		public boolean onScroll(MotionEvent e1,MotionEvent e2, float distanceX, float distanceY) {
			//allow scrolling only in normal mode
			if (mode != MapViewMode.NORMAL)
				return true;
			transform.postTranslate(-distanceX, -distanceY);
			recalculatePath();
			recalculateLocationMarkerTransform();
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
			
			//zoom factor is in meters
			float zoomFactor = calcZoomFactor(factor); 
			if ( zoomFactor < 15)
				return true;
			
			transform.postScale(factor,factor,scaleFocus.x,scaleFocus.y);
			recalculatePath();
			recalculateLocationMarkerTransform();
			invalidate();
			return true;
		}
		
		public void onScaleEnd(ScaleGestureDetector detector) {
			if (mode == MapViewMode.FOCUSED) {
				focus();
				invalidate();
			}
			if (mode == MapViewMode.ORIENTIATED_FOCUS) {
				orientAndFocus();
				invalidate();
			}
		}
	}	
	
	private class myTouchListener implements OnTouchListener {
		static final int CAMERA_ICON_SIZE=40;
		
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			switch (event.getAction() & MotionEvent.ACTION_MASK) {
			case MotionEvent.ACTION_DOWN:
				ArrayList<MapItem> items = new ArrayList<MapItem>();
				float x = event.getX();
				float y = event.getY();
				itemClicked(x, y, items);
				if (items.size() > 0) {
					//handle only first one for now
					//TODO handle more
					MapItem item = items.get(0);
					for (int i = 0; i < items.size(); i++) {
						item = items.get(i);
						
						if (item.getType()==Type.PICTURE || item.getType()==Type.NOTE)
							break;
					}
					
					switch (item.getType()) {
					case PICTURE:
						Intent i = new Intent(context,ViewPictureActivity.class);
						i.putExtra("imagepath",	item.getData());
						context.startActivity(i);
						break;
					case NOTE:
						Toast.makeText(context, item.getData(), Toast.LENGTH_SHORT).show();
						break;
					case HOUSE:
						//TODO ?
						break;
					case FLAG:
						//TODO ?
						break;
					}

				}
				break;
			}

			return false;
		}
		
		private void itemClicked(float x,float y,ArrayList<MapItem> items) {
			for (int i=0; i<mapItemsLocationArray.size(); i++){
				PointF point = mapItemsLocationArray.get(i);
				if ((point.x-CAMERA_ICON_SIZE < x) && (x < point.x+CAMERA_ICON_SIZE) &&
					(point.y-CAMERA_ICON_SIZE < y) && (y < point.y+CAMERA_ICON_SIZE)) {
					items.add(mapItemsArray.get(i));
				}
			}
		}
		
		
	}
	
	public void setViewMode(MapViewMode newMode) {
		switch (newMode) {
		case NORMAL:
			if (mode == MapViewMode.NORMAL)
				return;
			mode = MapViewMode.NORMAL;
			pathRotation.reset();
			recalculateLocationMarkerTransform();
			recalculatePath();
			invalidate();
			break;
		case FOCUSED:
			if (mode == MapViewMode.FOCUSED)
				return;
			mode = MapViewMode.FOCUSED;
			recalculateLocationMarkerTransform();
			focus();
			break;
		case ORIENTIATED_FOCUS:
			if (mode == MapViewMode.ORIENTIATED_FOCUS)
				return;
			mode = MapViewMode.ORIENTIATED_FOCUS;
			recalculateLocationMarkerTransform();
			orientAndFocus();
			break;
		default:
			break;
		}
		
	}
	
	
	public void nextViewMode() { 
		if (mode == MapViewMode.NORMAL) {
			setViewMode(MapViewMode.FOCUSED);
			Toast.makeText(context, "Focus Mode : CENTERED", Toast.LENGTH_SHORT).show();

			return;
		}
			
		if (mode == MapViewMode.FOCUSED) {
			setViewMode(MapViewMode.ORIENTIATED_FOCUS);
			Toast.makeText(context, "Focus Mode : CENTER ORIENTED", Toast.LENGTH_SHORT).show();

			return;
		}
			
		if (mode == MapViewMode.ORIENTIATED_FOCUS ) {
			setViewMode(MapViewMode.NORMAL);
			Toast.makeText(context, "Focus Mode : FREE LOOK", Toast.LENGTH_SHORT).show();

			return;
		}
	}
	/*************************************************************************************
	*Transformation functions
	**************************************************************************************/
	
	public PointF getPointFFromLocation(Location location) {
		return new PointF((float) location.getLongitude(), (float) location.getLatitude());
	}

	//all path/map item coordinates that are going to be drawn must pass threw here!!
	protected PointF transformPoint(PointF point,Matrix matrix) {
		float[] arr = new float[] {point.x,point.y};
		matrix.mapPoints(arr);
		
		return new PointF(arr[0],arr[1]);
	}
	
	private ArrayList<PointF> transformArray(ArrayList<PointF> array, Matrix matrix){
		ArrayList<PointF> newArr = new ArrayList<PointF>();
		for (Iterator<PointF> iterator = array.iterator(); iterator.hasNext();) {
			PointF point = (PointF) iterator.next();
			newArr.add(transformPoint(point,matrix));
		}
		return newArr;
	}
	
	protected String getCurrentZoomFactor(){
		float factor = calcZoomFactor(1f);
	    DecimalFormat df = new DecimalFormat("#.00");
	    String formated = df.format(factor);
	    return formated;
	}
	
	protected float calcZoomFactor(float newFactor) {
		float values[] = new float[9];
	    transform.getValues(values);
	    float scaleX = values[Matrix.MSCALE_X]*newFactor;
	    float factor = initPixToMeter / scaleX * SCALE_METER_LENGTH_PIX;
	    return factor;
	}
	
	
	
	//only adds 1 point to path without recalculating path
	//point needs to be clean(true long/lat)
	protected void addPointToPath (PointF point) {
		PointF transformedPoint = transformPoint(point,transform);
		
		if (locationArray.isEmpty()) {
			path.moveTo(transformedPoint.x, transformedPoint.y);
		} else {
			path.lineTo(transformedPoint.x, transformedPoint.y);
			
		}
		
	}
	
	
	//recalculates path and mapitemslocationarray subject to transform and rotate matrices
	protected void recalculatePath() {
		path.rewind();
		
		if (locationArray.isEmpty())
			return;
		
		ArrayList<PointF> transformedArray = transformArray(locationArray,transform);
		if (mode == MapViewMode.ORIENTIATED_FOCUS)
			transformedArray = transformArray(transformedArray, pathRotation);
		
		path.moveTo(transformedArray.get(0).x, transformedArray.get(0).y);
		
		for (Iterator<PointF> iterator = transformedArray.iterator(); iterator
				.hasNext();) {
			PointF point = (PointF) iterator.next();
			path.lineTo(point.x, point.y);
			
		}
		mapItemsLocationArray.clear();
		for (Iterator<MapItem> iterator = mapItemsArray.iterator(); iterator
				.hasNext();) {
			MapItem mapItem = (MapItem) iterator.next();
			PointF newPoint = transformPoint(mapItem.getLocation(),transform);
			if (mode ==MapViewMode.ORIENTIATED_FOCUS)
				newPoint= transformPoint(newPoint,pathRotation);
			
			mapItemsLocationArray.add(newPoint);
		}
		
    }
	
	protected void recalculateLocationMarkerTransform() {
		
		switch (mode) {
		case NORMAL:
		case FOCUSED:
			if(currentLocation == null)
				return;
			PointF transformedCurrent = transformPoint(currentLocation,transform);
			locationMarkerTransform.reset();
			locationMarkerTransform.setRotate(currentAzimut*360/(2*3.14159f), locationMarker.getWidth()/2, locationMarker.getHeight()/2);
			locationMarkerTransform.postScale(LOCATION_MARKER_SCALE,LOCATION_MARKER_SCALE);
			locationMarkerTransform.postTranslate(transformedCurrent.x-locationMarker.getWidth(),
	    			transformedCurrent.y-locationMarker.getHeight() - 20);
			break;
		
		case ORIENTIATED_FOCUS:
			locationMarkerTransform.reset();
			locationMarkerTransform.postScale(LOCATION_MARKER_SCALE,LOCATION_MARKER_SCALE);
			locationMarkerTransform.postTranslate(viewWidth/2-locationMarker.getWidth(),
	    			viewHeight/2-locationMarker.getHeight() - 20);
		default:
			break;
		}

	}
	
	public void focus() {
		if(currentLocation == null){
			return;
		}
		PointF temp = transformPoint(currentLocation, transform);
		transform.postTranslate(-temp.x, -temp.y);
		//post scaling to defualt
		transform.postTranslate(viewWidth/2, viewHeight/2);
		recalculateLocationMarkerTransform();
		recalculatePath();
		invalidate();
	}
	
	private void orientAndFocus() {
		if(currentLocation == null) 
			return;
		
		PointF temp = transformPoint(currentLocation, transform);
		transform.postTranslate(-temp.x, -temp.y);
		//post scaling to defualt
		transform.postTranslate(viewWidth/2, viewHeight/2);
		
		pathRotation.setRotate(-currentAzimut*360/(2*3.14159f),viewWidth/2,viewHeight/2);
		recalculatePath();
		invalidate();
		
	}
	
	/*************************************************************************************
	*Drawing functions
	**************************************************************************************/

	
	@Override
	protected void onDraw(Canvas canvas) {
	    super.onDraw(canvas);
	    canvas.drawBitmap(canvasBitmap, 0, 0, canvasPaint);
	    //draw path
	    canvas.drawPath(path, paint);
	    canvas.drawPath(path, paint2);
	    
	    //draw map items
	    for (int i = 0; i < mapItemsLocationArray.size(); i++) {
			PointF point = mapItemsLocationArray.get(i);
			switch (mapItemsArray.get(i).getType()) {
			case PICTURE:
				canvas.drawBitmap(cameraIcon, point.x, point.y-cameraIcon.getWidth(), null);
				break;
			case NOTE:
				canvas.drawBitmap(noteIcon, point.x, point.y-noteIcon.getHeight(), null);
				break;
			case HOUSE:
				canvas.drawBitmap(houseIcon, point.x, point.y-houseIcon.getHeight(), null);
				break;
			case FLAG:
				canvas.drawBitmap(flagIcon, point.x, point.y-flagIcon.getHeight(), null);
				break;
			}

		} 
	    //draw scale meter
	    canvas.drawText(getCurrentZoomFactor() + " m", 10 + SCALE_METER_LENGTH_PIX + 5, 35, textPaint);
	    canvas.drawLine(10, 35, 10+SCALE_METER_LENGTH_PIX, 35, linePaint);
	    canvas.drawLine(10, 30, 10, 40, linePaint);
	    canvas.drawLine(10+SCALE_METER_LENGTH_PIX, 30, 10+SCALE_METER_LENGTH_PIX, 40, linePaint);
	  //draw location marker
	    canvas.drawBitmap(locationMarker, locationMarkerTransform, null);
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
	
	/*************************************************************************************
	*serialization functions
	**************************************************************************************/
	public byte[] serializeRoute() {
		ByteArrayOutputStream baos;
		ObjectOutputStream oos;
		try{
			baos = new ByteArrayOutputStream();
			oos = new ObjectOutputStream(baos);
			
			SerializableRoute sroute = new SerializableRoute(locationArray,transform, mapItemsArray,initPixToMeter);
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
			mapItemsArray = sroute.getMapItemsArray();
			initPixToMeter = sroute.getInitPixToMeter();
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
	
}
