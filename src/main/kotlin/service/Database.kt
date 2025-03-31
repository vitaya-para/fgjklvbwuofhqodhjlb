package com.gallery.service

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.config.*
import kotlinx.coroutines.Dispatchers
import java.sql.PreparedStatement
import javax.sql.DataSource

class Database(private val dataSource: DataSource) {

    suspend fun <T> query(
        sql: String,
        vararg params: Any,
        block: suspend (PreparedStatement) -> T
    ): T = kotlinx.coroutines.withContext(Dispatchers.IO) {
        dataSource.connection.use { conn ->
            conn.prepareStatement(sql).use { ps ->
                setParams(ps, *params)
                block(ps)
            }
        }
    }

    suspend fun update(
        sql: String,
        vararg params: Any,
        block: suspend (PreparedStatement) -> Unit
    ): Int = kotlinx.coroutines.withContext(Dispatchers.IO) {
        dataSource.connection.use { conn ->
            conn.prepareStatement(sql).use { ps ->
                setParams(ps, *params)
                block(ps)
                ps.executeUpdate()
            }
        }
    }

    private fun setParams(ps: PreparedStatement, vararg params: Any) {
        params.forEachIndexed { index, param ->
            ps.setObject(index + 1, param) // Set each parameter based on its position
        }
    }
}

fun createDatabase(config: ApplicationConfig): Database {
    val dbConfig = config.config("database")
    val jdbcUrl = dbConfig.property("jdbcUrl").getString()
    val username = dbConfig.property("username").getString()
    val password = dbConfig.property("password").getString()
    val maximumPoolSize = dbConfig.property("maximumPoolSize").getString().toInt()

    return Database(HikariDataSource(
        HikariConfig().apply {
        this.jdbcUrl = jdbcUrl
        this.username = username
        this.password = password
        this.maximumPoolSize = maximumPoolSize
        isAutoCommit = true
        validate()
    }))
}
