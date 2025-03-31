CREATE TABLE photos (
    uuid UUID PRIMARY KEY,
    url VARCHAR(255) NOT NULL,
    album_id UUID NOT NULL,
    user_id INT NOT NULL,
    FOREIGN KEY (album_id) REFERENCES albums(uuid) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
