package com.ulab.motionapp.db

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.content.Context
import com.ulab.motionapp.R


/**
 * Created by R.S. on 11/08/18
 */
@Database(entities = [ThingyDeviceFSR::class, ThingyDevice::class, Session::class, EulerModel::class, GravityModel::class, DevicesModel::class, GravityConfigModel::class, EulerConfigModel::class,
    QuaternionsConfigModel::class, EulerAngleModel::class], version = 1, exportSchema = false)
abstract class ThingyDeviceDB : RoomDatabase() {

    abstract fun thingyDeviceDao(): ThingyDeviceDao
    abstract fun gravityDao(): GravityDao
    abstract fun eulerDao(): EulerDao

    abstract fun devicesDao(): DevicesModelDao

    abstract fun gravityConfigDao(): GravityConfigDao
    abstract fun eulerConfigDao(): EulerConfigDao
    abstract fun quaternionsConfigDao(): QuaternionsConfigDao

    abstract fun eulerAngleDao(): EulerAngleDao

    companion object {

        private var INSTANCE: ThingyDeviceDB? = null

        internal fun getDatabase(context: Context): ThingyDeviceDB? {
            if (INSTANCE == null) {
                synchronized(ThingyDeviceDB::class.java) {
                    if (INSTANCE == null) {
                        INSTANCE = Room.databaseBuilder(context.applicationContext,
                                ThingyDeviceDB::class.java, context.getString(R.string.databasename))
                                .allowMainThreadQueries()
                                .build()

                    }
                }
            }
            return INSTANCE
        }
    }

}