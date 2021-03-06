package com.ulab.motionapp.db

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey


/**
 * Created by R.S. on 11/08/18
 */
@Entity(tableName = "EulerConfig")
class EulerConfigModel(@ColumnInfo(name = "DeviceAddress") var deviceAddress: String = "",
                       @ColumnInfo(name = "DeviceName") var deviceName: String = "",
                       @ColumnInfo(name = "ValueR") var valueR: Float = 0f,
                       @ColumnInfo(name = "ValueP") var valueP: Float = 0f,
                       @ColumnInfo(name = "ValueY") var valueY: Float = 0f,
                       @ColumnInfo(name = "timestemp") var timestamp: String = "",
                       @ColumnInfo(name = "timestemp_milles") var timestampinMillies: String = "",
                       @ColumnInfo(name = "session_id") var session_id: Long = 0) {
    @ColumnInfo(name = "id")
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
}