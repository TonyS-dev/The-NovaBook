-- ============================================
-- LibroNova - Library Management System
-- Database Schema - PostgreSQL
-- ============================================

-- Enable UUID extension for future use
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ============================================
-- TABLE: users
-- System users (ADMIN/ASSISTANT roles)
-- Manages authentication and authorization
-- ============================================
CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(120) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    phone VARCHAR(30),
    role VARCHAR(20) NOT NULL DEFAULT 'ASSISTANT' CHECK (role IN ('ADMIN', 'ASSISTANT')),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE', 'DELETED')),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ============================================
-- TABLE: members
-- Library members who can request loans
-- ============================================
CREATE TABLE IF NOT EXISTS members (
    id SERIAL PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    document_id VARCHAR(20) NOT NULL UNIQUE,
    email VARCHAR(120) NOT NULL UNIQUE,
    phone VARCHAR(30),
    address VARCHAR(200),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE', 'SUSPENDED', 'DELETED')),
    registration_date DATE NOT NULL DEFAULT CURRENT_DATE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ============================================
-- TABLE: books
-- Complete book catalog with inventory control
-- ============================================
CREATE TABLE IF NOT EXISTS books (
    id SERIAL PRIMARY KEY,
    isbn VARCHAR(20) NOT NULL UNIQUE,
    title VARCHAR(200) NOT NULL,
    author VARCHAR(150) NOT NULL,
    category VARCHAR(50) NOT NULL,
    total_copies INT NOT NULL DEFAULT 1 CHECK (total_copies >= 0),
    available_copies INT NOT NULL DEFAULT 1 CHECK (available_copies >= 0),
    reference_price DECIMAL(10, 2),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_copies CHECK (available_copies <= total_copies)
);

-- ============================================
-- TABLE: loans
-- Complete loan and return management
-- ============================================
CREATE TABLE IF NOT EXISTS loans (
    id SERIAL PRIMARY KEY,
    member_id INT NOT NULL REFERENCES members(id) ON DELETE RESTRICT,
    book_id INT NOT NULL REFERENCES books(id) ON DELETE RESTRICT,
    loan_date DATE NOT NULL DEFAULT CURRENT_DATE,
    expected_return_date DATE NOT NULL,
    actual_return_date DATE,
    loan_days INT NOT NULL DEFAULT 7,
    fine DECIMAL(10, 2) DEFAULT 0.00,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'RETURNED', 'OVERDUE')),
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ============================================
-- INDEXES for Query Optimization
-- ============================================

-- Indexes for frequent book searches
CREATE INDEX idx_books_category ON books(category);
CREATE INDEX idx_books_author ON books(author);
CREATE INDEX idx_books_isbn ON books(isbn);
CREATE INDEX idx_books_active ON books(is_active);

-- Indexes for users and members
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_members_document ON members(document_id);
CREATE INDEX idx_members_status ON members(status);

-- Indexes for loans
CREATE INDEX idx_loans_member ON loans(member_id);
CREATE INDEX idx_loans_book ON loans(book_id);
CREATE INDEX idx_loans_status ON loans(status);
CREATE INDEX idx_loans_loan_date ON loans(loan_date);
CREATE INDEX idx_loans_expected_return ON loans(expected_return_date);

-- ============================================
-- TRIGGERS for Automatic Updates
-- ============================================

-- Function to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Triggers for each table
CREATE TRIGGER trg_users_updated_at
BEFORE UPDATE ON users
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trg_members_updated_at
BEFORE UPDATE ON members
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trg_books_updated_at
BEFORE UPDATE ON books
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trg_loans_updated_at
BEFORE UPDATE ON loans
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- ============================================
-- FUNCTION: Validate Available Stock
-- Prevents available_copies from being negative or exceeding total
-- ============================================
CREATE OR REPLACE FUNCTION validate_available_stock()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.available_copies < 0 THEN
        RAISE EXCEPTION 'Available stock cannot be negative';
    END IF;
    IF NEW.available_copies > NEW.total_copies THEN
        RAISE EXCEPTION 'Available stock cannot exceed total copies';
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_validate_stock
BEFORE INSERT OR UPDATE ON books
FOR EACH ROW
EXECUTE FUNCTION validate_available_stock();

-- ============================================
-- FUNCTION: Calculate Fine Automatically
-- Calculates fine based on overdue days
-- ============================================
CREATE OR REPLACE FUNCTION calculate_loan_fine()
RETURNS TRIGGER AS $$
DECLARE
    overdue_days INT;
    fine_per_day DECIMAL(10, 2) := 1500.00; -- As per requirements
BEGIN
    IF NEW.actual_return_date IS NOT NULL AND NEW.status = 'RETURNED' THEN
        overdue_days := GREATEST(0, NEW.actual_return_date - NEW.expected_return_date);
        NEW.fine := overdue_days * fine_per_day;
        
        IF overdue_days > 0 THEN
            NEW.status := 'OVERDUE';
        END IF;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_calculate_fine
BEFORE UPDATE ON loans
FOR EACH ROW
WHEN (NEW.actual_return_date IS NOT NULL AND OLD.actual_return_date IS NULL)
EXECUTE FUNCTION calculate_loan_fine();

-- ============================================
-- SEED DATA (Initial Test Data)
-- ============================================



-- Sample books catalog
INSERT INTO books (isbn, title, author, category, total_copies, available_copies, reference_price, is_active) 
VALUES 
    ('978-3-16-148410-0', 'One Hundred Years of Solitude', 'Gabriel García Márquez', 'Literature', 5, 5, 45000, TRUE),
    ('978-0-7432-7356-5', '1984', 'George Orwell', 'Science Fiction', 3, 3, 38000, TRUE),
    ('978-0-14-017739-8', 'The Little Prince', 'Antoine de Saint-Exupéry', 'Children', 4, 4, 25000, TRUE),
    ('978-84-376-0494-7', 'Don Quixote', 'Miguel de Cervantes', 'Classics', 2, 2, 55000, TRUE),
    ('978-0-06-112008-4', 'To Kill a Mockingbird', 'Harper Lee', 'Fiction', 3, 3, 42000, TRUE),
    ('978-0-7475-3269-9', 'Harry Potter and the Philosopher Stone', 'J.K. Rowling', 'Fantasy', 6, 6, 35000, TRUE)
ON CONFLICT (isbn) DO NOTHING;

-- ============================================
-- VIEWS for Reports and Exports
-- ============================================

-- View: Overdue Loans (for CSV export)
CREATE OR REPLACE VIEW v_overdue_loans AS
SELECT 
    l.id AS loan_id,
    l.loan_date,
    l.expected_return_date,
    l.actual_return_date,
    l.status,
    l.fine,
    m.first_name || ' ' || m.last_name AS member_name,
    m.document_id AS member_document,
    m.email AS member_email,
    m.phone AS member_phone,
    b.title AS book_title,
    b.isbn AS book_isbn,
    b.author AS book_author,
    CURRENT_DATE - l.expected_return_date AS days_overdue
FROM loans l
INNER JOIN members m ON l.member_id = m.id
INNER JOIN books b ON l.book_id = b.id
WHERE l.status IN ('ACTIVE', 'OVERDUE')
  AND l.actual_return_date IS NULL
  AND l.expected_return_date < CURRENT_DATE
ORDER BY days_overdue DESC;

-- View: Complete Catalog with Availability (for CSV export)
CREATE OR REPLACE VIEW v_catalog_availability AS
SELECT 
    b.id,
    b.isbn,
    b.title,
    b.author,
    b.category,
    b.total_copies,
    b.available_copies,
    b.reference_price,
    b.is_active,
    CASE 
        WHEN b.available_copies > 0 THEN 'AVAILABLE'
        ELSE 'NOT AVAILABLE'
    END AS availability_status,
    b.created_at,
    b.updated_at
FROM books b
WHERE b.is_active = TRUE
ORDER BY b.category, b.title;

-- View: Member Loan History
CREATE OR REPLACE VIEW v_member_loan_history AS
SELECT 
    m.id AS member_id,
    m.first_name || ' ' || m.last_name AS member_name,
    m.document_id,
    m.email,
    m.status AS member_status,
    COUNT(l.id) AS total_loans,
    COUNT(CASE WHEN l.status = 'ACTIVE' THEN 1 END) AS active_loans,
    COUNT(CASE WHEN l.status = 'RETURNED' THEN 1 END) AS returned_loans,
    COUNT(CASE WHEN l.status = 'OVERDUE' THEN 1 END) AS overdue_loans,
    COALESCE(SUM(l.fine), 0) AS total_fines
FROM members m
LEFT JOIN loans l ON m.id = l.member_id
GROUP BY m.id, m.first_name, m.last_name, m.document_id, m.email, m.status
ORDER BY total_loans DESC;

-- View: Book Loan Statistics
CREATE OR REPLACE VIEW v_book_statistics AS
SELECT 
    b.id AS book_id,
    b.isbn,
    b.title,
    b.author,
    b.category,
    b.total_copies,
    b.available_copies,
    b.total_copies - b.available_copies AS currently_loaned,
    COUNT(l.id) AS total_times_loaned,
    COUNT(CASE WHEN l.status = 'ACTIVE' THEN 1 END) AS current_active_loans
FROM books b
LEFT JOIN loans l ON b.id = l.book_id
WHERE b.is_active = TRUE
GROUP BY b.id, b.isbn, b.title, b.author, b.category, b.total_copies, b.available_copies
ORDER BY total_times_loaned DESC;

-- ============================================
-- TABLE COMMENTS (Documentation)
-- ============================================
COMMENT ON TABLE users IS 'System users with ADMIN or ASSISTANT roles for authentication';
COMMENT ON TABLE members IS 'Library members who can request book loans';
COMMENT ON TABLE books IS 'Complete book catalog with inventory management';
COMMENT ON TABLE loans IS 'Loan and return transaction records';

COMMENT ON COLUMN books.isbn IS 'Unique ISBN code (validated at service layer)';
COMMENT ON COLUMN books.available_copies IS 'Current stock available for lending';
COMMENT ON COLUMN loans.fine IS 'Fine calculated automatically based on overdue days';
COMMENT ON COLUMN loans.loan_days IS 'Number of days allowed for the loan (default: 7)';
COMMENT ON COLUMN users.role IS 'User role: ADMIN (full access) or ASSISTANT (limited access)';
COMMENT ON COLUMN members.status IS 'Member status: ACTIVE, INACTIVE, or SUSPENDED';
