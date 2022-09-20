package com.smart.planner;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.snackbar.Snackbar;
import com.smart.planner.Classes.RequestCodes;
import com.smart.planner.googlemapclasses.GetNearbyPlacesData;

import java.util.Arrays;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import toan.android.floatingactionmenu.FloatingActionButton;
import toan.android.floatingactionmenu.FloatingActionsMenu;

public class FragmentLocationReminder extends Fragment
        implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener, GoogleMap.OnMapLongClickListener {

    private static final float DEFAULT_ZOOM = 17f;
    private static final LatLngBounds LAT_LNG_BOUNDS = new LatLngBounds(
            new LatLng(-40, -168), new LatLng(71, 136));
    private static final String TAG = "Fragment Location Reminder";

    int PROXIMITY_RADIUS = 1500;
    double currentLatitude, currentLongitude;
    boolean mLocationPermissionGranted;

    GoogleMap mMap;
    View mView;
    Location mLastKnownLocation;
    FusedLocationProviderClient mFusedLocationProviderClient;

    FloatingActionButton searchPharmacy, searchSupermarket;
    FloatingActionsMenu searchNearbyMenu;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_location_reminder, container, false);
        init();
        return mView;
    }

    private void init() {
        searchNearbyMenu = mView.findViewById(R.id.fab_expand_menu);
        searchPharmacy = mView.findViewById(R.id.fab_nearby_pharmacy);
        searchSupermarket = mView.findViewById(R.id.fab_nearby_supermarket);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setHasOptionsMenu(true);

        getLocationPermission();

        // Construct a FusedLocationProviderClient.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity().getApplicationContext());

        SupportMapFragment mapFragment = new SupportMapFragment();
        FragmentManager manager = getParentFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.add(R.id.map_container, mapFragment, "map");
        transaction.commit();

        mapFragment.getMapAsync(this);

        searchPharmacy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchNearbyPlaces(view);
            }
        });
        searchSupermarket.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchNearbyPlaces(view);
            }
        });
    }

    private void searchNearbyPlaces(View v) {
        Object dataTransfer[] = new Object[3];
        GetNearbyPlacesData getNearbyPlacesData = new GetNearbyPlacesData();

        switch (v.getId()) {
            case R.id.fab_nearby_pharmacy:
                mMap.clear();
                String pharmacy = "pharmacy";
                String url = getUrl(currentLatitude, currentLongitude, pharmacy);
                dataTransfer[0] = mMap;
                dataTransfer[1] = url;
                dataTransfer[2] = getContext();

                getNearbyPlacesData.execute(dataTransfer);
                Toast.makeText(getContext(), "Showing Nearby " + pharmacy, Toast.LENGTH_SHORT).show();
                break;

            case R.id.fab_nearby_supermarket:
                mMap.clear();
                String supermarket = "supermarket";
                url = getUrl(currentLatitude, currentLongitude, supermarket);
                dataTransfer[0] = mMap;
                dataTransfer[1] = url;
                dataTransfer[2] = getContext();

                getNearbyPlacesData.execute(dataTransfer);
                Toast.makeText(getContext(), "Showing Nearby " + supermarket, Toast.LENGTH_SHORT).show();
                break;
        }
        if (searchNearbyMenu.isExpanded()) {
            searchNearbyMenu.collapse();
        }

    }

    private String getUrl(double latitude, double longitude, String placeType) {
        StringBuilder googlePlaceUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        googlePlaceUrl.append("location=" + latitude + "," + longitude);
        googlePlaceUrl.append("&radius=" + PROXIMITY_RADIUS);
        googlePlaceUrl.append("&type=" + placeType);
        googlePlaceUrl.append("&sensor=true");
        googlePlaceUrl.append("&key="+"AIzaSyC9y2kyNG4_g70rHyj3q-dcIEOXACGrvcU");

        return googlePlaceUrl.toString();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;

        // Turn on the My Location layer and the related control on the map.
        updateLocationUI();

        // Get the current location of the device and set the position of the map.
        getDeviceLocation();

        mMap.setOnMarkerClickListener(this);
        mMap.setOnMapLongClickListener(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case RequestCodes.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                    getDeviceLocation();
                }
            }
        }
        updateLocationUI();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == 123 && resultCode == getActivity().RESULT_OK) {
            Place place = Autocomplete.getPlaceFromIntent(data);
            Toast.makeText(getContext(), place.getName(), Toast.LENGTH_SHORT).show();
        }
    }

    // UPDATE LOCATION UI
    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }
        try {
            if (mLocationPermissionGranted) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                mLastKnownLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    //REQUESTING LOCATION PERMISSION METHOD
    private void getLocationPermission() {
        if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    RequestCodes.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    //REQUESTING DEVICE LOCATION METHOD
    private void getDeviceLocation() {
        try {
            if (mLocationPermissionGranted) {
                LocationRequest locationRequest = new LocationRequest();
                locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                LocationCallback locationChangeListener = new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        mLastKnownLocation = locationResult.getLastLocation();
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                new LatLng(mLastKnownLocation.getLatitude(),
                                        mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                        currentLatitude = mLastKnownLocation.getLatitude();
                        currentLongitude = mLastKnownLocation.getLongitude();
                    }
                };
                mFusedLocationProviderClient.requestLocationUpdates(locationRequest, locationChangeListener, Looper.myLooper());

            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    //CONVERT VECTOR DRAWABLE TO BITMAP
    public static Bitmap getBitmapFromVectorDrawable(Context context, int drawableId) {
        Drawable drawable = AppCompatResources.getDrawable(context, drawableId);
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        searchItem.setVisible(false);
        MenuItem location_search = menu.findItem(R.id.action_map_location_search);
        location_search.setVisible(true);

        location_search.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                if (!Places.isInitialized()) {
                    Places.initialize(getActivity().getApplicationContext(), getString(R.string.Google_API_Key));
                }
                List<Place.Field> fields = Arrays.asList(
                        Place.Field.ID,
                        Place.Field.NAME,
                        Place.Field.ADDRESS,
                        Place.Field.LAT_LNG,
                        Place.Field.TYPES
                );
                Intent autocompleteIntent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY,
                        fields).build(getActivity());
                startActivityForResult(autocompleteIntent, 123);
                return true;
            }
        });
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Snackbar snackbar = Snackbar.make(getView(), marker.getTitle() +"\n\nAdd Location Reminder ?", Snackbar.LENGTH_INDEFINITE)
                .setAction("YES", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
//                        BottomSheetAddLocationReminder reminder = new BottomSheetAddLocationReminder();
//                        reminder.show(getChildFragmentManager(),"bottom_add_locationReminder");
                    }
                });

        Snackbar.SnackbarLayout layout = (Snackbar.SnackbarLayout)snackbar.getView();
        ((TextView)layout.findViewById(com.google.android.material.R.id.snackbar_text)).setMaxLines(3);
        layout.setMinimumHeight(300);
        snackbar.show();
        return false;
    }

    @Override
    public void onMapLongClick(final LatLng latLng) {
        MarkerOptions markerOptions = new MarkerOptions().position(latLng);
        mMap.addMarker(markerOptions);

        final BottomSheetAddLocationReminder reminder = new BottomSheetAddLocationReminder();
        final Bundle bundle = new Bundle();
        Snackbar snackbar = Snackbar.make(getView(), "Add Location Reminder ?", Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction("YES", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        bundle.clear();
                        bundle.putDouble("placeLatitude",latLng.latitude);
                        bundle.putDouble("placeLongitude",latLng.longitude);
                        reminder.setArguments(bundle);
                        reminder.show(getChildFragmentManager(),"bottom_add_locationReminder");
                    }
                });
        snackbar.show();
    }

}
