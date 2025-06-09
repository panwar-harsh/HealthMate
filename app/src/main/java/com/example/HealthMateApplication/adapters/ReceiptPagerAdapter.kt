package com.example.HealthMateApplication.adapters

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.HealthMateApplication.ReceiptHandlingActivity
import com.example.HealthMateApplication.fragments.OwnReceiptFragment
import com.example.HealthMateApplication.fragments.ParentalReceiptFragment

class ReceiptPagerAdapter(activity: ReceiptHandlingActivity) : FragmentStateAdapter(activity) {
    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> OwnReceiptFragment()
            1 -> ParentalReceiptFragment()
            else -> OwnReceiptFragment()
        }
    }
}
