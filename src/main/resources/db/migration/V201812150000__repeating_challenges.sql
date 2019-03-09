-- Add columns
alter table challenge add column `maximum_count` INT DEFAULT 1;
alter table challenge add column `fulfilled_count` INT DEFAULT 0;

-- Set fulfilled count properly
update challenge set fulfilled_count = 1 where status = 3;

-- Update ordinal values of status
-- Accepted -> Proposed
update challenge set status = 0 where status = 1;

-- Rejected index update
update challenge set status = 1 where status = 2;

-- With Proof index update
update challenge set status = 2 where status = 3;

-- Proof Accepted -> With_Proof
update challenge set status = 2 where status = 4;

-- Proof Rejected -> Proposed
update challenge set status = 0 where status = 5;

-- Withdrawn Index Update
update challenge set status = 3 where status = 6;
