package com.ulab.motionapp.activity

import android.arch.persistence.db.SimpleSQLiteQuery
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.ulab.motionapp.MotionApp
import com.ulab.motionapp.R
import com.ulab.motionapp.ThingyService
import no.nordicsemi.android.thingylib.ThingySdkManager

class SplashActivity : AppCompatActivity(), ThingySdkManager.ServiceConnectionListener {
    override fun onServiceConnected() {
        setHandler()
    }

    private val TIME_INTERVAL = 2000
    private var handler: Handler? = null
    private var mThingySdkManager: ThingySdkManager? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
//       val sessionList= MotionApp.instance!!.getThingyDeviceDB()!!.thingyDeviceDao().getAllSession()

//        clearAllTables()

        mThingySdkManager = ThingySdkManager.getInstance()
    }

    private fun clearAllTables() {
//        MotionApp.instance!!.getThingyDeviceDB()!!.devicesDao().deleteTable(SimpleSQLiteQuery("ALTER TABLE Session DROP id;"))
        MotionApp.instance!!.getThingyDeviceDB()!!.clearAllTables()
//        MotionApp.instance!!.getThingyDeviceDB()!!.devicesDao().deleteAllDataFromSession()
//        MotionApp.instance!!.getThingyDeviceDB()!!.devicesDao().deleteTable(SimpleSQLiteQuery("VACUUM"))
//        MotionApp.instance!!.getThingyDeviceDB()!!.devicesDao().deleteAllDataFromSessionVACUUM()
//        MotionApp.instance!!.getThingyDeviceDB()!!.devicesDao().deleteAllData()
//        MotionApp.instance!!.getThingyDeviceDB()!!.eulerAngleDao().deleteAllData()
//        MotionApp.instance!!.getThingyDeviceDB()!!.eulerConfigDao().deleteAllData()
//        MotionApp.instance!!.getThingyDeviceDB()!!.eulerDao().deleteAllData()
//        MotionApp.instance!!.getThingyDeviceDB()!!.gravityConfigDao().deleteAllData()
//        MotionApp.instance!!.getThingyDeviceDB()!!.gravityDao().deleteAllData()
//        MotionApp.instance!!.getThingyDeviceDB()!!.quaternionsConfigDao().deleteAllData()
//        MotionApp.instance!!.getThingyDeviceDB()!!.thingyDeviceDao().deleteAllData()

//        Log.e("TAG", "calling clearAllTables")
//
//        // reset all auto-incrementalValues
//        val query = SimpleSQLiteQuery("UPDATE sqlite_sequence SET seq = 0")
//        val query1 = SimpleSQLiteQuery("DELETE FROM sqlite_sequence")
//
//        MotionApp.instance!!.getThingyDeviceDB()!!.beginTransaction()
//        return try {
//            MotionApp.instance!!.getThingyDeviceDB()!!.clearAllTables()
////            MotionApp.instance!!.getThingyDeviceDB()!!.devicesDao().deleteTable(query)
////            MotionApp.instance!!.getThingyDeviceDB()!!.devicesDao().deleteTable(query1)
//            MotionApp.instance!!.getThingyDeviceDB()!!.openHelper.writableDatabase.query(query)
//            MotionApp.instance!!.getThingyDeviceDB()!!.openHelper.writableDatabase.query(query1)
//            MotionApp.instance!!.getThingyDeviceDB()!!.setTransactionSuccessful()
//        } catch (e: Exception) {
//        } finally {
//            Log.e("TAG", "calling clearAllTables done")
//            MotionApp.instance!!.getThingyDeviceDB()!!.endTransaction()
//        }
    }

    override fun onStart() {
        super.onStart()
        mThingySdkManager!!.bindService(this, ThingyService::class.java)
    }

    override fun onStop() {
        super.onStop()
        mThingySdkManager!!.unbindService(this)
    }

    private fun setHandler() {
        handler = Handler()
        handler!!.postDelayed(runnable, TIME_INTERVAL.toLong())
    }

    private val runnable: Runnable = Runnable {

        clearAllTables()

        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (handler != null) {
            handler!!.removeCallbacks(runnable)
        }
    }
}
