USE master;
GO

-- Tạo cơ sở dữ liệu mới (tùy chọn, nếu chưa có)
IF NOT EXISTS (SELECT * FROM sys.databases WHERE name = 'QuanLySinhVienDB')
BEGIN
    CREATE DATABASE QuanLySinhVienDB;
END
GO

-- Sử dụng cơ sở dữ liệu vừa tạo
USE QuanLySinhVienDB;
GO

-- 1. Bảng Khoa
CREATE TABLE khoa (
    id BIGINT PRIMARY KEY IDENTITY(1,1),
    ten_khoa NVARCHAR(255) NOT NULL UNIQUE
);
GO

-- 2. Bảng Lớp học (lớp sinh hoạt)
CREATE TABLE lop_hoc (
    id BIGINT PRIMARY KEY IDENTITY(1,1),
    ten_lop NVARCHAR(100) NOT NULL UNIQUE,
    khoa_id BIGINT NOT NULL,
    CONSTRAINT FK_lop_hoc_khoa FOREIGN KEY (khoa_id) REFERENCES khoa(id)
);
GO

-- 3. Bảng Sinh viên
CREATE TABLE sinh_vien (
    id BIGINT PRIMARY KEY IDENTITY(1,1),
    ma_sinh_vien VARCHAR(50) NOT NULL UNIQUE,
    ho_ten NVARCHAR(255) NOT NULL,
    ngay_sinh DATE,
    email VARCHAR(255) NOT NULL UNIQUE,
    lop_hoc_id BIGINT,
    CONSTRAINT FK_sinh_vien_lop_hoc FOREIGN KEY (lop_hoc_id) REFERENCES lop_hoc(id)
);
GO

-- 4. Bảng Giáo viên
CREATE TABLE giao_vien (
    id BIGINT PRIMARY KEY IDENTITY(1,1),
    ma_giao_vien VARCHAR(50) NOT NULL UNIQUE,
    ho_ten NVARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    khoa_id BIGINT NOT NULL,
    CONSTRAINT FK_giao_vien_khoa FOREIGN KEY (khoa_id) REFERENCES khoa(id)
);
GO

-- 5. Bảng Môn học
CREATE TABLE mon_hoc (
    id BIGINT PRIMARY KEY IDENTITY(1,1),
    ma_mon_hoc VARCHAR(50) NOT NULL UNIQUE,
    ten_mon_hoc NVARCHAR(255) NOT NULL,
    so_tin_chi INT NOT NULL CHECK (so_tin_chi > 0),
    khoa_id BIGINT NOT NULL,
    CONSTRAINT FK_mon_hoc_khoa FOREIGN KEY (khoa_id) REFERENCES khoa(id)
);
GO

-- 6. Bảng Lớp tín chỉ
CREATE TABLE lop_tin_chi (
    id BIGINT PRIMARY KEY IDENTITY(1,1),
    ma_lop VARCHAR(50) NOT NULL UNIQUE,
    hoc_ky NVARCHAR(50) NOT NULL, -- Ví dụ: '2024-2025 Ky 1'
    mon_hoc_id BIGINT NOT NULL,
    giao_vien_id BIGINT NOT NULL,
    CONSTRAINT FK_lop_tin_chi_mon_hoc FOREIGN KEY (mon_hoc_id) REFERENCES mon_hoc(id),
    CONSTRAINT FK_lop_tin_chi_giao_vien FOREIGN KEY (giao_vien_id) REFERENCES giao_vien(id)
);
GO

-- 7. Bảng Đăng ký học
CREATE TABLE dang_ky_hoc (
    id BIGINT PRIMARY KEY IDENTITY(1,1),
    sinh_vien_id BIGINT NOT NULL,
    lop_tin_chi_id BIGINT NOT NULL,
    diem_so FLOAT CHECK (diem_so >= 0 AND diem_so <= 10),
    ngay_dang_ky DATETIME DEFAULT GETDATE(),
    CONSTRAINT FK_dang_ky_hoc_sinh_vien FOREIGN KEY (sinh_vien_id) REFERENCES sinh_vien(id),
    CONSTRAINT FK_dang_ky_hoc_lop_tin_chi FOREIGN KEY (lop_tin_chi_id) REFERENCES lop_tin_chi(id),
    -- Đảm bảo mỗi sinh viên chỉ đăng ký một lớp tín chỉ một lần duy nhất
    CONSTRAINT UQ_sinh_vien_lop_tin_chi UNIQUE (sinh_vien_id, lop_tin_chi_id)
);
GO

PRINT 'Tạo cơ sở dữ liệu và các bảng thành công!';

