CREATE DATABASE IF NOT EXISTS phonebook;
USE phonebook;

CREATE TABLE contacts (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    phone VARCHAR(20) NOT NULL
);

INSERT INTO contacts (name, phone) VALUES
('张三', '13800138001'),
('李四', '13800138002'),
('王五', '13800138003'),
('赵六', '13800138004'),
('孙七', '13800138005');
