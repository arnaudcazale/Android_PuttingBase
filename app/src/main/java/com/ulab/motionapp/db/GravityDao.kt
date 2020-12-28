package com.ulab.motionapp.db

import android.arch.persistence.room.*

/**
 * Created by R.S. on 11/08/18
 */
@Dao
public interface GravityDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    @JvmSuppressWildcards
    fun insertUlabData(motion: GravityModel)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    @JvmSuppressWildcards
    fun insertUlabDataAll(eulerAngleModels: ArrayList<GravityModel>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSession(session: Session): Long

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateSession(session: Session):Int

    @Query("SELECT * FROM Gravity")
    fun getAllData(): List<GravityModel>

    @Query("SELECT * FROM Session")
    fun getAllSession(): List<Session>

}