package com.jackandtim.friendcompass.fragments

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import com.jackandtim.friendcompass.R
import com.jackandtim.friendcompass.services.TrackingService

class SettingsFragment : PreferenceFragmentCompat() {
    private val PERMISSIONS_REQUEST = 0
    private lateinit var broadcastSwitch: SwitchPreference

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        broadcastSwitch = findPreference("broadcastPreference")!!
        broadcastSwitch.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, value ->
            val enabled = value as Boolean
            if (enabled) {
                val permission = ContextCompat.checkSelfPermission(context!!, Manifest.permission.ACCESS_FINE_LOCATION)

                if (permission == PackageManager.PERMISSION_GRANTED) {
                    startTrackerService()
                } else {
                    val permissions = arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION)
                    requestPermissions(permissions, PERMISSIONS_REQUEST)
                }
            } else {
                activity!!.stopService(Intent(activity, TrackingService::class.java))
            }
            true
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == PERMISSIONS_REQUEST && grantResults.size == 1
            && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startTrackerService()
        } else {
            broadcastSwitch.isChecked = false
            Toast.makeText(activity, getText(R.string.enable_location), Toast.LENGTH_LONG).show();
        }
    }

    private fun startTrackerService() {
        val locationManager = activity!!.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            broadcastSwitch.isChecked = false
            promptForGPS()
        } else {
            activity!!.startService(Intent(activity, TrackingService::class.java))
            Toast.makeText(activity, getText(R.string.tracking_enabled), Toast.LENGTH_SHORT).show()
        }
    }

    private fun promptForGPS() {
        AlertDialog.Builder(context!!).apply {
            setMessage(getString(R.string.gps_not_enabled))
            setPositiveButton(R.string.settings) { _, _ ->
                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
            setNegativeButton(getString(R.string.cancel)) { _, _ -> }
            show()
        }
    }
}