import { Injectable } from '@angular/core';
import * as pdfjsLib from 'pdfjs-dist';

pdfjsLib.GlobalWorkerOptions.workerSrc = new URL(
  'pdfjs-dist/build/pdf.worker.min.mjs',
  import.meta.url
).toString();

const LOW_TEXT_THRESHOLD = 35; // caratteri alfanumerici sotto cui la pagina è "senza testo"
const MIN_TOTAL_CHARS = 800; // totale minimo caratteri alfanumerici
const MIN_DENSITY_PER_KB = 2.0; // caratteri per KB minimi
const MAX_LOW_PAGES_PCT = 70; // % massima pagine senza testo

type Verdict = 'text' | 'scan' | 'hybrid';

interface ClassifyParams {
  fileSize: number;
  numPages: number;
  totalSigChars: number;
  lowTextPages: number;
  imagePages: number;
}

@Injectable({ providedIn: 'root' })
export class PdfValidatorService {
  /**
   * Verifica che un PDF:
   * 1. Contenga testo selezionabile (non sia una scansione)
   * 2. Sia di tipo PDF/A con conformance level "A" (qualsiasi versione: 1, 2, 3)
   * @returns true solo se entrambe le condizioni sono soddisfatte.
   *          In caso di errore nella lettura restituisce true (non blocca l'upload).
   */
  async isTextPdf(file: File): Promise<boolean> {
    try {
      // Validazione tipo/estensione
      const nameLower = (file.name || '').toLowerCase();
      if (file.type && file.type !== 'application/pdf' && !nameLower.endsWith('.pdf')) {
        return false;
      }

      const arrayBuffer = await file.arrayBuffer();

      // Validazione header %PDF-
      const header = new TextDecoder().decode(new Uint8Array(arrayBuffer, 0, 5));
      if (header !== '%PDF-') return false;

      const pdf = await pdfjsLib.getDocument({ data: arrayBuffer }).promise;

      const numPages = pdf.numPages;
      if (!numPages || numPages < 1) return false;

      // Controllo PDF/A conformance "A"
      const isPdfA = await this.checkPdfA(pdf);
      if (!isPdfA) return false;

      let totalSigChars = 0;
      let lowTextPages = 0;
      let imagePages = 0;

      for (let p = 1; p <= numPages; p++) {
        const page = await pdf.getPage(p);

        const textContent = await page.getTextContent();
        let pageText = '';
        for (const item of textContent.items) {
          pageText += ('str' in item ? (item as any).str : '') + ' ';
        }

        const sigChars = this.countAlphanumeric(pageText);
        totalSigChars += sigChars;
        if (sigChars < LOW_TEXT_THRESHOLD) lowTextPages++;

        const imgHits = await this.countImageOps(page);
        if (imgHits > 0) imagePages++;
      }

      return (
        this.classify({
          fileSize: file.size,
          numPages,
          totalSigChars,
          lowTextPages,
          imagePages,
        }) === 'text'
      );
    } catch {
      // PDF non leggibile o corrotto: non blocchiamo, l'upload fallirà in modo gestito
      return true;
    }
  }

  /**
   * Verifica che il PDF dichiari conformance PDF/A di livello "A" tramite metadati XMP.
   * Accetta qualsiasi versione (1, 2, 3) purché il livello sia "A".
   */
  private async checkPdfA(pdf: any): Promise<boolean> {
    try {
      const { metadata } = await pdf.getMetadata();
      if (!metadata) return false;

      // Tentativo 1: API strutturata di pdfjs-dist (pdfaid namespace)
      const conformance: unknown = metadata.get('pdfaid:conformance');
      if (typeof conformance === 'string') {
        return conformance.toUpperCase() === 'A';
      }

      // Tentativo 2: parsing raw XMP come fallback
      const raw: unknown = typeof metadata.getRaw === 'function' ? metadata.getRaw() : null;
      if (typeof raw === 'string') {
        const match = raw.match(/<pdfaid:conformance[^>]*>([^<]+)<\/pdfaid:conformance>/i);
        if (match) return match[1].trim().toUpperCase() === 'A';
      }

      return false;
    } catch {
      return false;
    }
  }

  private classify(params: ClassifyParams): Verdict {
    const kb = params.fileSize / 1024;
    const density = kb > 0 ? params.totalSigChars / kb : 0;
    const lowPct = params.numPages > 0 ? (params.lowTextPages / params.numPages) * 100 : 100;

    const enoughChars = params.totalSigChars >= MIN_TOTAL_CHARS;
    const enoughDensity = density >= MIN_DENSITY_PER_KB;
    const acceptableLow = lowPct <= MAX_LOW_PAGES_PCT;
    const looksText = enoughChars && enoughDensity && acceptableLow;

    const scanByChars = params.totalSigChars < Math.max(200, Math.round(MIN_TOTAL_CHARS * 0.25));
    const scanByDensity = density < Math.max(0.5, MIN_DENSITY_PER_KB * 0.35);
    const scanByPages = lowPct > Math.min(90, MAX_LOW_PAGES_PCT + 15);
    const scanByImages = params.imagePages >= Math.ceil(params.numPages * 0.5);
    const looksScan = scanByChars || (scanByDensity && scanByPages && scanByImages);

    if (looksText) return 'text';
    if (looksScan) return 'scan';
    return 'hybrid';
  }

  /** Conta caratteri alfanumerici (inclusi caratteri accentati latini). */
  private countAlphanumeric(s: string): number {
    const matches = s.match(/[0-9A-Za-zÀ-ÖØ-öø-ÿ]+/g);
    return matches ? matches.reduce((sum, t) => sum + t.length, 0) : 0;
  }

  /** Conta operazioni di disegno immagine raster in una pagina PDF. */
  private async countImageOps(page: any): Promise<number> {
    try {
      const ops = await page.getOperatorList();
      const OPS = pdfjsLib.OPS;
      const imgOps = new Set([
        OPS.paintImageXObject,
        OPS.paintImageXObjectRepeat,
        OPS.paintInlineImageXObject,
        OPS.paintInlineImageXObjectGroup,
        OPS.paintImageMaskXObject,
        OPS.paintImageMaskXObjectRepeat,
      ]);
      let hits = 0;
      for (const fn of ops.fnArray) {
        if (imgOps.has(fn) && ++hits >= 2) break;
      }
      return hits;
    } catch {
      return 0;
    }
  }
}
