package com.ulab.motionapp.db

import android.arch.persistence.room.*

/**
 * Created by R.S. on 11/08/18
 */
@Dao
public interface DevicesModelDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    @JvmSuppressWildcards
    fun insertUlabData(motion: DevicesModel)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSession(session: Session): Long

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateSession(session: Session):Int

    @Query("SELECT * FROM Devices")
    fun getAllData(): List<DevicesModel>

    @Query("SELECT * FROM Devices WHERE DeviceAddress= :address")
    fun getDeviceModel(address : String): DevicesModel?

    @Query("SELECT * FROM Session")
    fun getAllSession(): List<Session>

}