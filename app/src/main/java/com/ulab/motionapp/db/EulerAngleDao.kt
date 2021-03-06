package com.ulab.motionapp.db

import android.arch.persistence.room.*

/**
 * Created by R.S. on 11/08/18
 */
@Dao
public interface EulerAngleDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    @JvmSuppressWildcards
    fun insertUlabData(motion: EulerAngleModel)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    @JvmSuppressWildcards
    fun insertUlabDataAll(motion: ArrayList<EulerAngleModel>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSession(session: Session): Long

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateSession(session: Session): Int

    @Query("SELECT * FROM EulerAngle")
    fun getAllData(): List<EulerAngleModel>

    @Query("SELECT * FROM Session")
    fun getAllSession(): List<Session>

}