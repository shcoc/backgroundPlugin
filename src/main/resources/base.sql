CREATE TABLE IF NOT EXISTS image(
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    path VARCHAR(255) NOT NULL,
    url VARCHAR(255) NOT NULL,
    type INTEGER NOT NULL default 0
)