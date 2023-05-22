package com.example.ocr_application.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.example.ocr_application.databinding.ActivityFreeModeBinding
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class FreeModeActivity : AppCompatActivity() {

    lateinit var button: Button

    private var currentPhotoPath: String = ""
    private lateinit var binding: ActivityFreeModeBinding
    private val request_image_code = 101


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityFreeModeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        button = binding.btnPicture.also {
            it.setOnClickListener {
                capture()
//                mockCapture()
            }
        }

    }

    private fun mockCapture() {
        try {
            val inputStream = assets.open("sample.jpeg")
            val photoFile: File = File.createTempFile("sample", ".jpg", cacheDir)

            val buffer = ByteArray(8192)
            var bytesRead: Int
            val output = ByteArrayOutputStream()
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                output.write(buffer, 0, bytesRead)
            }

            photoFile.writeBytes(output.toByteArray())

            val intent = Intent(this, ResultActivity::class.java)

            intent.putExtra("imagePath", photoFile.absolutePath)

            startActivity(intent)
        } catch (_: IOException) {
        }

    }

    private fun capture() {
        val imageTakeIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (imageTakeIntent.resolveActivity(packageManager) != null) {
            var photoFile: File? = null

            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
            val imageFileName = "Capture_${timestamp}_"

            try {
                photoFile = File.createTempFile(imageFileName, ".jpg", cacheDir)
                currentPhotoPath = photoFile.absolutePath
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
}