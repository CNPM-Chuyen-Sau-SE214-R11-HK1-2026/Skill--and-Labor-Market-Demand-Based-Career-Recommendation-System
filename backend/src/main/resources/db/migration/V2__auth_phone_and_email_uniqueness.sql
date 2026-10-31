-- W1-T5: persist optional phone from the agreed auth API; leave V1 checksum unchanged.
ALTER TABLE users ADD COLUMN phone VARCHAR(20);

-- Registration and login treat email case-insensitively; also enforce this under concurrent writes.
-- Existing case-only duplicate accounts must be resolved explicitly before migrating.
CREATE UNIQUE INDEX uq_users_email_lower ON users (lower(email));
