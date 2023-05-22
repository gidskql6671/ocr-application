package com.example.ocr_application.activity

import android.app.ActionBar.LayoutParams
import android.content.Intent
import android.content.res.Resources
import android.graphics.Typeface
import android.os.Bundle
import android.util.TypedValue
import android.view.ContextThemeWrapper
import android.widget.Button
import android.widget.GridLayout
import android.widget.Space
import androidx.appcompat.app.AppCompatActivity
import com.example.ocr_application.R
import com.example.ocr_application.databinding.ActivityGradeMode2Binding


class GradeMode2Activity : AppCompatActivity() {

    lateinit var btnGrid: GridLayout
    lateinit var button: Button

    private lateinit var binding: ActivityGradeMode2Binding

    inline val Int.dp: Int
        get() = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, this.toFloat(), Resources.getSystem().displayMetrics
        ).toInt()

    inline val Float.dp: Float
        get() = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, this, Resources.getSystem().displayMetrics
        )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityGradeMode2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        val studentGrade = intent.extras!!.getInt("grade")

        btnGrid = binding.btnGrid
        for (i: Int in 1..10 step(2)) {
            Button(ContextThemeWrapper(this, R.style.Button_Default)).let {
                it.text = "${i}급수"
                it.typeface = Typeface.DEFAULT_BOLD

                it.setPadding(30.dp, 20.dp, 30.dp, 20.dp)

                it.layoutParams = GridLayout.LayoutParams()
                it.layoutParams.width = 120.dp
                it.layoutParams.height = LayoutParams.WRAP_CONTENT

                it.setOnClickListener {
                    val intent = Intent(this, GradeMode3Activity::class.java)

                    intent.putExtra("studentGrade", studentGrade)
                    intent.putExtra("problemGrade", i)

                    startActivity(intent)
                }

                btnGrid.addView(it)
            }

            Space(this).let {
                it.layoutParams = GridLayout.LayoutParams(
                    GridLayout.spec(GridLayout.UNDEFINED, GridLayout.FILL, 1f),
                    GridLayout.spec(GridLayout.UNDEFINED,GridLayout.FILL,1f)
                )

                btnGrid.addView(it)
            }

            Button(ContextThemeWrapper(this, R.style.Button_Default)).let {
                it.text = "${i + 1}급수"
                it.typeface = Typeface.DEFAULT_BOLD

                it.setPadding(30.dp, 20.dp, 30.dp, 20.dp)

                it.layoutParams = GridLayout.LayoutParams()
                it.layoutParams.width = 120.dp
                it.layoutParams.height = LayoutParams.WRAP_CONTENT

                it.setOnClickListener {
                    val intent = Intent(this, GradeMode3Activity::class.java)

                    intent.putExtra("studentGrade", studentGrade)
                    intent.putExtra("problemGrade", i + 1)

                    startActivity(intent)
                }

                btnGrid.addView(it)
            }
        }
    }
}