package com.example.android

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.Bundle
import android.os.Looper
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.AspectRatio
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.android.models.Constants.LABELS_PATH
import com.example.android.models.Constants.MODEL_PATH
import com.example.android.models.BoundingBox
import com.example.android.models.Detector
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import com.example.android.databinding.ActivityDetectorBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import android.os.Handler
import android.view.View
import com.example.android.models.Info
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import android.util.Base64
import java.io.ByteArrayOutputStream
import com.example.android.api.ApiClient
import com.example.android.api.CarPlateApi
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DetectorActivity : AppCompatActivity(), Detector.DetectorListener {
    private lateinit var binding: ActivityDetectorBinding
    private val isFrontCamera = false
    private var preview: Preview? = null
    private var imageAnalyzer: ImageAnalysis? = null
    private var camera: Camera? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private var detector: Detector? = null
    private lateinit var cameraExecutor: ExecutorService
    private val handler = Handler(Looper.getMainLooper())
    private val api = ApiClient.createService(CarPlateApi::class.java)
    private var isDataSave = false
    private var isUnder5Seconds = true
    private var isFirstRound = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetectorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cameraExecutor = Executors.newSingleThreadExecutor()

        cameraExecutor.execute {
            detector = Detector(baseContext, MODEL_PATH, LABELS_PATH, this)
        }

        handler.post(updateTimeRunnable)

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }

        bindListeners()
    }

    private fun bindListeners() {
        binding.apply {
            isGpu.setOnCheckedChangeListener { buttonView, isChecked ->
                cameraExecutor.submit {
                    detector?.restart(isGpu = isChecked)
                }
                if (isChecked) {
                    buttonView.setBackgroundColor(ContextCompat.getColor(baseContext, R.color.orange))
                } else {
                    buttonView.setBackgroundColor(ContextCompat.getColor(baseContext, R.color.gray))
                }
            }
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            cameraProvider  = cameraProviderFuture.get()
            bindCameraUseCases()
        }, ContextCompat.getMainExecutor(this))
    }

    private fun bindCameraUseCases() {
        val cameraProvider = cameraProvider ?: throw IllegalStateException("Camera initialization failed.")

        val rotation = binding.viewFinder.display.rotation

        val cameraSelector = CameraSelector
            .Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build()

        preview = Preview.Builder()
            .setTargetAspectRatio(AspectRatio.RATIO_4_3)
            .setTargetRotation(rotation)
            .build()

        imageAnalyzer = ImageAnalysis.Builder()
            .setTargetAspectRatio(AspectRatio.RATIO_4_3)
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .setTargetRotation(binding.viewFinder.display.rotation)
            .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
            .build()

        imageAnalyzer?.setAnalyzer(cameraExecutor) { imageProxy ->
            val bitmapBuffer =
                Bitmap.createBitmap(
                    imageProxy.width,
                    imageProxy.height,
                    Bitmap.Config.ARGB_8888
                )
            imageProxy.use { bitmapBuffer.copyPixelsFromBuffer(imageProxy.planes[0].buffer) }
            imageProxy.close()

            val matrix = Matrix().apply {
                postRotate(imageProxy.imageInfo.rotationDegrees.toFloat())

                if (isFrontCamera) {
                    postScale(
                        -1f,
                        1f,
                        imageProxy.width.toFloat(),
                        imageProxy.height.toFloat()
                    )
                }
            }

            val rotatedBitmap = Bitmap.createBitmap(
                bitmapBuffer, 0, 0, bitmapBuffer.width, bitmapBuffer.height,
                matrix, true
            )

            // Detect the license plate and bounding box
            val result = detector?.detect(rotatedBitmap)
            if(result != null){
                val (frame, bestBoxes, inferenceTime) = result
                Log.d("TEST", "frame: $frame , bestBoxes: $bestBoxes , inferenceTime: $inferenceTime")

                if(isUnder5Seconds){
                    isUnder5Seconds = false;

                    binding.Status.apply {
                        text = "Detecting License Plate \n Wait for 5 Seconds"
                        setTextColor(ContextCompat.getColor(baseContext, R.color.red))
                        Handler(Looper.getMainLooper()).postDelayed({
                            isUnder5Seconds = true;

                            if(!isFirstRound && !isDataSave){
                                Log.d("Execute", "Execute onDetect block")
                                onDetect(frame, bestBoxes!!, inferenceTime)
                            }

                            isFirstRound = false;
                            Log.d("Execute", "Execute the block after 5 seconds.")
                        }, 5000)
                    }
                }
            }
        }

        cameraProvider.unbindAll()

        try {
            camera = cameraProvider.bindToLifecycle(
                this,
                cameraSelector,
                preview,
                imageAnalyzer
            )
            preview?.setSurfaceProvider(binding.viewFinder.surfaceProvider)
        } catch(exc: Exception) {
            Log.e(TAG, "Use case binding failed", exc)
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()) {
        if (it[Manifest.permission.CAMERA] == true) { startCamera() }
    }

    override fun onDestroy() {
        super.onDestroy()
        detector?.close()
        cameraExecutor.shutdown()
    }

    override fun onResume() {
        super.onResume()
        if (allPermissionsGranted()){
            startCamera()
        } else {
            requestPermissionLauncher.launch(REQUIRED_PERMISSIONS)
        }
    }

    companion object {
        private const val TAG = "Camera"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = mutableListOf (
            Manifest.permission.CAMERA
        ).toTypedArray()
    }

    // Not detect anything
    override fun onEmptyDetect() {
        runOnUiThread {
            binding.Status.apply { text = "" }
            binding.EntryTime.apply { text = "" }
            binding.carplate.apply { text = "" }
            binding.Opening.apply { text = "" }
            binding.licensePlateThumbnail.apply {
                visibility = View.INVISIBLE
            }
            isDataSave = false
            isFirstRound = true
            binding.overlay.clear()
        }
    }

    // When detect something
    override fun onDetect(frame: Bitmap, boundingBoxes: List<BoundingBox>, inferenceTime: Long) {
        val carplateBox = boundingBoxes.firstOrNull { it.clsName == "Carplate" && it.cnf >= 0.75 }

        runOnUiThread {
            binding.inferenceTime.text = "${inferenceTime}ms"
            // Display the License Plate Bounding Box
            binding.overlay.apply {
                setResults(boundingBoxes)
                invalidate()
            }

            if (carplateBox != null) {
                var licensePlate = ""
                var localCroppedBitmap: Bitmap? = null
                val totalBoundingBoxes = boundingBoxes.size
                var completedCount = 0

                binding.Status.apply {
                    text = ""
                    setTextColor(ContextCompat.getColor(baseContext, R.color.red))
                }

                binding.EntryTime.apply {
                    setTextColor(ContextCompat.getColor(baseContext, R.color.black))
                    text = getCurrentTime()
                }

                binding.Opening.apply {
                    setTextColor(ContextCompat.getColor(baseContext, R.color.black))
                    text = "Now Opening"
                }
                binding.licensePlateThumbnail.apply {
                    visibility = View.VISIBLE
                }

                boundingBoxes.forEach { boundingBox ->
                    detectLicensePlate(frame, boundingBox) { detectedText, croppedBitmap ->
                        licensePlate = detectedText
                        localCroppedBitmap = croppedBitmap

                        completedCount++
                        if (completedCount == totalBoundingBoxes) {
                            localCroppedBitmap?.let { bitmap ->
                                val info = Info(
                                    LabelName = licensePlate,
                                    EntryTime = getCurrentTime(),
                                    OutTime = null,
                                    IsOut = false,
                                    ParkingHours = null,
                                    IsPaid = false,
                                    Total = 0.0,
//                                Picture = null,
                                    Picture = bitmapToBase64(frame),
//                                PictureThumbnail = null
                                    PictureThumbnail = bitmapToBase64(localCroppedBitmap ?: frame)
                                )
                                sendDataToServer(info)
                                isDataSave = true
                            }
                        }
                    }
                }
            }
        }
    }

    private fun extractBoundingBoxBitmap(frame: Bitmap, boundingBox: BoundingBox): Bitmap{
        val left = (boundingBox.x1 * frame.width).toInt()
        val top = (boundingBox.y1 * frame.height).toInt()
        val width = ((boundingBox.x2 - boundingBox.x1) * frame.width).toInt()
        val height = ((boundingBox.y2 - boundingBox.y1) * frame.height).toInt()

        return Bitmap.createBitmap(frame, left, top, width, height)
    }

    private fun detectLicensePlate(
        frame: Bitmap,
        boundingBox: BoundingBox,
        callback: (String, Bitmap) -> Unit
    ) {
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        val croppedBitmap = extractBoundingBoxBitmap(frame, boundingBox)
        val image = InputImage.fromBitmap(croppedBitmap, 0)

        // Display Cropped License Image
        binding.licensePlateThumbnail.setImageBitmap(croppedBitmap)

        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                val detectedText = visionText.text.uppercase().replace(Regex("\\s|ONTARIO"), "")
                binding.carplate.apply {
                    setTextColor(ContextCompat.getColor(baseContext, R.color.black))
                    text = detectedText
                }
                Log.d("Detected Text", detectedText)

                callback(detectedText, croppedBitmap)
            }
            .addOnFailureListener { e ->
                Log.e("Text Recognition Error", e.message.toString())
                callback("", croppedBitmap)
            }
    }


    fun bitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, outputStream)
        val byteArray = outputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    private fun sendDataToServer(info: Info) {
//        Log.d("Detect Data", info.toString())
        api.saveDetect(info).enqueue(object : Callback<Info> {
            override fun onResponse(call: Call<Info>, response: Response<Info>) {
                if (response.isSuccessful) {
                    Log.d("DetectActivity", "POST request to /api/action/saveDetect was successful")
                } else {
                    Log.d("DetectActivity", "POST request to /api/action/saveDetect failed")
                }
            }

            override fun onFailure(call: Call<Info>, t: Throwable) {
                Log.e("DetectActivity", "Error: ${t.message}")
            }
        })
    }

    private fun getCurrentTime(): String {
        val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone("America/Toronto")
        return sdf.format(Date())
    }

    private val updateTimeRunnable = object : Runnable {
        override fun run() {
            val currentTime = getCurrentTime()
            binding.nowTime.text = currentTime
            handler.postDelayed(this, 1000)
        }
    }
}