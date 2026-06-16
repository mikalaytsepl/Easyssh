package com.example.easyssh.data

import kotlinx.coroutines.flow.Flow

class SshKeyRepository(private val dao: SshKeyDao) {
    fun getAllKeys(): Flow<List<SshKey>> = dao.getAllKeys()
    suspend fun getKeyById(id: Int): SshKey? = dao.getKeyById(id)
    suspend fun insert(key: SshKey) = dao.insertKey(key)
    suspend fun delete(key: SshKey) = dao.deleteKey(key)
}