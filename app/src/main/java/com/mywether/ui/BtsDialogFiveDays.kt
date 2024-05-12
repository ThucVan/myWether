package com.mywether.ui

import android.app.Activity
import com.mywether.R
import com.mywether.databinding.BottomSheetFiveDaysBinding
import com.mywether.ui.base.BaseBottomSheetDialog

class BtsDialogFiveDays(activity: Activity, val title: String, val seeFiveDayAdapter : SeeFiveDayAdapter) :
    BaseBottomSheetDialog<BottomSheetFiveDaysBinding>(activity) {
    override fun getLayoutDialog() = R.layout.bottom_sheet_five_days

    override fun initViews() {
        super.initViews()

        mBinding.tvFiveDays.text = context.getString(R.string.txt_five_days, title)
        mBinding.rcvFiveDays.adapter = seeFiveDayAdapter
    }
}