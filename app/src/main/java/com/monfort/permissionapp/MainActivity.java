package com.monfort.permissionapp;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;

public class MainActivity extends AppCompatActivity {

    private MaterialButton main_BTN_contacts;
    private TextView main_LBL_info;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        main_BTN_contacts = findViewById(R.id.main_BTN_contacts);
        main_LBL_info = findViewById(R.id.main_LBL_info);

        main_BTN_contacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getContacts();
            }
        });
    }

    private void getContacts() {
        boolean isGranted = checkForPermission();

        if (!isGranted) {
            requestPermission();
            return;
        }

        String data = "";
        ContentResolver cr = getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);

        if ((cur != null ? cur.getCount() : 0) > 0) {
            while (cur != null && cur.moveToNext()) {
                String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
                String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

                if (cur.getInt(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
                    Cursor pCur = cr.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            new String[]{id}, null);
                    while (pCur.moveToNext()) {
                        String phoneNo = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                        data += "\n" + name + ": " + phoneNo;
                    }
                    pCur.close();
                }
            }
        }
        if (cur != null) {
            cur.close();
        }

        main_LBL_info.setText(data);
    }

    private static final int PERMISSION_CONTACTS_REQUEST_CODE = 123;
    private static final int MANUALLY_CONTACTS_PERMISSION_REQUEST_CODE = 124;

    private void requestPermission() {
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.READ_CONTACTS},
                PERMISSION_CONTACTS_REQUEST_CODE);
    }

    private void requestPermissionWithRationaleCheck() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.READ_CONTACTS)) {
            Log.d("pttt", "shouldShowRequestPermissionRationale = true");
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.READ_CONTACTS},
                    PERMISSION_CONTACTS_REQUEST_CODE);
        } else {
            Log.d("pttt", "shouldShowRequestPermissionRationale = false");
            openPermissionSettingDialog();
        }
    }

    private void openPermissionSettingDialog() {
        String message = "Setting screen if user have permanently disable the permission by clicking Don't ask again checkbox.";
        AlertDialog alertDialog =
                new AlertDialog.Builder(MainActivity.this)
                        .setMessage(message)
                        .setPositiveButton(getString(android.R.string.ok),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent intent = new Intent();
                                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                                        intent.setData(uri);
                                        startActivityForResult(intent, MANUALLY_CONTACTS_PERMISSION_REQUEST_CODE);
                                        dialog.cancel();
                                    }
                                }).show();
        alertDialog.setCanceledOnTouchOutside(true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == MANUALLY_CONTACTS_PERMISSION_REQUEST_CODE) {
            getContacts();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_CONTACTS_REQUEST_CODE: {

                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getContacts();
                } else {
                    requestPermissionWithRationaleCheck();
                    Toast.makeText(MainActivity.this, "Permission denied to read your External storage", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }


    private boolean checkForPermission() {
        if ( ContextCompat.checkSelfPermission( this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        return false;
    }


}