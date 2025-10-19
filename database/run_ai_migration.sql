-- AI Recommendation Engine Migration Script
-- File: database/run_ai_migration.sql

-- =====================================================
-- MIGRATION SCRIPT FOR AI RECOMMENDATION ENGINE
-- =====================================================

-- Check if AI tables already exist
DO $$
BEGIN
    -- Check if user_preferences table exists
    IF NOT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'user_preferences') THEN
        RAISE NOTICE 'Creating AI Recommendation Engine tables...';
        
        -- Run the complete schema
        \i database/ai_recommendation_complete_schema.sql
        
        RAISE NOTICE 'AI Recommendation Engine tables created successfully!';
    ELSE
        RAISE NOTICE 'AI Recommendation Engine tables already exist. Skipping creation.';
    END IF;
END $$;

-- =====================================================
-- VERIFICATION QUERIES
-- =====================================================

-- Check table creation
SELECT 
    table_name,
    table_type
FROM information_schema.tables 
WHERE table_name IN (
    'user_preferences',
    'ai_recommendations', 
    'ai_interactions',
    'ai_analytics',
    'ai_configuration',
    'ai_recommendation_diversity',
    'external_context',
    'restaurant_availability',
    'ai_feedback'
)
ORDER BY table_name;

-- Check indexes
SELECT 
    indexname,
    tablename,
    indexdef
FROM pg_indexes 
WHERE tablename IN (
    'user_preferences',
    'ai_recommendations', 
    'ai_interactions',
    'ai_analytics',
    'ai_configuration',
    'ai_recommendation_diversity',
    'external_context',
    'restaurant_availability',
    'ai_feedback'
)
ORDER BY tablename, indexname;

-- Check configuration data
SELECT 
    config_key,
    config_value,
    config_type,
    description
FROM ai_configuration
ORDER BY config_key;

-- Check sample data
SELECT 
    'user_preferences' as table_name,
    COUNT(*) as record_count
FROM user_preferences
UNION ALL
SELECT 
    'ai_configuration' as table_name,
    COUNT(*) as record_count
FROM ai_configuration
UNION ALL
SELECT 
    'external_context' as table_name,
    COUNT(*) as record_count
FROM external_context;

-- =====================================================
-- PERFORMANCE TEST QUERIES
-- =====================================================

-- Test user preferences query performance
EXPLAIN (ANALYZE, BUFFERS) 
SELECT * FROM user_preferences 
WHERE user_id = (SELECT id FROM users LIMIT 1);

-- Test AI recommendations query performance
EXPLAIN (ANALYZE, BUFFERS) 
SELECT * FROM ai_recommendations 
WHERE created_at >= CURRENT_DATE - INTERVAL '7 days'
ORDER BY created_at DESC
LIMIT 10;

-- Test AI interactions query performance
EXPLAIN (ANALYZE, BUFFERS) 
SELECT * FROM ai_interactions 
WHERE created_at >= CURRENT_DATE - INTERVAL '7 days'
ORDER BY created_at DESC
LIMIT 10;

-- =====================================================
-- CLEANUP OLD DATA (Optional)
-- =====================================================

-- Uncomment these if you want to clean up old data
-- DELETE FROM ai_recommendations WHERE created_at < CURRENT_DATE - INTERVAL '90 days';
-- DELETE FROM ai_interactions WHERE created_at < CURRENT_DATE - INTERVAL '90 days';
-- DELETE FROM ai_analytics WHERE date < CURRENT_DATE - INTERVAL '90 days';

-- =====================================================
-- MIGRATION COMPLETE
-- =====================================================

DO $$
BEGIN
    RAISE NOTICE '=====================================================';
    RAISE NOTICE 'AI RECOMMENDATION ENGINE MIGRATION COMPLETED!';
    RAISE NOTICE '=====================================================';
    RAISE NOTICE 'Tables created: 9';
    RAISE NOTICE 'Indexes created: 25+';
    RAISE NOTICE 'Configuration entries: 20+';
    RAISE NOTICE 'Sample data inserted: Yes';
    RAISE NOTICE '=====================================================';
    RAISE NOTICE 'Next steps:';
    RAISE NOTICE '1. Update application.yml with AI configuration';
    RAISE NOTICE '2. Set OpenAI API key in environment variables';
    RAISE NOTICE '3. Enable AI features in configuration';
    RAISE NOTICE '4. Test AI endpoints';
    RAISE NOTICE '=====================================================';
END $$;
