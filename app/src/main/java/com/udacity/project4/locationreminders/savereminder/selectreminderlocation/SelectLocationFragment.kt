package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.IntentSender
import android.content.pm.PackageManager
import android.content.res.Resources
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import org.koin.android.ext.android.inject
import java.util.*


class SelectLocationFragment : BaseFragment(), OnMapReadyCallback {
    private lateinit var map: GoogleMap

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var lastKnownLocation: Location? = null
    private var locationEnabled = false

    private lateinit var locationManager: LocationManager
    private lateinit var locationListener: LocationListener

    private lateinit var marker: Marker;
    private var markerCount = 0;

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        setHasOptionsMenu(true)

//        TODO: add the map setup implementation
//        TODO: zoom to the user location after taking his permission
//        TODO: add style to the map
//        TODO: put a marker to location that the user selected

        val mapFragment: SupportMapFragment =
            childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment

        if (mapFragment != null) {
            mapFragment.getMapAsync(this)
        }

//        TODO: call this function after the user confirms on the selected location
        onLocationSelected()

        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireContext())

        return binding.root
    }

    private fun onLocationSelected() {
        //        TODO: When the user confirms on the selected location,
        //         send back the selected location details to the view model
        //         and navigate back to the previous fragment to save the reminder and add the geofence
        binding.btnSaveLocation.setOnClickListener {
            _viewModel.latitude.value = marker.position.latitude
            _viewModel.longitude.value = marker.position.longitude
            _viewModel.reminderSelectedLocationStr.value = marker.title
            findNavController().navigateUp()
        }
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.normal_map -> {
            map.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.hybrid_map -> {
            map.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }
        R.id.satellite_map -> {
            map.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }
        R.id.terrain_map -> {
            map.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        locationManager =
            requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager

        locationListener = object : LocationListener {
            @SuppressLint("MissingPermission")
            override fun onLocationChanged(location: Location) {
                Log.i(this.javaClass.simpleName, "Location has changed:" + location.latitude + "," + location.longitude)
                map.setMyLocationEnabled(true)

                val userLocation = LatLng(location.latitude, location.longitude)
                map.moveCamera(CameraUpdateFactory.newLatLng(userLocation))
                map.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(
                            location.latitude,
                            location.longitude
                        ), DEFAULT_ZOOM.toFloat()
                    )
                )
            }

            override fun onStatusChanged(provider: String, status: Int, extras: Bundle?) {}

            override fun onProviderEnabled(provider: String) {}

            override fun onProviderDisabled(provider: String) {
            }
        }

        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            map.setMyLocationEnabled(true)
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                0,
                0f,
                locationListener
            )

            lastKnownLocation =
                locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            if (lastKnownLocation != null) {
                map.setMyLocationEnabled(true)
                val userLocation =
                    LatLng(lastKnownLocation!!.latitude, lastKnownLocation!!.longitude)
                map.moveCamera(CameraUpdateFactory.newLatLng(userLocation))
            }

        } else {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1
            )
        }

        setMapStyle(map)
        setMapLongClick(map)
        setPoiClick(map)


        if (locationEnabled) {
            map.setMyLocationEnabled(true)
        }
    }

    private fun setMapStyle(map: GoogleMap) {
        try {
            val success = map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    requireActivity(),
                    R.raw.map_style
                )
            )

            if (!success) {
                Log.e(TAG, "Style parsing failed.")
            }
        } catch (e: Resources.NotFoundException) {
            Log.e(TAG, "Can't find style. Error: ", e)
        }
    }

    private fun setMapLongClick(map: GoogleMap) {
        map.setOnMapLongClickListener { latLng ->
            removePreviousMarker()
            val snippet = String.format(
                Locale.getDefault(),
                "Lat: %1$.5f, Long: %2$.5f",
                latLng.latitude,
                latLng.longitude
            )

            marker = map.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title(getString(R.string.dropped_pin))
                    .snippet(snippet)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
            )
            markerCount++
        }
    }

    private fun setPoiClick(map: GoogleMap) {
        map.setOnPoiClickListener { poi ->
            removePreviousMarker()
            val poiMarker = map.addMarker(
                MarkerOptions()
                    .position(poi.latLng)
                    .title(poi.name)
            )
            poiMarker.showInfoWindow()

            marker = poiMarker
            markerCount++
        }
    }

    private fun removePreviousMarker() {
        if (markerCount > 0 && marker != null) marker.remove()
    }



    // result from requesting permissions
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                val locationRequest = LocationRequest.create().apply {
                    priority = LocationRequest.PRIORITY_LOW_POWER
                }

                val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
                val settingsClient = LocationServices.getSettingsClient(requireActivity())
                val locationSettingsResponseTask =
                    settingsClient.checkLocationSettings(builder.build())
                locationSettingsResponseTask.addOnFailureListener { exception ->
                    if (exception is ResolvableApiException){
                        try {
                            exception.startResolutionForResult(requireActivity(),
                                REQUEST_TURN_DEVICE_LOCATION_ON
                            )
                        } catch (sendEx: IntentSender.SendIntentException) {
                            Log.d(this.javaClass.simpleName, "Error getting location settings resolution: " + sendEx.message)
                        }
                    }
                }

                locationSettingsResponseTask.addOnCompleteListener {
                    if ( it.isSuccessful ) {
                        map.setMyLocationEnabled(true)
                        locationManager.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER,
                            0,
                            0f,
                            locationListener
                        )

                        lastKnownLocation =
                            locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                        if (lastKnownLocation != null) {
                            map.setMyLocationEnabled(true)
                            val userLocation =
                                LatLng(lastKnownLocation!!.latitude, lastKnownLocation!!.longitude)
                            map.moveCamera(CameraUpdateFactory.newLatLng(userLocation))
                        }
                    }

                }

                Log.i(TAG, "Permissions GRanted on Listener, setting location layer to true")
                map.setMyLocationEnabled(true)
                locationEnabled = true
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    0,
                    0f,
                    locationListener
                );
            }
        }
    }


    companion object {
        private const val DEFAULT_ZOOM = 15
        private val TAG = SelectLocationFragment::class.java.simpleName
    }
}

private const val REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE = 34
private const val REQUEST_TURN_DEVICE_LOCATION_ON = 29