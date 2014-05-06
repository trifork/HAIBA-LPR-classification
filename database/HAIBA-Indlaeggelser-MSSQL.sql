CREATE TABLE Class_dynamic_SHAK (
    ID BIGINT NOT NULL IDENTITY PRIMARY KEY,
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

);

CREATE TABLE Class_dynamic_procedures (
    ID BIGINT NOT NULL IDENTITY PRIMARY KEY,
    procedurekode VARCHAR(10),
    tillaegskode VARCHAR(10),
    H_HOFTE_PRO float NULL,
    H_HOFTE_IND float NULL,
    H_HOFTE_SPE float NULL,
    H_HOFTE_REO float NULL
);

CREATE TABLE Class_dynamic_diagnosis (
    ID BIGINT NOT NULL IDENTITY PRIMARY KEY,
    Diagnoseskode VARCHAR(10),
    tillaegskode VARCHAR(10),
    H_BAKT_D float NULL,
    H_UVI_D float NULL,
    H_SAAR_D float NULL
);