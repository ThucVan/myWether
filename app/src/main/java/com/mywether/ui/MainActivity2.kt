package com.mywether.ui

import android.net.Uri
import android.view.MotionEvent
import android.view.View
import com.huawei.hms.panorama.Panorama
import com.huawei.hms.panorama.PanoramaInterface
import com.huawei.hms.panorama.ResultCode
import com.mywether.R
import com.mywether.databinding.ActivityMain2Binding
import com.mywether.ui.base.BaseActivity


class MainActivity2 : BaseActivity<ActivityMain2Binding>(), View.OnTouchListener {
    private var mLocalInstance: PanoramaInterface.PanoramaLocalInterface? = null
    private var mPanoramaView: View? = null

    override fun getLayoutActivity() = R.layout.activity_main2

    override fun initViews() {
        super.initViews()


        mLocalInstance = Panorama.getInstance().getLocalInstance(this)
        if (mLocalInstance == null) {
            return
        }

        doDisplaySpherical()
    }

    private fun addViewToLayout() {
        mPanoramaView = mLocalInstance!!.view
        if (mPanoramaView == null) {
            return
        }
        mPanoramaView!!.setOnTouchListener(this)
        mBinding.mLayout.addView(mPanoramaView)
    }

    private fun doDisplaySpherical() {
        val uri = Uri.parse("android.resource://" + packageName + "/" + R.raw.test)
        val ret = mLocalInstance!!.setImage(uri, PanoramaInterface.IMAGE_TYPE_SPHERICAL)
        if (ret != ResultCode.SUCCEED) {
            return
        }
        addViewToLayout()
    }

    override fun onDestroy() {
        super.onDestroy()

        if (mLocalInstance != null) {
            mLocalInstance!!.deInit()
        }
    }

    override fun onTouch(view: View?, motionEvent: MotionEvent?): Boolean {
        if (mPanoramaView != null && mPanoramaView as View == view) {
            if (mLocalInstance != null) {
                mLocalInstance!!.updateTouchEvent(motionEvent)
                return true
            }
        }
        return false
    }
}
