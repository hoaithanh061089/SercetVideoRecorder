package thanhnguyen.com.sercetvideorecorder.cameraservice;


import android.Manifest;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.location.Location;
import android.media.AudioManager;
import android.media.CamcorderProfile;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.RingtoneManager;
import android.media.ThumbnailUtils;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.provider.Settings;
import android.provider.Settings.Secure;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.ZoomControls;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveResourceClient;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import thanhnguyen.com.sercetvideorecorder.ApplicationAppClass;
import thanhnguyen.com.sercetvideorecorder.R;
import thanhnguyen.com.sercetvideorecorder.StartupActivity;
import thanhnguyen.com.sercetvideorecorder.database.VideoDatabaseOpenHelper;

public class DrawOverCameraService extends Service implements
		LocationListener,
		GoogleApiClient.ConnectionCallbacks,
		GoogleApiClient.OnConnectionFailedListener {

	private Camera mCamera;
	private CameraRecording mPreview;
	private ZoomControls zoomControls;
	FrameLayout preview;
	MediaRecorder mMediaRecorder;
	public static final int MEDIA_TYPE_IMAGE = 1;
	public static final int MEDIA_TYPE_VIDEO = 2;

	boolean isRecording = false;
	LinearLayout surface;
	Runnable r;

	static int currentCameraId = CameraInfo.CAMERA_FACING_BACK;

	SharedPreferences sharedpre;
	Parameters cameraParams;
	private WindowManager windowManager;
	boolean iscamerarunning = false;
	static File mediaStorageDir;
	File savingfile;
	private static final String SCREEN_LABEL = "Real time camera";
	private static final String TABLE_NAME = "hiddenvideodatabse";
	private static final String FIELD_ID = "_id";
	private static final String FIELD_DATE = "date";
	private static final String FIELD_DURATION = "duration";
	private static final String FIELD_IMAGEPATH = "imagepath";
	private static final String FIELD_LONGTITUDE = "longtitude";
	private static final String FIELD_LATITUDE = "latitude";
	private static final String FIELD_THUMBNAILPATH = "thumbnailpath";
	View convertView;
	String videopath;
	static String timeStamp;
	Handler handler;
	SharedPreferences pre;
	int userstreamvolume;
	String servicetrigger;

	////// LOCATION

	LocationRequest mLocationRequest;
	GoogleApiClient mGoogleApiClient;
	Location mCurrentLocation;
	public static int UPDATE_INTERVAL = 30000;
	public static int FATEST_INTERVAL = 30000;


	@Override
	public void onDestroy() {
		super.onDestroy();

		//remove location update

		if(mGoogleApiClient!=null){

			if(mGoogleApiClient.isConnected()){
				LocationServices.FusedLocationApi.removeLocationUpdates(
						mGoogleApiClient, (LocationListener) this);
				mGoogleApiClient.disconnect();

			}
		}

		handler.removeCallbacks(r);
		try {
			releaseMediaRecorder();

		} catch (Exception e) {

			sendNotificationVideoRecording("Camera stop failed", "");


		}
		if (mCamera != null) {

			try {

				mCamera.stopPreview();

			} catch (RuntimeException e ){

				sendNotificationVideoRecording("Camera stop failed", "");

			}


			mCamera.release();
			mCamera = null;

		}

		if (convertView != null) {
			windowManager.removeView(convertView);
		}

		if (pre.getBoolean("zoominout", true)) {


			if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M){

				NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
				if(mNotificationManager.isNotificationPolicyAccessGranted()){

					SettingsContentObserver mSettingsContentObserver = new SettingsContentObserver(DrawOverCameraService.this, new Handler());
					DrawOverCameraService.this.getApplicationContext().getContentResolver().unregisterContentObserver(mSettingsContentObserver);
					AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
					audio.setStreamVolume(AudioManager.STREAM_RING, userstreamvolume, 0);

				}




			} else {

				SettingsContentObserver mSettingsContentObserver = new SettingsContentObserver(DrawOverCameraService.this, new Handler());
				DrawOverCameraService.this.getApplicationContext().getContentResolver().unregisterContentObserver(mSettingsContentObserver);
				AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
				audio.setStreamVolume(AudioManager.STREAM_RING, userstreamvolume, 0);

			}


		}

		if (iscamerarunning) {
			if (pre.getBoolean("camera_vibrate", true)) {

				Vibrator v = (Vibrator) getBaseContext().getSystemService(Context.VIBRATOR_SERVICE);
				// Vibrate for 500 milliseconds
				v.vibrate(300);

			}
			VideoDatabaseOpenHelper contactdatabase = new VideoDatabaseOpenHelper(DrawOverCameraService.this);
			SQLiteDatabase db = contactdatabase.getWritableDatabase();
			ContentValues values = new ContentValues();
			values.put(FIELD_IMAGEPATH, savingfile.getAbsolutePath());
			values.put(FIELD_DATE, java.text.DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime()));
			MediaPlayer mplayer = MediaPlayer.create(DrawOverCameraService.this, Uri.fromFile(new File(savingfile.getAbsolutePath())));
			if (mplayer != null) {
				int msec = mplayer.getDuration();
				String duration = String.format("%d min : %d sec",
						TimeUnit.MILLISECONDS.toMinutes(msec),
						TimeUnit.MILLISECONDS.toSeconds(msec) -
								TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(msec)));
				values.put(FIELD_DURATION, duration);



				///////
				//saving thumbnail to location
				FileOutputStream out = null;

				File file = new File(videopath, new SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date()) + ".jpeg");
				try {
					out = new FileOutputStream(file);
					Bitmap thumb = ThumbnailUtils.createVideoThumbnail(savingfile.getAbsolutePath(),
							MediaStore.Images.Thumbnails.MINI_KIND);
					thumb.compress(Bitmap.CompressFormat.JPEG, 100, out); // bmp is your Bitmap instance
					out.flush();
					// PNG is a lossless format, the compression factor (100) is ignored
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					try {
						if (out != null) {
							out.close();
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}

				values.put(FIELD_THUMBNAILPATH, file.getAbsolutePath());

				//save location data to file

				if(mCurrentLocation!=null){
					values.put(FIELD_LONGTITUDE, String.valueOf(mCurrentLocation.getLongitude()));
					values.put(FIELD_LATITUDE, String.valueOf(mCurrentLocation.getLatitude()));

				}

				long result = db.insert(TABLE_NAME, null, values);
				removeNotificationVideoRecording();
				Tracker tracker = ApplicationAppClass.getDefaultTracker(getBaseContext());
				tracker.send(new HitBuilders.EventBuilder()
						.setCategory("VideoService")
						.setAction("Video_Recording_Stopped: " + duration)
						.build());

			}


		}

		if (pre.getBoolean("google_upload_allow", false)) {

			if (pre.getBoolean("google_upload_wifi", false)) {

				ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
				NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

				if (mWifi.isConnected()) {


					uploadfiletodrive(savingfile);


				}


			} else {

				   uploadfiletodrive(savingfile);

			}


		}


	}

	public boolean HasBackFaceCamera() {

		boolean HasBackFaceCamera = false;

		if (this.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {

			CameraInfo cminfo = new CameraInfo();
			int number = Camera.getNumberOfCameras();
			for (int i = 0; i < number; i++) {
				Camera.getCameraInfo(i, cminfo);
				if (cminfo.facing == CameraInfo.CAMERA_FACING_BACK) {
					HasBackFaceCamera = true;
				}
			}


		}
		return HasBackFaceCamera;

	}

	public boolean HasFrontFaceCamera() {

		boolean HasFrontFaceCamera = false;

		if (this.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {

			CameraInfo cminfo = new CameraInfo();
			int number = Camera.getNumberOfCameras();
			for (int i = 0; i < number; i++) {
				Camera.getCameraInfo(i, cminfo);
				if (cminfo.facing == CameraInfo.CAMERA_FACING_FRONT) {
					HasFrontFaceCamera = true;
				}
			}


		}
		return HasFrontFaceCamera;

	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		if(intent==null){

			  servicetrigger = "startvideobutton";


		} else {

			if(intent.getExtras()==null){

				servicetrigger = "startvideobutton";

			} else {

				servicetrigger = intent.getExtras().getString("servicetrigger");

			}
		}


		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		//set up location
		locationSetup();
		sharedpre = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		pre = PreferenceManager.getDefaultSharedPreferences(this);
		windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
		int drawsscreentype;

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

			drawsscreentype = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
		} else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {

			drawsscreentype = WindowManager.LayoutParams.TYPE_TOAST;

		} else {

			drawsscreentype = WindowManager.LayoutParams.TYPE_PHONE;

		}


		WindowManager.LayoutParams params = new WindowManager.LayoutParams(
				WindowManager.LayoutParams.WRAP_CONTENT,
				WindowManager.LayoutParams.WRAP_CONTENT,
				drawsscreentype,
				WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
				PixelFormat.TRANSLUCENT);

		params.gravity = Gravity.TOP | Gravity.LEFT;
		params.x = 0;
		params.y = 100;

		LayoutInflater inflater = LayoutInflater.from(this);
		convertView = inflater.inflate(R.layout.cameralayout, null);

		windowManager.addView(convertView, params);


		Thread t = new Thread(new Runnable() {

			@Override
			public void run() {

				String cameraid_pre = sharedpre.getString("camera_chooser", "0");
				int cameraid = Integer.parseInt(cameraid_pre);

				if (cameraid == 0) {

					if (HasBackFaceCamera()) {

						currentCameraId = CameraInfo.CAMERA_FACING_BACK;


					} else {

						currentCameraId = CameraInfo.CAMERA_FACING_FRONT;

					}


				} else {

					if (HasFrontFaceCamera()) {

						currentCameraId = CameraInfo.CAMERA_FACING_FRONT;


					} else {

						currentCameraId = CameraInfo.CAMERA_FACING_BACK;

					}


				}

				mCamera = getCameraInstance();

				if (mCamera == null) {


					sendNotificationVideoRecording("Unexpected error initializing camera, video recording failed!", "");


					stopSelf();

					return;


				}


			}

		});

		t.start();
		try {
			t.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		surface = (LinearLayout) convertView.findViewById(R.id.middleSurface);
		mPreview = new CameraRecording(this, mCamera);

		surface.addView(mPreview);


		handler = new Handler();
		r = new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub

				if (prepareVideoRecorder()) {


					try {
						mMediaRecorder.start();
						Tracker tracker = ApplicationAppClass.getDefaultTracker(getBaseContext());
						tracker.send(new HitBuilders.EventBuilder()
								.setCategory("VideoService")
								.setAction("Video_Recording_Started")
								.build());


						if (pre.getBoolean("app_notification", true)) {

							if (servicetrigger.equals("shortcut")) {

								sendNotificationVideoRecording("New video is being recorded.", "Tap shortcut icon again to stop recording.");

							} else if (servicetrigger.equals("keypad")) {

								sendNotificationVideoRecording("New video is being recorded.", "Dial your number again to stop recording.");


							} else if (servicetrigger.equals("servicemaxduration")) {

								sendNotificationVideoRecording("New video is being recorded.","Maximum file duration, video recording restarts.");


							}  else if (servicetrigger.equals("startvideobutton")) {

								sendNotificationVideoRecording("New video is being recorded.", "");


							}

						}


					} catch (Exception e) {

						sendNotificationVideoRecording("Unexpected error initializing camera, video recording failed!", "");

						releaseMediaRecorder();
						removeNotificationVideoRecording();

						if (mCamera != null) {
							mCamera.stopPreview();
							mCamera.release();
							mCamera = null;

						}

						stopSelf();


					}


					iscamerarunning = true;
					if (pre.getBoolean("camera_vibrate", true)) {

						Vibrator v = (Vibrator) getBaseContext().getSystemService(Context.VIBRATOR_SERVICE);
						v.vibrate(300);

					}

					if (pre.getBoolean("zoominout", true)) {

						if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M){

							NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
							if(mNotificationManager.isNotificationPolicyAccessGranted()){


								SettingsContentObserver mSettingsContentObserver = new SettingsContentObserver(DrawOverCameraService.this, new Handler());
								DrawOverCameraService.this.getApplicationContext().getContentResolver().registerContentObserver(Settings.System.CONTENT_URI, true, mSettingsContentObserver);


							}




						} else {

							SettingsContentObserver mSettingsContentObserver = new SettingsContentObserver(DrawOverCameraService.this, new Handler());
							DrawOverCameraService.this.getApplicationContext().getContentResolver().registerContentObserver(Settings.System.CONTENT_URI, true, mSettingsContentObserver);


						}








					}


					if (mMediaRecorder != null) {

						mMediaRecorder.setOnInfoListener(new MediaRecorder.OnInfoListener() {

							@Override
							public void onInfo(MediaRecorder mr, int what, int extra) {

								if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED) {

									Toast.makeText(getBaseContext(), "Max file!", Toast.LENGTH_SHORT).show();


									DrawOverCameraService.this.stopSelf();

/*
									if(sharedpre.getBoolean("autostartsize", true)){
										releaseMediaRecorder();
										if(mCamera!=null){

											mCamera.stopPreview();
											mCamera.release();
											mCamera = null;

										}

										DrawOverCameraService.this.stopSelf();



										DrawOverCameraService.this.startService(new Intent(DrawOverCameraService.this, DrawOverCameraService.class ));

									} else {

										releaseMediaRecorder();
										if(mCamera!=null){

											mCamera.stopPreview();
											mCamera.release();
											mCamera = null;

										}

										DrawOverCameraService.this.stopSelf();

									}*/


								} else if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {

									DrawOverCameraService.this.stopSelf();

									DrawOverCameraService.this.startService(new Intent(DrawOverCameraService.this, DrawOverCameraService.class).putExtra("servicetrigger", "servicemaxduration"));



								}


							}

						});
					}


				}

			}


		};

		handler.postDelayed(r, 2000);


	}


	public Camera getCameraInstance() {
		Camera c = null;
		try {
			c = Camera.open(currentCameraId); // attempt to get a Camera instance


		} catch (final Exception e) {

			Handler hanlder = new Handler(Looper.getMainLooper());
			hanlder.post(new Runnable() {

				@Override
				public void run() {




				}


			});


			stopSelf();


		}

		if (c == null) {

			Handler hanlder = new Handler(Looper.getMainLooper());
			hanlder.post(new Runnable() {

				@Override
				public void run() {

					sendNotificationVideoRecording("Camera is being used, please close the app currently using the camera!", "");


				}


			});


			stopSelf();

		}

		return c; // returns null if camera is unavailable
	}


	private boolean prepareVideoRecorder() {

		if(mCamera.getParameters()!=null){

			cameraParams = mCamera.getParameters();
		}

		//setCameraDisplayOrientation(getBaseContext(), currentCameraId,mCamera );

		mMediaRecorder = new MediaRecorder();

		// Step 1: Unlock and set camera to MediaRecorder
		mCamera.unlock();
		mMediaRecorder.setCamera(mCamera);


		WindowManager wm = (WindowManager) getBaseContext().getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();

        /*if(display.getRotation() == Surface.ROTATION_0)
        {
        	mMediaRecorder.setOrientationHint(90);             
            
        }

        if(display.getRotation() == Surface.ROTATION_90)
        {
        	mMediaRecorder.setOrientationHint(0);             
        }

        if(display.getRotation() == Surface.ROTATION_180)
        {
        	mMediaRecorder.setOrientationHint(270);             
        }

        if(display.getRotation() == Surface.ROTATION_270)
        {
        	mMediaRecorder.setOrientationHint(180);             
           
        }*/


		int angle = getVideoOrientationAngle(getApplicationContext(), currentCameraId);
		mMediaRecorder.setOrientationHint(angle);


		// Step 2: Set sources
		boolean audiorecordcheck = sharedpre.getBoolean("audiorecordcheck", true);

		if (audiorecordcheck) {

			mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);

		}

		mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

		// Step 3: Set a CamcorderProfile (requires API Level 8 or higher)

		boolean autohighqualitycheck = sharedpre.getBoolean("autohighqualitycheck", false);

		if (autohighqualitycheck) {

			if (CamcorderProfile.hasProfile(currentCameraId, CamcorderProfile.QUALITY_HIGH)) {

				mMediaRecorder.setProfile(CamcorderProfile.get(currentCameraId, CamcorderProfile.QUALITY_HIGH));

			} else if (CamcorderProfile.hasProfile(currentCameraId, CamcorderProfile.QUALITY_LOW)) {

				mMediaRecorder.setProfile(CamcorderProfile.get(currentCameraId, CamcorderProfile.QUALITY_LOW));

			}

		} else {

			String videoresolution_pre = sharedpre.getString("camera_resolution", "1280");
			int videoresolution = Integer.parseInt(videoresolution_pre);

			boolean ismatch = false;


			if(cameraParams.getSupportedVideoSizes()!=null){

				for (int j = 0; j < cameraParams.getSupportedVideoSizes().size(); j++) {

					//float pixelCountTemp = getResolution(cameraParams.getSupportedVideoSizes().get(j).width,  cameraParams.getSupportedVideoSizes().get(j).height);

					int width = cameraParams.getSupportedVideoSizes().get(j).width;


					if (width == videoresolution) {

						ismatch = true;

						break;


					}


				}

			} else {


				ismatch = false;

			}






			if (ismatch == true) {


				if (videoresolution == 1920) {

					if (CamcorderProfile.hasProfile(currentCameraId, CamcorderProfile.QUALITY_1080P)) {

						mMediaRecorder.setProfile(CamcorderProfile.get(currentCameraId, CamcorderProfile.QUALITY_1080P));


					} else if (CamcorderProfile.hasProfile(currentCameraId, CamcorderProfile.QUALITY_HIGH)) {


						mMediaRecorder.setProfile(CamcorderProfile.get(currentCameraId, CamcorderProfile.QUALITY_HIGH));

					} else if (CamcorderProfile.hasProfile(currentCameraId, CamcorderProfile.QUALITY_LOW)) {

						mMediaRecorder.setProfile(CamcorderProfile.get(currentCameraId, CamcorderProfile.QUALITY_LOW));

					}


				} else if (videoresolution == 1280) {


					if (CamcorderProfile.hasProfile(currentCameraId, CamcorderProfile.QUALITY_720P)) {


						mMediaRecorder.setProfile(CamcorderProfile.get(currentCameraId, CamcorderProfile.QUALITY_720P));

					} else if (CamcorderProfile.hasProfile(currentCameraId, CamcorderProfile.QUALITY_HIGH)) {


						mMediaRecorder.setProfile(CamcorderProfile.get(currentCameraId, CamcorderProfile.QUALITY_HIGH));

					} else if (CamcorderProfile.hasProfile(currentCameraId, CamcorderProfile.QUALITY_LOW)) {

						mMediaRecorder.setProfile(CamcorderProfile.get(currentCameraId, CamcorderProfile.QUALITY_LOW));

					}


				} else if (videoresolution == 720) {

					if (CamcorderProfile.hasProfile(currentCameraId, CamcorderProfile.QUALITY_480P)) {


						mMediaRecorder.setProfile(CamcorderProfile.get(currentCameraId, CamcorderProfile.QUALITY_480P));

					} else {


						mMediaRecorder.setProfile(CamcorderProfile.get(currentCameraId, CamcorderProfile.QUALITY_LOW));


					}


				} else if (videoresolution == 320) {

					if (CamcorderProfile.hasProfile(currentCameraId, CamcorderProfile.QUALITY_QVGA)) {


						mMediaRecorder.setProfile(CamcorderProfile.get(currentCameraId, CamcorderProfile.QUALITY_QVGA));

					} else {


						mMediaRecorder.setProfile(CamcorderProfile.get(currentCameraId, CamcorderProfile.QUALITY_LOW));

					}


				}


			} else {


				if (videoresolution == 1920) {


					if (CamcorderProfile.hasProfile(currentCameraId, CamcorderProfile.QUALITY_HIGH)) {


						mMediaRecorder.setProfile(CamcorderProfile.get(currentCameraId, CamcorderProfile.QUALITY_HIGH));

					} else if (CamcorderProfile.hasProfile(currentCameraId, CamcorderProfile.QUALITY_LOW)) {

						mMediaRecorder.setProfile(CamcorderProfile.get(currentCameraId, CamcorderProfile.QUALITY_LOW));

					}


				} else if (videoresolution == 1280) {

					if (CamcorderProfile.hasProfile(currentCameraId, CamcorderProfile.QUALITY_HIGH)) {


						mMediaRecorder.setProfile(CamcorderProfile.get(currentCameraId, CamcorderProfile.QUALITY_HIGH));

					} else if (CamcorderProfile.hasProfile(currentCameraId, CamcorderProfile.QUALITY_LOW)) {

						mMediaRecorder.setProfile(CamcorderProfile.get(currentCameraId, CamcorderProfile.QUALITY_LOW));

					}

				} else if (videoresolution == 720) {

					mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_LOW));

				} else if (videoresolution == 320) {

					mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_LOW));


				}


			}

		}


		videopath = sharedpre.getString("video_path", null);
		if (videopath == null) {

			savingfile = getOutputMediaFile(MEDIA_TYPE_VIDEO);
			videopath = mediaStorageDir.getPath();

		} else {

			File mediaStorageDir = new File(videopath);
			if (!mediaStorageDir.exists()) {
				savingfile = getOutputMediaFile(MEDIA_TYPE_VIDEO);

			} else {

				String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date());
				savingfile = new File(videopath + File.separator +
						timeStamp + ".mp4");


			}


		}


		// Step 4: Set output file
		mMediaRecorder.setOutputFile(savingfile.getAbsolutePath());

		String recordingduration_string = sharedpre.getString("camera_duration", "3");
		int recordingduration = Integer.parseInt(recordingduration_string);

		    if(recordingduration==0){

				mMediaRecorder.setMaxDuration(60*1000);

			} else if(recordingduration==1){
				mMediaRecorder.setMaxDuration(120*1000);

			} else if(recordingduration==2){
				mMediaRecorder.setMaxDuration(300*1000);

			} else if(recordingduration==3){
				mMediaRecorder.setMaxDuration(900*1000);

			} else if(recordingduration==4){
				mMediaRecorder.setMaxDuration(3600*1000);

			}else if(recordingduration==5){
				mMediaRecorder.setMaxDuration(13600*1000);

			}

	    
	    
	   /*int recordingduration = sharedpre.getInt("recordingduration", 60);
	   if(recordingduration!=0){
		   
		   
		   if(recordingduration==60){
			   
			   mMediaRecorder.setMaxDuration(60*1000);
			   
		   } else if(recordingduration==120){
			   mMediaRecorder.setMaxDuration(120*1000);
			   
		   } else if(recordingduration==300){
			   mMediaRecorder.setMaxDuration(300*1000);
			   
		   } else if(recordingduration==900){
			   mMediaRecorder.setMaxDuration(900*1000);
			   
		   } else if(recordingduration==1800){
			   mMediaRecorder.setMaxDuration(1800*1000);
			   
		   }else if(recordingduration==13600){
			   mMediaRecorder.setMaxDuration(13600*1000);
			   
		   } 
		   
	   }
	   int recordingsize = sharedpre.getInt("recordingsize", 50);
	   if(recordingsize!=0){
		    
		   if(recordingsize==5){
			   
			   mMediaRecorder.setMaxFileSize(5*1000000);
			   
		   } else if(recordingsize==50){
			   mMediaRecorder.setMaxFileSize(50*1000000);
			   
		   } else if(recordingsize==100){
			   mMediaRecorder.setMaxFileSize(100*1000000);
			   
		   } else if(recordingsize==300){
			   mMediaRecorder.setMaxFileSize(300*1000000);
			   
		   } else if(recordingsize==500){
			   mMediaRecorder.setMaxFileSize(500*1000000);
			   
		   }else if(recordingsize==1000){
			   mMediaRecorder.setMaxFileSize(1000*1000000);
			   
		   } 
		   
	   }*/

		// Step 5: Set the preview output
		mMediaRecorder.setPreviewDisplay(mPreview.getHolder().getSurface());

		// Step 6: Prepare configured MediaRecorder
		try {
			mMediaRecorder.prepare();

		} catch (IllegalStateException e) {
			releaseMediaRecorder();
			if (mCamera != null) {

				mCamera.stopPreview();
				mCamera.release();
				mCamera = null;

			}
			return false;
		} catch (IOException e) {
			sendNotificationVideoRecording("Camera started failed, please check directory to save video or storage permission!", "");
			releaseMediaRecorder();
			if (mCamera != null) {

				mCamera.stopPreview();
				mCamera.release();
				mCamera = null;

			}
			return false;
		}
		return true;
	}

	private void releaseMediaRecorder() {
		if (mMediaRecorder != null) {
			if (iscamerarunning) {
				mMediaRecorder.stop();
			}

			mMediaRecorder.reset();   // clear recorder configuration
			mMediaRecorder.release(); // release the recorder object
			mMediaRecorder = null;
			// lock camera for later use
			try{
				mCamera.unlock();

			} catch (RuntimeException e){

			}


		}
	}

	private static File getOutputMediaFile(int type) {
		// To be safe, you should check that the SDCard is mounted
		// using Environment.getExternalStorageState() before doing this.

		mediaStorageDir = new File(Environment.getExternalStorageDirectory(
		), "Secret Video Recorder Video");
		// This location works best if you want the created images to be shared
		// between applications and persist after your app has been uninstalled.

		// Create the storage directory if it does not exist
		if (!mediaStorageDir.exists()) {
			if (!mediaStorageDir.mkdirs()) {
				Log.d("MyCameraApp", "failed to create directory");
				return null;
			}
		}

		// Create a media file name
		timeStamp = new SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date());
		File mediaFile;
		if (type == MEDIA_TYPE_IMAGE) {
			mediaFile = new File(mediaStorageDir.getPath() + File.separator +
					"IMG_" + timeStamp + ".jpg");
		} else if (type == MEDIA_TYPE_VIDEO) {
			mediaFile = new File(mediaStorageDir.getPath() + File.separator +
					timeStamp + ".mp4");
		} else {
			return null;
		}

		return mediaFile;
	}


	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public void onConnected(@Nullable Bundle bundle) {

		if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {

			return;
		}
		LocationServices.FusedLocationApi.requestLocationUpdates(
				mGoogleApiClient, mLocationRequest, (LocationListener) this);

	}

	@Override
	public void onConnectionSuspended(int i) {

	}

	@Override
	public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

	}

	@Override
	public void onLocationChanged(Location location) {

		mCurrentLocation = location;


	}


	public class SettingsContentObserver extends ContentObserver {
	    int previousVolume;
	    Context context;

	    public SettingsContentObserver(Context c, Handler handler) {
	        super(handler);
	        context=c;

	        context=c;
	        AudioManager audio = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
	        previousVolume = audio.getStreamVolume(AudioManager.STREAM_RING);
	        userstreamvolume = previousVolume;
        	audio.setStreamVolume(AudioManager.STREAM_RING, 0, 0);
	   
	    }

	    @Override
	    public boolean deliverSelfNotifications() {
	        return super.deliverSelfNotifications();
	    }

	    @Override
	    public void onChange(boolean selfChange) {
	        super.onChange(selfChange);

	       
	    }

		@Override
		public void onChange(boolean selfChange, Uri uri) {
			// TODO Auto-generated method stub
			super.onChange(selfChange, uri);
			 
			    AudioManager audio = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
		        int currentVolume = audio.getStreamVolume(AudioManager.STREAM_RING);

		        int delta=previousVolume-currentVolume;
		        
		        if(delta>=0) {
		               //Toast.makeText(context, "down", Toast.LENGTH_SHORT).show();
		                previousVolume=currentVolume;
		                zoomCamera(false);
		              
		        
		        }
		        else if(delta<0) {
		        	
		        	//Toast.makeText(context, "up", Toast.LENGTH_SHORT).show();
		        	previousVolume=currentVolume;
		        	zoomCamera(true);

		        	
		        	
		        }
		        
		    }
	    
	 		}
	public void zoomCamera(boolean zoomInOrOut) {
		Parameters parameter = null;
        if(mCamera!=null) {
			try {

				parameter = mCamera.getParameters();


			} catch(RuntimeException e) {

        		return;

			}

            if(parameter.isZoomSupported()) {
                int MAX_ZOOM = parameter.getMaxZoom();
                int currnetZoom = parameter.getZoom();
                    if(zoomInOrOut && (currnetZoom <MAX_ZOOM && currnetZoom >=0)) {
                        parameter.setZoom(++currnetZoom);
						//Toast.makeText(getBaseContext(), "Zoom in", Toast.LENGTH_LONG).show();

					}
                    else if(!zoomInOrOut && (currnetZoom <=MAX_ZOOM && currnetZoom >0)) {
                    parameter.setZoom(--currnetZoom);
						//Toast.makeText(getBaseContext(), "Zoom out", Toast.LENGTH_LONG).show();

					}
            }
            else


            mCamera.setParameters(parameter);
        }

	
}

	private void saveFiletoDrive( final File file, final String filetitle) {
 	    // Start by creating a new contents, and setting a callback.
 		

 	}

	public String getUniqueID(){    
	    String myAndroidDeviceId = "";
	    myAndroidDeviceId = Secure.getString(getApplicationContext().getContentResolver(), Secure.ANDROID_ID);
	    return myAndroidDeviceId;
	}
	public static int getVideoOrientationAngle(Context activity, int cameraId) { //The param cameraId is the number of the camera.
		int angle;
		WindowManager wm = (WindowManager) activity.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		int degrees = display.getRotation();
		android.hardware.Camera.CameraInfo info =
				new android.hardware.Camera.CameraInfo();
		android.hardware.Camera.getCameraInfo(cameraId, info);
		switch (degrees) {
			case Surface.ROTATION_0:
				angle = 90;
				break;
			case Surface.ROTATION_90:
				angle = 0;
				break;
			case Surface.ROTATION_180:
				angle = 270;
				break;
			case Surface.ROTATION_270:
				angle = 180;
				break;
			default:
				angle = 90;
				break;
		}
		if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT)
			angle = (angle + 180) % 360;

		return angle;
	}

	private void sendNotificationVideoRecording(String title, String messageBody) {
		Intent intent = new Intent(this, StartupActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
				PendingIntent.FLAG_ONE_SHOT);

		String channelId = getString(R.string.app_name);
		Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		NotificationCompat.Builder notificationBuilder =
				new NotificationCompat.Builder(this, channelId)
						.setSmallIcon(R.drawable.eye)
						.setContentTitle(title)
						.setContentText(messageBody)
						.setAutoCancel(true)
						.setSound(defaultSoundUri)
						.setContentIntent(pendingIntent);

		NotificationManager notificationManager =
				(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		notificationManager.notify(01
                /* ID of notification */, notificationBuilder.build());
	}
	private void removeNotificationVideoRecording() {

		NotificationManager notificationManager =
				(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		notificationManager.cancel(1);
	}
	private boolean isGooglePlayServicesAvailable() {
		GoogleApiAvailability api = GoogleApiAvailability.getInstance();
		int isAvailable = api.isGooglePlayServicesAvailable(this);


		if (isAvailable == ConnectionResult.SUCCESS) {

			return true;
		}
		return false;
	}
	public void locationSetup(){

		if (isGooglePlayServicesAvailable()) {
			mLocationRequest = new LocationRequest();
			mLocationRequest.setInterval(UPDATE_INTERVAL);
			mLocationRequest.setFastestInterval(FATEST_INTERVAL);
			mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
			mLocationRequest.setSmallestDisplacement(50);
			//mLocationRequest.setSmallestDisplacement(10.0f);  /* min dist for location change, here it is 10 meter */
			mGoogleApiClient = new GoogleApiClient.Builder(this)
					.addApi(LocationServices.API)
					.addConnectionCallbacks(this)
					.addOnConnectionFailedListener(this)
					.build();

			mGoogleApiClient.connect();
		}



	};


	public void uploadfiletodrive(final File file){

		GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(getBaseContext());

		if(acct==null){

			if (pre.getBoolean("app_notification", true)) {

				sendNotificationVideoRecording("Sign in your google account first to upload file to Google Drive", "");

			}


		} else {


			final DriveResourceClient mDriveResourceClient = Drive.getDriveResourceClient(getBaseContext(), acct);
			final Task<DriveFolder> rootFolderTask = mDriveResourceClient.getRootFolder();
			final Task<DriveContents> createContentsTask = mDriveResourceClient.createContents();


			Tasks.whenAll(rootFolderTask, createContentsTask)
					.continueWithTask(new Continuation<Void, Task<DriveFile>>() {
						@Override
						public Task<DriveFile> then(@NonNull Task<Void> task) throws Exception {
							DriveFolder parent = rootFolderTask.getResult();
							DriveContents contents = createContentsTask.getResult();
							OutputStream outputStream = contents.getOutputStream();

							FileInputStream inputstream = new FileInputStream(file.getPath());
							BufferedInputStream in = new BufferedInputStream(inputstream);
							byte[] buffer = new byte[8 * 1024];

							BufferedOutputStream out = new BufferedOutputStream(outputStream);
							int n = 0;
							try {
								while( ( n = in.read(buffer) ) > 0 ) {
									out.write(buffer, 0, n);
									out.flush();
								}
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}


							try {
								in.close();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}


							MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
									.setTitle(file.getName())
									.setMimeType("video/mp4")
									.setStarred(true)
									.build();

							return mDriveResourceClient.createFile(parent, changeSet, contents);
						}
					}).addOnSuccessListener(new OnSuccessListener<DriveFile>() {
						@Override
						public void onSuccess(DriveFile driveFile) {

							if (pre.getBoolean("app_notification", true)) {

								sendNotificationVideoRecording("Upload file to Google Drive successfully", "");

							}

						}
					}).addOnFailureListener(new OnFailureListener() {
						@Override
						public void onFailure(@NonNull Exception e) {

							if (pre.getBoolean("app_notification", true)) {

								sendNotificationVideoRecording("Upload file to Google Drive failed", "");

							}

						}
					});







		}





	}





}


