package com.example.ocr_application

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.ocr_application.fragment.ResultPageFragment

class ResultPageFragmentAdapter(
    fa: FragmentActivity,
    private val originText: String,
    private val correctText: String,
    private val isLoding: Boolean = true
    ): FragmentStateAdapter(fa) {
    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment =
        if (position == 0) {
            ResultPageFragment(originText, isLoding)
        } else {
            ResultPageFragment(correctText, isLoding)
        }

}