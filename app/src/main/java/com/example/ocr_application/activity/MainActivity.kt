package com.example.ocr_application.activity

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.ocr_application.databinding.ActivityMainBinding
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.normal.TedPermission


class MainActivity : AppCompatActivity() {

    lateinit var btnFreeMode: Button
    lateinit var btnGradeMode: Button
    private lateinit var binding: ActivityMainBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //권한 체크
        TedPermission.create()
            .setPermissionListener(object: PermissionListener {
                override fun onPermissionGranted() {
                    btnFreeMode = binding.btnFreeMode.also {
                        it.setOnClickListener {
                            val intent = Intent(this@MainActivity, FreeModeActivity::class.java)
                            startActivity(intent)
                        }
                    }

                    btnGradeMode = binding.btnGradeMode.also {
                        it.setOnClickListener {
                            val intent = Intent(this@MainActivity, FreeModeActivity::class.java)
                            startActivity(intent)
                        }
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


    companion object {
        init {
            System.loadLibrary("ocr_application")
            System.loadLibrary("opencv_java4")
        }
    }
}