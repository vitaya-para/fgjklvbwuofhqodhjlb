CREATE TABLE IF NOT EXISTS deleted_photos (
     id SERIAL PRIMARY KEY,
     url TEXT NOT NULL,
     deleted_at TIMESTAMP DEFAULT now()
);

CREATE OR REPLACE FUNCTION log_deleted_photos()
RETURNS TRIGGER AS $$
BEGIN
INSERT INTO deleted_photos (url, deleted_at)
VALUES (OLD.url, now());
RETURN OLD;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER photos_delete_trigger
    BEFORE DELETE ON photos
    FOR EACH ROW
    EXECUTE FUNCTION log_deleted_photos();