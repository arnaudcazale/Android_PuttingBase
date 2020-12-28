package com.ulab.motionapp

import android.app.Application
import android.bluetooth.BluetoothGatt
import com.crashlytics.android.Crashlytics
import com.facebook.stetho.Stetho
import com.ulab.motionapp.db.ThingyDeviceDB
import io.fabric.sdk.android.Fabric
import no.nordicsemi.android.thingylib.ThingySdkManager
import java.util.*

/**
 * Created by R.S. on 10/08/18
 */
class MotionApp : Application() {
    companion object {
        // guillaume : should not be a static pointer
        var instance: MotionApp? = null
    }

    private var thingyDeviceDB: ThingyDeviceDB? = null

    private val connectedDevicesHashMap = HashMap<String, BluetoothGatt>()

    private var mThingySdkManager: ThingySdkManager? = null

    fun getThingySdkManager(): ThingySdkManager? {
        return mThingySdkManager
    }

    fun setThingySdkManager(thingySdkManager: ThingySdkManager) {
        mThingySdkManager = thingySdkManager
    }

    fun getThingyDeviceDB(): ThingyDeviceDB? {
        return thingyDeviceDB
    }

    fun getConnectedDevicesHashMap(): HashMap<String, BluetoothGatt> {
        return connectedDevicesHashMap
    }

    fun addDevice(address: String, gatt: BluetoothGatt) {
        if (!connectedDevicesHashMap.containsKey(address)) {
            connectedDevicesHashMap[address] = gatt
        }
    }

    fun removeDevice(address: String, gatt: BluetoothGatt) {
        connectedDevicesHashMap.remove(address)
    }

    override fun onCreate() {
        super.onCreate()

        Fabric.with(this, Crashlytics())

        instance = this

        deleteDatabase(getString(R.string.databasename))
        thingyDeviceDB = ThingyDeviceDB.getDatabase(applicationContext)

        Stetho.initializeWithDefaults(this)
    }

}