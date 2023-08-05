package com.xiaeer.lock.data.room.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.xiaeer.lock.data.room.model.LockTask

@Dao
interface LockTaskDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTasks(vararg myTasks: LockTask)

    @Update
    fun updateTasks(vararg myTasks: LockTask)

    @Delete
    fun deleteTasks(vararg myTasks: LockTask)

    @Query("DELETE FROM lock_task")
    fun deleteAllTasks()

    @Query("SELECT * FROM lock_task ORDER BY startTime")
    fun getAllTasksLive(): LiveData<List<LockTask>>


    @Query("SELECT * FROM lock_task WHERE id=:id ORDER BY id ")
    fun getLockTaskByID(id: Long): LockTask
}