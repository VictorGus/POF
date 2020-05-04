SELECT 'CREATE DATABASE testbase' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'testbase')
