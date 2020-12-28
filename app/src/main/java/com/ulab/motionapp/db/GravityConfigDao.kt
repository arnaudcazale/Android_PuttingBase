package com.ulab.motionapp.db

import android.arch.persistence.room.*

/**
 * Created by R.S. on 11/08/18
 */
@Dao
public interface GravityConfigDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    @JvmSuppressWildcards
    fun insertUlabData(motion: GravityConfigModel)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSession(session: Session): Long

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateSession(session: Session): Int

    @Query("SELECT * FROM GravityConfig")
    fun getAllData(): List<GravityConfigModel>

    @Query("DELETE FROM GravityConfig WHERE DeviceAddress= :address")
    fun deleteAllModels(address: String)

    @Query("SELECT * FROM Session")
    fun getAllSession(): List<Session>

    @Query("DELETE FROM GravityConfig")
    fun deleteAllData()
}