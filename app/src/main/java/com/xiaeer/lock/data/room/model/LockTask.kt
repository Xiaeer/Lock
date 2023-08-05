package com.xiaeer.lock.data.room.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "lock_task")
data class LockTask(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,

    var startTime: Date? = null,
    var endTime: Date? = null,

    var enable: Boolean = false
)