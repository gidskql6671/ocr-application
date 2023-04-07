package com.example.ocr_application

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.example.ocr_application.databinding.ActivityMainBinding
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.normal.TedPermission
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {

    lateinit var button: Button

    private var currentPhotoPath: String = ""
    private lateinit var binding: ActivityMainBinding
    private val request_image_code = 101


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //권한 체크
        TedPermission.create()
            .setPermissionListener(object: PermissionListener {
                override fun onPermissionGranted() {
                    button = binding.btnPicture.also {
                        it.setOnClickListener { capture() }
                    }
                }
                override fun onPermissionDenied(deniedPermissions: MutableList<String>) {
                    Toast.makeText(this@MainActivity, "권한을 허가해주세요.", Toast.LENGTH_SHORT).show()
                }
            })
            .setRationaleMessage("권한이 필요합니다.")
            .setDeniedMessage("권한을 거부하셨습니다.")
            .setPermissions(Manifest.permission.CAMERA)
            .check();

    }

    private fun capture() {
        val imageTakeIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (imageTakeIntent.resolveActivity(packageManager) != null) {
            var photoFile: File? = null

            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
            val imageFileName = "Capture_${timestamp}_"

            try {
                val tempImage = File.createTempFile(imageFileName, ".jpg", cacheDir)
                currentPhotoPath = tempImage.absolutePath
                photoFile = tempImage
            } catch (_: IOException) {
            }

            if (photoFile != null) {
                val photoURI: Uri =
                    FileProvider.getUriForFile(this, "$packageName.provider", photoFile)
                imageTakeIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                startActivityForResult(imageTakeIntent, request_image_code)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == request_image_code && resultCode == RESULT_OK) {
            val intent = Intent(this, ResultActivity::class.java)

            intent.putExtra("imagePath", currentPhotoPath)

            startActivity(intent)
        }
    }

    companion object {
        init {
            System.loadLibrary("ocr_application")
            System.loadLibrary("opencv_java4")
        }
    }
}