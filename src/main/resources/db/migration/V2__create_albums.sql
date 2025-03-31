CREATE TABLE albums (
    uuid UUID PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    cover_url VARCHAR(255),
    user_id INT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
