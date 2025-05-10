-- Create tables
CREATE TABLE IF NOT EXISTS books (
    id IDENTITY PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    author VARCHAR(255) NOT NULL,
    isbn VARCHAR(20) UNIQUE,
    "year" INT,
    available BOOLEAN DEFAULT TRUE
    );

CREATE TABLE IF NOT EXISTS members (
    id IDENTITY PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    contact VARCHAR(255)
    );

CREATE TABLE IF NOT EXISTS loans (
    id IDENTITY PRIMARY KEY,
    book_id BIGINT NOT NULL,
    member_id BIGINT NOT NULL,
    loan_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    return_date TIMESTAMP,
    returned BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (book_id) REFERENCES books(id),
    FOREIGN KEY (member_id) REFERENCES members(id)
    );

CREATE TABLE IF NOT EXISTS users (
    id IDENTITY PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(50) DEFAULT 'borrower' NOT NULL -- Add role, default to 'borrower'
);

-- Insert a default manager user (password: admin)
-- You should change the password in a real application
-- BCrypt hash for "admin": $2a$10$N0AMP3rXgZJzL7o7J9aRzO0qKzW8Z.2u.nFw2.dKYdYkfl2XyY.S6
CREATE TABLE IF NOT EXISTS users (
    id IDENTITY PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(50) DEFAULT 'borrower' NOT NULL -- Add role, default to 'borrower'
);

-- Insert a default manager user (password: admin)
-- You should change the password in a real application
-- BCrypt hash for "admin": $2a$10$N0AMP3rXgZJzL7o7J9aRzO0qKzW8Z.2u.nFw2.dKYdYkfl2XyY.S6
MERGE INTO users (username, password_hash, role) KEY(username) VALUES ('admin', '$2a$10$N0AMP3rXgZJzL7o7J9aRzO0qKzW8Z.2u.nFw2.dKYdYkfl2XyY.S6', 'manager');

-- Insert some sample data into books
INSERT INTO books (title, author, isbn, "year", available) VALUES
('The Lord of the Rings', 'J.R.R. Tolkien', '9780618640157', 1954, TRUE),
('Pride and Prejudice', 'Jane Austen', '9780141439518', 1813, TRUE),
('To Kill a Mockingbird', 'Harper Lee', '9780061120084', 1960, FALSE),
('1984', 'George Orwell', '9780451524935', 1949, TRUE),
('The Great Gatsby', 'F. Scott Fitzgerald', '9780743273565', 1925, TRUE),
('Harry Potter and the Sorcerer''s Stone', 'J.K. Rowling', '9780590353427', 1997, FALSE),
('The Hobbit', 'J.R.R. Tolkien', '9780547928227', 1937, TRUE),
('The Catcher in the Rye', 'J.D. Salinger', '9780316769488', 1951, TRUE),
('Brave New World', 'Aldous Huxley', '9780060850524', 1932, TRUE),
('Moby Dick', 'Herman Melville', '9780142437247', 1851, FALSE),
('War and Peace', 'Leo Tolstoy', '9780140447934', 1869, TRUE),
('The Odyssey', 'Homer', '9780140268867', -800, TRUE), -- Using negative for BC years
('Crime and Punishment', 'Fyodor Dostoevsky', '9780140449136', 1866, TRUE),
('The Divine Comedy', 'Dante Alighieri', '9780142437223', 1320, FALSE),
('Alice''s Adventures in Wonderland', 'Lewis Carroll', '9780141439761', 1865, TRUE),
('Don Quixote', 'Miguel de Cervantes', '9780060934347', 1605, TRUE),
('Frankenstein', 'Mary Shelley', '9780486282114', 1818, TRUE),
('The Adventures of Sherlock Holmes', 'Arthur Conan Doyle', '9780140437713', 1892, FALSE),
('One Hundred Years of Solitude', 'Gabriel Garcia Marquez', '9780060883287', 1967, TRUE),
('The Chronicles of Narnia', 'C.S. Lewis', '9780066238500', 1950, TRUE);