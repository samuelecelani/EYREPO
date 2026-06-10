// Serve la cartella dist/browser sotto /area-riservata/ con fallback SPA
// + proxy verso il BE (replica proxy.conf.json), per testare il build come in collaudo.
//
// Uso:
//   node serve-dist.mjs
//   poi apri http://localhost:8080/area-riservata/
//
// Richiede solo Node 18+ (zero dipendenze esterne).
// Pre-requisito: BE attivo su http://localhost:9080 (come per ng serve).

import http from 'node:http';
import fs from 'node:fs';
import path from 'node:path';
import crypto from 'node:crypto';

const PORT = 4200;
const BASE = '/area-riservata';
const DIST = path.resolve('dist/browser');

// CSP policy identica a nginx.conf di collaudo (con $csp_nonce sostituito a runtime).
// connect-src include localhost:8098 (Keycloak) e localhost:9080 (BE, per sicurezza
// anche se le chiamate vanno via same-origin proxy).
function buildCspHeader(nonce) {
  return (
    "default-src 'self'; " +
    "script-src 'self'; " +
    `style-src 'self' 'nonce-${nonce}' 'sha256-47DEQpj8HBSa+/TImW+5JCeuQeRkm5NMpJWZG3hSuFU=' 'sha256-wPXhisdsFu1DtHYH1D9W5isSGqS5vIPn6QJWSNLqfCM=' 'sha256-dh9oI9UPbCE5zYKYoVgkz/V5cg/4dci2DpOJxbHZ1+E='; ` +
    "img-src 'self' data:; " +
    "font-src 'self' data:; " +
    "connect-src 'self' http://localhost:8098 http://localhost:9080 https://localhost:8543 https://keycloak.ey.test:8543 https://sso.dfp.gov.it https://sso-coll.dfp.gov.it; " +
    "object-src 'none'; " +
    "base-uri 'self'; " +
    "frame-ancestors 'none'; " +
    "form-action 'self';"
  );
}

const SECURITY_HEADERS = {
  'X-Frame-Options': 'SAMEORIGIN',
  'X-Content-Type-Options': 'nosniff',
  'Referrer-Policy': 'no-referrer-when-downgrade',
};

// Stesso target di proxy.conf.json
const BE_HOST = 'localhost';
const BE_PORT = 9080;

// Regole proxy in ordine. La prima che matcha vince.
// rewrite: funzione che trasforma il path originale nel path verso il BE.
const PROXY_RULES = [
  {
    test: (p) => p === '/api' || p.startsWith('/api/'),
    rewrite: (p) => p.replace(/^\/api/, '/api/piao/bff'),
  },
  {
    // Solo /area-riservata/config esatto o /area-riservata/config/...
    // NON deve matchare /area-riservata/config-static-storico/
    test: (p) => p === '/area-riservata/config' || p.startsWith('/area-riservata/config/'),
    rewrite: (p) => p.replace(/^\/area-riservata\/config/, '/api/piao/bff/config'),
  },
  {
    test: (p) => p === '/external' || p.startsWith('/external/'),
    rewrite: (p) => '/api/piao/bff' + p,
  },
];

const MIME = {
  '.html': 'text/html; charset=utf-8',
  '.js': 'application/javascript; charset=utf-8',
  '.mjs': 'application/javascript; charset=utf-8',
  '.css': 'text/css; charset=utf-8',
  '.json': 'application/json; charset=utf-8',
  '.svg': 'image/svg+xml',
  '.png': 'image/png',
  '.jpg': 'image/jpeg',
  '.jpeg': 'image/jpeg',
  '.gif': 'image/gif',
  '.ico': 'image/x-icon',
  '.woff': 'font/woff',
  '.woff2': 'font/woff2',
  '.ttf': 'font/ttf',
  '.map': 'application/json; charset=utf-8',
  '.txt': 'text/plain; charset=utf-8',
};

function sendFile(res, filePath) {
  const ext = path.extname(filePath).toLowerCase();
  res.writeHead(200, { 'Content-Type': MIME[ext] || 'application/octet-stream' });
  fs.createReadStream(filePath).pipe(res);
}

// Serve index.html iniettando il meta tag csp-nonce e l'header CSP,
// replicando il comportamento di nginx (sub_filter + add_header in nginx.conf).
function sendIndexHtml(res) {
  fs.readFile(path.join(DIST, 'index.html'), 'utf8', (err, html) => {
    if (err) {
      res.writeHead(500);
      return res.end('index.html read error: ' + err.message);
    }
    const nonce = crypto.randomBytes(16).toString('base64');
    const metaTag = `<meta property="csp-nonce" content="${nonce}">`;
    // 1) Meta tag letto dal CspNonceService custom dell'app
    // 2) Attributo ngCspNonce sul root: Angular lo legge al bootstrap e lo applica
    //    a tutti gli <style> generati dal DomRenderer per i componenti scoped.
    //    Senza questo, dom_renderer.mjs inietta <style> senza nonce → bloccati dalla CSP.
    let patched = html.replace(/<head>/i, `<head>${metaTag}`);
    patched = patched.replace(/<app-root(\s|>)/i, `<app-root ngCspNonce="${nonce}"$1`);
    res.writeHead(200, {
      'Content-Type': 'text/html; charset=utf-8',
      'Content-Security-Policy': buildCspHeader(nonce),
      ...SECURITY_HEADERS,
    });
    res.end(patched);
  });
}

function proxyTo(req, res, rewrittenPath) {
  const headers = { ...req.headers, host: `${BE_HOST}:${BE_PORT}` };
  const options = {
    hostname: BE_HOST,
    port: BE_PORT,
    method: req.method,
    path: rewrittenPath,
    headers,
  };
  const proxyReq = http.request(options, (proxyRes) => {
    res.writeHead(proxyRes.statusCode || 502, proxyRes.headers);
    proxyRes.pipe(res);
  });
  proxyReq.on('error', (err) => {
    res.writeHead(502, { 'Content-Type': 'text/plain' });
    res.end(`Proxy error: ${err.message}\nBE down? Avvia il backend su ${BE_HOST}:${BE_PORT}`);
  });
  req.pipe(proxyReq);
}

const server = http.createServer((req, res) => {
  // WHATWG URL API (no più warning DEP0169)
  const reqUrl = new URL(req.url, `http://localhost:${PORT}`);
  const pathname = reqUrl.pathname;

  // 1) Proxy verso BE per le rotte API
  for (const rule of PROXY_RULES) {
    if (rule.test(pathname)) {
      const newPath = rule.rewrite(pathname) + (reqUrl.search || '');
      return proxyTo(req, res, newPath);
    }
  }

  // 2) Redirect root → /area-riservata/ preservando query string
  //    (importante per OAuth callback: ?code=...&state=...)
  if (pathname === '/' || pathname === '') {
    const target = BASE + '/' + (reqUrl.search || '');
    res.writeHead(302, { Location: target });
    return res.end();
  }

  // 3a) Asset fuori dalla base (es. /assets/img/... o /qualunque/route/assets/img/...)
  //     L'AssetService in locale ritorna prefix vuoto, e i background-image CSS con
  //     path relativo si risolvono contro l'URL di route. Catturiamo qui:
  //     qualunque path che contenga '/assets/' lo serviamo da dist/browser/assets/.
  const assetIdx = pathname.indexOf('/assets/');
  if (assetIdx !== -1 && !pathname.startsWith(BASE + '/')) {
    const rel = pathname.slice(assetIdx); // "/assets/..."
    const full = path.join(DIST, decodeURIComponent(rel));
    if (full.startsWith(DIST)) {
      return fs.stat(full, (err, stat) => {
        if (!err && stat.isFile()) return sendFile(res, full);
        res.writeHead(404);
        res.end('Not found');
      });
    }
  }

  // 3b) Solo path sotto la base servono asset statici
  if (!pathname.startsWith(BASE + '/') && pathname !== BASE) {
    res.writeHead(404);
    return res.end('Not found');
  }

  // Rimuovi prefisso base
  let rel = pathname.slice(BASE.length) || '/';
  if (rel === '/') return sendIndexHtml(res);

  const full = path.join(DIST, decodeURIComponent(rel));

  // Sicurezza: no path traversal
  if (!full.startsWith(DIST)) {
    res.writeHead(403);
    return res.end('Forbidden');
  }

  fs.stat(full, (err, stat) => {
    if (!err && stat.isFile()) return sendFile(res, full);
    // SPA fallback → index.html con nonce
    sendIndexHtml(res);
  });
});

server.listen(PORT, () => {
  console.log(`\n  dist servita come collaudo:`);
  console.log(`  http://localhost:${PORT}${BASE}/`);
  console.log(`  Proxy API → http://${BE_HOST}:${BE_PORT}\n`);
});
