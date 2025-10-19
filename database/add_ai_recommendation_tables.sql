-- AI Recommendation Engine Database Schema
-- File: database/add_ai_recommendation_tables.sql

-- 1. User Preferences Table
CREATE TABLE IF NOT EXISTS user_preferences (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    
    -- Cuisine preferences (JSON array)
    cuisine_preferences JSONB DEFAULT '[]'::jsonb,
    
    -- Price range preferences
    price_range JSONB DEFAULT '{"min": 0, "max": 1000000}'::jsonb,
    
    -- Location preferences
    location_preferences JSONB DEFAULT '{"max_distance": 10, "districts": []}'::jsonb,
    
    -- Dining occasion preferences
    dining_occasion JSONB DEFAULT '[]'::jsonb,
    
    -- Dietary restrictions
    dietary_restrictions JSONB DEFAULT '[]'::jsonb,
    
    -- AI learning data
    favorite_restaurants JSONB DEFAULT '[]'::jsonb,
    disliked_restaurants JSONB DEFAULT '[]'::jsonb,
    booking_patterns JSONB DEFAULT '{}'::jsonb,
    
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now(),
    
    CONSTRAINT unique_user_preferences UNIQUE (user_id)
);

-- 2. AI Recommendations Table
CREATE TABLE IF NOT EXISTS ai_recommendations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES users(id) ON DELETE SET NULL,
    session_id VARCHAR(100), -- For anonymous users
    
    -- Query information
    query_text TEXT NOT NULL,
    query_type VARCHAR(50) DEFAULT 'search', -- 'search', 'chat', 'filter'
    source VARCHAR(50) DEFAULT 'web', -- 'web', 'mobile', 'api'
    
    -- AI analysis results
    intent_analysis JSONB DEFAULT '{}'::jsonb,
    extracted_keywords JSONB DEFAULT '[]'::jsonb,
    
    -- Recommendations
    recommendations JSONB NOT NULL DEFAULT '[]'::jsonb,
    confidence_score DECIMAL(3,2) DEFAULT 0.0, -- 0.00 to 1.00
    
    -- Performance metrics
    response_time_ms INTEGER DEFAULT 0,
    api_cost_usd DECIMAL(10,4) DEFAULT 0.0,
    
    created_at TIMESTAMPTZ DEFAULT now()
);

-- 3. AI Interactions Table (for learning)
CREATE TABLE IF NOT EXISTS ai_interactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES users(id) ON DELETE SET NULL,
    session_id VARCHAR(100),
    
    -- Interaction details
    interaction_type VARCHAR(50) NOT NULL, -- 'search', 'chat', 'filter', 'booking'
    query_text TEXT,
    response_text TEXT,
    
    -- Restaurant context
    restaurant_id INTEGER REFERENCES restaurant_profile(restaurant_id) ON DELETE SET NULL,
    restaurant_name VARCHAR(255),
    
    -- User actions
    action_taken VARCHAR(50), -- 'viewed', 'booked', 'saved', 'ignored'
    action_timestamp TIMESTAMPTZ,
    
    -- AI performance
    ai_model_used VARCHAR(100),
    tokens_used INTEGER DEFAULT 0,
    cost_usd DECIMAL(10,4) DEFAULT 0.0,
    
    created_at TIMESTAMPTZ DEFAULT now()
);

-- 4. AI Analytics Table
CREATE TABLE IF NOT EXISTS ai_analytics (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    
    -- Time period
    date DATE NOT NULL,
    hour INTEGER NOT NULL, -- 0-23
    
    -- Metrics
    total_queries INTEGER DEFAULT 0,
    successful_queries INTEGER DEFAULT 0,
    failed_queries INTEGER DEFAULT 0,
    
    -- Performance metrics
    avg_response_time_ms DECIMAL(10,2) DEFAULT 0.0,
    total_tokens_used INTEGER DEFAULT 0,
    total_cost_usd DECIMAL(10,4) DEFAULT 0.0,
    
    -- User engagement
    unique_users INTEGER DEFAULT 0,
    bookings_generated INTEGER DEFAULT 0,
    conversion_rate DECIMAL(5,2) DEFAULT 0.0,
    
    -- Popular queries
    top_queries JSONB DEFAULT '[]'::jsonb,
    top_cuisines JSONB DEFAULT '[]'::jsonb,
    
    created_at TIMESTAMPTZ DEFAULT now(),
    
    CONSTRAINT unique_ai_analytics_hour UNIQUE (date, hour)
);

-- 5. AI Configuration Table
CREATE TABLE IF NOT EXISTS ai_configuration (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    
    -- Configuration keys
    config_key VARCHAR(100) NOT NULL UNIQUE,
    config_value JSONB NOT NULL,
    config_type VARCHAR(50) DEFAULT 'string', -- 'string', 'number', 'boolean', 'json'
    
    -- Metadata
    description TEXT,
    is_active BOOLEAN DEFAULT true,
    updated_by VARCHAR(100),
    
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now()
);

-- Indexes for performance
CREATE INDEX IF NOT EXISTS idx_user_preferences_user_id ON user_preferences(user_id);
CREATE INDEX IF NOT EXISTS idx_ai_recommendations_user_id ON ai_recommendations(user_id);
CREATE INDEX IF NOT EXISTS idx_ai_recommendations_session_id ON ai_recommendations(session_id);
CREATE INDEX IF NOT EXISTS idx_ai_recommendations_created_at ON ai_recommendations(created_at);
CREATE INDEX IF NOT EXISTS idx_ai_interactions_user_id ON ai_interactions(user_id);
CREATE INDEX IF NOT EXISTS idx_ai_interactions_session_id ON ai_interactions(session_id);
CREATE INDEX IF NOT EXISTS idx_ai_interactions_created_at ON ai_interactions(created_at);
CREATE INDEX IF NOT EXISTS idx_ai_interactions_restaurant_id ON ai_interactions(restaurant_id);
CREATE INDEX IF NOT EXISTS idx_ai_analytics_date_hour ON ai_analytics(date, hour);

-- Insert default AI configuration
INSERT INTO ai_configuration (config_key, config_value, config_type, description) VALUES
('openai_model', '"gpt-4"', 'string', 'Default OpenAI model to use'),
('max_tokens', '1000', 'number', 'Maximum tokens per request'),
('temperature', '0.7', 'number', 'AI creativity level (0.0-1.0)'),
('max_recommendations', '5', 'number', 'Maximum recommendations per query'),
('cache_duration_minutes', '30', 'number', 'Cache duration for similar queries'),
('rate_limit_per_user', '50', 'number', 'Max AI queries per user per hour'),
('rate_limit_per_hour', '1000', 'number', 'Max AI queries per hour globally'),
('enable_learning', 'true', 'boolean', 'Enable AI learning from user interactions'),
('enable_analytics', 'true', 'boolean', 'Enable AI analytics tracking')
ON CONFLICT (config_key) DO NOTHING;

-- Insert sample user preferences for testing
INSERT INTO user_preferences (user_id, cuisine_preferences, price_range, location_preferences, dining_occasion) 
SELECT 
    u.id,
    '["vietnamese", "japanese", "italian"]'::jsonb,
    '{"min": 100000, "max": 500000}'::jsonb,
    '{"max_distance": 5, "districts": ["q1", "q3"]}'::jsonb,
    '["date", "business", "family"]'::jsonb
FROM users u 
WHERE u.role = 'customer' 
LIMIT 5
ON CONFLICT (user_id) DO NOTHING;

-- Create triggers for updated_at
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = now();
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_user_preferences_updated_at 
    BEFORE UPDATE ON user_preferences 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_ai_configuration_updated_at 
    BEFORE UPDATE ON ai_configuration 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Comments for documentation
COMMENT ON TABLE user_preferences IS 'Stores user preferences for AI recommendations';
COMMENT ON TABLE ai_recommendations IS 'Stores AI recommendation results and metadata';
COMMENT ON TABLE ai_interactions IS 'Tracks user interactions with AI for learning';
COMMENT ON TABLE ai_analytics IS 'Daily/hourly analytics for AI performance';
COMMENT ON TABLE ai_configuration IS 'Configuration settings for AI features';

COMMENT ON COLUMN user_preferences.cuisine_preferences IS 'Array of preferred cuisine types';
COMMENT ON COLUMN user_preferences.price_range IS 'Preferred price range with min/max';
COMMENT ON COLUMN user_preferences.location_preferences IS 'Location preferences including max distance and districts';
COMMENT ON COLUMN ai_recommendations.confidence_score IS 'AI confidence in recommendations (0.0-1.0)';
COMMENT ON COLUMN ai_interactions.action_taken IS 'User action: viewed, booked, saved, ignored';
COMMENT ON COLUMN ai_analytics.conversion_rate IS 'Percentage of queries that resulted in bookings';
