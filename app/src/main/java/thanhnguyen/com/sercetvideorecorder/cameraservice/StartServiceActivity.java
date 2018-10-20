package thanhnguyen.com.sercetvideorecorder.cameraservice;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import java.util.List;

import thanhnguyen.com.sercetvideorecorder.utility.CheckPermission;

public class StartServiceActivity extends Activity {

	Intent shortcutIntent;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
	}
		
@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	if(isMyServiceRunning(DrawOverCameraService.class)){
		shortcutIntent = new Intent(this,
				DrawOverCameraService.class);

		Handler handler = new Handler(Looper.getMainLooper());
		handler.postDelayed(new Runnable(){

			@Override
			public void run() {

				stopService(shortcutIntent);

			}

		}, 3000);


		finish();

	} else {
		List<String> listPermissionsNeeded = CheckPermission.checkCameraRelatedPermission(this);

		if(listPermissionsNeeded.isEmpty()){

			if(CheckPermission.checkDrawOverlayPermission(this)){

				Intent shortcutIntent1 = new Intent(this,
						DrawOverCameraService.class).putExtra("servicetrigger", "shortcut");
				startService(shortcutIntent1);
				finish();

			}



		} else {

			CheckPermission.sendNotificationVideoRecording(this, "Permission needed to start recording",
					listPermissionsNeeded.toString());
			finish();

		}


	}
		
		
	}

	private boolean isMyServiceRunning(Class<?> serviceClass) {
	    ActivityManager manager = (ActivityManager) this.getSystemService(Context.ACTIVITY_SERVICE);
	    for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
	        if (serviceClass.getName().equals(service.service.getClassName())) {
	            return true;
	        }
	    }
	    return false;
	}

}