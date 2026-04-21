package com.example.easyssh.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ServerDao {
    // display servers in address book
    @Query("SELECT * FROM servers")
    // flow parameter will auto update view when data changes
    fun getAllServers(): Flow<List<Server>>

    // filter by environment
    @Query("SELECT * FROM servers WHERE environment = :env")
    fun getServersByEnv(env: String): Flow<List<Server>>

    // CURD part
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertServer(server: Server)

    @Delete
    suspend fun deleteServer(server: Server)
}