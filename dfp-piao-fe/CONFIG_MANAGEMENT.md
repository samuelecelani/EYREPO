# Gestione File di Configurazione Statica

## Panoramica

Il file `config-static-storico/storico-campi-config.json` contiene i label e le descrizioni dei campi utilizzati in tutta l'applicazione PIAO.

**Caratteristica principale:** Questo file è servito come **file statico via nginx**, il che significa **NON richiede il rebuild e il redeployment dell'applicazione** per essere aggiornato in produzione.

---

## Architettura

### Come funziona

1. **Build time (CI/CD)**: Il file è copiato dal Dockerfile in `config-static-storico/` direttamente nella cartella statica di nginx
2. **Runtime**: Nginx serve il file direttamente senza passare per Angular
3. **Caricamento nell'app**: Il service `CampiModificatiService` carica il file all'avvio dell'applicazione
4. **Caching**: Nginx è configurato con `Cache-Control: no-cache` - il file viene ricaricato ad ogni refresh della pagina

### Struttura del progetto

```
PIAO_FE/
├── config-static-storico/
│   └── storico-campi-config.json     ← File statico modificabile
├── Dockerfile                         ← Copia il file dalla radice
├── nginx.conf                         ← Configura il serving statico
└── src/
    └── app/
        └── shared/
            └── services/
                └── campi-modificati-service.ts  ← Carica il file
```

---

## Modificare il File in Produzione

### Opzione 1: SSH diretto (Più semplice)

**Prerequisiti:** Accesso SSH al server di produzione

**Passi:**

```bash
# 1. Collegati al server
ssh user@prod-server

# 2. Naviga alla cartella
cd /usr/share/nginx/html/config-static-storico

# 3. Edita il file
nano storico-campi-config.json

# 4. Trova la sezione e il property da cambiare
# Es: cerca "SEZIONE_1" e modifica "quadroNormativo"

# 5. Salva (Ctrl+O, Enter, Ctrl+X) e esci
```

**Esempio di modifica:**

Before:

```json
"SEZIONE_1": {
  "quadroNormativo": "Quadro normativo",
```

After:

```json
"SEZIONE_1": {
  "quadroNormativo": "Nuovo Label Quadro Normativo",
```

**6. Utenti refresh la pagina** - vedranno subito il cambio

---

### Opzione 2: Kubernetes (kubectl cp)

**Prerequisiti:** kubectl configurato e accesso al cluster

**Passi:**

```bash
# 1. Assicurati di avere la versione aggiornata del file localmente
# (modifica il file nella cartella config-static-storico/)

# 2. Copia il file nel pod
kubectl cp config-static-storico/storico-campi-config.json \
  <pod-name>:/usr/share/nginx/html/config-static-storico/ \
  -n <namespace>

# 3. Verifica che il file sia stato copiato
kubectl exec -it <pod-name> -n <namespace> -- \
  cat /usr/share/nginx/html/config-static-storico/storico-campi-config.json
```

**4. Utenti refresh la pagina** - vedranno subito il cambio

---

### Opzione 3: ConfigMap Kubernetes (Avanzato)

Se usi Kubernetes, la soluzione più elegante è usare ConfigMaps:

```bash
# 1. Crea la ConfigMap dal file
kubectl create configmap piao-config \
  --from-file=storico-campi-config.json=config-static-storico/storico-campi-config.json \
  -n <namespace> \
  --dry-run=client -o yaml | kubectl apply -f -

# 2. Monta la ConfigMap nel Dockerfile
# VOLUME - monta il config da ConfigMap in /usr/share/nginx/html/config-static-storico/
```

---

## Struttura del File JSON

Il file è organizzato per sezioni:

```json
{
  "SEZIONE_1": {
    "quadroNormativo": "Quadro normativo",
    "missione": "Missione",
    ...
  },
  "SEZIONE_21": {
    ...
  },
  "SEZIONE_22": {
    ...
  }
}
```

**Key naming convention:**

- `nomeCampo.properties.value` → Campi con array di valori
- `nomeCampo.subCampo` → Nested properties
- `nomeCampo` → Semplici stringhe

---

## Cosa NON Fare

❌ Non fare il rebuild di Angular per modifiche a questo file

❌ Non caricare manualmente il file nel dist/ - nginx lo serve da `/usr/share/nginx/html/`

---

## Troubleshooting

### Il cambio non si vede in app

**Causa probabile:** Browser cache

**Soluzione:**

```bash
# Hard refresh nel browser
Ctrl+Shift+R  (Windows/Linux)
Cmd+Shift+R   (Mac)

# Oppure pulisci esplicitamente la cache
# Premi F12 → Application → Clear site data
```

### Errore: File non trovato

```bash
# Verifica che il file esista
curl http://localhost:8080/config-static-storico/storico-campi-config.json

# Verifica i permessi
ls -la /usr/share/nginx/html/config-static-storico/
```

### Errore: JSON non valido

```bash
# Valida il JSON prima di caricare
cat storico-campi-config.json | jq empty

# Se restituisce un errore, hai errori di sintassi JSON
```

---

## Checklist Prima di Modificare

- [ ] Ho il file `storico-campi-config.json` locale aggiornato
- [ ] Ho accesso SSH al server o kubectl al cluster
- [ ] Ho fatto un backup del file attuale
- [ ] Il JSON è valido (no virgole mancanti, parentesi non chiuse)
- [ ] Ho modificato solo i valori, non le chiavi
- [ ] Ho comunicato agli utenti che vedranno cambamenti senza refresh (ma refresh è consigliato)

---

## Contatti & Supporto

Per domande sul setup o modifiche, contattare il team DevOps.
