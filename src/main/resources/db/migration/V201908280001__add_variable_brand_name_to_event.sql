ALTER TABLE event ADD brand VARCHAR(255);

UPDATE event e SET e.brand = CONCAT("BreakOut ", YEAR(e.date)) where e.brand is null;