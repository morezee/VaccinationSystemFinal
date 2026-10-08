CREATE TABLE IF NOT EXISTS Patient (
    PatientID INT AUTO_INCREMENT PRIMARY KEY,
    FullName VARCHAR(100) NOT NULL,
    IDPassportNumber VARCHAR(50) UNIQUE NOT NULL,
    DateOfBirth DATE NOT NULL,
    Gender VARCHAR(20),
    ContactDetails VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS user_account (
    userid BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(30) NOT NULL
);

CREATE TABLE IF NOT EXISTS UserProfile (
    UserID BIGINT PRIMARY KEY,
    FullName VARCHAR(100) NOT NULL,
    FOREIGN KEY (UserID) REFERENCES user_account(userid)
);

-- Legacy SQL foreign keys refer to this original account table. The app keeps this
-- mirror synchronized with Hibernate's user_account table.
CREATE TABLE IF NOT EXISTS UserAccount (
    UserID INT AUTO_INCREMENT PRIMARY KEY,
    Username VARCHAR(50) UNIQUE NOT NULL,
    Password VARCHAR(255) NOT NULL,
    Role VARCHAR(30) NOT NULL
);

CREATE TABLE IF NOT EXISTS DemoSeedStatus (
    SeedKey VARCHAR(60) PRIMARY KEY,
    SeededAt DATETIME NOT NULL
);

CREATE TABLE IF NOT EXISTS VaccineInventory (
    BatchLotNumber VARCHAR(50) PRIMARY KEY,
    VaccineName VARCHAR(100) NOT NULL,
    QuantityReceived INT NOT NULL,
    CurrentStockQuantity INT NOT NULL,
    Manufacturer VARCHAR(100),
    ManufactureDate DATE,
    ExpiryDate DATE
);

CREATE TABLE IF NOT EXISTS Vaccination (
    VaccinationID INT AUTO_INCREMENT PRIMARY KEY,
    PatientID INT NOT NULL,
    UserID INT NOT NULL,
    BatchLotNumber VARCHAR(50) NOT NULL,
    VaccineType VARCHAR(100) NOT NULL,
    DoseNumber INT NOT NULL,
    DateAdministered DATE NOT NULL,
    FOREIGN KEY (PatientID) REFERENCES Patient(PatientID),
    FOREIGN KEY (UserID) REFERENCES UserAccount(UserID),
    FOREIGN KEY (BatchLotNumber) REFERENCES VaccineInventory(BatchLotNumber)
);

CREATE TABLE IF NOT EXISTS AEFI (
    AEFIID INT AUTO_INCREMENT PRIMARY KEY,
    VaccinationID INT NOT NULL,
    AEFIInformation TEXT,
    FOREIGN KEY (VaccinationID) REFERENCES Vaccination(VaccinationID)
);

CREATE TABLE IF NOT EXISTS AuditLog (
    AuditLogID INT AUTO_INCREMENT PRIMARY KEY,
    UserID INT NOT NULL,
    Actiontaken VARCHAR(255) NOT NULL,
    Timestamp1 DATETIME NOT NULL,
    FOREIGN KEY (UserID) REFERENCES UserAccount(UserID)
);
