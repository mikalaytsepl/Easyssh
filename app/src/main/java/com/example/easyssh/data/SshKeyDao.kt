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
    suspend fun insertKey(key: SshKey)

    @Delete
    suspend fun deleteKey(key: SshKey)
}
