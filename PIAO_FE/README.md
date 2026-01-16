# Angular Starter con Runtime Config, Interceptor e Tailwind

Questo progetto crea uno starter Angular (Standalone, Signals, nuovo control flow) con:

- Tailwind CSS (PostCSS, purge configurata)
- Configurazione runtime da `assets/config.json` caricata prima del bootstrap via `APP_INITIALIZER`
- Interceptor: Auth, CorrelationId, Retry (backoff 5xx/timeout), Error (Toast)
- Servizi: `BaseApiService`, `LoggerService`, `ToastService` con `ToastContainer`
- ESLint + Prettier
- Test unit con Jest + Testing Library

## Avvio

1. Installa le dipendenze

```powershell
npm i
```

2. Avvia in dev

```powershell
npm run dev
```

Apri http://localhost:4200 e verifica la Home.

## Modifica `assets/config.json` senza rebuild

`assets/config.json` è servito come asset statico e viene richiesto all'avvio prima del bootstrap. Modificando il file e ricaricando la pagina, l'app userà i nuovi valori senza necessità di rebuild. Esempio:

```json
{
  "appName": "PIAO",
  "apiBaseUrl": "http://localhost:8080",
  "oauth": { "clientId": "spa-public-client" },
  "requestTimeoutMs": 8000,
  "retry": { "maxAttempts": 3, "baseDelayMs": 300 }
}
```

## Perché non mettere segreti nel client

Il codice e gli asset del client sono pubblici sul browser. Qualsiasi valore incluso può essere letto. Le chiavi private e i segreti devono vivere nel backend (es. Spring Boot) o in segreti gestiti (Vault). Il client riceverà solo token sessione in cookie HttpOnly gestiti dal backend, non accessibili via JavaScript.

## Note CORS/HSTS lato server

- Abilita CORS sul backend per origin http://localhost:4200 in dev, limitando metodi e header necessari (Authorization, x-correlation-id, Content-Type).
- Imposta HSTS e altri header di sicurezza (Content-Security-Policy, X-Frame-Options) in produzione.

## Script utili

- `npm run lint` esegue ESLint
- `npm run format` applica Prettier
- `npm run test` esegue i test Jest

## Demo

- Home mostra i valori di configurazione e un form con validazioni.
- Pulsante "Simula errore 500" chiama un endpoint fittizio (`/dev/always-500`) utile per testare Retry/Error/Toast.
