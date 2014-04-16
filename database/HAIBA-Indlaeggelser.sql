CREATE DATABASE IF NOT EXISTS HAIBA;
USE HAIBA;
 
CREATE TABLE IF NOT EXISTS anvendt_klass_shak (
    ID BIGINT(15) NOT NULL AUTO_INCREMENT PRIMARY KEY,
    sygehuskode VARCHAR(10),
    afdelingskode VARCHAR(10),
    Ejerforhold VARCHAR(20),
    Institutionsart VARCHAR(20),
    Regionskode VARCHAR(20),
    H_ITA_gruppe float,
    H_kir_gruppe float,
    H_med_gruppe float,
    H_MiBa_prefix float,
    H_SOR_mappet float

) ENGINE=InnoDB COLLATE=utf8_bin;

CREATE TABLE IF NOT EXISTS anvendt_klass_procedurer (
    ID BIGINT(15) NOT NULL AUTO_INCREMENT PRIMARY KEY,
    procedurekode VARCHAR(10),
    tillaegskode VARCHAR(10),
    H_HOFTE_PRO float NULL,
    H_HOFTE_IND float NULL,
    H_HOFTE_SPE float NULL,
    H_HOFTE_REO float NULL
) ENGINE=InnoDB COLLATE=utf8_bin;

CREATE TABLE IF NOT EXISTS anvendt_klass_diagnoser (
    ID BIGINT(15) NOT NULL AUTO_INCREMENT PRIMARY KEY,
    Diagnoseskode VARCHAR(10),
    tillaegskode VARCHAR(10),
    H_BAKT_D float NULL,
    H_UVI_D float NULL,
    H_SAAR_D float NULL
) ENGINE=InnoDB COLLATE=utf8_bin;

CREATE TABLE IF NOT EXISTS ImporterStatus (
    Id BIGINT(15) AUTO_INCREMENT NOT NULL PRIMARY KEY,
    StartTime DATETIME NOT NULL,
    EndTime DATETIME,
    Outcome VARCHAR(20),
    ErrorMessage VARCHAR(200),

    INDEX (StartTime)
) ENGINE=InnoDB COLLATE=utf8_bin;