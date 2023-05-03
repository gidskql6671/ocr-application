package com.example.ocr_application.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.ocr_application.R


class ResultPageFragment(private val content: String, private val isLoding: Boolean) : Fragment() {
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {

        val view: View = inflater.inflate(R.layout.fragment_result_slide_page, container, false)

        view.findViewById<TextView>(R.id.content).text = content

        if (!isLoding) {
            val progressBar = view.findViewById(R.id.spin_kit) as ProgressBar
            progressBar.visibility = View.GONE
        }

        return view
    }

}