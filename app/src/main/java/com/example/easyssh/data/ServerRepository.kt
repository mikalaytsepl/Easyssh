package com.example.easyssh.data

import kotlinx.coroutines.flow.Flow

class ServerRepository(private val dao: ServerDao) {
    fun getAllServers(): Flow<List<Server>> = dao.getAllServers()
    fun getServersByEnv(env: String): Flow<List<Server>> = dao.getServersByEnv(env)
    suspend fun getServerById(id: Int): Server? = dao.getServerById(id)
    suspend fun insert(server: Server) = dao.insertServer(server)
    suspend fun delete(server: Server) = dao.deleteServer(server)
}
