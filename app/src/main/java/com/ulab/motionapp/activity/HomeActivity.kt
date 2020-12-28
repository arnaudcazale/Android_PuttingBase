package com.ulab.motionapp.activity

import android.app.NotificationManager
import android.arch.persistence.room.RoomDatabase
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.view.ViewCompat
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import com.ulab.motionapp.R
import com.ulab.motionapp.ThingyService
import com.ulab.motionapp.common.FileUtils
import com.ulab.motionapp.common.Utils
import com.ulab.motionapp.fragment.DashboardFragment
import no.nordicsemi.android.thingylib.ThingySdkManager
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream



/**
 * Created by R.S. on 10/08/18
 */

// guillaume : in kotlin the symbol "!!" should be avoided. It's everywhere in the code.

class HomeActivity : AppCompatActivity(), ThingySdkManager.ServiceConnectionListener, View.OnClickListener {
    // guillaume : unused
    override fun onClick(v: View?) {

//        when (v) {
//            tvQuaternions -> replaceFragment(R.id.activity_home_llContainer, QuaternionsFragment(), false, false)
//            tvEuler -> replaceFragment(R.id.activity_home_llContainer, EulerFragment(), false, false)
//            tvGravity -> replaceFragment(R.id.activity_home_llContainer, GravityFragment(), false, false)
//        }
    }

    lateinit var mBinder: ThingyService.ThingyBinder

    fun getmBinder() :ThingyService.ThingyBinder {
        return mBinder
    }

    /**
     * Called when all the services of BLE devices are discovered
     */
    override fun onServiceConnected() {
        //Use this binder to access you own methods declared in the ThingyService
        mBinder = mThingySdkManager!!.thingyBinder as ThingyService.ThingyBinder
    }

    private var mThingySdkManager: ThingySdkManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, getString(R.string.alert_bluetooth_is_not_supported), Toast.LENGTH_SHORT).show()
            finish()
        }

        mThingySdkManager = ThingySdkManager.getInstance()
        mThingySdkManager!!.bindService(this, ThingyService::class.java)

        replaceFragment(R.id.activity_home_llContainer, DashboardFragment(), false, false)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val fragment:Fragment = supportFragmentManager.findFragmentById(R.id.activity_home_llContainer)!!
        fragment.onActivityResult(requestCode, resultCode, data)
    }

    override fun onDestroy() {
        super.onDestroy()

        mThingySdkManager!!.unbindService(this)

        if (isFinishing) {
            ThingySdkManager.clearInstance()
        }
        exportDatabase()

        try {
            val notificationManager = applicationContext.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancelAll()
        } catch (e: Exception) {
        }

        System.exit(0);
    }

    private fun exportDatabase() {
        try {
            val dbFile = getDatabasePath(getString(R.string.databasename))
            val newDBFilePath = File("" + getExternalFilesDir(null) + "/" + getString(R.string.app_name) + ".db")
            FileUtils.copyFile(FileInputStream(dbFile.absoluteFile), FileOutputStream(newDBFilePath))
        } catch (e: Exception) {
            e.stackTrace
        }
    }

    /**
     * Adds the Fragment into layout container
     *
     * @param fragmentContainerResourceId
     * Resource id of the layout in which Fragment will be added
     * @param currentFragment
     * Current loaded Fragment to be hide
     * @param nextFragment
     * New Fragment to be loaded into fragmentContainerResourceId
     * @param requiredAnimation
     * true if screen transition animation is required
     * @param commitAllowingStateLoss
     * true if commitAllowingStateLoss is needed
     *
     * @return true if new Fragment added successfully into container, false otherwise
     * @throws IllegalStateException
     * Exception if Fragment transaction is invalid
     */
    @Throws(IllegalStateException::class)
    fun addFragment(fragmentContainerResourceId: Int, currentFragment: Fragment?, nextFragment: Fragment?, requiredAnimation: Boolean, commitAllowingStateLoss: Boolean): Boolean {

        try {
            Utils.hideSoftKeyBoard(this, currentFocus)
        } catch (ignored: Exception) {
        }

        if (currentFragment == null || nextFragment == null) {
            return false
        }
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()

        if (requiredAnimation) {
//            FragmentAnimation.setDefaultFragmentAnimation(fragmentTransaction)
        }

        fragmentTransaction.add(fragmentContainerResourceId, nextFragment, nextFragment.javaClass.simpleName)
        fragmentTransaction.addToBackStack(nextFragment.javaClass.simpleName)

        val parentFragment = currentFragment.parentFragment
        fragmentTransaction.hide(parentFragment ?: currentFragment)

        if (!commitAllowingStateLoss) {
            fragmentTransaction.commit()
        } else {
            fragmentTransaction.commitAllowingStateLoss()
        }

        return true
    }

    /**
     * Adds the Fragment into layout container
     *
     * @param fragmentContainerResourceId
     * Resource id of the layout in which Fragment will be added
     * @param currentFragment
     * Current loaded Fragment to be hide
     * @param nextFragment
     * New Fragment to be loaded into fragmentContainerResourceId
     * @param requiredAnimation
     * true if screen transition animation is required
     * @param commitAllowingStateLoss
     * true if commitAllowingStateLoss is needed
     *
     * @return true if new Fragment added successfully into container, false otherwise
     * @throws IllegalStateException
     * Exception if Fragment transaction is invalid
     */
    @Throws(IllegalStateException::class)
    fun addFragmentWithSharedElement(fragmentContainerResourceId: Int, currentFragment: Fragment?, nextFragment: Fragment?, requiredAnimation: Boolean, commitAllowingStateLoss: Boolean, iv: ImageView): Boolean {

        try {
            Utils.hideSoftKeyBoard(this, currentFocus)
        } catch (ignored: Exception) {
        }

        if (currentFragment == null || nextFragment == null) {
            return false
        }
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()

        if (requiredAnimation) {
//            FragmentAnimation.setDefaultFragmentAnimation(fragmentTransaction)
        }

        fragmentTransaction.addSharedElement(iv, ViewCompat.getTransitionName(iv)!!)

        fragmentTransaction.replace(fragmentContainerResourceId, nextFragment, nextFragment.javaClass.simpleName)
        fragmentTransaction.addToBackStack(nextFragment.javaClass.simpleName)

//        val parentFragment = currentFragment.parentFragment
//        fragmentTransaction.hide(parentFragment ?: currentFragment)

        if (!commitAllowingStateLoss) {
            fragmentTransaction.commit()
        } else {
            fragmentTransaction.commitAllowingStateLoss()
        }

        return true
    }

    /**
     * Replaces the Fragment into layout container
     *
     * @param fragmentContainerResourceId
     * Resource id of the layout in which Fragment will be added
     * @param nextFragment
     * New Fragment to be loaded into fragmentContainerResourceId
     * @param requiredAnimation
     * true if screen transition animation is required
     * @param commitAllowingStateLoss
     * true if commitAllowingStateLoss is needed
     *
     * @return true if new Fragment added successfully into container, false otherwise
     * @throws IllegalStateException
     * Exception if Fragment transaction is invalid
     */
    @Throws(IllegalStateException::class)
    fun replaceFragment(fragmentContainerResourceId: Int, nextFragment: Fragment?, requiredAnimation: Boolean, commitAllowingStateLoss: Boolean): Boolean {
        try {
            Utils.hideSoftKeyBoard(this, currentFocus)
        } catch (ignored: Exception) {
        }

        val fragmentManager = supportFragmentManager
        if (nextFragment == null || fragmentManager == null) {
            return false
        }
        val fragmentTransaction = fragmentManager.beginTransaction()
        if (requiredAnimation) {
//            FragmentAnimation.setDefaultFragmentAnimation(fragmentTransaction)
        }
        fragmentTransaction.replace(fragmentContainerResourceId, nextFragment, nextFragment.javaClass.simpleName)

        if (!commitAllowingStateLoss) {
            fragmentTransaction.commit()
        } else {
            fragmentTransaction.commitAllowingStateLoss()
        }

        return true
    }

    /**
     * Replaces the Fragment into layout container
     *
     * @param fragmentContainerResourceId
     * Resource id of the layout in which Fragment will be added
     * @param nextFragment
     * New Fragment to be loaded into fragmentContainerResourceId
     * @param requiredAnimation
     * true if screen transition animation is required
     * @param commitAllowingStateLoss
     * true if commitAllowingStateLoss is needed
     *
     * @return true if new Fragment added successfully into container, false otherwise
     * @throws IllegalStateException
     * Exception if Fragment transaction is invalid
     */
    @Throws(IllegalStateException::class)
    fun replaceChildFragment(fragmentContainerResourceId: Int, fragmentManager: FragmentManager?, nextFragment: Fragment?, requiredAnimation: Boolean, commitAllowingStateLoss: Boolean): Boolean {
        try {
            Utils.hideSoftKeyBoard(this, currentFocus)
        } catch (ignored: Exception) {
        }

        if (nextFragment == null || fragmentManager == null) {
            return false
        }
        val fragmentTransaction = fragmentManager.beginTransaction()
        if (requiredAnimation) {
//            FragmentAnimation.setDefaultFragmentAnimation(fragmentTransaction)
        }
        fragmentTransaction.replace(fragmentContainerResourceId, nextFragment, nextFragment.javaClass.simpleName)

        if (!commitAllowingStateLoss) {
            fragmentTransaction.commit()
        } else {
            fragmentTransaction.commitAllowingStateLoss()
        }
        return true
    }
}
