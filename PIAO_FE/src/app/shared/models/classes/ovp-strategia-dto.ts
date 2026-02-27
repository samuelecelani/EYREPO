import { CampiTecniciDTO } from './campi-tecnici-dto';
import { OVPStrategiaIndicatoreDTO } from './ovp-strategia-indicatore-dto';

export class OVPStrategiaDTO extends CampiTecniciDTO {
  id?: number;
  codStrategia?: string;
  denominazioneStrategia?: string;
  descrizioneStrategia?: string;
  soggettoResponsabile?: string;
  indicatori?: OVPStrategiaIndicatoreDTO[];
}
