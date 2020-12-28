package com.ulab.motionapp.fragment

import android.os.SystemClock
import android.view.View
import com.ulab.motionapp.R
import com.ulab.motionapp.common.Utils
import kotlinx.android.synthetic.main.fragment_dashboard.*
import kotlinx.android.synthetic.main.header_home.*



/**
 * Created by R.S. on 09/10/18
 */
class DashboardFragment : BaseFragment() {
    private var lastClickedTime: Long = 0

    override fun onClick(view: View) {
        super.onClick(view)
        /*
         * Logic to Prevent the Launch of the Fragment Twice if User makes
         * the Tap(Click) very Fast.
         */
        if (SystemClock.elapsedRealtime() - lastClickedTime < Utils.MAX_CLICK_INTERVAL) {
            return
        }
        lastClickedTime = SystemClock.elapsedRealtime()

        when (view) {
            fragment_dashboard_llQuaterninos -> {
                addFragmentWithSharedElement(R.id.activity_home_llContainer, this, QuaternionsFragment(), false, false, header_iv)
            }
            fragment_dashboard_llEuler -> addFragmentWithSharedElement(R.id.activity_home_llContainer, this, EulerFragment(), false, false, header_iv)
            fragment_dashboard_llGravity -> addFragmentWithSharedElement(R.id.activity_home_llContainer, this, GravityFragment(), false, false, header_iv)
            fragment_dashboard_llGeneralSettings -> addFragment(R.id.activity_home_llContainer, this, GeneralSettingFragment(), false, false)
        }
    }

    override fun initializeComponent(view: View) {
        fragment_dashboard_llQuaterninos.setOnClickListener(this)
        fragment_dashboard_llEuler.setOnClickListener(this)
        fragment_dashboard_llGravity.setOnClickListener(this)
        fragment_dashboard_llGeneralSettings.setOnClickListener(this)
    }

    override fun defineLayoutResource(): Int {
        return R.layout.fragment_dashboard
    }
}