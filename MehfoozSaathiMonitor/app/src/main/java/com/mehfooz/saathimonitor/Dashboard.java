package com.mehfooz.saathimonitor;

import android.Manifest;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat.Builder;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import static android.content.ContentValues.TAG;

public class Dashboard extends AppCompatActivity {

    ArrayList<String> listItems = new ArrayList<>();
    ArrayAdapter<String> adapter;
    private FirebaseFirestore firestore;
    private ListView mListView;
    public static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 1;
    Builder mBuilder = new Builder(this);
    TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        adapter = new ArrayAdapter<>(this,
                R.layout.listrow,
                listItems);
        if (checkAndRequestPermissions()) {
            mListView = findViewById(R.id.listSMS);
            mListView.setAdapter(adapter);
        }

        firestore = FirebaseFirestore.getInstance();

        LocalBroadcastManager.getInstance(this).
                registerReceiver(receiver, new IntentFilter("Inbox"));

//        FloatingActionButton fab = findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });

        loginToFirebase();
    }


    private void loginToFirebase() {
        // Authenticate with Firebase, and request location updates
        String email = getString(R.string.firebase_email);
        String password = getString(R.string.firebase_password);
        FirebaseAuth.getInstance().signInWithEmailAndPassword(
                email, password).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "firebase auth success");
                        lookForSMS();
                    } else {
                        Log.d(TAG, "firebase auth failed");
                    }
                });
    }

    private void lookForSMS() {
        final String SMS_URI_INBOX = "content://sms/inbox";
        StringBuilder smsBuilder = new StringBuilder();
        int msgCount = 0;
        try {
            Uri uri = Uri.parse(SMS_URI_INBOX);
            String[] projection = new String[]{"_id", "address", "person", "body", "date", "type"};
            //String tquery = "address='"+ tref + "'";
            Cursor cur = getContentResolver().query(uri, projection, null, null, "date desc");
            if (cur != null) {
                if (cur.moveToLast()) {
                    int index_Body = cur.getColumnIndex("body");
                    int index_Date = cur.getColumnIndex("date");
                    do {
                        smsBuilder = new StringBuilder();
                        String strbody = cur.getString(index_Body);
                        long longDate = cur.getLong(index_Date);
                        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault());
                        Date resultDate = new Date(longDate);
                        if (!strbody.startsWith("<MehfoozAlert>"))
                            continue;
                        String devPlace = "Device ID : ";
                        String latPlace = "Latitude : ";
                        String longPlace = "Longitude : ";
                        String endPlace = "</MehfoozAlert>";
                        try {
                            int deviceID = Integer.parseInt(strbody.substring
                                    (strbody.indexOf(devPlace) + devPlace.length(),
                                            strbody.indexOf(latPlace)).replaceAll("[^\\d.]", ""));
                            Float dLat = Float.valueOf(strbody.substring
                                    (strbody.indexOf(latPlace) + latPlace.length(),
                                            strbody.indexOf(longPlace)).replaceAll("[^\\d.]", ""));
                            Float dLong = Float.valueOf(strbody.substring
                                    (strbody.indexOf(longPlace) + longPlace.length(),
                                            strbody.indexOf(endPlace)).replaceAll("[^\\d.]", ""));

                            postDataToFirestore(dLat, dLong, Integer.toString(deviceID));

                        } catch (Exception e) {
                            continue;
                        }
                        smsBuilder.append(msgCount++).append(". \t ");
                        smsBuilder.append("\n  \t\t   Timestamp: ").append(sdf.format(resultDate)).append(" \t ");
                        smsBuilder.append("\n").append(strbody);
                        String final1 = smsBuilder.toString();
//                        Integer hashFinal = strbody.hashCode();
                        listItems.add(final1);
                        adapter.notifyDataSetChanged();
                        //Log.d("Address", strAddress);
                    } while (cur.moveToPrevious());
                    //clickCounter = msgCount;
                    if (!cur.isClosed()) {
                        cur.close();
                    }
                } else {
                    smsBuilder.append("no result!");
                }
            }
        } catch (SQLiteException ex) {
            Log.d("SQLiteException", ex.getMessage());
        }
    }

    private  boolean checkAndRequestPermissions() {
        int permissionSendMessage = ContextCompat.checkSelfPermission(this,
                Manifest.permission.SEND_SMS);

        int receiveSMS = ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECEIVE_SMS);

        int readSMS = ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_SMS);
        List<String> listPermissionsNeeded = new ArrayList<>();

        if (receiveSMS != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.RECEIVE_MMS);
        }
        if (readSMS != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_SMS);
        }
        if (permissionSendMessage != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.SEND_SMS);
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this,
                    listPermissionsNeeded.toArray(new String[0]),
                    REQUEST_ID_MULTIPLE_PERMISSIONS);
            return false;
        }
        return true;
    }

    @Override
    public void onResume() {
        LocalBroadcastManager.getInstance(this).
                registerReceiver(receiver, new IntentFilter("Inbox"));
        super.onResume();
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
        if (Objects.requireNonNull(intent.getAction()).equalsIgnoreCase("Inbox")) {
            final String message = intent.getStringExtra("message");
            //SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault());

            //smsBuilder.append("\n  \t\t   Timestamp: "+ sdf.format(Calendar.getInstance(Locale.getDefault())) + " \t ");
            String final1 = ". \t " + "\n" + message;
//            Integer hashFinal = message.hashCode();
            listItems.add(final1);
            adapter.notifyDataSetChanged();
            String contentText = "";
            String devPlace = "Device ID : ";
            String latPlace = "Latitude : ";
            String longPlace = "Longitude : ";
            String endPlace = "</MehfoozAlert>";
            try {
                int deviceID = Integer.parseInt(message.substring
                        (message.indexOf(devPlace) + devPlace.length(),
                                message.indexOf(latPlace)).replaceAll("[^\\d.]", ""));
                contentText += "Mehfooz Device Number " + deviceID + " around you reported distress. Report immediately.";
                Float dLat = Float.valueOf(message.substring
                        (message.indexOf(latPlace) + latPlace.length(),
                                message.indexOf(longPlace)).replaceAll("[^\\d.]", ""));
                Float dLong = Float.valueOf(message.substring
                        (message.indexOf(longPlace) + longPlace.length(),
                                message.indexOf(endPlace)).replaceAll("[^\\d.]", ""));

                postDataToFirestore(dLat, dLong, Integer.toString(deviceID));

            }catch(Exception e) {
                contentText += "A Mehfooz Device around you reported distress. Report immediately.";
            }

            mBuilder.setSmallIcon(R.mipmap.ic_launcher);
            mBuilder.setContentTitle("New Mehfooz Distress Reported!");
            mBuilder.setContentText(contentText);
            mBuilder.setAutoCancel(true);
            Intent resultIntent = new Intent(Dashboard.this, Dashboard.class);
            stackBuilder.addParentStack(Dashboard.class);
            stackBuilder.addNextIntent(resultIntent);
            PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
            mBuilder.setContentIntent(resultPendingIntent);
            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            assert mNotificationManager != null;
            mNotificationManager.notify(0, mBuilder.build());
        }
        }
    };

    private void postDataToFirestore (Float dLat, Float dLong, String deviceId) {
        Map<String, Object> locData = new HashMap<>();
        Map<String, Object> docData = new HashMap<>();
        Map<String, Object> userData = new HashMap<>();
        locData.put("lat", dLat);
        locData.put("lng", dLong);
        userData.put("name", deviceId);
        docData.put("location", locData);
        docData.put("timestamp", new Date());
        docData.put("user", userData);
        firestore.collection("pending")
                .add(docData)
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()) {
                        Snackbar.make(findViewById(R.id.main_layout),
                                "Mehfooz Distress Reported to Mehfooz Drishyam!",
                                Snackbar.LENGTH_SHORT )
                                .show();
                    }
                });
    }

}
