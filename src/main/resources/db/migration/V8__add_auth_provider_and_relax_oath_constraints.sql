-- 1. Add a new column `auth_provider` to the `sys_user` table to store the authentication provider information.
ALTER TABLE sys_user ADD COLUMN auth_provider VARCHAR(255);

-- 2. Update old data to set the `auth_provider` to 'local' for existing users where it is currently NULL.
UPDATE sys_user SET auth_provider = 'local' WHERE auth_provider IS NULL;

-- 3. After ensuring that there are no Null values, we can enable the NOT NULL constraint
ALTER TABLE sys_user ALTER COLUMN auth_provider SET NOT NULL;

-- 4. Remove NOT NULL constraint for password and phone number to support SSO (Google)
-- PostgreSQL use DROP NOT NULL command to loosen columns
ALTER TABLE sys_user ALTER COLUMN password DROP NOT NULL;
ALTER TABLE sys_user ALTER COLUMN phone DROP NOT NULL;