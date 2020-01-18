package com.mehfooz.client;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "MainActivity";
    private static final int RC_LOCATION = 101;
    private static final int RC_SIGN_IN = 9001;
    private static final int RC_COMPLETE_USER = 900;

    private GoogleMap mMap;
    private FirebaseFirestore firebaseFirestore;
    private ImageView centerMarker;
    private FusedLocationProviderClient fusedLocationClient;
    private GoogleSignInClient signInClient;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private UserObject userObject;
    private ArrayList<Button> buttonsList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        centerMarker = findViewById(R.id.mapMarker);
        firebaseFirestore = FirebaseFirestore.getInstance();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        GoogleSignInOptions gso =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        firebaseAuth = FirebaseAuth.getInstance();
        signInClient = GoogleSignIn.getClient(this, gso);

        userObject = new UserObject();

        buttonsList.add((Button)findViewById(R.id.disaster));
        buttonsList.add((Button)findViewById(R.id.health));
        buttonsList.add((Button)findViewById(R.id.law_order));
        for(Button button : buttonsList){
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendDistress(v.getTag().toString());
                }
            });
        }

//        FloatingActionButton fab = findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Call Mehfooz Helpline", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        getLocationPermission();

        mMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
            @Override
            public void onCameraMove() {
                mMap.clear();
                centerMarker.setVisibility(View.VISIBLE);
            }
        });

        mMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                centerMarker.setVisibility(View.GONE);
                mMap.addMarker(new MarkerOptions()
                        .position(mMap.getCameraPosition().target));
            }
        });

        setLocation();

        // Add a marker in Sydney, Australia, and move the camera.
//        LatLng sydney = new LatLng(-34, 151);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

    @SuppressLint("MissingPermission")
    private void setLocation() {
        mMap.setMyLocationEnabled(true);
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            LatLng latLng =
                                    new LatLng(location.getLatitude(), location.getLongitude());
                            CameraUpdate cameraUpdate =
                                    CameraUpdateFactory.newLatLngZoom(latLng, 15);
                            mMap.animateCamera(cameraUpdate);
                        }
                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Forward results to EasyPermissions
        EasyPermissions
                .onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @AfterPermissionGranted(RC_LOCATION)
    private void getLocationPermission() {
        String[] perms = {Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION};
        if (EasyPermissions.hasPermissions(this, perms)) {
            setLocation();
        } else {
            // Do not have permissions, request them now
            EasyPermissions.requestPermissions(this, getString(R.string.location_rationale),
                    RC_LOCATION, perms);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_login:
                signIn();
                return true;
            case R.id.action_edit:
                Intent completeUserIntent = new Intent(MainActivity.this, UserInfo.class);
                completeUserIntent.putExtra("tag", "");
                completeUserIntent.putExtra("name", userObject.getName());
                completeUserIntent.putExtra("number", userObject.getPhoneNumber());
                startActivityForResult(completeUserIntent, RC_COMPLETE_USER);
                return true;
            case R.id.action_about:
                // TODO: about
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void signIn() {
        Intent signInIntent = signInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                assert account != null;
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
                // ...
            }
        }
        else if (requestCode == RC_COMPLETE_USER) {
            if (resultCode == RESULT_OK) {
                userObject.setName(data.getStringExtra("name"));
                userObject.setPhoneNumber(data.getStringExtra("number"));
                String tag = data.getStringExtra("tag");
                if(!tag.isEmpty()) sendDistress(tag);
                Snackbar.make(findViewById(R.id.main_layout),
                        "Details updated!", Snackbar.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle( GoogleSignInAccount account) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + account.getId());

        AuthCredential credential =
                GoogleAuthProvider.getCredential(account.getIdToken(), null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            firebaseUser = firebaseAuth.getCurrentUser();
                            updateUI();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Snackbar.make(findViewById(R.id.main_layout),
                                    "Authentication Failed.", Snackbar.LENGTH_SHORT).show();
                            updateUI();
                        }

                        // ...
                    }
                });
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        firebaseUser = firebaseAuth.getCurrentUser();
        updateUI();
    }

    private void updateUI() {
        if(firebaseUser!=null && !userObject.isComplete()) {
            userObject.setName(firebaseUser.getDisplayName());
            userObject.setPhoneNumber(firebaseUser.getPhoneNumber());
            Snackbar.make(findViewById(R.id.main_layout), "Welcome " +  userObject.getName() + "!", Snackbar.LENGTH_LONG)
                    .show();
            Log.d(TAG, userObject.getName());
        }
    }

    private void sendDistress(String tag) {
        if(userObject.isComplete()){
            Map<String, Object> locData = new HashMap<>();
            locData.put("lat", mMap.getCameraPosition().target.latitude);
            locData.put("lng", mMap.getCameraPosition().target.longitude);
            Map<String, Object> userData = new HashMap<>();
            userData.put("name", userObject.getName());
            userData.put("contactNumber", userObject.getPhoneNumber());
            userData.put("type", tag);
            Map<String, Object> docData = new HashMap<>();
            docData.put("location",locData );
            docData.put("timestamp", new Date());
            docData.put("user", userData);
            firebaseFirestore.collection("pending")
                    .add(docData)
                    .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                @Override
                public void onComplete(@NonNull Task<DocumentReference> task) {
                    if(task.isSuccessful()){
                        Snackbar.make(findViewById(R.id.main_layout),
                                R.string.success_post, Snackbar.LENGTH_LONG).show();
                    }
                }
            });
        }
        else {
            Intent completeUserIntent = new Intent(MainActivity.this, UserInfo.class);
            completeUserIntent.putExtra("tag", tag);
            completeUserIntent.putExtra("name", userObject.getName());
            completeUserIntent.putExtra("number", userObject.getPhoneNumber());
            startActivityForResult(completeUserIntent, RC_COMPLETE_USER);
        }
    }

}
