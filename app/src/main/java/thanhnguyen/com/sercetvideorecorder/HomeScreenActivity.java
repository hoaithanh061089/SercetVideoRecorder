package thanhnguyen.com.sercetvideorecorder;

import android.Manifest;
import android.app.ActivityManager;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.folderselector.FolderChooserDialog;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import thanhnguyen.com.sercetvideorecorder.adapter.AppInfoTabsPagerAdapter;
import thanhnguyen.com.sercetvideorecorder.adapter.ShareAppCustomAdapter;
import thanhnguyen.com.sercetvideorecorder.adapter.ShareAppCustomItem;
import thanhnguyen.com.sercetvideorecorder.cameraservice.DrawOverCameraService;
import thanhnguyen.com.sercetvideorecorder.fragment.GalleryFragment;
import thanhnguyen.com.sercetvideorecorder.fragment.SettingViewFragmnent;
import thanhnguyen.com.sercetvideorecorder.utility.CheckPermission;
import za.co.riggaroo.materialhelptutorial.TutorialItem;
import za.co.riggaroo.materialhelptutorial.tutorial.MaterialTutorialActivity;

public class HomeScreenActivity extends AppCompatActivity implements FolderChooserDialog.FolderCallback {

    /**
     * code to post/handler request for permission
     */
    public final static int DrawOverlayPermission = 1;
    int REQUEST_ID_MULTIPLE_PERMISSIONS = 2;
    public static TextView username_header;
    FloatingActionsMenu actionmenu;
    FloatingActionButton startvideo;
    FloatingActionButton stopvideo;
    int REQUEST_CODE_INTRO=10;
    int color = R.color.material_blue_400;
    boolean checkcamerapermission = false;
    boolean checkDrawoPermission = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        AppInfoTabsPagerAdapter mAdapter= new AppInfoTabsPagerAdapter(getSupportFragmentManager(), this);
        ViewPager pager = (ViewPager) findViewById(R.id.view_pager);
        pager.setAdapter(mAdapter);
        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

                if(position==1){

                    GalleryFragment galleryfrag = (GalleryFragment)
                            getSupportFragmentManager().getFragments().get(1);
                    galleryfrag.updatevideo();





                }

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        TabLayout tabs = (TabLayout) findViewById(R.id.tabs);
        actionmenu = findViewById(R.id.multiple_actions);
        startvideo = findViewById(R.id.startvideo);
        stopvideo = findViewById(R.id.stopvideo);
        startvideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                List<String> listPermissionsNeeded = CheckPermission.checkCameraRelatedPermission(getBaseContext());

                if(listPermissionsNeeded.isEmpty()){

                    if(CheckPermission.checkDrawOverlayPermission(getBaseContext())){

                        Intent shortcutIntent1 = new Intent(getBaseContext(),
                                DrawOverCameraService.class).putExtra("servicetrigger", "startvideobutton");
                        startService(shortcutIntent1);

                    }



                } else {

                    CheckPermission.sendNotificationVideoRecording(getBaseContext(), "Permission needed to start recording",
                            listPermissionsNeeded.toString());

                }



            }
        });
        stopvideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(isMyServiceRunning(DrawOverCameraService.class)){
                    final Intent shortcutIntent = new Intent(getBaseContext(),
                            DrawOverCameraService.class);

                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.postDelayed(new Runnable(){

                        @Override
                        public void run() {

                            stopService(shortcutIntent);

                        }

                    }, 3000);


                }

            }
        });

        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int tabposition = tab.getPosition();
                if(tabposition==0){

                    actionmenu.setVisibility(View.VISIBLE);


                } else {


                    actionmenu.setVisibility(View.INVISIBLE);

                }


            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        tabs.setupWithViewPager(pager);
        final DrawerLayout mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                mDrawerLayout.closeDrawers();

                int id = item.getItemId();
                switch (id) {

                    case R.id.shareapp:
                        Uri uri_share = Uri.parse("http://play.google.com/store/apps/details?id="
                                + getPackageName());

                        shareContent("Share app with:", "Installing this app to record your video secretly. " +
                                 uri_share.toString());


                        break;

                    case R.id.rateapp:

                        try {
                            Uri uri = Uri.parse("market://details?id=" + getPackageName());
                            Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
                            startActivity(goToMarket);
                        } catch (ActivityNotFoundException e) {
                            startActivity(new Intent(Intent.ACTION_VIEW,
                                    Uri.parse("http://play.google.com/store/apps/details?id="
                                            + getPackageName())));
                        }


                        break;

                    case R.id.tutorial:

                        loadTutorial();

                        break;





                }




                return false;
            }
        });
        View headerLayout = navigationView.getHeaderView(0);
        username_header = (TextView) headerLayout.findViewById(R.id.username_header);

        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(this);
        if (acct != null) {
            String personName = acct.getDisplayName();
            username_header.setText(personName);

        }


    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if(!checkcamerapermission){
                checkcamerapermission = true;
                checkCameraRelatedPermission();
            }



        }

        if(!checkDrawoPermission){
            checkDrawOverlayPermission();
            checkDrawoPermission=true;


        }


    }

    public void checkDrawOverlayPermission() {
        /** check if we already  have permission to draw over other apps */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!Settings.canDrawOverlays(this)) {
                /** if not construct intent to request permission */
                new MaterialDialog.Builder(HomeScreenActivity.this)
                        .title("Permission needed")
                        .content("This app need Draw Over Other Apps permission to work. Please toggle on app to allow permission")
                        .positiveText("Agree")
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                        Uri.parse("package:" + getPackageName()));
                                /** request permission via start activity for result */
                                startActivityForResult(intent, DrawOverlayPermission);


                            }
                        })
                        .show();

            }
        }
    }


    public void checkCameraRelatedPermission() {

        int permissionCamera = ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA);
        int permissionMicro = ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO);
        int permissionStorage = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int permissionCall = ContextCompat.checkSelfPermission(this,
                Manifest.permission.PROCESS_OUTGOING_CALLS);
        int permissionsShortcut = ContextCompat.checkSelfPermission(this,
                Manifest.permission.INSTALL_SHORTCUT);
        int permissionsLocation = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);

        List<String> listPermissionsNeeded = new ArrayList<>();
        if (permissionCamera != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.CAMERA);
        }
        if (permissionMicro != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.RECORD_AUDIO);
        }
        if (permissionStorage != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (permissionCall != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.PROCESS_OUTGOING_CALLS);
        }
        if (permissionsShortcut != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.INSTALL_SHORTCUT);
        }
        if (permissionsLocation != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }

        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), REQUEST_ID_MULTIPLE_PERMISSIONS);

        }


    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {

        if(requestCode==REQUEST_ID_MULTIPLE_PERMISSIONS){

            Map<String, Integer> perms = new HashMap<>();
            // Initialize the map with both permissions
            perms.put(Manifest.permission.CAMERA, PackageManager.PERMISSION_GRANTED);
            perms.put(Manifest.permission.RECORD_AUDIO, PackageManager.PERMISSION_GRANTED);
            perms.put(Manifest.permission.WRITE_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
            perms.put(Manifest.permission.PROCESS_OUTGOING_CALLS, PackageManager.PERMISSION_GRANTED);
            perms.put(Manifest.permission.INSTALL_SHORTCUT, PackageManager.PERMISSION_GRANTED);
            perms.put(Manifest.permission.ACCESS_FINE_LOCATION, PackageManager.PERMISSION_GRANTED);



            if (grantResults.length > 0) {

                for (int i = 0; i < permissions.length; i++) {

                    perms.put(permissions[i], grantResults[i]);


                }

                if (perms.get(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                        && perms.get(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
                        && perms.get(Manifest.permission.PROCESS_OUTGOING_CALLS) == PackageManager.PERMISSION_GRANTED
                        && perms.get(Manifest.permission.INSTALL_SHORTCUT) == PackageManager.PERMISSION_GRANTED
                        && perms.get(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                        && perms.get(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {

                     Toast.makeText(getBaseContext(), "All permissions granted", Toast.LENGTH_SHORT).show();

                    checkDrawoPermission=true;
                    checkDrawOverlayPermission();

                    SettingViewFragmnent.videoPathSetter(SettingViewFragmnent.getOutputMediaFile().getPath());



                } else {


                    if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)
                            || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECORD_AUDIO)
                            || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.PROCESS_OUTGOING_CALLS)
                            || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.INSTALL_SHORTCUT)
                            || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)
                            || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                        Toast.makeText(getBaseContext(), "Permission required for this app", Toast.LENGTH_SHORT).show();



                    } else {

                        new MaterialDialog.Builder(HomeScreenActivity.this)
                                .title("Permisson denied")
                                .content("App can' work without permission? Do you want to grant app permisson?")
                                .positiveText("Agree")
                                .onPositive(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                                        Intent intent = new Intent();
                                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                                        intent.setData(uri);
                                        startActivity(intent);


                                    }
                                })
                                .show();


                    }


                }



            }


        }


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == DrawOverlayPermission) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (!Settings.canDrawOverlays(this)) {

                    Toast.makeText(getBaseContext(), "Permission required for this app to work. App will quit!", Toast.LENGTH_SHORT).show();

                    finish();

                }
            }
        }
    }

    @Override
    public void onFolderSelection(@NonNull FolderChooserDialog dialog, @NonNull File folder) {

        SettingViewFragmnent settingfrag = (SettingViewFragmnent)
                getSupportFragmentManager().getFragments().get(0);
        settingfrag.updateVideoPath(folder);



    }

    @Override
    public void onFolderChooserDismissed(@NonNull FolderChooserDialog dialog) {

    }

    @Override
    protected void onStop() {
       SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);

        if(sp.getBoolean("hide_icon", false )){

            PackageManager p = getPackageManager();
            p.setComponentEnabledSetting(new ComponentName(this, thanhnguyen.com.sercetvideorecorder.StartupActivity.class),
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP);


        }
        super.onStop();
    }
    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
    public void shareContent(final String title, final String content){

        final Dialog infodialog = new Dialog(HomeScreenActivity.this);
        infodialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        infodialog.setContentView(R.layout.shareappcustomlistview);
        infodialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        infodialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        ((TextView) infodialog.findViewById(R.id.shareapptv)).setText(title);
        final ListView sharelistview  = (ListView) infodialog.findViewById(R.id.shareappcustomlistview);

        ArrayList listitem  = new ArrayList<ShareAppCustomItem> ();


        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        PackageManager pm = getBaseContext().getPackageManager();
        List<ResolveInfo> resInfos = pm.queryIntentActivities(intent, 0);

        for (ResolveInfo resInfo : resInfos) {
            String packageClassName = resInfo.activityInfo.packageName;
            String className = resInfo.activityInfo.name;
            Drawable icon = resInfo.loadIcon(pm);
            CharSequence label = resInfo.loadLabel(pm);
            listitem.add(new ShareAppCustomItem((String) label, icon,  packageClassName, className));


        }
        ShareAppCustomAdapter shareadapter = new ShareAppCustomAdapter(HomeScreenActivity.this,  listitem);
        sharelistview.setAdapter(shareadapter);

        Button cancelbutton = (Button) infodialog.findViewById(R.id.cancelbutton);
        cancelbutton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                infodialog.dismiss();


            }
        });
        sharelistview.setOnItemClickListener(new AdapterView.OnItemClickListener(){

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {

                ShareAppCustomItem singleitem = (ShareAppCustomItem) sharelistview.getItemAtPosition(arg2);
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.setComponent(new ComponentName(singleitem.getPackageClassName(), singleitem.getClassName()));
                shareIntent.putExtra(Intent.EXTRA_TEXT,
                        content);

                startActivity(shareIntent);
                infodialog.dismiss();


            }


        });

        infodialog.show();

    };
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

