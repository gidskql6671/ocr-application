package com.example.ocr_application

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.ocr_application.databinding.ActivityResultBinding

class ResultActivity : AppCompatActivity() {

    lateinit var textView: TextView

    private lateinit var binding: ActivityResultBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        textView = binding.textView

        textView.text = "test"

    }

}