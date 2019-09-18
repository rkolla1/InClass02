package com.example.chatroom;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RotateDrawable;
import android.location.Location;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.chatroom.directionhelpers.FetchURL;
import com.example.chatroom.directionhelpers.TaskLoadedCallback;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;


public class Routing extends AppCompatActivity implements OnMapReadyCallback, TaskLoadedCallback {
    GoogleMap map;
    MarkerOptions place1, place2;

    LatLng latLng2, latLng3, Latlng4, latLng, latLng1, LatLng5, LatLng6;
    Trips t1, t2;
    Button pickedup, cancel;
    private String status = "Accept";

    private Polyline currentPolyline;
    private static final int REQUEST_FINE_LOCATION = 101;
    private LocationRequest mLocationRequest;

    //private long UPDATE_INTERVAL = 200 * 100;  /* 10 secs */
    //private long FASTEST_INTERVAL = 1000; /* 2 sec */

    FirebaseUser firebaseUser;
    DatabaseReference databaseReference, databaseReference1, databaseReference2;
    String useruid;
    Trips t;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_routing);
        //startLocationUpdates();


        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        useruid = firebaseUser.getUid();
        pickedup = findViewById(R.id.finish);
        cancel = findViewById(R.id.cancel);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map1);
        mapFragment.getMapAsync(this);


        t = (Trips) getIntent().getExtras().get("trip");
        if (useruid.equals(t.getDriverid())) {
            startLocationUpdates();
        } else {
            pickedup.setVisibility(View.GONE);
            mp1();
        }
        databaseReference1 = FirebaseDatabase.getInstance().getReference("Trips").child(t.getTrip_id());


        if (useruid.equals(t.getRiderid())) {
            Routing.this.setTitle("Rider");
        } else {
            Routing.this.setTitle("Driver");
            cancel.setVisibility(View.GONE);

        }

        databaseReference = FirebaseDatabase.getInstance().getReference("Trips").child(t.getTrip_id());

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder1 = new AlertDialog.Builder(Routing.this);
                builder1.setMessage("Going back will End the Ride");
                builder1.setCancelable(true);

                builder1.setPositiveButton(
                        "Yes",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                t1.setStatus("cancel");
                                status = "Done";
                                databaseReference.setValue(t1);

                                Intent intent = new Intent(Routing.this, Main2Activity.class);
                                startActivity(intent);
                                finish();
                                dialog.cancel();
                            }
                        });

                builder1.setNegativeButton(
                        "No",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

                AlertDialog alert11 = builder1.create();
                alert11.show();

            }
        });


        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //Do something after 100ms
//                 latLng = new LatLng(t.sourcelat,t.sourcelng);
//                 latLng1 = new LatLng(t.destlat,t.destlng);
//
//                Log.d("marker", String.valueOf(latLng));
//
//                map.addMarker(new MarkerOptions().position(latLng)
//                        .title("pickup"));
//                //map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latLng.latitude,latLng.longitude),15));
//
//                map.addMarker(new MarkerOptions().position(latLng1)
//                        .title("Drop"));
                //map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latLng1.latitude,latLng1.longitude),15));

                //place1 = new MarkerOptions().position(new LatLng(latLng2.latitude,latLng2.longitude)).title("Location 1");
                //place2 = new MarkerOptions().position(new LatLng(t.sourcelat,t.sourcelng)).title("Location 2");

                if (useruid.equals(t.getRiderid())) {
//                    place1 = new MarkerOptions().position(new LatLng(t.getSourcelat(),t.getSourcelng())).title("Location 1");
//                    Latlng4 = new LatLng(Double.parseDouble(t1.getDriverlat()), Double.parseDouble(t1.getDriverlong()));
//                    place2 = new MarkerOptions().position(new LatLng(Latlng4.latitude,Latlng4.longitude)).title("Location 2");
//
//                    String url = getUrl(place1.getPosition(), place2.getPosition(), "driving");
//
//                    new FetchURL(Routing.this).execute(url,"driving");


                } else {

                    LatLng5 = new LatLng(t.sourcelat, t.sourcelng);
                    LatLng6 = new LatLng(t.destlat, t.destlng);
                    map.addMarker(new MarkerOptions().position(LatLng5)
                            .title("pickup"));
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(LatLng5.latitude, LatLng5.longitude), 15));

                    map.addMarker(new MarkerOptions().position(LatLng6)
                            .title("Drop"));

                    map.addMarker(new MarkerOptions().position(latLng3)
                            .title("I am here"))
                            .setIcon(bitmapDescriptorFromVector(Routing.this, R.drawable.car2));


                    System.out.println("klklklk" + t1.getDriverlat());
                    Latlng4 = new LatLng(Double.parseDouble(t1.getDriverlat()), Double.parseDouble(t1.getDriverlong()));
                    place1 = new MarkerOptions().position(new LatLng(Latlng4.latitude, Latlng4.longitude)).title("Location 1");
                    place2 = new MarkerOptions().position(new LatLng(t.getSourcelat(), t.getSourcelng())).title("Location 1");


                    String url = getUrl(place1.getPosition(), place2.getPosition(), "driving");

                    new FetchURL(Routing.this).execute(url, "driving");
                }


                String url = getUrl(place1.getPosition(), place2.getPosition(), "driving");

                new FetchURL(Routing.this).execute(url, "driving");


            }
        }, 5000);

        pickedup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                status = "Done";
                t1.setStatus("Done");
                databaseReference1.setValue(t1);


                Intent intent = new Intent(Routing.this, Main2Activity.class);
                startActivity(intent);
                finish();

            }
        });

        databaseReference1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Trips t = dataSnapshot.getValue(Trips.class);
                if (t.getStatus().equals("Done")) {
                    //Looper.myLooper().getThread().interrupt();
                    //Looper.getMainLooper().quit();
                    Toast.makeText(Routing.this, "completed", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(Routing.this, Main2Activity.class);
                    startActivity(intent);
                    finish();


                } else if (t.getStatus().equals("cancel")) {
                    status = "cancel";
                    Intent intent = new Intent(Routing.this, Main2Activity.class);
                    startActivity(intent);
                    finish();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    protected void startLocationUpdates() {

        // Create the location request to start receiving updates
        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (useruid.equals(t.getRiderid())) {
            long UPDATE_INTERVAL = 1000;  /* 10 secs */
            long FASTEST_INTERVAL = 1000; /* 2 sec */
            mLocationRequest.setInterval(UPDATE_INTERVAL);
            mLocationRequest.setFastestInterval(FASTEST_INTERVAL);

        } else {
            long UPDATE_INTERVAL = 10000;  /* 10 secs */
            long FASTEST_INTERVAL = 2000; /* 2 sec */
            mLocationRequest.setInterval(UPDATE_INTERVAL);
            mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        }
//        mLocationRequest.setInterval(UPDATE_INTERVAL);
//        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);


        // Create LocationSettingsRequest object using location request
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        LocationSettingsRequest locationSettingsRequest = builder.build();

        // Check whether location settings are satisfied
        // https://developers.google.com/android/reference/com/google/android/gms/location/SettingsClient
        SettingsClient settingsClient = LocationServices.getSettingsClient(this);
        settingsClient.checkLocationSettings(locationSettingsRequest);

        // new Google API SDK v11 uses getFusedLocationProviderClient(this)
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }


        /*getFusedLocationProviderClient(this).requestLocationUpdates(mLocationRequest, new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        // do work here
                        onLocationChanged(locationResult.getLastLocation());
                        if(status.equals("Done")){
                            status = "Accept";
                            //Looper.myLooper().quitSafely();
                            //Looper.myLooper().quit();

                        }
                        else if(status.equals("cancel")){
                            status = "Accept";
                            System.out.println("cancelled"+status);
                            //Looper.myLooper().quitSafely();
                            //Looper.myLooper().quit();
                        }
                    }
                },
                Looper.myLooper());*/


        Runnable r = new Runnable() {
            @Override
            public void run() {
                if (ActivityCompat.checkSelfPermission(Routing.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(Routing.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                getFusedLocationProviderClient(Routing.this).getLastLocation().addOnSuccessListener(Routing.this, location -> {
                    System.out.println("Came fused");
                    if (location != null) {
                        System.out.println("lOCATION:" + location.getLatitude());
                        onLocationChanged(location);
                    }
                });

                try{
                    Thread.sleep(500);
                }
                catch (Exception e){

                }
            }
        };

        Thread thread = new Thread(r);
        thread.start();



    }

    public void onLocationChanged(Location location1) {
        // New location has now been determined
        String msg = "Updated Location: " +
                Double.toString(location1.getLatitude()) + "," +
                Double.toString(location1.getLongitude());
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        // You can now create a LatLng Object for use with maps

        latLng2 = new LatLng(location1.getLatitude(), location1.getLongitude());


        if (useruid.equals(t.getDriverid())) {
            t.setDriverlat(String.valueOf(latLng2.latitude));
            t.setDriverlong(String.valueOf(latLng2.longitude));
            databaseReference.setValue(t);
            System.out.println("sjsjsjs" + t.getDestlat());
        }
        Log.d("marker", String.valueOf(latLng2));

        mp1();

    }

    private void mp1() {

        if(useruid.equals(t.getRiderid())) {

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {
                    // yourMethod();
                    databaseReference.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            t1 = dataSnapshot.getValue(Trips.class);
                            if (t1.getDriverlat() != null) {
                                latLng3 = new LatLng(Double.parseDouble(t1.getDriverlat()), Double.parseDouble(t1.getDriverlong()));
                            }
                            map.clear();
                            LatLng5 = new LatLng(t.sourcelat,t.sourcelng);
                            LatLng6 = new LatLng(t.destlat,t.destlng);
                            map.addMarker(new MarkerOptions().position(LatLng5)
                                    .title("pickup"));
                            //map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latLng.latitude,latLng.longitude),15));

                            map.addMarker(new MarkerOptions().position(LatLng6)
                                    .title("Drop"));

                            place1 = new MarkerOptions().position(new LatLng(t.getSourcelat(),t.getSourcelng())).title("Location 1");
                            Latlng4 = new LatLng(Double.parseDouble(t1.getDriverlat()), Double.parseDouble(t1.getDriverlong()));
                            place2 = new MarkerOptions().position(new LatLng(Latlng4.latitude,Latlng4.longitude)).title("Location 2");

                            String url = getUrl(place1.getPosition(), place2.getPosition(), "driving");

                            new FetchURL(Routing.this).execute(url,"driving");

                            map.addMarker(new MarkerOptions().position(latLng3)
                                    .title("I am here"))
                                    .setIcon(bitmapDescriptorFromVector(Routing.this, R.drawable.car2));
                            map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latLng3.latitude, latLng3.longitude), 15));

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                }
            }, 1000);   //1 seconds
        }else{
            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    t1 = dataSnapshot.getValue(Trips.class);
                    if (!t1.getDriverlat().equals(null)) {
                        latLng3 = new LatLng(Double.parseDouble(t1.getDriverlat()),Double.parseDouble( t1.getDriverlong()));
                    }

                    System.out.println("driverlat"+ t1.getDriverlat());
                    System.out.println("latlng2"+latLng2.latitude);

                    if(!t1.getDriverlat().equals(String.valueOf(latLng2.latitude))) {
                        map.clear();

                        latLng = new LatLng(t.sourcelat, t.sourcelng);
                        latLng1 = new LatLng(t.destlat, t.destlng);

                        Log.d("marker", String.valueOf(latLng));

                        map.addMarker(new MarkerOptions().position(latLng)
                                .title("pickup"));
                        //map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latLng.latitude,latLng.longitude),15));

                        map.addMarker(new MarkerOptions().position(latLng1)
                                .title("Drop"));

                        map.addMarker(new MarkerOptions().position(latLng3)
                                .title("I am here"))
                                .setIcon(bitmapDescriptorFromVector(Routing.this, R.drawable.car2));
                        //map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latLng3.latitude, latLng3.longitude), 15));

                        Latlng4 = new LatLng(Double.parseDouble(t1.getDriverlat()), Double.parseDouble(t1.getDriverlong()));
                        place1 = new MarkerOptions().position(new LatLng(Latlng4.latitude, Latlng4.longitude)).title("Location 1");
                        place2 = new MarkerOptions().position(new LatLng(t.getSourcelat(), t.getSourcelng())).title("Location 1");

                        LatLngBounds.Builder builder = new LatLngBounds.Builder();
                        builder = new LatLngBounds.Builder();
                        builder.include(latLng);
                        builder.include(latLng3);
                        map.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 17));

                        String url = getUrl(place1.getPosition(), place2.getPosition(), "driving");

                        new FetchURL(Routing.this).execute(url, "driving");
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
        /*map.addMarker(new MarkerOptions().position(latLng3)
                .title("I am here"))
                .setIcon(bitmapDescriptorFromVector(Routing.this, R.drawable.car2));
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latLng3.latitude,latLng3.longitude),15));*/
    }

    public void getLastLocation() {
        // Get last known recent location using new Google Play Services SDK (v11+)
        FusedLocationProviderClient locationClient = getFusedLocationProviderClient(this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        locationClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener <Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // GPS location can be null if GPS is switched off
                        if (location != null) {
                            onLocationChanged(location);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("MapDemoActivity", "Error trying to get last GPS location");
                        e.printStackTrace();
                    }
                });
    }


    private String getUrl(LatLng origin, LatLng dest, String directionMode) {
        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        // Mode
        String mode = "mode=" + directionMode;
        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + mode;
        // Output format
        String output = "json";
        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters + "&key=AIzaSyCjQlEN9SKDCtC30zy7grp-lyhPjEv792Q";
        Log.d("url",url);
        return url;
    }


    public void onMapReady(GoogleMap googleMap) {

       map = googleMap;


    }

    private BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }


    @Override
    public void onTaskDone(Object... values) {
        if (currentPolyline != null)
            currentPolyline.remove();
        currentPolyline = map.addPolyline((PolylineOptions) values[0]);
    }

    @Override
    public void onBackPressed() {

        if(useruid.equals(t.getRiderid())) {

            AlertDialog.Builder builder1 = new AlertDialog.Builder(Routing.this);
            builder1.setMessage("Going back will End the Ride");
            builder1.setCancelable(true);

            builder1.setPositiveButton(
                    "Yes",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            t1.setStatus("cancel");
                            databaseReference.setValue(t1);

                            Intent intent = new Intent(Routing.this, Main2Activity.class);
                            startActivity(intent);
                            finish();
                            dialog.cancel();
                        }
                    });

            builder1.setNegativeButton(
                    "No",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });

            AlertDialog alert11 = builder1.create();
            alert11.show();
        }


    }
}
