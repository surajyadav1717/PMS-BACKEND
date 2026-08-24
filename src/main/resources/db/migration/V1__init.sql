CREATE TABLE departments (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(120) NOT NULL UNIQUE,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
);

CREATE TABLE employees (
    id BIGSERIAL PRIMARY KEY,
    employee_code VARCHAR(50) NOT NULL UNIQUE,
    first_name VARCHAR(80) NOT NULL,
    last_name VARCHAR(80) NOT NULL,
    email VARCHAR(160) NOT NULL UNIQUE,
    department_id BIGINT REFERENCES departments(id),
    manager_id BIGINT REFERENCES employees(id),
    head_id BIGINT REFERENCES employees(id),
    designation VARCHAR(120),
    joining_date DATE,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
);

CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(80) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(30) NOT NULL,
    employee_id BIGINT UNIQUE REFERENCES employees(id),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
);

CREATE TABLE review_cycles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(160) NOT NULL,
    review_type VARCHAR(20) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN'
);

CREATE TABLE performance_criteria (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    description VARCHAR(500),
    weight NUMERIC(5,2) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE performance_reviews (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL REFERENCES employees(id),
    reviewer_id BIGINT NOT NULL REFERENCES employees(id),
    review_cycle_id BIGINT NOT NULL REFERENCES review_cycles(id),
    review_type VARCHAR(20) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    overall_score NUMERIC(4,2),
    manager_comments TEXT,
    employee_comments TEXT,
    submitted_at TIMESTAMP,
    approved_at TIMESTAMP,
    acknowledged_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(employee_id, review_cycle_id)
);

CREATE TABLE performance_review_items (
    id BIGSERIAL PRIMARY KEY,
    review_id BIGINT NOT NULL REFERENCES performance_reviews(id) ON DELETE CASCADE,
    criterion_id BIGINT NOT NULL REFERENCES performance_criteria(id),
    score NUMERIC(4,2) NOT NULL,
    comments TEXT
);

CREATE TABLE goals (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL REFERENCES employees(id),
    title VARCHAR(200) NOT NULL,
    description TEXT,
    start_date DATE,
    target_date DATE,
    weight NUMERIC(5,2),
    progress_percentage INTEGER NOT NULL DEFAULT 0,
    status VARCHAR(30) NOT NULL DEFAULT 'IN_PROGRESS',
    created_by BIGINT REFERENCES employees(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE goal_updates (
    id BIGSERIAL PRIMARY KEY,
    goal_id BIGINT NOT NULL REFERENCES goals(id) ON DELETE CASCADE,
    progress_percentage INTEGER NOT NULL,
    comment TEXT,
    created_by BIGINT REFERENCES employees(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE audit_logs (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id),
    entity_type VARCHAR(80) NOT NULL,
    entity_id BIGINT,
    action VARCHAR(80) NOT NULL,
    old_value TEXT,
    new_value TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_employee_manager ON employees(manager_id);
CREATE INDEX idx_employee_department ON employees(department_id);
CREATE INDEX idx_review_employee ON performance_reviews(employee_id);
CREATE INDEX idx_review_reviewer ON performance_reviews(reviewer_id);
CREATE INDEX idx_review_cycle ON performance_reviews(review_cycle_id);
CREATE INDEX idx_goal_employee ON goals(employee_id);
