package com.jackandtim.friendcompass.activities

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.FirebaseFirestore
import com.jackandtim.friendcompass.R
import java.util.*

class CompassActivity : AppCompatActivity(), SensorEventListener {
    private var compassImg: ImageView? = null
    private var mAzimuth: Int = 0
    private var mSensorManager: SensorManager? = null
    private var mRotationV: Sensor? = null
    private var mAccelerometer: Sensor? = null
    private var mMagnetometer: Sensor? = null
    private var haveSensor = false
    private var haveSensor2 = false
    private var rMat = FloatArray(16)
    private var orientation = FloatArray(3)
    private val mLastAccelerometer = FloatArray(3)
    private val mLastMagnetometer = FloatArray(3)
    private var mLastAccelerometerSet = false
    private var mLastMagnetometerSet = false
    private  val db = FirebaseFirestore.getInstance()
    private var friendLocation = Location("friend")
    private lateinit var fusedLocationClient: FusedLocationProviderClient


    private val hasLocationPermissions
        get() = checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

    private fun promptForGPS() {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            AlertDialog.Builder(this).apply {
                setMessage(getString(R.string.gps_not_enabled))
                setPositiveButton(R.string.settings) { _, _ ->
                    startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                }
                setNegativeButton(getString(R.string.cancel)) { _, _ -> }
                show()
            }
        }
    }


    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_compass)

        mSensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        compassImg = findViewById(R.id.arrow)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        promptForGPS()
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager



        val friendId = intent.getStringExtra("tracked_user_id")

        db.collection("users").document(friendId)
            .addSnapshotListener{ snapshot, _ ->
                if (snapshot != null && snapshot.exists()) {
                    if (snapshot["location"] != null) {
                        val location = snapshot["location"] as HashMap<String, Double>
                        friendLocation.latitude = location["latitude"]!!
                        friendLocation.longitude = location["longitude"]!!
                    } else {
                        Toast.makeText(this, getText(R.string.stopped_sharing), Toast.LENGTH_LONG).show()
                        finish()
                    }
                }
            }
    }

    private fun noSensorsAlert() {
        val alertDialog = AlertDialog.Builder(this)
        alertDialog.setMessage(getString(R.string.no_support))
            .setCancelable(false)
            .setNegativeButton(getString(R.string.close)) { _, _ -> finish() }
        alertDialog.show()
    }

    override fun onPause() {
        super.onPause()

        if (haveSensor && haveSensor2) {
            mSensorManager!!.unregisterListener(this, mAccelerometer)
            mSensorManager!!.unregisterListener(this, mMagnetometer)
        } else {
            if (haveSensor) {
                mSensorManager!!.unregisterListener(this, mRotationV)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (mSensorManager!!.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR) == null) {
            if (mSensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) == null ||
                mSensorManager!!.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) == null) {
                noSensorsAlert()
            } else {
                mAccelerometer = mSensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
                mMagnetometer = mSensorManager!!.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
                haveSensor = mSensorManager!!.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_UI)
                haveSensor2 = mSensorManager!!.registerListener(this, mMagnetometer, SensorManager.SENSOR_DELAY_UI)
            }
        } else {
            mRotationV = mSensorManager!!.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
            haveSensor = mSensorManager!!.registerListener(this, mRotationV, SensorManager.SENSOR_DELAY_UI)
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

    }

    private var MY_PERMISSIONS_REQUEST_LOCATION = 99;
    override fun onSensorChanged(event: SensorEvent) {

        if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
            SensorManager.getRotationMatrixFromVector(rMat, event.values);
            mAzimuth = ((Math.toDegrees(SensorManager.getOrientation(rMat, orientation)[0].toDouble()) + 360) % 360).toInt();
        }

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, mLastAccelerometer, 0, event.values.size);
            mLastAccelerometerSet = true;
        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, mLastMagnetometer, 0, event.values.size);
            mLastMagnetometerSet = true;
        }
        if (mLastAccelerometerSet && mLastMagnetometerSet) {
            SensorManager.getRotationMatrix(rMat, null, mLastAccelerometer, mLastMagnetometer);
            SensorManager.getOrientation(rMat, orientation)
            mAzimuth =
                ((Math.toDegrees(SensorManager.getOrientation(rMat, orientation)[0].toDouble()) + 360) % 360).toInt()
        }


        mAzimuth = Math.round(mAzimuth.toDouble()).toInt()

        // check permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                Array(2){Manifest.permission.ACCESS_FINE_LOCATION},
                MY_PERMISSIONS_REQUEST_LOCATION)
            ActivityCompat.requestPermissions(this,
                Array(2){Manifest.permission.ACCESS_COARSE_LOCATION},
                MY_PERMISSIONS_REQUEST_LOCATION)
        } else {
            // already permission granted
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location ->
                val bearing = location.bearingTo(friendLocation)

                compassImg!!.rotation = (-mAzimuth + bearing)

            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }
 }