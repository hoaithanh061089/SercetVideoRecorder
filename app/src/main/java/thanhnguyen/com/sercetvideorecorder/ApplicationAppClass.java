package thanhnguyen.com.sercetvideorecorder;

import android.content.Context;
import android.support.multidex.MultiDexApplication;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

public class ApplicationAppClass extends MultiDexApplication {
        private static GoogleAnalytics sAnalytics;
        private static Tracker sTracker;
    @Override
    public void onCreate() {
        super.onCreate();
        Fresco.initialize(this);

    }
        synchronized public static Tracker getDefaultTracker(Context context) {
            sAnalytics = GoogleAnalytics.getInstance(context);
            if (sTracker == null) {
                sTracker = sAnalytics.newTracker(R.xml.global_tracker);
            }

            return sTracker;
        }
}
