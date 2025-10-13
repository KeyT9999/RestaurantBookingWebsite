-- Table for storing review reports submitted by restaurant owners
CREATE TABLE IF NOT EXISTS review_report (
    report_id SERIAL PRIMARY KEY,
    review_id INTEGER REFERENCES review(review_id) ON DELETE SET NULL,
    restaurant_id INTEGER NOT NULL REFERENCES restaurant_profile(restaurant_id) ON DELETE CASCADE,
    owner_id UUID NOT NULL REFERENCES restaurant_owner(owner_id) ON DELETE CASCADE,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    reason_text TEXT NOT NULL,
    review_id_snapshot INTEGER,
    review_rating_snapshot INTEGER,
    review_comment_snapshot TEXT,
    review_created_at_snapshot TIMESTAMPTZ,
    customer_name_snapshot VARCHAR(255),
    resolution_message TEXT,
    resolved_at TIMESTAMPTZ,
    resolved_by_admin_id UUID,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_review_report_status ON review_report(status);
CREATE INDEX IF NOT EXISTS idx_review_report_restaurant ON review_report(restaurant_id);
CREATE INDEX IF NOT EXISTS idx_review_report_review ON review_report(review_id);

CREATE OR REPLACE FUNCTION update_review_report_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_review_report_updated_at ON review_report;
CREATE TRIGGER trg_review_report_updated_at
BEFORE UPDATE ON review_report
FOR EACH ROW
EXECUTE FUNCTION update_review_report_updated_at();


CREATE TABLE IF NOT EXISTS review_report_evidence (
    evidence_id SERIAL PRIMARY KEY,
    report_id INTEGER NOT NULL REFERENCES review_report(report_id) ON DELETE CASCADE,
    file_url VARCHAR(500) NOT NULL,
    content_type VARCHAR(100),
    file_size BIGINT,
    sort_order INTEGER,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_review_report_evidence_report ON review_report_evidence(report_id);

CREATE OR REPLACE FUNCTION update_review_report_evidence_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_review_report_evidence_updated_at ON review_report_evidence;
CREATE TRIGGER trg_review_report_evidence_updated_at
BEFORE UPDATE ON review_report_evidence
FOR EACH ROW
EXECUTE FUNCTION update_review_report_evidence_updated_at();

