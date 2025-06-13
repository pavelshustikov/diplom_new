
CREATE TABLE IF NOT EXISTS users (
                                     id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                     username VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    auth_token VARCHAR(255),
    token_creation_time TIMESTAMP
    );


CREATE TABLE IF NOT EXISTS files (
                                     id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                     filename VARCHAR(255) NOT NULL,
    size BIGINT NOT NULL,
    upload_date TIMESTAMP NOT NULL,
    path VARCHAR(500) NOT NULL,
    user_id BIGINT NOT NULL,
    CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES users(id),
    UNIQUE (filename, user_id)
    );