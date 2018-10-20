package thanhnguyen.com.sercetvideorecorder.lockpattern;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.animation.AnimationUtils;

import com.andrognito.pinlockview.IndicatorDots;
import com.andrognito.pinlockview.PinLockListener;
import com.andrognito.pinlockview.PinLockView;

import java.security.MessageDigest;

import thanhnguyen.com.sercetvideorecorder.HomeScreenActivity;
import thanhnguyen.com.sercetvideorecorder.R;

/**
 * Created by THANHNGUYEN on 12/24/17.
 */

public class PinLockView_Setting extends AppCompatActivity implements PinLockListener {

    boolean createpinlock = false;
    boolean forgetpinlock = false;
    boolean forgetpinlock_startapp =false;
    PinLockView mPinLockView;
    IndicatorDots mIndicatorDots;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pinlocklayout);
        createpinlock=getIntent().getExtras().getBoolean("createpinlock");
        forgetpinlock=getIntent().getExtras().getBoolean("forgetpinlock");
        forgetpinlock_startapp=getIntent().getExtras().getBoolean("forgetpinlock_startapp");
        mPinLockView = (PinLockView) findViewById(R.id.pin_lock_view);
        mIndicatorDots = (IndicatorDots) findViewById(R.id.indicator_dots);
        mPinLockView.attachIndicatorDots(mIndicatorDots);
        mPinLockView.setPinLockListener(this);
    }

    @Override
    public void onComplete(String pin) {

        if(createpinlock){

            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
            sp.edit().putString("pinlockview", getSha1Hex(pin)).apply();
            sp.edit().putBoolean("lock_pattern_setup", true).apply();


            finish();

        }

        if(forgetpinlock){

            String sha1 = getSha1Hex(pin);
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
            String savedsha1= sp.getString("pinlockview", null);
            if(sha1.equals(savedsha1)){

                finish();
                startActivity(new Intent(this, SetPatternActivity_Setting.class));

            } else {

                mPinLockView.resetPinLockView();
                mIndicatorDots.startAnimation(AnimationUtils.loadAnimation(this, R.anim.shake));

            }

        }

        if(forgetpinlock_startapp){

            String sha1 = getSha1Hex(pin);
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
            String savedsha1= sp.getString("pinlockview", null);
            if(sha1.equals(savedsha1)){

                finish();
                startActivity(new Intent(this, HomeScreenActivity.class));

            } else {

                mPinLockView.resetPinLockView();
                mIndicatorDots.startAnimation(AnimationUtils.loadAnimation(this, R.anim.shake));

            }



        }



    }

    @Override
    public void onEmpty() {

    }

    @Override
    public void onPinChange(int pinLength, String intermediatePin) {

    }

    public static String getSha1Hex(String clearString)
    {
        try
        {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");
            messageDigest.update(clearString.getBytes("UTF-8"));
            byte[] bytes = messageDigest.digest();
            StringBuilder buffer = new StringBuilder();
            for (byte b : bytes)
            {
                buffer.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
            }
            return buffer.toString();
        }
        catch (Exception ignored)
        {
            ignored.printStackTrace();
            return null;
        }
    }
}
