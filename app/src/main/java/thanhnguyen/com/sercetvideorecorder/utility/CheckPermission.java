package thanhnguyen.com.sercetvideorecorder.utility;

import android.Manifest;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

import thanhnguyen.com.sercetvideorecorder.R;
import thanhnguyen.com.sercetvideorecorder.StartupActivity;

/**
 * Created by THANHNGUYEN on 12/26/17.
 */

public class CheckPermission {

    public static List<String> checkCameraRelatedPermission(Context context) {

        int permissionCamera = ContextCompat.checkSelfPermission(context,
                Manifest.permission.CAMERA);
        int permissionMicro = ContextCompat.checkSelfPermission(context,
                Manifest.permission.RECORD_AUDIO);
        int permissionStorage = ContextCompat.checkSelfPermission(context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int permissionCall = ContextCompat.checkSelfPermission(context,
                Manifest.permission.PROCESS_OUTGOING_CALLS);
        int permissionsShortcut = ContextCompat.checkSelfPermission(context,
                Manifest.permission.INSTALL_SHORTCUT);
        int permissionsLocation = ContextCompat.checkSelfPermission(context,
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

        return listPermissionsNeeded;


    }
    public static void sendNotificationVideoRecording(Context context, String title, String messageBody) {
        Intent intent = new Intent(context, StartupActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        String channelId = context.getString(R.string.app_name);
        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(context, channelId)
                        .setSmallIcon(R.drawable.eye)
                        .setContentTitle(title)
                        .setContentText(messageBody)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(01
                /* ID of notification */, notificationBuilder.build());
    }

    public static boolean checkDrawOverlayPermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!Settings.canDrawOverlays(context)) {

                CheckPermission.sendNotificationVideoRecording(context, "Draw overlay permission needed to start recording",
                        "");

                return false;

            }  else {


                return true;
            }
        } else {

            return true;
        }
    }
}
