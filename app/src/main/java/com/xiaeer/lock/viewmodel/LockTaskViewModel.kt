package com.xiaeer.lock.viewmodel

import androidx.lifecycle.AndroidViewModel
import com.xiaeer.lock.App
import com.xiaeer.lock.data.room.model.LockTask
import com.xiaeer.lock.data.room.repo.LockTaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LockTaskViewModel @Inject internal constructor(
    private val lockTaskRepository: LockTaskRepository,
) : AndroidViewModel(App.application) {

    val allLockTaskLive = lockTaskRepository.getListLiveDataLockTask()

    fun insertLockTasks(vararg myTasks: LockTask) {
        CoroutineScope(Dispatchers.IO).launch {
            lockTaskRepository.insertLockTasks(*myTasks)
        }
    }

    fun updateLockTasks(vararg myTasks: LockTask) {
        CoroutineScope(Dispatchers.IO).launch {
            lockTaskRepository.updateLockTasks(*myTasks)
        }
    }

    fun deleteLockTasks(vararg myTasks: LockTask) {
        CoroutineScope(Dispatchers.IO).launch {
            lockTaskRepository.deleteLockTasks(*myTasks)
        }
    }

    fun getLockTaskByID(id: Long): LockTask {
        return lockTaskRepository.getLockTaskByID(id)
    }
}