package com.example.ocr_application.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.ocr_application.databinding.ActivityGradeModeBinding


class GradeModeActivity : AppCompatActivity() {

    lateinit var button: Button

    private lateinit var binding: ActivityGradeModeBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityGradeModeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }

        binding.btn1.setOnClickListener {
            val intent = Intent(this, GradeMode2Activity::class.java)
            intent.putExtra("grade", 1)
            startActivity(intent)
        }
        binding.btn2.setOnClickListener {
            val intent = Intent(this, GradeMode2Activity::class.java)
            intent.putExtra("grade", 2)
            startActivity(intent)
        }

    }

}