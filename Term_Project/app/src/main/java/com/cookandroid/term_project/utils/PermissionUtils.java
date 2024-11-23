package com.cookandroid.term_project.utils;

import android.app.Activity;
import android.content.pm.PackageManager;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

public class PermissionUtils {

    public static void requestLocationPermissions(Activity activity, int requestCode, boolean finishActivity) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, android.Manifest.permission.ACCESS_FINE_LOCATION)
                || ActivityCompat.shouldShowRequestPermissionRationale(activity, android.Manifest.permission.ACCESS_COARSE_LOCATION)) {
            // Show an explanation to the user *asynchronously* -- don't block
            // this thread waiting for the explanation! After the user
            // sees the explanation, try again to request the permission.
            // You can show a dialog here or a Snackbar.
        } else {
            // No explanation needed; request the permission
            ActivityCompat.requestPermissions(activity,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION},
                    requestCode);
        }
    }

    public static boolean isPermissionGranted(@NonNull String[] permissions, @NonNull int[] grantResults, String permission) {
        for (int i = 0; i < permissions.length; i++) {
            if (permissions[i].equals(permission) && grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                return true;
            }
        }
        return false;
    }

    public static class PermissionDeniedDialog extends androidx.fragment.app.DialogFragment {
        public static PermissionDeniedDialog newInstance(boolean finishActivity) {
            PermissionDeniedDialog dialog = new PermissionDeniedDialog();
            // You can pass arguments to the dialog if needed
            return dialog;
        }

        // Implement dialog creation and display logic here
    }
}
