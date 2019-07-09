package com.jackandtim.friendcompass.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.jackandtim.friendcompass.R

class TrackingService : Service() {
    val user = FirebaseAuth.getInstance().currentUser
    val db = FirebaseFirestore.getInstance()

    lateinit var client: FusedLocationProviderClient

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            buildNotification()
        } else {
            buildNotificationWithChannel()
        }
        requestLocationUpdates()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onDestroy() {
        client.removeLocationUpdates(locationCallback)
        val updates = HashMap<String, Any>()
        updates["location"] = FieldValue.delete()
        db.collection("users").document(user!!.uid).update(updates)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val mNotificationManager = this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?
            mNotificationManager!!.deleteNotificationChannel(NOTIFICATION_CHANNEL_ID)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun buildNotificationWithChannel() {
        val notification = NotificationCompat.Builder(this, Notification.CATEGORY_SERVICE).run {
            setContentTitle(getString(R.string.app_name))
            setContentText(getString(R.string.tracking_enabled_notif))
            setOngoing(true)
            setSmallIcon(R.drawable.tracking_enabled)
            setChannelId(NOTIFICATION_CHANNEL_ID)
            setAutoCancel(true)
            build()
        }

        val mNotificationManager = this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?

        val importance = NotificationManager.IMPORTANCE_LOW
        val notificationChannel =
            NotificationChannel(NOTIFICATION_CHANNEL_ID, "Friend Compass", importance).apply {
                description = "Notify the user when location is being shared"
            }

        mNotificationManager!!.createNotificationChannel(notificationChannel)
        mNotificationManager.notify(1, notification)
    }

    companion object {
        const val NOTIFICATION_CHANNEL_ID = "10001"
    }

    private fun buildNotification() {
        val notification = NotificationCompat.Builder(this, Notification.CATEGORY_SERVICE).apply {
            setContentTitle(getString(R.string.app_name))
            setContentText(getString(R.string.tracking_enabled_notif))
            setOngoing(true)
            setSmallIcon(R.drawable.tracking_enabled)
        }

        startForeground(1, notification.build())
    }

    private fun requestLocationUpdates() {
        val request = LocationRequest().apply {
            interval = 5000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        client = LocationServices.getFusedLocationProviderClient(this)
        val permission = ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)

        if (permission == PackageManager.PERMISSION_GRANTED) {
            client.requestLocationUpdates(request, locationCallback, null)
        }
    }

    private val locationCallback = object: LocationCallback() {
        override fun onLocationResult(result: LocationResult?) {
            if (result != null) {
                val latLong = LatLng(result.lastLocation.latitude, result.lastLocation.longitude)
                val newLocation = HashMap<String, Any>()
                newLocation["location"] = latLong
                db.collection("users").document(user!!.uid).set(newLocation, SetOptions.merge())
            }
        }
    }
}
