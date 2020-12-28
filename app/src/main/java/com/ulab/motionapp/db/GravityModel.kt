package com.ulab.motionapp.db

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.ForeignKey
import android.arch.persistence.room.ForeignKey.CASCADE
import android.arch.persistence.room.PrimaryKey


/**
 * Created by R.S. on 11/08/18
 */
@Entity(tableName = "Gravity", foreignKeys = [
    (ForeignKey(entity = Session::class,
            parentColumns = ["id"],
            childColumns = ["session_id"],
            onDelete = CASCADE))])
class GravityModel(@ColumnInfo(name = "DeviceAddress") var deviceAddress: String = "",
                   @ColumnInfo(name = "DeviceName") var deviceName: String = "",
                   @ColumnInfo(name = "ValueX") var valueX: Float = 0f,
                   @ColumnInfo(name = "ValueY") var valueY: Float = 0f,
                   @ColumnInfo(name = "ValueZ") var valueZ: Float = 0f,
                   @ColumnInfo(name = "timestemp") var timestamp: String = "",
                   @ColumnInfo(name = "timestemp_milles") var timestampinMillies: String = "",
                   @ColumnInfo(name = "session_id") var session_id: Long=0) {
    @ColumnInfo(name = "id")
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
}