DROP TABLE IF EXISTS travel_plans;
DROP TABLE IF EXISTS users;

CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL
);

CREATE TABLE travel_plans (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL,
    travel_plan TEXT NOT NULL,
    preferences TEXT,
    expenses TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (username) REFERENCES users(username) ON DELETE CASCADE
);