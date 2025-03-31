package com.gallery.service

import org.mindrot.jbcrypt.BCrypt
import com.gallery.dto.User as UserDto

class User(private val db: Database) {
    suspend fun createUser(login: String, password: String): UserDto? {
        return try {
            val hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt())
            db.query("INSERT INTO users (login, password) VALUES (?, ?) RETURNING *", login, hashedPassword) { ps ->
                ps.executeQuery().use { rs ->
                    if (rs.next()) {
                        UserDto(
                            id = rs.getInt("id"),
                            login = rs.getString("login"),
                            passwordHash = rs.getString("password"),
                            isActive = rs.getBoolean("is_active")
                        )
                    } else null
                }
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun authenticate(login: String, password: String): UserDto? {
        return db.query("SELECT * FROM users WHERE login = ?", login) { ps ->
            ps.executeQuery().use { rs ->
                if (rs.next()) {
                    val user = UserDto(
                        id = rs.getInt("id"),
                        login = rs.getString("login"),
                        passwordHash = rs.getString("password"),
                        isActive = rs.getBoolean("is_active")
                    )
                    if (user.isActive && BCrypt.checkpw(password, user.passwordHash)) user else null
                } else null
            }
        }
    }
}