package thanhnguyen.com.sercetvideorecorder.lockpattern;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.List;

import me.zhanghai.android.patternlock.PatternUtils;
import me.zhanghai.android.patternlock.PatternView;
import me.zhanghai.android.patternlock.SetPatternActivity;

/**
 * Created by THANHNGUYEN on 12/24/17.
 */

public class SetPatternActivity_Setting extends SetPatternActivity {


    @Override
    protected void onSetPattern(List<PatternView.Cell> pattern) {
        String patternSha1 = PatternUtils.patternToSha1String(pattern);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        sp.edit().putString("patternSha1", patternSha1).apply();
        startActivity(new Intent(this, PinLockView_Setting.class).putExtra("createpinlock", true));
        super.onSetPattern(pattern);
    }



}
