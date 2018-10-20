package thanhnguyen.com.sercetvideorecorder.lockpattern;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import java.util.List;

import me.zhanghai.android.patternlock.ConfirmPatternActivity;
import me.zhanghai.android.patternlock.PatternUtils;
import me.zhanghai.android.patternlock.PatternView;
import thanhnguyen.com.sercetvideorecorder.HomeScreenActivity;

/**
 * Created by THANHNGUYEN on 12/24/17.
 */

public class ConfirmPatternActivity_Setting extends ConfirmPatternActivity {

    @Override
    protected boolean isStealthModeEnabled() {

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        return sp.getBoolean("pattern_invisible", false);
    }

    @Override
    protected boolean isPatternCorrect(List<PatternView.Cell> pattern) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        String patternSha1 = sp.getString("patternSha1", null);
        return TextUtils.equals(PatternUtils.patternToSha1String(pattern), patternSha1);
    }

    @Override
    protected void onForgotPassword() {

        boolean lockstartup;

        if(getIntent().getExtras()!=null){

            lockstartup = getIntent().getExtras().getBoolean("lockstartup");


        } else {


            lockstartup = false;
        }


        if(lockstartup){

            startActivity(new Intent(this, PinLockView_Setting.class).putExtra("forgetpinlock_startapp", true));


        } else {


            startActivity(new Intent(this, PinLockView_Setting.class).putExtra("forgetpinlock", true));

        }

        super.onForgotPassword();
    }

    @Override
    protected void onConfirmed() {

        boolean lockstartup;

        if(getIntent().getExtras()!=null){

            lockstartup = getIntent().getExtras().getBoolean("lockstartup");


        } else {


            lockstartup = false;
        }

        if(lockstartup){

            startActivity(new Intent(this, HomeScreenActivity.class));


        } else {

            startActivity(new Intent(this, SetPatternActivity_Setting.class));

        }


        finish();

        super.onConfirmed();
    }

}
