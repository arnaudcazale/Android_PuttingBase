package com.ulab.motionapp.db

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey


/**
 * Created by R.S. on 11/08/18
 */
@Entity(tableName = "Devices")
class DevicesModel(@ColumnInfo(name = "DeviceAddress") @PrimaryKey var deviceAddress: String = "",
                   @ColumnInfo(name = "DeviceName") var deviceName: String = "",
                   @ColumnInfo(name = "GravityMinX") var valueGravityMinX: Float = 0f,
                   @ColumnInfo(name = "GravityMinY") var valueGravityMinY: Float = 0f,
                   @ColumnInfo(name = "GravityMinZ") var valueGravityMinZ: Float = 0f,
                   @ColumnInfo(name = "GravityMaxX") var valueGravityMaxX: Float = 5f,
                   @ColumnInfo(name = "GravityMaxY") var valueGravityMaxY: Float = 5f,
                   @ColumnInfo(name = "GravityMaxZ") var valueGravityMaxZ: Float = 5f,
                   @ColumnInfo(name = "GravityWarningMin") var warGravityMin: Boolean = false,
                   @ColumnInfo(name = "GravityWarningMax") var warGravityMax: Boolean = false,
                   @ColumnInfo(name = "EulerMinR") var valueEulerMinR: Float = 0f,
                   @ColumnInfo(name = "EulerMinP") var valueEulerMinP: Float = 0f,
                   @ColumnInfo(name = "EulerMinY") var valueEulerMinY: Float = 0f,
                   @ColumnInfo(name = "EulerMaxR") var valueEulerMaxR: Float = 5f,
                   @ColumnInfo(name = "EulerMaxP") var valueEulerMaxP: Float = 5f,
                   @ColumnInfo(name = "EulerMaxY") var valueEulerMaxY: Float = 5f,
                   @ColumnInfo(name = "EulerWarningMin") var warEulerMin: Boolean = false,
                   @ColumnInfo(name = "EulerWarningMax") var warEulerMax: Boolean = false
//                   ,
//                   @ColumnInfo(name = "timestemp") var timestamp: String = "",
//                   @ColumnInfo(name = "timestemp_milles") var timestampinMillies: String = "",
//                   @ColumnInfo(name = "session_id") var session_id: Long = 0
)
