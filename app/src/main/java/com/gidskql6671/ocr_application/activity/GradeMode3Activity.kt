package com.gidskql6671.ocr_application.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeech.ERROR
import android.util.Log
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.gidskql6671.ocr_application.databinding.ActivityGradeMode3Binding
import com.gidskql6671.ocr_application.retrofit.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class GradeMode3Activity : AppCompatActivity() {

    lateinit var ttsBtn: ImageButton

    private lateinit var tts: TextToSpeech
    private var currentPhotoPath: String = ""
    private lateinit var binding: ActivityGradeMode3Binding
    private val request_image_code = 101
    private var correctText: String = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityGradeMode3Binding.inflate(layoutInflater)
        setContentView(binding.root)

        ttsBtn = binding.ttsBtn

        binding.btnBack.setOnClickListener { finish() }

        tts = TextToSpeech(this) {
            if(it != ERROR) {
                tts.language = Locale.KOREAN
            }
        }

        getProblem(
            intent.extras!!.getInt("studentGrade"),
            intent.extras!!.getInt("problemGrade")
        )
    }

    private fun getProblem(studentGrade: Int, problemGrade: Int) {

        RetrofitClient.getApiService().problem(studentGrade, problemGrade)
            .enqueue(object: Callback<String> {
                override fun onResponse(call: Call<String>, response: Response<String>) {
                    if (response.isSuccessful.not()) {
                        return
                    }

                    response.body()?.let {
                        Log.d("dong_request", it)

                        correctText = it

                        binding.btnPicture.setOnClickListener {
                            capture()
//                            captureMock()
                        }

                        ttsBtn.setOnClickListener {
                            tts.speak(correctText, TextToSpeech.QUEUE_FLUSH, null, null)
                        }
                    }
                }

                override fun onFailure(call: Call<String>, t: Throwable) {
                    Log.d("dong_request", t.toString())
                }
            })

    }

    private fun captureMock() {
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

            intent.putExtra("correctText", correctText)
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

            intent.putExtra("correctText", correctText)
            intent.putExtra("imagePath", currentPhotoPath)

            startActivity(intent)
        }
    }
}