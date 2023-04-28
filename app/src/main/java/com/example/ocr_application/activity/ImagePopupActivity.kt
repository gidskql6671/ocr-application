package com.example.ocr_application.activity

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.ImageView
import com.example.ocr_application.databinding.ActivityImagePopupBinding
import java.io.File
import java.io.FileInputStream

class ImagePopupActivity : Activity() {
    lateinit var imageView: ImageView

    private lateinit var binding: ActivityImagePopupBinding
    private lateinit var imagePath: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE)

        binding = ActivityImagePopupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        imageView = binding.imageView

        val intent: Intent = intent
        imagePath = intent.extras!!.getString("imagePath")!!

        val file = File(imagePath)
        val options = BitmapFactory.Options()
        options.inPreferredConfig = Bitmap.Config.ARGB_8888
        val imageBitmap = BitmapFactory.decodeStream(FileInputStream(file), null, options)!!

        imageView.setImageBitmap(getRotatedBitmap(imageBitmap, 90))
    }

    fun mOnClose(v: View?) {
        finish()
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

}