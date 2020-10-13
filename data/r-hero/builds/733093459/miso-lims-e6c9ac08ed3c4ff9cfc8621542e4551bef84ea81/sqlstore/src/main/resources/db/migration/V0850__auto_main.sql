-- make_ld_and_pools_distributable
ALTER TABLE LibraryDilution ADD COLUMN distributed BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE LibraryDilution ADD COLUMN distributionDate DATE DEFAULT NULL;
ALTER TABLE LibraryDilution ADD COLUMN distributionRecipient VARCHAR(250) DEFAULT NULL;

ALTER TABLE Pool ADD COLUMN distributed BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE Pool ADD COLUMN distributionDate DATE DEFAULT NULL;
ALTER TABLE Pool ADD COLUMN distributionRecipient VARCHAR(250) DEFAULT NULL;

