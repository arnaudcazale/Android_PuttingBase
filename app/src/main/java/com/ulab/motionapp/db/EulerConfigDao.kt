package com.ulab.motionapp.db

import android.arch.persistence.room.*

/**
 * Created by R.S. on 11/08/18
 */
@Dao
public interface EulerConfigDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    @JvmSuppressWildcards
    fun insertUlabData(motion: EulerConfigModel)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSession(session: Session): Long

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateSession(session: Session): Int

    @Query("SELECT * FROM EulerConfig")
    fun getAllData(): List<EulerConfigModel>

    @Query("DELETE FROM EulerConfig WHERE DeviceAddress= :address")
    fun deleteAllModels(address: String)

    @Query("SELECT * FROM Session")
    fun getAllSession(): List<Session>

    @Query("SELECT id, DeviceAddress, DeviceName, sum(ValueR)/:maxCount AS ValueR, sum(ValueP)/:maxCount AS ValueP, sum(ValueY)/:maxCount AS ValueY, timestemp, timestemp_milles, session_id FROM EulerConfig WHERE DeviceAddress = :deviceAddress")
    fun getAvgModel(deviceAddress: String, maxCount: Int): EulerConfigModel

}