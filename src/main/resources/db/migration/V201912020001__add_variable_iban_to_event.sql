ALTER TABLE event ADD iban VARCHAR(255);

UPDATE event e SET e.iban = "DE85 7002 2200 0020 2418 37" where e.iban is null;