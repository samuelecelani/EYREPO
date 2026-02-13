import { FunzionalitaDTO } from '../models/classes/funzionalita-dto';
import { PARiferimentoDTO } from '../models/classes/pa-riferimento-dto';
import { RuoloUtenteDTO } from '../models/classes/ruolo-utente-dto';
import { StrutturaIndicePiaoDTO } from '../models/classes/struttura-indice-piao-dto';
import { UtenteDTO } from '../models/classes/utente-dto';
import { GenericResponse } from '../models/interfaces/generic-response';

export const mockFunzionalitaDTOHttp: GenericResponse<FunzionalitaDTO[] | null> = {
  data: [
    {
      id: 1,
      nomeFunzionalita: 'GET_ALL_AVVISI',
      descrizioneFunzionalita: 'GET_ALL_AVVISI',
      codiceFunzionalita: 'F001',
      funzionalitaByRuoli: [
        {
          codRuolo: 'TEST',
          descrizione: 'TEST',
        },
      ],
    },
    {
      id: 2,
      nomeFunzionalita: 'GET_CARD',
      descrizioneFunzionalita: 'GET_CARD',
      codiceFunzionalita: 'F002',
      funzionalitaByRuoli: [
        {
          codRuolo: 'TEST',
          descrizione: 'TEST',
        },
      ],
    },
  ],
  status: {
    success: true,
  },
  error: null,
};

export const mockFunzionalitaDTO: FunzionalitaDTO = {
  id: 1,
  nomeFunzionalita: 'GET_CARD1',
  descrizioneFunzionalita: 'GET_CARD1',
  codiceFunzionalita: 'F002',
  funzionalitaByRuoli: [
    {
      codRuolo: 'TEST',
      descrizione: 'TEST',
    },
  ],
};

export const mockFunzionalitaDTOList: FunzionalitaDTO[] = [
  {
    id: 1,
    nomeFunzionalita: 'GET_CARD',
    descrizioneFunzionalita: 'GET_CARD',
    codiceFunzionalita: 'F002',
    funzionalitaByRuoli: [
      {
        codRuolo: 'TEST',
        descrizione: 'TEST',
      },
    ],
  },
  {
    id: 2,
    nomeFunzionalita: 'GET_ALL_AVVISI',
    descrizioneFunzionalita: 'GET_ALL_AVVISI',
    codiceFunzionalita: 'F001',
    funzionalitaByRuoli: [
      {
        codRuolo: 'TEST',
        descrizione: 'TEST',
      },
    ],
  },
];

export const mockFunzionalitaList: string[] = ['F001', 'F002'];

export const mockRuoloUtenteDTO: RuoloUtenteDTO = {
  codice: 'TEST',
  descrizione: 'TEST',
  ruoloAttivo: true,
  sezioneAssociata: ['TEST'],
};

export const mockPARiferimento: PARiferimentoDTO = {
  attiva: true,
  codePA: 'TEST',
  denominazionePA: 'TEST',
  ruoli: [mockRuoloUtenteDTO],
};

export const mockUtente: UtenteDTO = {
  nome: 'Esempio',
  paRiferimento: [mockPARiferimento],
  codiceFiscale: 'XXXXX',
  typeAuthority: 'DFP',
};

export const mockUtenteHttp: GenericResponse<UtenteDTO> = {
  data: {
    nome: 'Esempio',
    paRiferimento: [mockPARiferimento],
    codiceFiscale: 'XXXXX',
    typeAuthority: 'DFP',
  },
  status: {
    success: true,
  },
  error: {
    messageError: null,
    errorCode: null,
  },
};

export const mockStructureIndicePiao: StrutturaIndicePiaoDTO[] = [
  {
    id: 1,
    numeroSezione: '1',
    testo: 'Presentazione metodologica e anagrafica ',
    statoSezione: 'Da compilare',
    children: [],
  },
  {
    id: 2,
    numeroSezione: '2',
    testo: 'Valore pubblico, performance, rischi corruttivi e trasparenza',
    statoSezione: 'In compilazione',
    children: [
      {
        id: 21,
        numeroSezione: '2.1',
        testo: 'Valore pubblico',
        statoSezione: 'In validazione',
        children: [],
      },
      {
        id: 22,
        numeroSezione: '2.2',
        testo: 'Performance',
        statoSezione: 'Compilata',
        children: [],
      },
      {
        id: 23,
        numeroSezione: '2.3',
        testo: 'Performance',
        statoSezione: 'Validata',
        children: [],
      },
    ],
  },
];
