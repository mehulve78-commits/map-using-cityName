package com.app.map_currentusingsearchcityname

import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var googleMap: GoogleMap
    private lateinit var cityEditText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        cityEditText = findViewById(R.id.edt_city)
        val submitButton: Button = findViewById(R.id.btn_search)

        submitButton.setOnClickListener {
            val cityName = cityEditText.text.toString()
            if (cityName.isNotBlank()) {
                fetchLocationAndShowMarker(cityName)
            }
        }

        // Request location permission if not granted
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }



    override fun onMapReady(gMap: GoogleMap) {
        googleMap = gMap
        googleMap.uiSettings.isZoomControlsEnabled = true
        googleMap.uiSettings.isMyLocationButtonEnabled = true
    }

    private fun fetchLocationAndShowMarker(cityName: String) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val apiKey = "AIzaSyAoSIMdMnHUUAkJ7vuKCBkRUOMXVqo6gfA"
                val apiUrl =
                    "https://maps.googleapis.com/maps/api/geocode/json?address=$cityName&key=$apiKey"

                val url = URL(apiUrl)
                val connection = url.openConnection() as HttpURLConnection
                val inputStream = connection.inputStream
                val reader = InputStreamReader(inputStream)

                val responseData = StringBuilder()
                reader.forEachLine {
                    responseData.append(it)
                }

                val json = JSONObject(responseData.toString())
                val location = json.getJSONArray("results")
                    .getJSONObject(0)
                    .getJSONObject("geometry")
                    .getJSONObject("location")

                val lat = location.getDouble("lat")
                val lng = location.getDouble("lng")

                launch(Dispatchers.Main) {
                    showMarkerOnMap(LatLng(lat, lng), cityName)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun showMarkerOnMap(latLng: LatLng, cityName: String) {
        googleMap.clear()
        val markerOptions = MarkerOptions()
            .position(latLng)
            .title(cityName)
        googleMap.addMarker(markerOptions)
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10f))
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
    }
}