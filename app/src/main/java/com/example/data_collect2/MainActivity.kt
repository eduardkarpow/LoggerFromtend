package com.example.data_collect2

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Camera
import android.graphics.SurfaceTexture
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CaptureRequest
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.provider.MediaStore
import android.view.Surface
import android.view.TextureView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import com.example.data_collect2.databinding.ActivityMainBinding
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var sensorManager: SensorManager
    private lateinit var accelerometer: Sensor
    private lateinit var client: OkHttpClient
    private lateinit var cameraManager: CameraManager
    private lateinit var handler: Handler
    private lateinit var cameraDevice: CameraDevice
    private val ip: String = "http://192.168.1.128:8000/"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        client = OkHttpClient()

        binding.button2.setOnClickListener {
            if(binding.button2.text == "ON"){
                binding.button2.text = "OFF"
                setUpSensorStuff()
            } else {
                binding.button2.text = "ON"
                sensorManager.unregisterListener(this)
            }
        }

        val handlerThread = HandlerThread("videoThread")
        handlerThread.start()
        handler = Handler(handlerThread.looper)

        getPermissions()
        binding.textureView.surfaceTextureListener = object:TextureView.SurfaceTextureListener{
            override fun onSurfaceTextureAvailable(
                surface: SurfaceTexture,
                width: Int,
                height: Int
            ) {
                openCamera()
            }

            override fun onSurfaceTextureSizeChanged(
                surface: SurfaceTexture,
                width: Int,
                height: Int
            ) {
                TODO("Not yet implemented")
            }

            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
                return false
            }

            override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
                println("123")
            }
        }
        cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager

    }

    @SuppressLint("MissingPermission")
    private fun openCamera() {
        cameraManager.openCamera(cameraManager.cameraIdList[0],
            object:CameraDevice.StateCallback(){
                override fun onOpened(camera: CameraDevice) {
                    cameraDevice = camera

                    var surfaceTexture = binding.textureView.surfaceTexture
                    var surface = Surface(surfaceTexture)

                    var captureRequest = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
                    captureRequest.addTarget(surface)

                    cameraDevice.createCaptureSession(listOf(surface), object: CameraCaptureSession.StateCallback() {
                        override fun onConfigured(session: CameraCaptureSession) {
                            session.setRepeatingRequest(captureRequest.build(), null, null)
                        }

                        override fun onConfigureFailed(session: CameraCaptureSession) {
                            TODO("Not yet implemented")
                        }
                    }, handler)
                }

                override fun onDisconnected(camera: CameraDevice) {
                    TODO("Not yet implemented")
                }

                override fun onError(camera: CameraDevice, error: Int) {
                    TODO("Not yet implemented")
                }
                                               }, handler)
    }

    private fun getPermissions() {
        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(arrayOf(android.Manifest.permission.CAMERA), 101)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(grantResults[0] != PackageManager.PERMISSION_GRANTED){
            getPermissions()
        }
    }

    private fun setUpSensorStuff() {
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.also{
            sensorManager.registerListener(this, it,
                SensorManager.SENSOR_DELAY_NORMAL,
                SensorManager.SENSOR_DELAY_NORMAL)
        }
        sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)?.also {
            sensorManager.registerListener(
                this, it,
                SensorManager.SENSOR_DELAY_NORMAL,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }




    }
    private fun uploadPhoto(photo: Bitmap){
        val sdf = SimpleDateFormat("yyyyMMddHHmmss", Locale.ENGLISH)
        var filename = sdf.format(Date())
        val path = applicationContext.filesDir.path
        var file: File = File(path, "image1.jpg")
    }
    private fun postAcc(ax:Float, ay:Float, az:Float){
        val retrofit = Retrofit.Builder()
            .baseUrl(ip)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val retrofitAPI = retrofit.create(RetrofitAPI::class.java)

        val dataModal: DataModal = DataModal(ax.toString(),
                                             ay.toString(),
                                             az.toString())
        val call: Call<DataModal?>? = retrofitAPI.postData(dataModal)
        call!!.enqueue(object: Callback<DataModal?>{
            override fun onResponse(call: Call<DataModal?>, response: Response<DataModal?>) {
                println("Success")
            }

            override fun onFailure(call: Call<DataModal?>, t: Throwable) {
                println("Error"+t.message)
            }
        })



    }
    private fun postGyro(bx:Float, by:Float, bz:Float){
        val retrofit = Retrofit.Builder()
            .baseUrl(ip)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val retrofitAPI = retrofit.create(RetrofitAPI::class.java)

        val dataModal: GyroModal = GyroModal(bx.toString(),
            by.toString(),
            bz.toString())
        val call: Call<GyroModal?>? = retrofitAPI.postGyro(dataModal)
        call!!.enqueue(object: Callback<GyroModal?>{
            override fun onResponse(call: Call<GyroModal?>, response: Response<GyroModal?>) {
                println("Success")
            }

            override fun onFailure(call: Call<GyroModal?>, t: Throwable) {
                println("Error"+t.message)
            }
        })
    }

    public override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        return
    }

    public override fun onSensorChanged(event: SensorEvent?) {
        if(event?.sensor?.type == Sensor.TYPE_ACCELEROMETER){
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            val str = "${x.toString()} ${y.toString()} ${z.toString()}"
            binding.text.text = str
            Thread {
                postAcc(x, y, z)
                runOnUiThread {

                }
            }.start()

        }
        if(event?.sensor?.type == Sensor.TYPE_GYROSCOPE){
            val bx = event.values[0]
            val by = event.values[1]
            val bz = event.values[2]

            val str = "${bx.toString()} ${by.toString()} ${bz.toString()}"
            binding.gyro.text = str
            Thread{
                postGyro(bx,by,bz)
                runOnUiThread {

                }
            }.start()

        }
    }

    override fun onDestroy() {
        sensorManager.unregisterListener(this)
        super.onDestroy()
    }
}