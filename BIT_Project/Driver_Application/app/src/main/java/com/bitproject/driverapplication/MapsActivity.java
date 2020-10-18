package com.bitproject.driverapplication;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.appcompat.widget.Toolbar;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

	public static final String ex = "Switch";
	GoogleApiClient mGoogleAPIClient;

	Location mLastLocation;
	private Toolbar mToolbar;
	LocationRequest mLocationRequest;
	private GoogleMap mMap;
	private SharedPreferences sharedPreferences;
	private Switch mWorkingSwitch;

	private String parentID = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_maps);
		// Obtain the SupportMapFragment and get notified when the map is ready to be used.
		SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
				.findFragmentById(R.id.map);
		mapFragment.getMapAsync(this);

		mToolbar = (Toolbar) findViewById(R.id.mapToolbar);

		if (this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PackageManager.PERMISSION_GRANTED);
			ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PackageManager.PERMISSION_GRANTED);
		}


		mWorkingSwitch = (Switch) findViewById(R.id.workingSwitch);

		sharedPreferences = getSharedPreferences(" ", MODE_PRIVATE);
		final SharedPreferences.Editor editor = sharedPreferences.edit();
		mWorkingSwitch.setChecked(sharedPreferences.getBoolean(ex, false));
		mWorkingSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					connectDriver();
					editor.putBoolean(ex, true);
				} else {
					editor.putBoolean(ex, false);
					disconnectDriver();
				}
				editor.commit();
			}
		});

		connectParent();
	}

	private void connectParent() {
		String driverID = FirebaseAuth.getInstance().getCurrentUser().getUid();
		DatabaseReference connectParentRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Driver").child(driverID).child("parentConnectID");
		connectParentRef.addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				if (dataSnapshot.exists()) {
					parentID = dataSnapshot.getValue().toString();
					getChildPickupDropLocation();
				}
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {

			}
		});
	}

	private void getChildPickupDropLocation() {
		DatabaseReference pickupDropLocationRef = FirebaseDatabase.getInstance().getReference().child("ParentRequest").child(parentID).child("l");
		pickupDropLocationRef.addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				if (dataSnapshot.exists()) {
					List<Object> map = (List<Object>) dataSnapshot.getValue();
					double locationLat = 0;
					double locationLng = 0;
					if (map.get(0) != null) {
						locationLat = Double.parseDouble(map.get(0).toString());
					}
					if (map.get(1) != null) {
						locationLng = Double.parseDouble(map.get(1).toString());
					}
					LatLng parentLatLng = new LatLng(locationLat, locationLng);
					mMap.addMarker(new MarkerOptions().position(parentLatLng).title("Parent Location").icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher_pin_foreground)));
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
	@Override
	public void onMapReady(GoogleMap googleMap) {
		mMap = googleMap;

		if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			return;
		}
		buildGoogleAPIClient();
		mMap.setMyLocationEnabled(true);
	}

	protected synchronized void buildGoogleAPIClient() {
		mGoogleAPIClient = new GoogleApiClient.Builder(this)
				.addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this)
				.addApi(LocationServices.API)
				.build();
		mGoogleAPIClient.connect();
	}

	private void connectDriver() {
		if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			return;
		}
		LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleAPIClient, mLocationRequest, this);
	}

	private void disconnectDriver() {
		if (!mWorkingSwitch.isChecked()) {
			LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleAPIClient, this);
			String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
			DatabaseReference ref = FirebaseDatabase.getInstance().getReference("VehicalLocation");

			GeoFire geoFire = new GeoFire(ref);
			geoFire.removeLocation(userID);

			Intent intent = new Intent(MapsActivity.this, OptionActivity.class);
			startActivity(intent);
		}
	}

	@Override
	public void onConnected(@Nullable Bundle bundle) {
		mLocationRequest = new LocationRequest();
		mLocationRequest.setInterval(5000);
		mLocationRequest.setFastestInterval(5000);
		mLocationRequest.setPriority(LocationRequest.PRIORITY_LOW_POWER);
	}

	@Override
	public void onConnectionSuspended(int i) {

	}

	@Override
	public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

	}

	@Override
	public void onLocationChanged(Location location) {
		if (getApplicationContext() != null && mWorkingSwitch.isChecked()) {

			mLastLocation = location;

			LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

			mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
			mMap.animateCamera(CameraUpdateFactory.zoomTo(17));

			String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
			DatabaseReference ref = FirebaseDatabase.getInstance().getReference("VehicalLocation");

			GeoFire geoFire = new GeoFire(ref);
			geoFire.setLocation(userID, new GeoLocation(location.getLatitude(), location.getLongitude()));
		}
	}
}
