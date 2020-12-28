package com.ulab.motionapp.db

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.ForeignKey
import android.arch.persistence.room.ForeignKey.CASCADE
import android.arch.persistence.room.PrimaryKey


/**
 * Created by R.S. on 11/08/18
 */
@Entity(tableName = "Quaternions", foreignKeys = [
    (ForeignKey(entity = Session::class,
            parentColumns = ["id"],
            childColumns = ["session_id"],
            onDelete = CASCADE))])
class ThingyDevice(@ColumnInfo(name = "DeviceAddress") var deviceAddress: String = "",
                   @ColumnInfo(name = "DeviceName") var deviceName: String = "",
                   @ColumnInfo(name = "ValueX_quat") var valueX_quat: Float = 0f,
                   @ColumnInfo(name = "ValueY_quat") var valueY_quat: Float = 0f,
                   @ColumnInfo(name = "ValueZ_quat") var valueZ_quat: Float = 0f,
                   @ColumnInfo(name = "ValueW_quat") var valueW_quat: Float = 0f,
                   @ColumnInfo(name = "ValueX_gravity") var X_gravity: Float = 0f,
                   @ColumnInfo(name = "ValueY_gravity") var Y_gravity: Float = 0f,
                   @ColumnInfo(name = "ValueZ_gravity") var Z_gravity: Float = 0f,
                   @ColumnInfo(name = "ValueRoll_euler") var roll_euler: Float = 0f,
                   @ColumnInfo(name = "ValuePitch_euler") var pitch_euler: Float = 0f,
                   @ColumnInfo(name = "ValueYaw_euler") var yaw_euler: Float = 0f,
                   @ColumnInfo(name = "ValueX_acc") var X_acc: Float = 0f,
                   @ColumnInfo(name = "ValueY_acc") var Y_acc: Float = 0f,
                   @ColumnInfo(name = "ValueZ_acc") var Z_acc: Float = 0f,
                   @ColumnInfo(name = "Value_heading") var heading: Float = 0f,
                   @ColumnInfo(name = "ValueFSR1_voltage") var ValueFSR1_voltage: Short = 0,
                   @ColumnInfo(name = "ValueFSR2_voltage") var ValueFSR2_voltage: Short = 0,
                   @ColumnInfo(name = "ValueFSR3_voltage") var ValueFSR3_voltage: Short = 0,
                   @ColumnInfo(name = "ValueFSR4_voltage") var ValueFSR4_voltage: Short = 0,
                   @ColumnInfo(name = "ValueFSR1_force") var ValueFSR1_force: Float = 0f,
                   @ColumnInfo(name = "ValueFSR2_force") var ValueFSR2_force: Float = 0f,
                   @ColumnInfo(name = "ValueFSR3_force") var ValueFSR3_force: Float = 0f,
                   @ColumnInfo(name = "ValueFSR4_force") var ValueFSR4_force: Float = 0f,
                   @ColumnInfo(name = "ValueFSR1_force_calculated") var ValueFSR1_force_calculated: Float = 0f,
                   @ColumnInfo(name = "ValueFSR2_force_calculated") var ValueFSR2_force_calculated: Float = 0f,
                   @ColumnInfo(name = "ValueFSR3_force_calculated") var ValueFSR3_force_calculated: Float = 0f,
                   @ColumnInfo(name = "ValueFSR4_force_calculated") var ValueFSR4_force_calculated: Float = 0f,
                   @ColumnInfo(name = "timestemp") var timestamp: String = "",
                   @ColumnInfo(name = "timestemp_milles") var timestampinMillies: String = "",
                   @ColumnInfo(name = "session_id") var session_id: Long=0,
                   @ColumnInfo(name = "event_type") var event_type: String=""){
    @ColumnInfo(name = "id")
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
}