package com.example.ocr_application

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.ocr_application.databinding.ActivityResultBinding
import com.example.ocr_application.dto.OcrResponse
import com.example.ocr_application.retrofit.RetrofitClient
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileInputStream

class ResultActivity : AppCompatActivity() {

    lateinit var correctTextView: TextView
    lateinit var originTextView: TextView
    lateinit var correctPercentTextView: TextView
    lateinit var imageView: ImageView

    private lateinit var binding: ActivityResultBinding
    private lateinit var imagePath: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        originTextView = binding.originTextView
        correctTextView = binding.correctTextView
        correctPercentTextView = binding.correctPercentTextView
        imageView = binding.imageView

        imagePath = intent.extras!!.getString("imagePath")!!

        val file = File(imagePath)
        val options = BitmapFactory.Options()
        options.inPreferredConfig = Bitmap.Config.ARGB_8888
        val imageBitmap = BitmapFactory.decodeStream(FileInputStream(file), null, options)!!

        imageView.setImageBitmap(getRotatedBitmap(imageBitmap, 90))

//        callOcr(file)
        callOcrMock()
    }

    private fun callOcrMock() {
        correctTextView.text = "temp"
        originTextView.text = "temp"

        val correctPercent = 0.5 * 100

        correctPercentTextView.text = String.format("맞춤법 정답률은 %.2f%%입니다.", correctPercent)
    }

    private fun callOcr(file: File) {
        RetrofitClient.getApiService().ocr(getMultipartData(file))
            .enqueue(object: Callback<OcrResponse> {
                override fun onResponse(call: Call<OcrResponse>, response: Response<OcrResponse>) {
                    if (response.isSuccessful.not()) {
                        return
                    }

                    response.body()?.let {
                        Log.d("dong_request", it.toString())

                        correctTextView.text = it.correctString
                        originTextView.text = it.originString

                        val correctPercent = it.answerPercent * 100

                        correctPercentTextView.text = String.format("맞춤법 정답률은 %.2f%%입니다.", correctPercent)
                    }
                }

                override fun onFailure(call: Call<OcrResponse>, t: Throwable) {
                    Log.d("dong_request", t.toString())
                }
            })

    }

    @Synchronized
    private fun getRotatedBitmap(bitmap: Bitmap, degrees: Int): Bitmap {
        var bitmap = bitmap
        if (degrees != 0 && bitmap != null) {
            val m = Matrix()
            m.setRotate(degrees.toFloat(), bitmap.width.toFloat() / 2, bitmap.height.toFloat() / 2)
            try {
                val b2 = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, m, true)
                if (bitmap != b2) {
                    bitmap = b2
                }
            } catch (ex: OutOfMemoryError) {
                ex.printStackTrace()
            }
        }
        return bitmap
    }

    private fun getMultipartData(image: File): MultipartBody.Part {
        val requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), image)

        return MultipartBody.Part.createFormData("image", image.name, requestFile)
    }
}