package project.aop.jinwoo.gp2.Register

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_take_photo.*
import project.aop.jinwoo.gp2.R
import project.aop.jinwoo.gp2.databinding.ActivityTakePhotoBinding

import java.io.File
import java.text.SimpleDateFormat
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraActivity : AppCompatActivity() {
    // 뷰 바인딩 사용////////////////////////////////////
    lateinit private var binding : ActivityTakePhotoBinding
    private var preview : Preview? = null
    private var imageCapture: ImageCapture? = null
    private lateinit var outputDirectory: File
    private lateinit var cameraExecutor: ExecutorService
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTakePhotoBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        startCamera()
        binding.takePhotoButton.setOnClickListener {
            takePhoto()
        }
        outputDirectory = getOutputDirectory()
        cameraExecutor = Executors.newSingleThreadExecutor()
    }
    private fun takePhoto() {
        val imageCapture = imageCapture ?: return
// 이밎 파일 생성/////////////////////////////////////
        val photoFile = File(
            outputDirectory,
            newJpgFileName())
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    ///이미지 작성 실패 시 ...//////////////////////
                    Log.d("CameraX-Debug", "Photo capture failed: ${exc.message}", exc)
                }
                //이미지 저장하기 ////////////////////
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = Uri.fromFile(photoFile)
                    val msg = "Photo capture succeeded: $savedUri"
                    Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
                    Snackbar.make(take_photo_button, "촬영완료", Snackbar.LENGTH_SHORT).show()
                    //액티비티 전환시 이전 작업 유지를 위한 인텐트 주고 받기////////////////////
                    var intent2 = Intent(this@CameraActivity, RegisterParkinglot::class.java)
                    intent2.putExtra("url",savedUri.toString())
                    if(intent.hasExtra("name")&&intent.hasExtra("num")&&intent.hasExtra("coin")) {
                        intent2.putExtra("name",intent.getStringExtra("name"))
                        intent2.putExtra("num",intent.getStringExtra("num"))
                        intent2.putExtra("coin",intent.getStringExtra("coin"))

                    }
                    startActivity(intent2)
                    //////////////////////////////////////////////////////////////////
                }
            })
    }
    // viewFinder 설정 : Preview
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
// Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
// Preview
            preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
                }
// ImageCapture
            imageCapture = ImageCapture.Builder()
                .build()
// Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            try {
// Unbind use cases before rebinding
                cameraProvider.unbindAll()
// 바인드 작업
                cameraProvider.bindToLifecycle(
                    this,
                    cameraSelector,
                    preview,
                    imageCapture)
            } catch(exc: Exception) {
                Log.d("CameraX-Debug", "Use case binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(this))
    }
    //이미지파일 이름 설정
    private fun newJpgFileName() : String {
        val sdf = SimpleDateFormat("yyyyMMdd_HHmmss")
        val filename = sdf.format(System.currentTimeMillis())
        return "${filename}.jpg"
    } //파일에 저장
    private fun getOutputDirectory(): File {
        val mediaDir = externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply {
                mkdirs()
            }
        }
        return if (mediaDir != null && mediaDir.exists()) mediaDir
        else filesDir
    }
    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

}
