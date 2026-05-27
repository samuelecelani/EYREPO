import { Pipe, PipeTransform } from '@angular/core';
import { SectionEnum } from '../models/enums/section.enum';
import { CampiModificatiService } from '../services/campi-modificati-service';

@Pipe({
  name: 'formatCampiModificati',
  standalone: true,
})
export class FormatCampiModificatiPipe implements PipeTransform {
  constructor(private campiModificatiService: CampiModificatiService) {}

  transform(
    value: string | null | undefined,
    sezione: string = 'SEZIONE_1', // Cambiato in string per accettare i codici puntuali come SEZIONE_22_ADEMPIMENTO
    fallback: string = '-'
  ): string {
    // se value è null o undefined o stringa vuota, ritorniamo fallback
    if (!value) return fallback;

    // separo i campi usando la virgola e elimino i duplicati
    const campi = [
      ...new Set(
        value
          .split(',')
          .map((c) => c.trim())
          .filter((c) => c.length > 0)
      ),
    ];

    // Recuperiamo il mapping caricato dal service
    const config = this.campiModificatiService.getConfig() as Record<string, any>;

    if (!config) {
      // Se il config non è ancora stato caricato
      return value;
    }

    // Recuperiamo il mapping per la sezione specifica passata (es. SEZIONE_22_ADEMPIMENTO)
    const mapSezioneSpecifica = config[sezione] || {};

    // Per ogni campo, cerco la traduzione
    const campiMappati = campi.map((campo) => {
      // Cerco prima nella sezione specifica passata
      let valore = mapSezioneSpecifica[campo];

      // Se non lo trovo, provo a cercare nella sezione "padre"
      // ( se sono in SEZIONE_22_ADEMPIMENTO e non trovo, cerco in SEZIONE_22)

      if (!valore && sezione.includes('_')) {
        const sezionePadre = sezione.split('_').slice(0, 2).join('_'); // Prende SEZIONE_22
        valore = config[sezionePadre]?.[campo];
      }

      //  Ricerca globale in tutto il JSON
      if (!valore) {
        for (const sec in config) {
          if (config[sec][campo]) {
            valore = config[sec][campo];
            break;
          }
        }
      }

      // --- Logica di formattazione originale mantenuta ---

      // se il valore è una stringa, la ritorniamo direttamente
      if (typeof valore === 'string') return valore;

      // se il valore è un oggetto, trasformiamo i valori in stringa
      if (typeof valore === 'object' && valore !== null) {
        return Object.values(valore).join(', ');
      }

      // Se non trovo nulla in nessun modo, ritorno il nome tecnico del campo
      return campo;
    });

    const currentYear = new Date().getFullYear();
    return campiMappati
      .join(', ')
      .replace(/anno corrente/gi, String(currentYear))
      .replace(/anno 1/gi, String(currentYear + 1))
      .replace(/anno 2/gi, String(currentYear + 2));
  }
}
