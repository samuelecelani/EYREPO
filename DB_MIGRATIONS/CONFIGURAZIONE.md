# Configurazione Liquibase via Variabile di Ambiente

## Descrizione
Il modulo DB_MIGRATIONS è ora integrato con il backend PIAO e può essere controllato tramite la variabile di ambiente `LIQUIBASE_ENABLED` configurata nel file values.yaml del deployment Kubernetes.

## Configurazione in values.yaml

La variabile `LIQUIBASE_ENABLED` è stata aggiunta nella sezione `cmValues` del deployment backend:

```yaml
deployments:
  gateway:
    backend:
      cmValues: {
        ...
        LIQUIBASE_ENABLED: true,  # ← Controlla l'esecuzione di Liquibase
      }
```

## Come Funziona

### 1. **Liquibase Abilitato** (LIQUIBASE_ENABLED=true)
- All'avvio del backend, Liquibase esegue automaticamente le migrazioni
- Applica tutti i changelog in `DB_MIGRATIONS/src/main/resources/db/changelog/changes/`
- Crea/aggiorna le tabelle del database
- Mantiene traccia delle migrazioni nella tabella `DATABASECHANGELOG`

### 2. **Liquibase Disabilitato** (LIQUIBASE_ENABLED=false)
- Liquibase non viene eseguito all'avvio
- Il database deve essere già aggiornato
- Utile in ambienti dove le migrazioni sono gestite manualmente

### 3. **Default** (variabile non impostata)
- Se la variabile non è presente, Liquibase è **DISABILITATO** (default: false)
- Questo previene modifiche accidentali al database

## Modifica della Configurazione

### Abilitare Liquibase in Kubernetes
Nel file `arch/dfp-piao/values.yaml`:
```yaml
LIQUIBASE_ENABLED: true
```

### Disabilitare Liquibase in Kubernetes
Nel file `arch/dfp-piao/values.yaml`:
```yaml
LIQUIBASE_ENABLED: false
```

### Sviluppo Locale
Imposta la variabile d'ambiente prima di avviare l'applicazione:

**Windows PowerShell:**
```powershell
$env:LIQUIBASE_ENABLED="true"
mvn spring-boot:run
```

**Linux/Mac:**
```bash
export LIQUIBASE_ENABLED=true
mvn spring-boot:run
```

## Flusso di Esecuzione

```
1. Kubernetes legge values.yaml
   ↓
2. Crea ConfigMap con LIQUIBASE_ENABLED=true
   ↓
3. Backend PIAO_BE si avvia
   ↓
4. Spring Boot legge spring.liquibase.enabled=${LIQUIBASE_ENABLED:false}
   ↓
5. Se true → Liquibase esegue migrazioni
   Se false → Liquibase viene saltato
```

## Best Practices

### ✅ Ambienti di Sviluppo/Test
```yaml
LIQUIBASE_ENABLED: true
```
- Le migrazioni vengono applicate automaticamente ad ogni deploy
- Il database si aggiorna sempre all'ultima versione

### ✅ Ambienti di Staging
```yaml
LIQUIBASE_ENABLED: true
```
- Verifica che le migrazioni funzionino prima della produzione

### ⚠️ Ambienti di Produzione
Due opzioni:

**Opzione 1 - Automatico (consigliato per piccoli team):**
```yaml
LIQUIBASE_ENABLED: true
```
- Richiede backup del database prima del deploy
- Rischio di downtime durante migrazioni pesanti

**Opzione 2 - Manuale (consigliato per team enterprise):**
```yaml
LIQUIBASE_ENABLED: false
```
- Esegui migrazioni manualmente tramite Maven prima del deploy:
  ```bash
  cd DB_MIGRATIONS
  mvn liquibase:update
  ```

## Verificare le Migrazioni

### Controllare lo stato delle migrazioni
```bash
cd DB_MIGRATIONS
mvn liquibase:status
```

### Vedere quali migrazioni sono state applicate
Interroga la tabella di Liquibase:
```sql
SELECT * FROM DATABASECHANGELOG ORDER BY DATEEXECUTED DESC;
```

## Rollback (Solo Manuale)

Se una migrazione causa problemi:

```bash
cd DB_MIGRATIONS
mvn liquibase:rollback -Dliquibase.rollbackTag=version-1.0.0
```

## Troubleshooting

### Problema: Liquibase non si avvia
**Verifica:**
```bash
kubectl get configmap -n <namespace>
kubectl describe configmap <configmap-name>
# Controlla che LIQUIBASE_ENABLED sia presente
```

### Problema: Migrazioni già applicate
**Soluzione:**
Liquibase salta automaticamente i changelog già eseguiti (usa MD5 checksum)

### Problema: Lock database
**Sintomo:** `Waiting for changelog lock....`

**Soluzione:**
```sql
DELETE FROM DATABASECHANGELOGLOCK WHERE ID=1;
```

## Log Liquibase

Per vedere i log di Liquibase durante l'esecuzione:

```bash
kubectl logs -f <backend-pod-name> -n <namespace> | grep -i liquibase
```

Cerca output tipo:
```
Running Changeset: db/changelog/changes/002-create-table-ruolo.yaml::002-create-table-ruolo::developer
Successfully released change log lock
```

## Integrazione nel CI/CD

Nel pipeline, puoi decidere quando abilitare Liquibase:

```yaml
# values-dev.yaml
LIQUIBASE_ENABLED: true

# values-staging.yaml
LIQUIBASE_ENABLED: true

# values-prod.yaml
LIQUIBASE_ENABLED: false  # Esegui migrazioni manualmente prima del deploy
```
