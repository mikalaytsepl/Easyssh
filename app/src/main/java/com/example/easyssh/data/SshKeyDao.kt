package com.example.easyssh.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SshKeyDao {
    @Query("SELECT * FROM ssh_keys")
    fun getAllKeys(): Flow<List<SshKey>>

    @Query("SELECT * FROM ssh_keys WHERE id = :id")
    suspend fun getKeyById(id: Int): SshKey?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertKey(key: SshKey): Long

    @Query("UPDATE ssh_keys SET serverId = :serverId WHERE id = :keyId")
    suspend fun setKeyServer(keyId: Int, serverId: Int?)

    @Delete
    suspend fun deleteKey(key: SshKey)
}
