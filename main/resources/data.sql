-- Fictional demonstration records. INSERT ... ON DUPLICATE KEY UPDATE keeps each startup idempotent.
INSERT INTO Patient (FullName, IDPassportNumber, DateOfBirth, Gender, ContactDetails) VALUES
('Amahle Nkosi', 'IC-DEMO-0001', DATE_SUB(CURDATE(), INTERVAL 10 MONTH), 'Female', '+27 10 000 0101'),
('Kabelo Dlamini', 'IC-DEMO-0002', DATE_SUB(CURDATE(), INTERVAL 3 YEAR), 'Male', '+27 10 000 0102'),
('Refilwe Mokoena', 'IC-DEMO-0003', DATE_SUB(CURDATE(), INTERVAL 8 YEAR), 'Female', '+27 10 000 0103'),
('Sizwe Ndlovu', 'IC-DEMO-0004', DATE_SUB(CURDATE(), INTERVAL 16 YEAR), 'Male', '+27 10 000 0104'),
('Aisha Naidoo', 'IC-DEMO-0005', DATE_SUB(CURDATE(), INTERVAL 25 YEAR), 'Female', '+27 10 000 0105'),
('Pieter Botha', 'IC-DEMO-0006', DATE_SUB(CURDATE(), INTERVAL 42 YEAR), 'Male', '+27 10 000 0106'),
('Nomsa Khumalo', 'IC-DEMO-0007', DATE_SUB(CURDATE(), INTERVAL 57 YEAR), 'Female', '+27 10 000 0107'),
('Thandi Mahlangu', 'IC-DEMO-0008', DATE_SUB(CURDATE(), INTERVAL 68 YEAR), 'Female', '+27 10 000 0108'),
('Johan van der Merwe', 'IC-DEMO-0009', DATE_SUB(CURDATE(), INTERVAL 83 YEAR), 'Male', '+27 10 000 0109'),
('Lindiwe Maseko', 'IC-DEMO-0010', DATE_SUB(CURDATE(), INTERVAL 34 YEAR), 'Female', '+27 10 000 0110')
AS new_rows ON DUPLICATE KEY UPDATE
    FullName= new_rows.FullName, DateOfBirth=new_rows.DateOfBirth,
    Gender=new_rows.Gender, ContactDetails=new_rows.ContactDetails;

INSERT INTO VaccineInventory (BatchLotNumber, VaccineName, QuantityReceived, CurrentStockQuantity, Manufacturer, ManufactureDate, ExpiryDate) VALUES
('IC-DEMO-BCG-01', 'BCG', 60, 24, 'Serum Institute of India', DATE_SUB(CURDATE(), INTERVAL 300 DAY), DATE_ADD(CURDATE(), INTERVAL 180 DAY)),
('IC-DEMO-HBV-01', 'Hepatitis B', 60, 12, 'GSK', DATE_SUB(CURDATE(), INTERVAL 340 DAY), DATE_ADD(CURDATE(), INTERVAL 14 DAY)),
('IC-DEMO-OPV-01', 'Polio (OPV)', 80, 5, 'Bio Farma', DATE_SUB(CURDATE(), INTERVAL 200 DAY), DATE_ADD(CURDATE(), INTERVAL 160 DAY)),
('IC-DEMO-MMR-01', 'MMR', 50, 18, 'Merck', DATE_SUB(CURDATE(), INTERVAL 250 DAY), DATE_ADD(CURDATE(), INTERVAL 65 DAY)),
('IC-DEMO-C19-01', 'COVID-19', 70, 7, 'Pfizer', DATE_SUB(CURDATE(), INTERVAL 100 DAY), DATE_ADD(CURDATE(), INTERVAL 270 DAY)),
('IC-DEMO-FLU-01', 'Influenza', 100, 14, 'Seqirus', DATE_SUB(CURDATE(), INTERVAL 270 DAY), DATE_ADD(CURDATE(), INTERVAL 22 DAY)),
('IC-DEMO-TD-01', 'Tetanus (Td)', 90, 42, 'Sanofi', DATE_SUB(CURDATE(), INTERVAL 180 DAY), DATE_ADD(CURDATE(), INTERVAL 380 DAY))
AS new_rows ON DUPLICATE KEY UPDATE
    VaccineName=new_rows.VaccineName, QuantityReceived=new_rows.QuantityReceived,
    Manufacturer=new_rows.Manufacturer,
    ManufactureDate=new_rows.ManufactureDate, ExpiryDate=new_rows.ExpiryDate;
