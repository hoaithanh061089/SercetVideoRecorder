package thanhnguyen.com.sercetvideorecorder;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;

import java.util.ArrayList;

import thanhnguyen.com.sercetvideorecorder.lockpattern.ConfirmPatternActivity_Setting;
import za.co.riggaroo.materialhelptutorial.TutorialItem;
import za.co.riggaroo.materialhelptutorial.tutorial.MaterialTutorialActivity;

/**
 * Created by THANHNGUYEN on 12/24/17.
 */

public class StartupActivity extends Activity {

    int REQUEST_CODE_INTRO=10;
    int color = R.color.material_blue_400;
    SharedPreferences sp;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sp = PreferenceManager.getDefaultSharedPreferences(this);

        if(sp.getBoolean("firststartup_appintro", true)){

            loadTutorial();

            return;


        }

        if(sp.getBoolean("lock_pattern_allow", false)){

            if(sp.getString("patternSha1", null)!=null) {

                startActivity(new Intent(this, ConfirmPatternActivity_Setting.class).putExtra("lockstartup", true));
                finish();

            } else {

                startActivity(new Intent(this, HomeScreenActivity.class));
                finish();


            }


        } else {

            startActivity(new Intent(this, HomeScreenActivity.class));
            finish();


        }



    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_INTRO) {

            sp.edit().putBoolean("firststartup_appintro", false).apply();

            startActivity(new Intent(this, HomeScreenActivity.class));
            finish();


        }
    }

    public void loadTutorial() {
        Intent mainAct = new Intent(this, MaterialTutorialActivity.class);
        mainAct.putParcelableArrayListExtra(MaterialTutorialActivity.MATERIAL_TUTORIAL_ARG_TUTORIAL_ITEMS, getTutorialItems(this));
        startActivityForResult(mainAct, REQUEST_CODE_INTRO);

    }
    private ArrayList<TutorialItem> getTutorialItems(Context context) {

        TutorialItem tutorialItem1 = new TutorialItem(
                "App setting",
                "Change your recording options at app setting",
                color,
                R.drawable.tut1,
                R.drawable.tut1);
        TutorialItem tutorialItem2 = new TutorialItem(
                "Video",
                "Click into video to show options",
                color,
                R.drawable.tut2,
                R.drawable.tut2);
        TutorialItem tutorialItem3 = new TutorialItem(
                "Quick shortcut",
                "Press shortcut to start or stop recording",
                color,
                R.drawable.tut3,
                R.drawable.tut3);
        TutorialItem tutorialItem4 = new TutorialItem(
                "Dial number",
                "Dial to start or stop recording.",
                color,
                R.drawable.tut4,
                R.drawable.tut4);



        ArrayList<TutorialItem> tutorialItems = new ArrayList<>();
        tutorialItems.add(tutorialItem1);
        tutorialItems.add(tutorialItem2);
        tutorialItems.add(tutorialItem3);
        tutorialItems.add(tutorialItem4);


        return tutorialItems;
    }

}
