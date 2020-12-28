package com.ulab.motionapp.db

import android.arch.persistence.room.*

/**
 * Created by R.S. on 11/08/18
 */
@Dao
public interface QuaternionsConfigDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    @JvmSuppressWildcards
    fun insertUlabData(motion: QuaternionsConfigModel)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSession(session: Session): Long

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateSession(session: Session):Int

    @Query("SELECT * FROM QuaternionsConfig")
    fun getAllData(): List<QuaternionsConfigModel>

    @Query("DELETE FROM QuaternionsConfig WHERE DeviceAddress= :address")
    fun deleteAllModels(address: String)

    @Query("SELECT * FROM Session")
    fun getAllSession(): List<Session>

}