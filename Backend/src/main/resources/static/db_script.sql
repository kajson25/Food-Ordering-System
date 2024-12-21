-- Creating Users table
CREATE TABLE IF NOT EXISTS users (
                id BIGSERIAL PRIMARY KEY,
                first_name VARCHAR(255) NOT NULL,
                last_name VARCHAR(255) NOT NULL,
                email VARCHAR(255) NOT NULL UNIQUE,
                password VARCHAR(255) NOT NULL,
                is_admin BOOLEAN NOT NULL DEFAULT FALSE,
                UNIQUE (email)
);

-- Creating Orders table
CREATE TABLE IF NOT EXISTS orders (
                id BIGSERIAL PRIMARY KEY,
                status VARCHAR(50) CHECK (status IN ('ORDERED', 'PREPARING', 'IN_DELIVERY', 'DELIVERED', 'CANCELED')) NOT NULL,
                created_by BIGINT NOT NULL,
                active BOOLEAN NOT NULL DEFAULT TRUE,
                FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE CASCADE
);

-- Creating Dishes table
CREATE TABLE IF NOT EXISTS dishes (
                id BIGSERIAL PRIMARY KEY,
                name VARCHAR(255) NOT NULL,
                description TEXT,
                price NUMERIC(10, 2) NOT NULL
);

-- Creating Items table (to resolve many-to-many relationship between Orders and Dishes)
CREATE TABLE IF NOT EXISTS items (
                id BIGSERIAL PRIMARY KEY,
                order_id BIGINT NOT NULL,
                dish_id BIGINT NOT NULL,
                quantity INT NOT NULL CHECK (quantity > 0),
                FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
                FOREIGN KEY (dish_id) REFERENCES dishes(id) ON DELETE CASCADE,
                UNIQUE(order_id, dish_id) -- To ensure no duplicate dish entries for the same order
);

CREATE TABLE IF NOT EXISTS error_messages (
                                     id BIGSERIAL PRIMARY KEY,
                                     date date,
                                     order_id BIGINT NOT NULL,
                                     operation VARCHAR(255),
                                     message VARCHAR(255),
                                     FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
);