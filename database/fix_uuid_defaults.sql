-- Fix UUID defaults for existing tables
-- File: database/fix_uuid_defaults.sql

-- Enable pgcrypto extension if not exists
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- Fix user_preferences table
ALTER TABLE user_preferences ALTER COLUMN id SET DEFAULT gen_random_uuid();
UPDATE user_preferences SET id = gen_random_uuid() WHERE id IS NULL;

-- Fix ai_recommendations table  
ALTER TABLE ai_recommendations ALTER COLUMN id SET DEFAULT gen_random_uuid();
UPDATE ai_recommendations SET id = gen_random_uuid() WHERE id IS NULL;

-- Fix ai_interactions table
ALTER TABLE ai_interactions ALTER COLUMN id SET DEFAULT gen_random_uuid();
UPDATE ai_interactions SET id = gen_random_uuid() WHERE id IS NULL;

-- Fix ai_analytics table
ALTER TABLE ai_analytics ALTER COLUMN id SET DEFAULT gen_random_uuid();
UPDATE ai_analytics SET id = gen_random_uuid() WHERE id IS NULL;

-- Fix ai_configuration table
ALTER TABLE ai_configuration ALTER COLUMN id SET DEFAULT gen_random_uuid();
UPDATE ai_configuration SET id = gen_random_uuid() WHERE id IS NULL;

-- Fix ai_recommendation_diversity table
ALTER TABLE ai_recommendation_diversity ALTER COLUMN id SET DEFAULT gen_random_uuid();
UPDATE ai_recommendation_diversity SET id = gen_random_uuid() WHERE id IS NULL;

-- Fix external_context table
ALTER TABLE external_context ALTER COLUMN id SET DEFAULT gen_random_uuid();
UPDATE external_context SET id = gen_random_uuid() WHERE id IS NULL;

-- Fix restaurant_availability table
ALTER TABLE restaurant_availability ALTER COLUMN id SET DEFAULT gen_random_uuid();
UPDATE restaurant_availability SET id = gen_random_uuid() WHERE id IS NULL;

-- Fix ai_feedback table
ALTER TABLE ai_feedback ALTER COLUMN id SET DEFAULT gen_random_uuid();
UPDATE ai_feedback SET id = gen_random_uuid() WHERE id IS NULL;

-- Add generated columns for faster JSONB queries (optional optimization)
ALTER TABLE user_preferences
  ADD COLUMN IF NOT EXISTS cuisine_arr text[]
  GENERATED ALWAYS AS (
    ARRAY(SELECT value::text FROM jsonb_array_elements(cuisine_preferences))
  ) STORED;

ALTER TABLE user_preferences
  ADD COLUMN IF NOT EXISTS dietary_arr text[]
  GENERATED ALWAYS AS (
    ARRAY(SELECT value::text FROM jsonb_array_elements(dietary_restrictions))
  ) STORED;

-- Create GIN indexes for generated columns
CREATE INDEX IF NOT EXISTS idx_upref_cuisine_arr_gin ON user_preferences USING gin (cuisine_arr);
CREATE INDEX IF NOT EXISTS idx_upref_dietary_arr_gin ON user_preferences USING gin (dietary_arr);

-- Verify fixes
SELECT 'user_preferences' as table_name, COUNT(*) as total_rows, 
       COUNT(CASE WHEN id IS NULL THEN 1 END) as null_ids
FROM user_preferences
UNION ALL
SELECT 'ai_recommendations', COUNT(*), COUNT(CASE WHEN id IS NULL THEN 1 END)
FROM ai_recommendations
UNION ALL
SELECT 'ai_interactions', COUNT(*), COUNT(CASE WHEN id IS NULL THEN 1 END)
FROM ai_interactions;
