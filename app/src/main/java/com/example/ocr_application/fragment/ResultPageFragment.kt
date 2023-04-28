package com.example.ocr_application.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.ocr_application.R

class ResultPageFragment(
    private val title: String,
    private val content: String
) : Fragment() {
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val view: View = inflater.inflate(R.layout.fragment_result_slide_page, container, false)

        view.findViewById<TextView>(R.id.titleTextView).text = title
        view.findViewById<TextView>(R.id.content).text = content

        return view
    }

}