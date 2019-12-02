ALTER TABLE event ADD bank VARCHAR(255);

UPDATE event e SET e.bank = "Fidor Bank" where e.bank is null;

ALTER TABLE event ADD bic VARCHAR(255);

UPDATE event e SET e.bic = "FDDODEMMXXX" where e.bic is null;