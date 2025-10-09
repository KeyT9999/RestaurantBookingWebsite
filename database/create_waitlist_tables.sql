-- Create waitlist_dish table to store selected dishes for waitlist
CREATE TABLE IF NOT EXISTS waitlist_dish (
    waitlist_dish_id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    waitlist_id INTEGER NOT NULL REFERENCES waitlist(waitlist_id) ON DELETE CASCADE,
    dish_id INTEGER NOT NULL REFERENCES dish(dish_id),
    quantity INTEGER NOT NULL DEFAULT 1,
    price NUMERIC(18,2) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Create waitlist_service table to store selected services for waitlist
CREATE TABLE IF NOT EXISTS waitlist_service (
    waitlist_service_id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    waitlist_id INTEGER NOT NULL REFERENCES waitlist(waitlist_id) ON DELETE CASCADE,
    service_id INTEGER NOT NULL REFERENCES restaurant_service(service_id),
    quantity INTEGER NOT NULL DEFAULT 1,
    price NUMERIC(18,2) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Create waitlist_table table to store selected tables for waitlist
CREATE TABLE IF NOT EXISTS waitlist_table (
    waitlist_table_id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    waitlist_id INTEGER NOT NULL REFERENCES waitlist(waitlist_id) ON DELETE CASCADE,
    table_id INTEGER NOT NULL REFERENCES restaurant_table(table_id),
    assigned_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Add indexes for better performance
CREATE INDEX IF NOT EXISTS idx_waitlist_dish_waitlist_id ON waitlist_dish(waitlist_id);
CREATE INDEX IF NOT EXISTS idx_waitlist_service_waitlist_id ON waitlist_service(waitlist_id);
CREATE INDEX IF NOT EXISTS idx_waitlist_table_waitlist_id ON waitlist_table(waitlist_id);
