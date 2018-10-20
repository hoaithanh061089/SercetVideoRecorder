package thanhnguyen.com.sercetvideorecorder.cameraservice;



import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Camera;
import android.hardware.Camera.Face;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.FrameLayout;

public class CameraRecording extends SurfaceView implements SurfaceHolder.Callback{
	
	private SurfaceHolder mHolder;
    private Camera mCamera;
    Face[] detectedFaces;
    FrameLayout framelayout;
    SharedPreferences pre;
    Context context;
    
public CameraRecording(Context context, Camera camera) {
		super(context);
		mCamera = camera;
		mHolder = getHolder();
        mHolder.addCallback(this);
        this.context = context;
        
        
        // deprecated setting, but required on Android versions prior to 3.0
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {

	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {

            

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		
	}


}
