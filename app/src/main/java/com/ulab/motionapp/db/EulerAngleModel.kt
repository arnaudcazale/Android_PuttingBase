package com.ulab.motionapp.db

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.ForeignKey
import android.arch.persistence.room.ForeignKey.CASCADE
import android.arch.persistence.room.PrimaryKey


/**
 * Created by R.S. on 11/08/18
 */
@Entity(tableName = "EulerAngle", foreignKeys = [
    (ForeignKey(entity = Session::class,
            parentColumns = ["id"],
            childColumns = ["session_id"],
            onDelete = CASCADE))])
class EulerAngleModel(@ColumnInfo(name = "DeviceAddress") var deviceAddress: String = "",
                      @ColumnInfo(name = "DeviceName") var deviceName: String = "",
                      @ColumnInfo(name = "ValueX") var valueX: Float = 0f,
                      @ColumnInfo(name = "ValueY") var valueY: Float = 0f,
                      @ColumnInfo(name = "ValueZ") var valueZ: Float = 0f,
                      @ColumnInfo(name = "CX1") var cx1: Float = 0f,
                      @ColumnInfo(name = "CY1") var cy1: Float = 0f,
                      @ColumnInfo(name = "CZ1") var cz1: Float = 0f,
                      @ColumnInfo(name = "CX2") var cx2: Float = 0f,
                      @ColumnInfo(name = "CY2") var cy2: Float = 0f,
                      @ColumnInfo(name = "CZ2") var cz2: Float = 0f,
                      @ColumnInfo(name = "CX3") var cx3: Float = 0f,
                      @ColumnInfo(name = "CY3") var cy3: Float = 0f,
                      @ColumnInfo(name = "CZ3") var cz3: Float = 0f,
                      @ColumnInfo(name = "timestemp") var timestamp: String = "",
                      @ColumnInfo(name = "timestemp_milles") var timestampinMillies: String = "",
                      @ColumnInfo(name = "session_id") var session_id: Long = 0) {
    @ColumnInfo(name = "id")
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
}