package com.xiaeer.lock.data.room.repo

import com.xiaeer.lock.data.room.dao.LockTaskDao
import com.xiaeer.lock.data.room.model.LockTask
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LockTaskRepository @Inject constructor(private val lockTaskDao: LockTaskDao) {

    fun getListLiveDataLockTask() = lockTaskDao.getAllTasksLive()

    fun insertLockTasks(vararg myTasks: LockTask) {
        lockTaskDao.insertTasks(*myTasks)
    }

    fun updateLockTasks(vararg myTasks: LockTask) {
        lockTaskDao.updateTasks(*myTasks)
    }

    fun deleteLockTasks(vararg myTasks: LockTask) {
        lockTaskDao.deleteTasks(*myTasks)
    }

    fun getLockTaskByID(id: Long): LockTask {
        return lockTaskDao.getLockTaskByID(id)
    }

    companion object {
        @Volatile private var instance: LockTaskRepository? = null

        fun getInstance(lockTaskDao: LockTaskDao): LockTaskRepository {
            return instance ?: synchronized(this) {
                instance ?: LockTaskRepository(lockTaskDao)
                    .also { instance = it }
            }
        }
    }
}