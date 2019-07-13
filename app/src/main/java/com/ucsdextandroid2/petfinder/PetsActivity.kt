package com.ucsdextandroid2.petfinder

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.*
import com.google.android.material.bottomnavigation.BottomNavigationView

class PetsActivity : AppCompatActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val LOCATION_REQUEST_CODE = 9

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pets)
        val navView: BottomNavigationView = findViewById(R.id.nav_view)

        val recyclerView: RecyclerView = findViewById(R.id.recyclerview)
//        val adapter = PetsAdapter()
        recyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
//        recyclerView.adapter = adapter

        //LivePagedListBuilder of the PetsDataSourceFactory

        checkForLocationPermission(true)
    }

    private fun checkForLocationPermission(showRationale: Boolean) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getLocation()
        }
        else {
            if (!showRationale) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_REQUEST_CODE)
            }
            else {
                showPermissionRationaleDialog()
            }
        }
    }

    private fun showPermissionRationaleIfAble(): Boolean {
        val ableToShowRationale: Boolean = ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)
        if (ableToShowRationale) {
            showPermissionRationaleDialog()
            return true
        }
        else {
            return false
        }
    }

    private fun showPermissionRationaleDialog() {
        AlertDialog.Builder(this)
            .setTitle("Location")
            .setMessage("We need your location in order to show you pets in your area")
            .setPositiveButton("Ok") { dialog, which ->
                if (which == DialogInterface.BUTTON_POSITIVE)
                    checkForLocationPermission(false)
            }.setNegativeButton("No Thanks") {dialog, which ->
                if (which == DialogInterface.BUTTON_NEGATIVE)
                    onGetLocationFailed()
            }
            .show()
    }


    @SuppressLint("MissingPermission")
    private fun getLocation() {
        toast("Getting Location")

        /*LocationServices
            .getFusedLocationProviderClient(this)
            .lastLocation
            .addOnSuccessListener { location: Location? ->
                toast("Location Found: ${location?.latitude}, ${location?.longitude}")
                setTitle("${location?.latitude}, ${location?.longitude}")
            }
            .addOnFailureListener {
                toast(it.message ?: "Find Location Failed")
            }*/

        val client = LocationServices.getFusedLocationProviderClient(this)
        val locationCallback: LocationCallback = object: LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                super.onLocationResult(locationResult)

                val location = locationResult?.lastLocation

                toast("Location Found: ${location?.latitude}, ${location?.longitude}")
                setTitle("${location?.latitude}, ${location?.longitude}")
            }
        }

        lifecycle.addObserver(object: DefaultLifecycleObserver {
            override fun onResume(owner: LifecycleOwner) {
                super.onResume(owner)
                client.requestLocationUpdates(LocationRequest(), locationCallback, null)
            }

            override fun onPause(owner: LifecycleOwner) {
                super.onPause(owner)
                client.removeLocationUpdates(locationCallback)
            }
        })
    }

    private fun onGetLocationFailed() {
        if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            toast("Getting Location Failed, go to Settings to enable this")
        }
        else {
            toast("Getting Location Failed")
        }
    }

    private fun toast(toastMessage: String) {
        Toast.makeText(this, toastMessage, Toast.LENGTH_LONG).show()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == LOCATION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocation()
            }
            else {
                onGetLocationFailed()
            }
        }
    }

    private class PetCardViewHolder private constructor(view: View) : RecyclerView.ViewHolder(view) {

        val image: ImageView = itemView.findViewById(R.id.vnc_image)
        val titleView: TextView = itemView.findViewById(R.id.vnc_title)
        val textView: TextView = itemView.findViewById(R.id.vnc_text)

        companion object {
            fun inflate(parent: ViewGroup): PetCardViewHolder = PetCardViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.view_holder_note_card, parent, false)
            )
        }

        fun bind(note: PetModel?) {
            image.isVisible = note?.imageUrl != null
//            image.loadImageUrl(note?.imageUrl)
            titleView.text = note?.name
            textView.text = note?.breed
        }

    }

}
