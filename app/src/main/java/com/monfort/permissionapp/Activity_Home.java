package com.monfort.permissionapp;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;

public class Activity_Home extends AppCompatActivity {

    private static final int MANUALLY_CONTACTS_PERMISSION_REQUEST_CODE = 124;

    private MaterialButton home_BTN_location;
    private TextView home_LBL_info;


    private boolean FORCE = true;
    private boolean CAN_GRANT_MANUALLY = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new);

        home_BTN_location = findViewById(R.id.home_BTN_location);
        home_LBL_info = findViewById(R.id.home_LBL_info);

        home_BTN_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getLocationPermission(Activity_Home.this);
            }
        });
    }

    private void getLocationPermission(Context context) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // You can use the API that requires the permission.
            Toast.makeText(context, "GOOD", Toast.LENGTH_SHORT).show();
            action();
        } else if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
            // In an educational UI, explain to the user why your app requires this
            // permission for a specific feature to behave as expected. In this UI,
            // include a "cancel" or "no thanks" button that allows the user to
            // continue using your app without granting the permission.
            Toast.makeText(context, "Can't show window", Toast.LENGTH_SHORT).show();
            requestWithExplainDialog();
        } else {
            // You can directly ask for the permission.
            // The registered ActivityResultCallback gets the result of this request.
            firstRequestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    private void getLocationPermission() {
        if (FORCE  &&  shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
            // In an educational UI, explain to the user why your app requires this
            // permission for a specific feature to behave as expected. In this UI,
            // include a "cancel" or "no thanks" button that allows the user to
            // continue using your app without granting the permission.
            requestWithExplainDialog();
        } else if (CAN_GRANT_MANUALLY  &&  !shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
            // You can directly ask for the permission.
            // The registered ActivityResultCallback gets the result of this request.
            manuallyDialog();
        } else {
            cantAction();
        }
    }

    private void requestWithExplainDialog() {
        String message = "We need permission for...";
        AlertDialog alertDialog =
                new AlertDialog.Builder(Activity_Home.this)
                        .setMessage(message)
                        .setCancelable(false)
                        .setPositiveButton(getString(android.R.string.ok),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
                                    }
                                }).show();
        alertDialog.setCanceledOnTouchOutside(true);
    }

    private void manuallyDialog() {
        if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
            cantAction();
            return;
        }

        String message = "Setting screen if user have permanently disable the permission by clicking Don't ask again checkbox.";
        AlertDialog alertDialog =
                new AlertDialog.Builder(Activity_Home.this)
                        .setMessage(message)
                        .setCancelable(false)
                        .setPositiveButton(getString(android.R.string.ok),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent intent = new Intent();
                                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                                        intent.setData(uri);
                                        manuallyActivityResultLauncher.launch(intent);
                                        dialog.cancel();
                                    }
                                }).show();
        alertDialog.setCanceledOnTouchOutside(false);
    }

    private ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    Log.d("pttt", "Is Granted");
                    action();
                    // Permission is granted. Continue the action or workflow in your
                    // app.
                } else {
                    getLocationPermission();


                    Log.d("pttt", "No Granted");
                    // Explain to the user that the feature is unavailable because the
                    // features requires a permission that the user has denied. At the
                    // same time, respect the user's decision. Don't link to system
                    // settings in an effort to convince the user to change their
                    // decision.
                }
            });

    private ActivityResultLauncher<String> firstRequestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    Log.d("pttt", "Is Granted");
                    action();
                    // Permission is granted. Continue the action or workflow in your
                    // app.
                } else {
                    requestWithExplainDialog();
                    Log.d("pttt", "No Granted");
                    // Explain to the user that the feature is unavailable because the
                    // features requires a permission that the user has denied. At the
                    // same time, respect the user's decision. Don't link to system
                    // settings in an effort to convince the user to change their
                    // decision.
                }
            });

    ActivityResultLauncher<Intent> manuallyActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        if (ContextCompat.checkSelfPermission(Activity_Home.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                            action();
                        } else if (FORCE) {
                            getLocationPermission(Activity_Home.this);
                        } else {
                            cantAction();
                        }
                    }
                }
            });

    private void action() {
        Log.d("pttt", "action ! !");
    }

    private void cantAction() {
        Log.d("pttt", "Cant Action ! !");
    }

}