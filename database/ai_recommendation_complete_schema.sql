-- AI Recommendation Engine - Complete Database Schema
-- File: database/ai_recommendation_complete_schema.sql

-- =====================================================
-- 1. USER PREFERENCES TABLE (Enhanced)
-- =====================================================
CREATE TABLE IF NOT EXISTS user_preferences (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    
    -- Basic Preferences
    cuisine_preferences JSONB DEFAULT '[]'::jsonb,
    price_range JSONB DEFAULT '{"min": 0, "max": 1000000}'::jsonb,
    location_preferences JSONB DEFAULT '{"max_distance": 10, "districts": []}'::jsonb,
    dining_occasion JSONB DEFAULT '[]'::jsonb,
    dietary_restrictions JSONB DEFAULT '[]'::jsonb,
    
    -- AI Learning Data
    favorite_restaurants JSONB DEFAULT '[]'::jsonb,
    disliked_restaurants JSONB DEFAULT '[]'::jsonb,
    booking_patterns JSONB DEFAULT '{}'::jsonb,
    
    -- Advanced Preferences
    preferred_ambiance JSONB DEFAULT '[]'::jsonb, -- ["romantic", "casual", "family-friendly"]
    preferred_cuisine_styles JSONB DEFAULT '[]'::jsonb, -- ["fine_dining", "street_food", "buffet"]
    special_requirements JSONB DEFAULT '[]'::jsonb, -- ["wheelchair_accessible", "parking", "wifi"]
    
    -- Learning Metrics
    total_interactions INTEGER DEFAULT 0,
    successful_bookings INTEGER DEFAULT 0,
    last_updated_preferences TIMESTAMPTZ DEFAULT now(),
    
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now(),
    
    CONSTRAINT unique_user_preferences UNIQUE (user_id)
);

-- =====================================================
-- 2. AI RECOMMENDATIONS TABLE (Enhanced)
-- =====================================================
CREATE TABLE IF NOT EXISTS ai_recommendations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES users(id) ON DELETE SET NULL,
    session_id VARCHAR(100),
    
    -- Query Information
    query_text TEXT NOT NULL,
    query_type VARCHAR(50) DEFAULT 'search',
    source VARCHAR(50) DEFAULT 'web',
    language VARCHAR(10) DEFAULT 'vi',
    
    -- AI Analysis Results
    intent_analysis JSONB DEFAULT '{}'::jsonb,
    extracted_keywords JSONB DEFAULT '[]'::jsonb,
    context_data JSONB DEFAULT '{}'::jsonb, -- weather, events, time
    
    -- Recommendations
    recommendations JSONB NOT NULL DEFAULT '[]'::jsonb,
    confidence_score DECIMAL(3,2) DEFAULT 0.0,
    diversity_score DECIMAL(3,2) DEFAULT 0.0, -- How diverse are recommendations
    
    -- Performance Metrics
    response_time_ms INTEGER DEFAULT 0,
    api_cost_usd DECIMAL(10,4) DEFAULT 0.0,
    tokens_used INTEGER DEFAULT 0,
    model_used VARCHAR(100) DEFAULT 'gpt-4',
    
    -- User Feedback
    user_feedback VARCHAR(50), -- 'helpful', 'not_helpful', 'irrelevant'
    feedback_timestamp TIMESTAMPTZ,
    
    created_at TIMESTAMPTZ DEFAULT now()
);

-- =====================================================
-- 3. AI INTERACTIONS TABLE (Enhanced)
-- =====================================================
CREATE TABLE IF NOT EXISTS ai_interactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES users(id) ON DELETE SET NULL,
    session_id VARCHAR(100),
    
    -- Interaction Details
    interaction_type VARCHAR(50) NOT NULL,
    query_text TEXT,
    response_text TEXT,
    
    -- Restaurant Context
    restaurant_id INTEGER REFERENCES restaurant_profile(restaurant_id) ON DELETE SET NULL,
    restaurant_name VARCHAR(255),
    
    -- User Actions
    action_taken VARCHAR(50),
    action_timestamp TIMESTAMPTZ,
    action_context JSONB DEFAULT '{}'::jsonb, -- Additional context about action
    
    -- AI Performance
    ai_model_used VARCHAR(100),
    tokens_used INTEGER DEFAULT 0,
    cost_usd DECIMAL(10,4) DEFAULT 0.0,
    
    -- Session Context
    session_context JSONB DEFAULT '{}'::jsonb, -- User's journey in this session
    
    created_at TIMESTAMPTZ DEFAULT now()
);

-- =====================================================
-- 4. AI ANALYTICS TABLE (Enhanced)
-- =====================================================
CREATE TABLE IF NOT EXISTS ai_analytics (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    
    -- Time Period
    date DATE NOT NULL,
    hour INTEGER NOT NULL,
    
    -- Basic Metrics
    total_queries INTEGER DEFAULT 0,
    successful_queries INTEGER DEFAULT 0,
    failed_queries INTEGER DEFAULT 0,
    
    -- Performance Metrics
    avg_response_time_ms DECIMAL(10,2) DEFAULT 0.0,
    total_tokens_used INTEGER DEFAULT 0,
    total_cost_usd DECIMAL(10,4) DEFAULT 0.0,
    
    -- User Engagement
    unique_users INTEGER DEFAULT 0,
    bookings_generated INTEGER DEFAULT 0,
    conversion_rate DECIMAL(5,2) DEFAULT 0.0,
    
    -- Quality Metrics
    avg_confidence_score DECIMAL(3,2) DEFAULT 0.0,
    avg_diversity_score DECIMAL(3,2) DEFAULT 0.0,
    user_satisfaction_score DECIMAL(3,2) DEFAULT 0.0,
    
    -- Popular Data
    top_queries JSONB DEFAULT '[]'::jsonb,
    top_cuisines JSONB DEFAULT '[]'::jsonb,
    top_restaurants JSONB DEFAULT '[]'::jsonb,
    
    -- Error Analysis
    error_types JSONB DEFAULT '[]'::jsonb,
    error_frequency INTEGER DEFAULT 0,
    
    created_at TIMESTAMPTZ DEFAULT now(),
    
    CONSTRAINT unique_ai_analytics_hour UNIQUE (date, hour)
);

-- =====================================================
-- 5. AI CONFIGURATION TABLE (Enhanced)
-- =====================================================
CREATE TABLE IF NOT EXISTS ai_configuration (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    
    -- Configuration
    config_key VARCHAR(100) NOT NULL UNIQUE,
    config_value JSONB NOT NULL,
    config_type VARCHAR(50) DEFAULT 'string',
    
    -- Metadata
    description TEXT,
    category VARCHAR(50) DEFAULT 'general', -- 'model', 'rate_limit', 'feature_flag'
    is_active BOOLEAN DEFAULT true,
    updated_by VARCHAR(100),
    
    -- Versioning
    version INTEGER DEFAULT 1,
    previous_value JSONB,
    
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now()
);

-- =====================================================
-- 6. RECOMMENDATION DIVERSITY TABLE (NEW)
-- =====================================================
CREATE TABLE IF NOT EXISTS ai_recommendation_diversity (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    date DATE NOT NULL,
    
    -- Diversity Metrics
    restaurant_id INTEGER REFERENCES restaurant_profile(restaurant_id) ON DELETE CASCADE,
    recommendation_count INTEGER DEFAULT 0,
    last_recommended_at TIMESTAMPTZ,
    
    -- Diversity Scores
    cuisine_diversity_score DECIMAL(3,2) DEFAULT 0.0,
    price_diversity_score DECIMAL(3,2) DEFAULT 0.0,
    location_diversity_score DECIMAL(3,2) DEFAULT 0.0,
    
    -- User Response
    user_response VARCHAR(50), -- 'accepted', 'ignored', 'booked'
    
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now(),
    
    CONSTRAINT unique_user_restaurant_date UNIQUE (user_id, restaurant_id, date)
);

-- =====================================================
-- 7. EXTERNAL CONTEXT TABLE (NEW)
-- =====================================================
CREATE TABLE IF NOT EXISTS external_context (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    
    -- Time Period
    date DATE NOT NULL,
    hour INTEGER NOT NULL,
    
    -- Weather Data
    weather_data JSONB DEFAULT '{}'::jsonb,
    
    -- Events Data
    events_data JSONB DEFAULT '{}'::jsonb,
    
    -- Traffic Data
    traffic_data JSONB DEFAULT '{}'::jsonb,
    
    -- Business Context
    business_context JSONB DEFAULT '{}'::jsonb, -- holidays, special days
    
    -- Data Source
    data_source VARCHAR(100) DEFAULT 'api',
    data_quality_score DECIMAL(3,2) DEFAULT 1.0,
    
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now(),
    
    CONSTRAINT unique_context_hour UNIQUE (date, hour)
);

-- =====================================================
-- 8. RESTAURANT AVAILABILITY TABLE (NEW)
-- =====================================================
CREATE TABLE IF NOT EXISTS restaurant_availability (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    restaurant_id INTEGER REFERENCES restaurant_profile(restaurant_id) ON DELETE CASCADE,
    
    -- Time Period
    date DATE NOT NULL,
    hour INTEGER NOT NULL,
    
    -- Availability Data
    available_tables INTEGER DEFAULT 0,
    total_tables INTEGER DEFAULT 0,
    reserved_tables INTEGER DEFAULT 0,
    
    -- Capacity Data
    max_capacity INTEGER DEFAULT 0,
    current_occupancy INTEGER DEFAULT 0,
    
    -- Status
    status VARCHAR(50) DEFAULT 'open', -- 'open', 'closed', 'full', 'maintenance'
    
    -- Last Update
    last_updated TIMESTAMPTZ DEFAULT now(),
    data_source VARCHAR(100) DEFAULT 'manual',
    
    CONSTRAINT unique_restaurant_hour UNIQUE (restaurant_id, date, hour)
);

-- =====================================================
-- 9. AI FEEDBACK TABLE (NEW)
-- =====================================================
CREATE TABLE IF NOT EXISTS ai_feedback (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES users(id) ON DELETE SET NULL,
    session_id VARCHAR(100),
    
    -- Feedback Context
    recommendation_id UUID REFERENCES ai_recommendations(id) ON DELETE CASCADE,
    interaction_id UUID REFERENCES ai_interactions(id) ON DELETE CASCADE,
    
    -- Feedback Data
    feedback_type VARCHAR(50) NOT NULL, -- 'rating', 'comment', 'correction'
    feedback_value JSONB NOT NULL,
    
    -- Feedback Details
    rating INTEGER CHECK (rating >= 1 AND rating <= 5),
    comment TEXT,
    correction_data JSONB DEFAULT '{}'::jsonb,
    
    -- Context
    feedback_context JSONB DEFAULT '{}'::jsonb,
    
    created_at TIMESTAMPTZ DEFAULT now()
);

-- =====================================================
-- INDEXES FOR PERFORMANCE
-- =====================================================

-- User Preferences Indexes
CREATE INDEX IF NOT EXISTS idx_user_preferences_user_id ON user_preferences(user_id);
CREATE INDEX IF NOT EXISTS idx_user_preferences_updated_at ON user_preferences(updated_at);

-- AI Recommendations Indexes
CREATE INDEX IF NOT EXISTS idx_ai_recommendations_user_id ON ai_recommendations(user_id);
CREATE INDEX IF NOT EXISTS idx_ai_recommendations_session_id ON ai_recommendations(session_id);
CREATE INDEX IF NOT EXISTS idx_ai_recommendations_created_at ON ai_recommendations(created_at);
CREATE INDEX IF NOT EXISTS idx_ai_recommendations_query_type ON ai_recommendations(query_type);
CREATE INDEX IF NOT EXISTS idx_ai_recommendations_confidence ON ai_recommendations(confidence_score);

-- AI Interactions Indexes
CREATE INDEX IF NOT EXISTS idx_ai_interactions_user_id ON ai_interactions(user_id);
CREATE INDEX IF NOT EXISTS idx_ai_interactions_session_id ON ai_interactions(session_id);
CREATE INDEX IF NOT EXISTS idx_ai_interactions_created_at ON ai_interactions(created_at);
CREATE INDEX IF NOT EXISTS idx_ai_interactions_restaurant_id ON ai_interactions(restaurant_id);
CREATE INDEX IF NOT EXISTS idx_ai_interactions_action_taken ON ai_interactions(action_taken);

-- AI Analytics Indexes
CREATE INDEX IF NOT EXISTS idx_ai_analytics_date_hour ON ai_analytics(date, hour);
CREATE INDEX IF NOT EXISTS idx_ai_analytics_date ON ai_analytics(date);

-- AI Configuration Indexes
CREATE INDEX IF NOT EXISTS idx_ai_configuration_key ON ai_configuration(config_key);
CREATE INDEX IF NOT EXISTS idx_ai_configuration_category ON ai_configuration(category);
CREATE INDEX IF NOT EXISTS idx_ai_configuration_active ON ai_configuration(is_active);

-- Recommendation Diversity Indexes
CREATE INDEX IF NOT EXISTS idx_ai_diversity_user_date ON ai_recommendation_diversity(user_id, date);
CREATE INDEX IF NOT EXISTS idx_ai_diversity_restaurant ON ai_recommendation_diversity(restaurant_id);

-- External Context Indexes
CREATE INDEX IF NOT EXISTS idx_external_context_date_hour ON external_context(date, hour);

-- Restaurant Availability Indexes
CREATE INDEX IF NOT EXISTS idx_restaurant_availability_restaurant ON restaurant_availability(restaurant_id);
CREATE INDEX IF NOT EXISTS idx_restaurant_availability_date_hour ON restaurant_availability(date, hour);
CREATE INDEX IF NOT EXISTS idx_restaurant_availability_status ON restaurant_availability(status);

-- AI Feedback Indexes
CREATE INDEX IF NOT EXISTS idx_ai_feedback_user_id ON ai_feedback(user_id);
CREATE INDEX IF NOT EXISTS idx_ai_feedback_recommendation_id ON ai_feedback(recommendation_id);
CREATE INDEX IF NOT EXISTS idx_ai_feedback_type ON ai_feedback(feedback_type);

-- =====================================================
-- TRIGGERS FOR UPDATED_AT
-- =====================================================
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = now();
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Apply triggers
CREATE TRIGGER update_user_preferences_updated_at 
    BEFORE UPDATE ON user_preferences 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_ai_configuration_updated_at 
    BEFORE UPDATE ON ai_configuration 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_ai_diversity_updated_at 
    BEFORE UPDATE ON ai_recommendation_diversity 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_external_context_updated_at 
    BEFORE UPDATE ON external_context 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- =====================================================
-- DEFAULT CONFIGURATION DATA
-- =====================================================
INSERT INTO ai_configuration (config_key, config_value, config_type, description, category) VALUES
-- Model Configuration
('openai_model', '"gpt-4"', 'string', 'Default OpenAI model to use', 'model'),
('max_tokens', '1000', 'number', 'Maximum tokens per request', 'model'),
('temperature', '0.7', 'number', 'AI creativity level (0.0-1.0)', 'model'),
('max_recommendations', '5', 'number', 'Maximum recommendations per query', 'model'),

-- Rate Limiting
('rate_limit_per_user', '50', 'number', 'Max AI queries per user per hour', 'rate_limit'),
('rate_limit_per_hour', '1000', 'number', 'Max AI queries per hour globally', 'rate_limit'),
('rate_limit_per_day', '10000', 'number', 'Max AI queries per day globally', 'rate_limit'),

-- Caching
('cache_duration_minutes', '30', 'number', 'Cache duration for similar queries', 'cache'),
('enable_query_cache', 'true', 'boolean', 'Enable query result caching', 'cache'),
('cache_similarity_threshold', '0.8', 'number', 'Similarity threshold for cache hits', 'cache'),

-- Learning
('enable_learning', 'true', 'boolean', 'Enable AI learning from user interactions', 'learning'),
('learning_update_frequency', '24', 'number', 'Hours between learning updates', 'learning'),
('min_interactions_for_learning', '5', 'number', 'Minimum interactions before learning', 'learning'),

-- Analytics
('enable_analytics', 'true', 'boolean', 'Enable AI analytics tracking', 'analytics'),
('analytics_retention_days', '90', 'number', 'Days to retain analytics data', 'analytics'),

-- Diversity
('enable_diversity_check', 'true', 'boolean', 'Enable recommendation diversity checking', 'diversity'),
('diversity_threshold', '0.7', 'number', 'Minimum diversity score threshold', 'diversity'),
('max_same_restaurant_per_day', '3', 'number', 'Max times same restaurant recommended per day', 'diversity'),

-- External Context
('enable_weather_context', 'true', 'boolean', 'Enable weather context in recommendations', 'context'),
('enable_event_context', 'true', 'boolean', 'Enable event context in recommendations', 'context'),
('enable_availability_check', 'true', 'boolean', 'Enable real-time availability checking', 'context'),

-- Quality Control
('min_confidence_threshold', '0.6', 'number', 'Minimum confidence for recommendations', 'quality'),
('enable_feedback_learning', 'true', 'boolean', 'Enable learning from user feedback', 'quality'),
('feedback_weight', '0.3', 'number', 'Weight of feedback in learning algorithm', 'quality')

ON CONFLICT (config_key) DO NOTHING;

-- =====================================================
-- SAMPLE DATA FOR TESTING
-- =====================================================

-- Insert sample user preferences
INSERT INTO user_preferences (user_id, cuisine_preferences, price_range, location_preferences, dining_occasion, preferred_ambiance) 
SELECT 
    u.id,
    '["vietnamese", "japanese", "italian"]'::jsonb,
    '{"min": 100000, "max": 500000}'::jsonb,
    '{"max_distance": 5, "districts": ["q1", "q3"]}'::jsonb,
    '["date", "business", "family"]'::jsonb,
    '["romantic", "casual"]'::jsonb
FROM users u 
WHERE u.role = 'customer' 
LIMIT 5
ON CONFLICT (user_id) DO NOTHING;

-- Insert sample external context
INSERT INTO external_context (date, hour, weather_data, events_data, traffic_data) VALUES
(CURRENT_DATE, EXTRACT(HOUR FROM now()), 
 '{"temperature": 25, "condition": "sunny", "humidity": 60}'::jsonb,
 '{"holiday": false, "special_event": false}'::jsonb,
 '{"congestion_level": "medium", "peak_hour": true}'::jsonb)
ON CONFLICT (date, hour) DO NOTHING;

-- =====================================================
-- COMMENTS FOR DOCUMENTATION
-- =====================================================
COMMENT ON TABLE user_preferences IS 'Enhanced user preferences with advanced learning capabilities';
COMMENT ON TABLE ai_recommendations IS 'AI recommendation results with quality metrics and feedback';
COMMENT ON TABLE ai_interactions IS 'Comprehensive user interaction tracking for learning';
COMMENT ON TABLE ai_analytics IS 'Detailed analytics with quality and diversity metrics';
COMMENT ON TABLE ai_configuration IS 'Flexible configuration system with versioning';
COMMENT ON TABLE ai_recommendation_diversity IS 'Tracks recommendation diversity to prevent bias';
COMMENT ON TABLE external_context IS 'External context data (weather, events, traffic)';
COMMENT ON TABLE restaurant_availability IS 'Real-time restaurant availability data';
COMMENT ON TABLE ai_feedback IS 'User feedback on AI recommendations';

-- Column comments
COMMENT ON COLUMN user_preferences.preferred_ambiance IS 'User preferred restaurant ambiance types';
COMMENT ON COLUMN ai_recommendations.diversity_score IS 'Score indicating how diverse the recommendations are';
COMMENT ON COLUMN ai_recommendations.user_feedback IS 'User feedback on recommendation quality';
COMMENT ON COLUMN ai_analytics.user_satisfaction_score IS 'Overall user satisfaction score';
COMMENT ON COLUMN ai_recommendation_diversity.cuisine_diversity_score IS 'Diversity score for cuisine types';
COMMENT ON COLUMN external_context.data_quality_score IS 'Quality score for external data (0.0-1.0)';
COMMENT ON COLUMN restaurant_availability.data_source IS 'Source of availability data (manual, api, integration)';
COMMENT ON COLUMN ai_feedback.correction_data IS 'Data corrections provided by user';
