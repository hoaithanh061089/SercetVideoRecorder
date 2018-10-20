package thanhnguyen.com.sercetvideorecorder.fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.SwitchPreferenceCompat;
import android.text.InputType;
import android.view.View;
import android.widget.Spinner;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.folderselector.FolderChooserDialog;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.valdesekamdem.library.mdtoast.MDToast;

import java.io.File;

import thanhnguyen.com.sercetvideorecorder.ApplicationAppClass;
import thanhnguyen.com.sercetvideorecorder.HomeScreenActivity;
import thanhnguyen.com.sercetvideorecorder.R;
import thanhnguyen.com.sercetvideorecorder.adapter.CustomSpinnerAdapter;
import thanhnguyen.com.sercetvideorecorder.cameraservice.StartServiceActivity;
import thanhnguyen.com.sercetvideorecorder.lockpattern.ConfirmPatternActivity_Setting;
import thanhnguyen.com.sercetvideorecorder.lockpattern.SetPatternActivity_Setting;

/**
 * Created by THANHNGUYEN on 12/23/17.
 */

public class SettingViewFragmnent extends PreferenceFragmentCompat {

    static PreferenceScreen video_path;
    SharedPreferences sp;
    Spinner spinner=null;
    TextInputLayout shortcut_name = null;
    GoogleSignInClient mGoogleSignInClient;
    int RC_SIGN_IN=0;
    MaterialDialog login_dialog;
    SwitchPreferenceCompat lock_pattern_allow, zoominout;
    String video_path_record;
    int icon[] = {R.drawable.eye, R.drawable.hat, R.drawable.movie, R.drawable.music, R.drawable.note, R.drawable.pencil};


    @Override
    public void onResume() {
        super.onResume();
        if(sp.getString("patternSha1", null)==null){

            lock_pattern_allow.setChecked(false);

        }

        if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {

            NotificationManager mNotificationManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
            if (!mNotificationManager.isNotificationPolicyAccessGranted()) {

                zoominout.setChecked(false);


            }

        }

        if(sp.getBoolean("create_recordingshortcut", true)){


            if(android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {

                addshortcutlolilopstartapp(icon);

            } else {

                addShortcutstartapp(icon);

            }

            sp.edit().putBoolean("create_recordingshortcut", false).apply();


        }


    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        Tracker tracker = ApplicationAppClass.getDefaultTracker(getContext());
        tracker.setScreenName("Setting Fragment");
        tracker.send(new HitBuilders.ScreenViewBuilder().build());
        addPreferencesFromResource(R.xml.app_preferences);
        sp = PreferenceManager.getDefaultSharedPreferences(getContext());
        zoominout = (SwitchPreferenceCompat) findPreference("zoominout");
        final SwitchPreferenceCompat hide_icon = (SwitchPreferenceCompat) findPreference("hide_icon");
        hide_icon.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {

                if(!(boolean) newValue){

                    PackageManager p =  getActivity().getPackageManager();
                    p.setComponentEnabledSetting(new ComponentName(getActivity(), thanhnguyen.com.sercetvideorecorder.StartupActivity.class),
                            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                            PackageManager.DONT_KILL_APP);

                }

                return true;

            }
        });

        zoominout.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if((boolean) newValue){

                    if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {

                        NotificationManager mNotificationManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
                        if (!mNotificationManager.isNotificationPolicyAccessGranted()) {

                            new MaterialDialog.Builder(getActivity())
                                    .title("Permission needed")
                                    .content("This feature need Do Not Disturb permission to work. Please toggle on app to allow permission")
                                    .positiveText("Agree")
                                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                                        @Override
                                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                                            Intent intent = new Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
                                            startActivity(intent);


                                        }
                                    })
                                    .show();








                        }

                    }


                }


                return true;
            }
        });




        final PreferenceScreen call_open_app = (PreferenceScreen) findPreference("call_open_app");
        String defaultnumber = sp.getString("app_opendialnumber", "*#6789#*");
        call_open_app.setTitle("Dial " + defaultnumber + " to open app.");
        call_open_app.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

                new MaterialDialog.Builder(getActivity())
                        .title("Dial to open app")
                        .inputType(InputType.TYPE_CLASS_PHONE)
                        .input("Your dialing number", "", new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(MaterialDialog dialog, CharSequence input) {

                                if(input.toString().isEmpty()){

                                    return;
                                }


                                call_open_app.setTitle("Dial " + input.toString() + " to open app.");
                                sp.edit().putString("app_opendialnumber", input.toString()).apply();


                            }
                        }).show();


                return false;
            }
        });
        final PreferenceScreen call_video_recording = (PreferenceScreen) findPreference("call_video_recording");
        String defaultnumber_record = sp.getString("videodialnumber", "*#0610#*");
        call_video_recording.setTitle("Dial" + " " + defaultnumber_record + " to start and stop recording.");
        call_video_recording.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

                new MaterialDialog.Builder(getActivity())
                        .title("Dial to start and stop recording")
                        .content("Dial your number to start recording. When done, dial this number again to stop recording.")
                        .inputType(InputType.TYPE_CLASS_PHONE)
                        .input("Your dialing number", "", new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(MaterialDialog dialog, CharSequence input) {

                                if(input.toString().isEmpty()){

                                    return;
                                }

                                call_video_recording.setTitle("Dial " + input.toString() + " to start and stop video recording.");
                                sp.edit().putString("videodialnumber", input.toString()).apply();

                            }
                        }).show();


                return false;
            }
        });
        final PreferenceScreen add_shortcut = (PreferenceScreen) findPreference("add_shortcut");
        add_shortcut.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

                String[] names = {"Eye", "Hat", "Movie", "Music", "Note", "Pencil"};
                  MaterialDialog dialog = new MaterialDialog.Builder(getActivity()).onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                        if(shortcut_name.getEditText().getText().toString().isEmpty()){

                            shortcut_name.setErrorEnabled(true);
                            shortcut_name.setError("Name must not be empty");

                        } else {

                            if(android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {

                                addshortcutlolilop(icon);


                            } else {

                                addShortcut(icon);


                            }

                            dialog.dismiss();

                        }


                    }
                })
                        .title("New shortcut")
                        .customView(R.layout.custom_view_shortcut, true)
                        .positiveText("OK")
                        .autoDismiss(false)
                        .show();

                spinner = dialog.getCustomView().findViewById(R.id.shortcut_icon);
                spinner.setSelection(0);
                CustomSpinnerAdapter customAdapter = new CustomSpinnerAdapter(getActivity(), icon, names);
                spinner.setAdapter(customAdapter);
                shortcut_name = dialog.getCustomView().findViewById(R.id.shortcut_name);


                return false;
            }
        });
        video_path = (PreferenceScreen) findPreference("video_path");
        video_path_record = sp.getString("video_path", null);
        if(video_path_record!=null){

            if(!new File(video_path_record).exists()){

                int permissionStorage = ContextCompat.checkSelfPermission(getActivity(),
                        Manifest.permission.WRITE_EXTERNAL_STORAGE);
                if(permissionStorage==PackageManager.PERMISSION_GRANTED){

                    video_path_record=getOutputMediaFile().getPath();

                }

            }


        } else {

            int permissionStorage = ContextCompat.checkSelfPermission(getActivity(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if(permissionStorage==PackageManager.PERMISSION_GRANTED){

                video_path_record=getOutputMediaFile().getPath();

            }


        }

        video_path.setSummary(video_path_record);
        video_path.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

                new FolderChooserDialog.Builder(SettingViewFragmnent.this.getContext())
                        .chooseButton(R.string.string_choose_videopath)  // changes label of the choose button
                        .initialPath(video_path_record)  // changes initial path, defaults to external storage directory
                        .tag("optional-identifier")
                        .allowNewFolder(true, 0)  // pass 0 in the second parameter to use default button label
                        .show(getActivity());


                return false;
            }
        });




        lock_pattern_allow = (SwitchPreferenceCompat) findPreference("lock_pattern_allow");
        lock_pattern_allow.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {

                if((boolean) newValue){
                    if(sp.getString("patternSha1", null)==null){

                            startActivity(new Intent(getContext(), SetPatternActivity_Setting.class));

                    }



                }

                return true;

            }
        });
        PreferenceScreen lock_pattern_change = (PreferenceScreen) findPreference("lock_pattern_change");
        lock_pattern_change.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getContext());
                if(sp.getString("patternSha1", null)==null){

                    startActivity(new Intent(getContext(), SetPatternActivity_Setting.class));

                } else {

                    startActivity(new Intent(getContext(), ConfirmPatternActivity_Setting.class));


                }



                return false;
            }
        });

        PreferenceScreen google_upload_account = (PreferenceScreen) findPreference("google_upload_account");
        google_upload_account.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

                new MaterialDialog.Builder(getActivity())
                        .title("Google Account")
                        .items(R.array.googleaccount)
                        .itemsCallback(new MaterialDialog.ListCallback() {
                            @Override
                            public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                                GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                        .requestEmail()
                                        .requestScopes(Drive.SCOPE_FILE)
                                        .requestScopes(Drive.SCOPE_APPFOLDER)
                                        .build();
                                mGoogleSignInClient = GoogleSignIn.getClient(getContext(), gso);

                                if(which==0) {

                                    Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                                    startActivityForResult(signInIntent, RC_SIGN_IN);

                                    login_dialog = new MaterialDialog.Builder(getActivity())
                                            .title("Sign in process")
                                            .content("Please wait ...")
                                            .progress(true, 0)
                                            .show();

                                } else {


                                    mGoogleSignInClient.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if(task.isSuccessful()){

                                                HomeScreenActivity.username_header.setText("Secret Video Recorder");


                                                MDToast.makeText(getContext(), "Log out your google account successfully.", MDToast.LENGTH_SHORT, MDToast.TYPE_SUCCESS).show();



                                            } else {

                                                MDToast.makeText(getContext(), "Log out your google account failed.", MDToast.LENGTH_SHORT, MDToast.TYPE_SUCCESS).show();


                                            }




                                        }
                                    });


                                }










                            }
                        })
                        .show();



                return false;
            }
        });


    }

    public static File getOutputMediaFile() {

        File mediaStorageDir = new File(Environment.getExternalStorageDirectory(
        ), "Secret Video Recorder Video");

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {

                return null;
            } else {

                return mediaStorageDir;
            }
        } else {

            return mediaStorageDir;
        }


    }
    public void updateVideoPath(File folder){

        video_path.setSummary(folder.getAbsolutePath().toString());
        sp.edit().putString("video_path", folder.getAbsolutePath().toString()).apply();
        video_path_record= folder.getAbsolutePath().toString();

    }

    public void addshortcutlolilop(int icon[]){

        Intent addIntent = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME,  shortcut_name.getEditText().getText().toString());
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
                Intent.ShortcutIconResource.fromContext(getActivity(),
                        icon[spinner.getSelectedItemPosition()]));
        addIntent.putExtra("duplicate", false);

        Intent shortcutIntent = new Intent(getActivity(),
                StartServiceActivity.class);
        addIntent
                .putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);

        getActivity().sendBroadcast(addIntent);
    }
    private void addShortcut(int icon[]) {
        //Adding shortcut for MainActivity
        //on Home screen
        Intent shortcutIntent = new Intent(getActivity(),
                StartServiceActivity.class);

        Intent addIntent = new Intent();
        addIntent
                .putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME,  shortcut_name.getEditText().getText().toString());
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
                Intent.ShortcutIconResource.fromContext(getActivity(),
                        icon[spinner.getSelectedItemPosition()]));

        addIntent
                .setAction("com.android.launcher.action.INSTALL_SHORTCUT");
        @SuppressLint("WrongConstant")
        String shortcutUri = addIntent.toUri(Context.MODE_WORLD_WRITEABLE);
        getActivity().sendBroadcast(addIntent);
    }
    public void addshortcutlolilopstartapp(int icon[]){

        Intent addIntent = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME,  "Quick record");
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
                Intent.ShortcutIconResource.fromContext(getActivity(),
                        icon[0]));
        addIntent.putExtra("duplicate", false);

        Intent shortcutIntent = new Intent(getActivity(),
                StartServiceActivity.class);
        addIntent
                .putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);

        getActivity().sendBroadcast(addIntent);
    }
    private void addShortcutstartapp(int icon[]) {
        //Adding shortcut for MainActivity
        //on Home screen
        Intent shortcutIntent = new Intent(getActivity(),
                StartServiceActivity.class);

        Intent addIntent = new Intent();
        addIntent
                .putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME,  "Quick record");
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
                Intent.ShortcutIconResource.fromContext(getActivity(),
                        icon[0]));

        addIntent
                .setAction("com.android.launcher.action.INSTALL_SHORTCUT");
        @SuppressLint("WrongConstant")
        String shortcutUri = addIntent.toUri(Context.MODE_WORLD_WRITEABLE);
        getActivity().sendBroadcast(addIntent);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.

            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            if(task!=null){

                handleSignInResult(task);
            }

        }
    }
    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            HomeScreenActivity.username_header.setText(account.getDisplayName());

            MDToast.makeText(getContext(), "Sign in your google account successfully.", MDToast.LENGTH_SHORT, MDToast.TYPE_SUCCESS).show();

            if(login_dialog!=null){
                login_dialog.dismiss();
            }


        } catch (ApiException e) {

            if(login_dialog!=null){
                login_dialog.dismiss();
            }

            MDToast.makeText(getContext(), "Sign in your google account failed.", MDToast.LENGTH_SHORT, MDToast.TYPE_ERROR).show();



        }
    }

    public static void videoPathSetter(String path){

        video_path.setSummary(path);

    }
}
