package com.bitproject.parentapplication;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,  com.google.android.gms.location.LocationListener{

    private GoogleMap mMap;

    Location mLastLocation;
    LocationRequest mLocationRequest;
   GoogleApiClient mGoogleAPIClient;

    private ImageView imageViewBack;
    private TextView mCheckLocation;
    private LatLng parentLocation;

    private Boolean isLoggingOut = false;

    //Create the activity and request permission to access location
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PackageManager.PERMISSION_GRANTED);
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PackageManager.PERMISSION_GRANTED);

        mCheckLocation = (TextView) findViewById(R.id.txtCheckLocation);
        imageViewBack = (ImageView) findViewById(R.id.imgBack);

        imageViewBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                disconnectParent();
                Intent intent = new Intent(MapsActivity.this, OptionActivity.class);
                startActivity(intent);
            }
        });
    }


    private  int radius = 1;
    private  Boolean driverFound = false;
    private String driverFoundID;

    //get connected with the driver and make a reference in firebase
    private void getDriver(){
        DatabaseReference driverLocation = FirebaseDatabase.getInstance().getReference().child("VehicalLocation");

        GeoFire geoFire = new GeoFire(driverLocation);
        GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(parentLocation.latitude, parentLocation.longitude), radius);
        geoQuery.removeAllListeners();

        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if (!driverFound) {
                    driverFoundID = key;
                    /*
                    DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Driver").child(driverFoundID);
                    String parentID = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    HashMap map =  new HashMap();
                    map.put("ParentRequestID", parentID);
                    driverRef.updateChildren(map);
                     */

                    getDriverLocation();
                }
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                if (!driverFound){
                    radius++;
                    getDriver();
                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }

    private Marker mDriverMarker;

    private static DecimalFormat distanceRoundValue = new DecimalFormat("0.00");

    //getting driver location on the map
    private void getDriverLocation() {
        DatabaseReference driverLocationRef = FirebaseDatabase.getInstance().getReference().child("VehicalLocation").child(driverFoundID).child("l");
        driverLocationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    List<Object> map = (List<Object>) dataSnapshot.getValue();
                    double locationLat = 0;
                    double locationLng = 0;
                    mCheckLocation.setText("Driver Found!!");
                    if (map.get(0) != null) {
                        locationLat = Double.parseDouble(map.get(0).toString());
                    }
                    if (map.get(1) != null) {
                        locationLng = Double.parseDouble(map.get(1).toString());
                    }
                    LatLng driverLatLng = new LatLng(locationLat, locationLng);
                    if (mDriverMarker != null) {
                        mDriverMarker.remove();
                    }
                    Location loc1 = new Location("");
                    loc1.setLatitude(parentLocation.latitude);
                    loc1.setLongitude(parentLocation.longitude);

                    Location loc2 = new Location("");
                    loc2.setLatitude(driverLatLng.latitude);
                    loc2.setLongitude(driverLatLng.longitude);

                    float distance = loc1.distanceTo(loc2) / 1000;
                    distanceRoundValue.setRoundingMode(RoundingMode.UP);

                    LatLngBounds.Builder builder = new LatLngBounds.Builder();
                    builder.include(driverLatLng);
                    builder.include(parentLocation);

                    LatLngBounds bounds = builder.build();

                    int width = getResources().getDisplayMetrics().widthPixels;
                    int padding = (int) (width * 0.2);

                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, padding);
                    mMap.animateCamera(cameraUpdate);

                    if (distance < 0.1) {
                        mCheckLocation.setText("Vehical is Here");
                    } else {
                        mCheckLocation.setText("Vehical is " + distanceRoundValue.format(distance) + " km from you.");
                    }

                    mDriverMarker = mMap.addMarker(new MarkerOptions().position(driverLatLng).title("Vehical Current Location").icon(BitmapDescriptorFactory.fromResource(R.mipmap.car_foreground)));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }

        });
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */

    //On map ready build API client and enable location
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            return;
        }
        buildGoogleAPIClient();
        mMap.setMyLocationEnabled(true);

    }

    protected synchronized void buildGoogleAPIClient(){
        mGoogleAPIClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleAPIClient.connect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(5000);     //5 seconds
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_LOW_POWER);

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            return;
        }

        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleAPIClient, mLocationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;

        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(16));

        String parentId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("ParentRequest");
        GeoFire geoFire = new GeoFire(ref);
        geoFire.setLocation(parentId, new GeoLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude()));

        parentLocation = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
        mMap.addMarker(new MarkerOptions().position(parentLocation).title("You are Here Now!").icon(BitmapDescriptorFactory.fromResource(R.mipmap.pin_foreground)));

        mCheckLocation.setText("Getting Driver!!");

        getDriver();
    }

    //disconnect parent and remove from firebase
    private void disconnectParent() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleAPIClient, this);
        String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("ParentRequest");

        GeoFire geoFire = new GeoFire(ref);
        geoFire.removeLocation(userID);
    }
}
