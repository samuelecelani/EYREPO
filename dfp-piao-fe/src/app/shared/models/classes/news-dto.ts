export interface INewsDTO {
  id: number | string;
  tipoNovita: string;
  titolo: string;
  intro: string;
  descrizione: string;
  dataPubblicazione?: string;
  dataCreazione?: string;
  createdAt?: string;
  paroleChiave?: string[];
  documenti?: Array<{
    id?: string;
    nome?: string;
    tipo?: string;
    sizeMb?: number;
    downloadUrl?: string;
  }>;
}

export interface INewsTipologiaDTO {
  id: string;
  label: string;
  count: number;
}

export interface INewsTipologieResponseDTO {
  items: INewsTipologiaDTO[];
  _debug?: Record<string, unknown>;
}

export interface INewsSearchItemDTO {
  id: string;
  titolo: string;
  abstract?: string;
  abstractText?: string;
  data: string;
  tipologia: string;
}

export interface INewsDocumentDTO {
  id?: string;
  nome?: string;
  tipo?: string;
  sizeMb?: number | string;
  downloadUrl?: string;
}

export interface INewsSearchResponseDTO {
  page: number;
  limit: number;
  total: number;
  items: INewsSearchItemDTO[];
  _debug?: Record<string, unknown>;
}

export interface INewsDetailDTO {
  id: string;
  titolo: string;
  abstract?: string;
  abstractText?: string;
  data: string;
  testoHtml: string | null;
  tipologia: string;
  documenti?: INewsDocumentDTO[] | null;
  allegati?: INewsDocumentDTO[] | null;
  imageBase64?: string | null;
  immagineBase64?: string | null;
  backgroundImageBase64?: string | null;
}

export interface INewsErrorResponseDTO {
  error: {
    code: string;
    message: string;
  };
}
