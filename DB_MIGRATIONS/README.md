# Liquibase Database Migrations Module

Modulo autoconsistente per la gestione delle migrazioni del database tramite Liquibase.

## Struttura
```
DB_MIGRATIONS/
├── pom.xml
├── README.md
├── .env.example
└── src/main/resources/
    └── db/changelog/
        ├── db.changelog-master.yaml
        └── changes/
            ├── 001-baseline.yaml
            ├── 002-create-table-ruolo.yaml
            ├── 003-create-table-utente-ruolo-pa.yaml
            ├── 004-create-table-ruolo-utente.yaml
            ├── 005-create-table-utente-pa.yaml
            ├── 006-create-table-piao.yaml
            ├── 007-create-table-stakeholder.yaml
            ├── 008-insert-ruoli-default.yaml
            └── 999-esempi-modifiche.yaml
```

## Changelog Creati

### ✅ Tabelle Base
- **002-create-table-ruolo.yaml**: Tabella Ruolo con codRuolo univoco
- **003-create-table-utente-ruolo-pa.yaml**: Tabella utenteRuoloPa 
- **004-create-table-ruolo-utente.yaml**: Tabella ruoloUtente (relazione many-to-many)
- **005-create-table-utente-pa.yaml**: Tabella UtentePa

### ✅ Tabelle Business
- **006-create-table-piao.yaml**: Tabella Piao con campi tecnici (CampiTecnici)
- **007-create-table-stakeholder.yaml**: Tabella stakeholder con FK a Piao

### ✅ Dati Iniziali
- **008-insert-ruoli-default.yaml**: Inserimento ruoli (ADMIN, RESPONSABILE, OPERATORE, LETTORE)

### ✅ Esempi
- **999-esempi-modifiche.yaml**: Esempi di modifiche future (addColumn, modifyDataType, renameColumn, dropColumn)

## Comandi Maven

### 🚀 Applicare le migrazioni
```bash
cd DB_MIGRATIONS
mvn liquibase:update
```

### 📊 Verificare lo stato
```bash
mvn liquibase:status
```

### 🔄 Rollback all'ultimo tag
```bash
mvn liquibase:rollback -Dliquibase.rollbackTag=version-1.0.0
```

### 📝 Generare SQL senza applicare
```bash
mvn liquibase:updateSQL
```

### ✔️ Validare i changelog
```bash
mvn liquibase:validate
```

### 🧹 Pulire i checksum (se modifichi changelog esistenti)
```bash
mvn liquibase:clearCheckSums
```

## Variabili d'ambiente richieste

Imposta queste variabili prima di eseguire i comandi:

### Windows PowerShell
```powershell
$env:POSTGRES_URL="jdbc:postgresql://localhost:5432/piao_db"
$env:POSTGRES_USER="postgres"
$env:POSTGRES_PSW="password"
```

### Linux/Mac
```bash
export POSTGRES_URL="jdbc:postgresql://localhost:5432/piao_db"
export POSTGRES_USER="postgres"
export POSTGRES_PSW="password"
```

## Come creare un nuovo changelog

1. Crea un nuovo file YAML in `src/main/resources/db/changelog/changes/`
2. Nominalo con formato progressivo: `NNN-descrizione.yaml` 
   - Esempio: `013-create-table-allegato.yaml`
3. Il file verrà incluso automaticamente dal master changelog (in ordine alfabetico)

### Template base per nuovo changelog

```yaml
databaseChangeLog:
  - changeSet:
      id: 013-create-table-allegato
      author: tuo-nome
      comment: "Descrizione del changeSet"
      changes:
        - createTable:
            tableName: allegato
            columns:
              - column:
                  name: id
                  type: BIGINT
                  autoIncrement: true
                  constraints:
                    primaryKey: true
                    nullable: false
              # ... altri campi
```

## Pattern Comuni

### Tabella con Campi Tecnici
Includi sempre questi campi per le tabelle che estendono `CampiTecnici`:
```yaml
- column:
    name: X_VALIDITY_IN
    type: BOOLEAN
    defaultValueBoolean: true
    constraints:
      nullable: false
- column:
    name: X_CREATEDBY
    type: VARCHAR(20)
    constraints:
      nullable: false
- column:
    name: X_CREATED_TS
    type: DATE
    constraints:
      nullable: false
- column:
    name: X_UPDATEDBY
    type: VARCHAR(20)
- column:
    name: X_UPDATED_TS
    type: DATE
```

### Foreign Key
```yaml
- addForeignKeyConstraint:
    baseTableName: tabella_figlia
    baseColumnNames: id_padre
    constraintName: fk_figlia_padre
    referencedTableName: tabella_padre
    referencedColumnNames: id
    onDelete: CASCADE
    onUpdate: CASCADE
```

### Indici
```yaml
- createIndex:
    tableName: nome_tabella
    indexName: idx_tabella_campo
    columns:
      - column:
          name: nome_campo
```

## Note Importanti

- ⚠️ **NON modificare changelog già applicati** - crea sempre nuovi changelog per modifiche
- ✅ Usa `preConditions` per rendere i changeSet idempotenti
- 🔖 Usa `tagDatabase` per creare punti di rollback
- 📝 Scrivi sempre `comment` descrittivi per ogni changeSet
- 🔄 Testa il rollback prima di applicare in produzione

## Troubleshooting

### Checksum non corrisponde
Se hai modificato un changelog già applicato:
```bash
mvn liquibase:clearCheckSums
```

### Lock bloccato
Se Liquibase rimane in lock:
```sql
DELETE FROM DATABASECHANGELOGLOCK WHERE ID=1;
```

### Reset completo (SOLO DEVELOPMENT!)
```sql
DROP TABLE DATABASECHANGELOG;
DROP TABLE DATABASECHANGELOGLOCK;
```

