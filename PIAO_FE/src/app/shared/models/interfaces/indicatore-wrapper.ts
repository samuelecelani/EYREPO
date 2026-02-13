import { IndicatoreDTO } from '../classes/indicatore-dto';

/**
 * Interfaccia per il tipo comune che contiene id e indicatoreDTO.
 * Utilizzata per wrappare oggetti che contengono un indicatore, come OVPStrategiaIndicatoreDTO.
 */
export interface IIndicatoreWrapper {
  id: number;
  indicatore: IndicatoreDTO;
}
