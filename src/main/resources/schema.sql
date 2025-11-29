CREATE TABLE IF NOT EXISTS locations (
    id INTEGER PRIMARY KEY,
    name TEXT NOT NULL,
    type TEXT,
    dimension TEXT
);

CREATE TABLE IF NOT EXISTS characters (
    id INTEGER PRIMARY KEY,
    name TEXT NOT NULL,
    status TEXT,
    species TEXT,
    gender TEXT,
    origin TEXT,
    location TEXT,
    image TEXT
);

CREATE TABLE IF NOT EXISTS location_residents (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    location_id INTEGER NOT NULL,
    character_id INTEGER NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_location_character ON location_residents(location_id, character_id);

CREATE TABLE IF NOT EXISTS character_notes (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    character_id INTEGER NOT NULL,
    note TEXT NOT NULL,
    author TEXT,
    created_at TEXT DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS generations (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    subject_type TEXT NOT NULL,
    subject_id INTEGER NOT NULL,
    prompt TEXT NOT NULL,
    output TEXT NOT NULL,
    evaluator JSON,
    created_at TEXT DEFAULT CURRENT_TIMESTAMP
);
