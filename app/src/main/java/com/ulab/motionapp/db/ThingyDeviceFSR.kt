package com.ulab.motionapp.db

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.ForeignKey
import android.arch.persistence.room.ForeignKey.CASCADE
import android.arch.persistence.room.PrimaryKey


/**
 * Created by R.S. on 11/08/18
 */
@Entity(tableName = "FSR", foreignKeys = [
    (ForeignKey(entity = Session::class,
            parentColumns = ["id"],
            childColumns = ["session_id"],
            onDelete = CASCADE))])
class ThingyDeviceFSR(@ColumnInfo(name = "DeviceAddress") var deviceAddress: String = "",
                      @ColumnInfo(name = "DeviceName") var deviceName: String = "",
                      @ColumnInfo(name = "ValueFSR1_voltage") var ValueFSR1_voltage: Short = 0,
                      @ColumnInfo(name = "ValueFSR2_voltage") var ValueFSR2_voltage: Short = 0,
                      @ColumnInfo(name = "ValueFSR3_voltage") var ValueFSR3_voltage: Short = 0,
                      @ColumnInfo(name = "ValueFSR4_voltage") var ValueFSR4_voltage: Short = 0,
                      @ColumnInfo(name = "timestemp") var timestamp: String = "",
                      @ColumnInfo(name = "timestemp_milles") var timestampinMillies: String = "",
                      @ColumnInfo(name = "session_id") var session_id: Long=0) {
    @ColumnInfo(name = "id")
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
}