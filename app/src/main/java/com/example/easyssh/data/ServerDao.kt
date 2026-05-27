package com.example.easyssh.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ServerDao {
    @Query("SELECT * FROM servers ORDER BY id DESC")
    fun getAllServers(): Flow<List<Server>>

    @Query("SELECT * FROM servers WHERE environment = :env ORDER BY id DESC")
    fun getServersByEnv(env: String): Flow<List<Server>>

    @Query("SELECT * FROM servers WHERE id = :id LIMIT 1")
    suspend fun getServerById(id: Int): Server?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertServer(server: Server)

    @Delete
    suspend fun deleteServer(server: Server)
}
