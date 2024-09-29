use Attendance;

-- 建立用戶表
CREATE TABLE users (
                       id INT AUTO_INCREMENT PRIMARY KEY,
                       username VARCHAR(50) NOT NULL UNIQUE,
                       password VARCHAR(100) NOT NULL,
                       role VARCHAR(20) NOT NULL,
                       created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                       updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 插入用戶範例資料
INSERT INTO users (username, password, role) VALUES
                                                 ('user1', '$2a$10$E9m6Q/9Y4C3/1s5E2v2yheG9E7b0Q/6Z8c5H7b1K3d4A5F6H7I8J9', 'USER'),
                                                 ('admin', '$2a$10$K5l8V/6Z3B7/2d4E1u3xkeH6F8c3N/5V7d6J2c4M5n6O7P8Q9R0S1', 'ADMIN');

-- 建立打卡記錄表
CREATE TABLE attendance_records (
                                    id INT AUTO_INCREMENT PRIMARY KEY,
                                    user_id INT NOT NULL,
                                    clock_in_time DATETIME NOT NULL,
                                    latitude DOUBLE NOT NULL,
                                    longitude DOUBLE NOT NULL,
                                    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- 插入打卡記錄範例資料
INSERT INTO attendance_records (user_id, clock_in_time, latitude, longitude) VALUES
                                                                                 (1, '2024-09-25 08:30:00', 25.033964, 121.564468),
                                                                                 (1, '2024-09-26 08:32:00', 25.033980, 121.564480),
                                                                                 (2, '2024-09-25 08:45:00', 25.034000, 121.564500);

-- 建立公司位置表（可選）
CREATE TABLE company_locations (
                                   id INT AUTO_INCREMENT PRIMARY KEY,
                                   name VARCHAR(100) NOT NULL,
                                   latitude DOUBLE NOT NULL,
                                   longitude DOUBLE NOT NULL
);

-- 插入公司位置範例資料
INSERT INTO company_locations (name, latitude, longitude) VALUES
    ('公司總部', 25.033964, 121.564468);
