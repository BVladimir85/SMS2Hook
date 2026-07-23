package com.smswebhook.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PresetDao {
    @Query("SELECT * FROM presets ORDER BY sortOrder, id")
    fun observeAll(): Flow<List<Preset>>

    @Query("SELECT * FROM presets WHERE enabled = 1")
    suspend fun getEnabled(): List<Preset>

    @Query("SELECT * FROM presets WHERE id = :id")
    suspend fun getById(id: Long): Preset?

    @Query("SELECT COUNT(*) FROM presets")
    suspend fun count(): Int

    @Insert
    suspend fun insert(preset: Preset): Long

    @Update
    suspend fun update(preset: Preset)

    @Delete
    suspend fun delete(preset: Preset)
}

@Dao
interface DeliveryLogDao {
    @Query("SELECT * FROM logs ORDER BY timestamp DESC LIMIT 200")
    fun observeRecent(): Flow<List<DeliveryLog>>

    @Insert
    suspend fun insert(log: DeliveryLog)

    @Query("DELETE FROM logs")
    suspend fun clear()

    @Query("DELETE FROM logs WHERE id NOT IN (SELECT id FROM logs ORDER BY timestamp DESC LIMIT 500)")
    suspend fun trim()
}
