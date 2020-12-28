package com.ulab.motionapp.db

import android.arch.persistence.room.*

/**
 * Created by R.S. on 11/08/18
 */
@Dao
public interface EulerDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    @JvmSuppressWildcards
    fun insertUlabData(motion: EulerModel)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    @JvmSuppressWildcards
    fun insertUlabDataAll(eulerAngleModels: ArrayList<EulerModel>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSession(session: Session): Long

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateSession(session: Session): Int

    @Query("SELECT * FROM Euler")
    fun getAllData(): List<EulerModel>

    @Query("SELECT * FROM Session")
    fun getAllSession(): List<Session>

}