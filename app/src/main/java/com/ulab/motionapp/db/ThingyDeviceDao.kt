package com.ulab.motionapp.db

import android.arch.persistence.room.*

/**
 * Created by R.S. on 11/08/18
 */
@Dao
public interface ThingyDeviceDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    @JvmSuppressWildcards
    fun insertUlabData(motion: ThingyDevice)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    @JvmSuppressWildcards
    fun insertUlabDataAll(motion: ArrayList<ThingyDevice>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    @JvmSuppressWildcards
    fun insertUlabFsrDataAll(motion: ArrayList<ThingyDeviceFSR>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSession(session: Session): Long

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateSession(session: Session): Int

    @Query("SELECT * FROM Quaternions")
    fun getAllData(): List<ThingyDevice>

    @Query("SELECT * FROM Session")
    fun getAllSession(): List<Session>
}