CREATE DATABASE IF NOT EXISTS hourglass CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE hourglass;

CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    full_name VARCHAR(120) NOT NULL,
    email VARCHAR(180) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE wallets (
    user_id BIGINT PRIMARY KEY,
    balance INT NOT NULL DEFAULT 5,
    CONSTRAINT fk_wallet_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT chk_wallet_balance CHECK (balance >= 0)
);

CREATE TABLE services (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    provider_id BIGINT NOT NULL,
    title VARCHAR(160) NOT NULL,
    category VARCHAR(80) NOT NULL,
    duration_hours INT NOT NULL,
    mode ENUM('Online', 'Offline') NOT NULL,
    description TEXT NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_service_provider FOREIGN KEY (provider_id) REFERENCES users(id),
    CONSTRAINT chk_service_duration CHECK (duration_hours > 0)
);

CREATE TABLE service_requests (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    requester_id BIGINT NOT NULL,
    service_id BIGINT NOT NULL,
    status ENUM('PENDING','ACCEPTED','IN_PROGRESS','COMPLETED','VERIFIED','REJECTED','CANCELLED') NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    verified_at TIMESTAMP NULL,
    CONSTRAINT fk_requester FOREIGN KEY (requester_id) REFERENCES users(id),
    CONSTRAINT fk_requested_service FOREIGN KEY (service_id) REFERENCES services(id)
);

CREATE TABLE transactions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    request_id BIGINT NOT NULL UNIQUE,
    from_user_id BIGINT NOT NULL,
    to_user_id BIGINT NOT NULL,
    credits INT NOT NULL,
    reason VARCHAR(255) NOT NULL,
    verification_token CHAR(36) NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_transaction_request FOREIGN KEY (request_id) REFERENCES service_requests(id),
    CONSTRAINT fk_transaction_from FOREIGN KEY (from_user_id) REFERENCES users(id),
    CONSTRAINT fk_transaction_to FOREIGN KEY (to_user_id) REFERENCES users(id),
    CONSTRAINT chk_transaction_credits CHECK (credits > 0)
);

INSERT INTO users(full_name, email, password_hash) VALUES
('Rahul', 'rahul@campus.edu', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy'),
('Shreya', 'shreya@campus.edu', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy'),
('Amit', 'amit@campus.edu', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy');

INSERT INTO wallets(user_id, balance) SELECT id, 5 FROM users;
INSERT INTO services(provider_id, title, category, duration_hours, mode, description) VALUES
(2, 'Python Basics Help', 'Programming', 2, 'Online', 'Beginner-friendly Python fundamentals and problem solving.'),
(3, 'Photoshop Assistance', 'Design', 1, 'Offline', 'Learn editing, layers, tools and presentation graphics.');
