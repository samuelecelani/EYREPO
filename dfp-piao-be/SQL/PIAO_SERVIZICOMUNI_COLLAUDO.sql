--
-- PostgreSQL database dump
--

-- 1. Creazione schema PIAO
CREATE SCHEMA IF NOT EXISTS piao_private;

-- 2. Creazione utente solo se non esiste
DO
$$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_roles
        WHERE rolname = 'piao_private_user'
    ) THEN
        CREATE USER piao_private_user
        WITH PASSWORD 'MJHDZ5t1VgmD8fRUBw2X';
    END IF;
END
$$;

-- 3. Assegna l'owner dello schema
ALTER SCHEMA piao_private OWNER TO piao_private_user;

-- 4. Privilegi ALL sullo schema
GRANT ALL PRIVILEGES ON SCHEMA piao_private TO piao_private_user;

-- 5. Privilegi su oggetti esistenti
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA piao_private TO piao_private_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA piao_private TO piao_private_user;
GRANT ALL PRIVILEGES ON ALL FUNCTIONS IN SCHEMA piao_private TO piao_private_user;

-- 6. Privilegi automatici su oggetti futuri
ALTER DEFAULT PRIVILEGES IN SCHEMA piao_private
GRANT ALL PRIVILEGES ON TABLES TO piao_private_user;

ALTER DEFAULT PRIVILEGES IN SCHEMA piao_private
GRANT ALL PRIVILEGES ON SEQUENCES TO piao_private_user;

ALTER DEFAULT PRIVILEGES IN SCHEMA piao_private
GRANT ALL PRIVILEGES ON FUNCTIONS TO piao_private_user;

-- 1. Creazione schema
CREATE SCHEMA IF NOT EXISTS common_services;

-- 2. Creazione utente solo se non esiste common_services
DO
$$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_roles
        WHERE rolname = 'common_services_user'
    ) THEN
        CREATE USER common_services_user
        WITH PASSWORD 'MJH4Z5t1wgmS8fRUBw2Y';
    END IF;
END
$$;

-- 3. Assegna l'owner dello schema
ALTER SCHEMA common_services OWNER TO common_services_user;

-- 4. Privilegi ALL sullo schema
GRANT ALL PRIVILEGES ON SCHEMA common_services TO common_services_user;

-- 5. Privilegi su oggetti esistenti
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA common_services TO common_services_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA common_services TO common_services_user;
GRANT ALL PRIVILEGES ON ALL FUNCTIONS IN SCHEMA common_services TO common_services_user;

-- 6. Privilegi automatici su oggetti futuri
ALTER DEFAULT PRIVILEGES IN SCHEMA common_services
GRANT ALL PRIVILEGES ON TABLES TO common_services_user;

ALTER DEFAULT PRIVILEGES IN SCHEMA common_services
GRANT ALL PRIVILEGES ON SEQUENCES TO common_services_user;

ALTER DEFAULT PRIVILEGES IN SCHEMA common_services
GRANT ALL PRIVILEGES ON FUNCTIONS TO common_services_user;

--------------------------------------------------------

CREATE TABLE common_services.allegati_piao (
    id integer NOT NULL,
    id_piao character varying(50),
    nome_file character varying(255) NOT NULL,
    s3_key character varying(512) DEFAULT NULL::character varying
);


--
-- Name: allegati_piao_id_seq; Type: SEQUENCE; Schema: common_services; Owner: -
--

CREATE SEQUENCE common_services.allegati_piao_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: allegati_piao_id_seq; Type: SEQUENCE OWNED BY; Schema: common_services; Owner: -
--

ALTER SEQUENCE common_services.allegati_piao_id_seq OWNED BY common_services.allegati_piao.id;


--
-- Name: allegato; Type: TABLE; Schema: common_services; Owner: -
--

CREATE TABLE common_services.allegato_ticket (
   id int8 NOT NULL,
   idticketfk int8 NOT NULL,
   coddocumento varchar(500) NULL,
   descrizione text NULL,
   sizeallegato varchar(20) NULL,
   x_validity_in bool DEFAULT true NOT NULL,
   x_createdby varchar(20) NOT NULL,
   x_created_ts date DEFAULT CURRENT_DATE NOT NULL,
   x_updatedby varchar(20) NULL,
   x_updated_ts date NULL,
   x_createdbyrole varchar(50) NULL,
   x_updatedbyrole varchar(50) NULL,
   x_createdbynamesurname varchar(100) NULL,
    x_updatedbynamesurname varchar(100) NULL,
    x_active boolean DEFAULT true,
    x_deactivationtime timestamp without time zone,
    esitoantivirus varchar(250) NULL,
    x_updatebyheldesk varchar(50) NULL
);


--
-- Name: allegato_ticket_id_seq; Type: SEQUENCE; Schema: common_services; Owner: -
--

CREATE SEQUENCE common_services.allegato_ticket_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: allegato_ticket_id_seq; Type: SEQUENCE OWNED BY; Schema: common_services; Owner: -
--

ALTER SEQUENCE common_services.allegato_ticket_id_seq OWNED BY common_services.allegato_ticket.id;


--
-- Name: amministrazioni; Type: TABLE; Schema: common_services; Owner: -
--

CREATE TABLE common_services.amministrazioni (
    codice_ipa character varying(50) NOT NULL,
    denominazione character varying(255) NOT NULL,
    tipologia character varying(50) NOT NULL
);


--
-- Name: categoria_ticket_id_seq; Type: SEQUENCE; Schema: common_services; Owner: -
--

CREATE SEQUENCE common_services.categoria_ticket_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: categoriaticket; Type: TABLE; Schema: common_services; Owner: -
--

CREATE TABLE common_services.categoriaticket (
    id bigint DEFAULT nextval('common_services.categoria_ticket_id_seq'::regclass) NOT NULL,
    codice character varying(255) NOT NULL,
    testo text,
    id_modulo character varying(50) NOT NULL,
    externalid integer
);


--
-- Name: documenti_piao; Type: TABLE; Schema: common_services; Owner: -
--

CREATE TABLE common_services.documenti_piao (
    id character varying(50) NOT NULL,
    codice_piao character varying(50) NOT NULL,
    full_name character varying(255) NOT NULL,
    codice_ipa_rif character varying(50),
    versione integer DEFAULT 1,
    data_approvazione date,
    data_pubblicazione date,
    link_esterno character varying(255)
);


--
-- Name: notification; Type: TABLE; Schema: common_services; Owner: -
--

CREATE TABLE common_services.notification (
    id BIGSERIAL NOT NULL,
    message character varying(255) NOT NULL,
    sender character varying(255),
    isready boolean,
    isread boolean,
    creationdate timestamp without time zone,
    readdate timestamp without time zone,
    type character varying(20),
    id_modulo character varying(50) NOT NULL,
    codicefiscale character varying(255) NOT NULL,
    codicepa character varying(255) NOT NULL
);


--
-- Name: notify_seq; Type: SEQUENCE; Schema: common_services; Owner: -
--

CREATE SEQUENCE common_services.notify_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: ticket_id_seq; Type: SEQUENCE; Schema: common_services; Owner: -
--

CREATE SEQUENCE common_services.ticket_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: ticket; Type: TABLE; Schema: common_services; Owner: -
--

CREATE TABLE common_services.ticket (
    id bigint DEFAULT nextval('common_services.ticket_id_seq'::regclass) NOT NULL,
    nome character varying(255),
    cognome character varying(255),
    mail character varying(255),
    oggetto text,
    messaggio text,
    idcategoriaticketfk bigint,
    id_modulo character varying(50) NOT NULL,
    codicefiscale character varying(255) NOT NULL,
    codicepa character varying(255) NOT NULL,
    x_validity_in boolean DEFAULT true NOT NULL,
    x_createdby character varying(20),
    x_created_ts date DEFAULT CURRENT_DATE NOT NULL,
    x_updatedby character varying(20),
    x_updated_ts date,
    x_createdbyrole character varying(50),
    x_updatedbyrole character varying(50),
    x_createdbynamesurname character varying(100),
    x_updatedbynamesurname character varying(100),
    x_active boolean DEFAULT true,
    x_deactivationtime timestamp without time zone,
    idticketesterno bigint,
    stato character varying(100),
    messaggiorisposta text,
    x_updatebyheldesk character varying(50)
);


--
-- Name: adempimentinormativi; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE piao_private.adempimentinormativi (
    id BIGSERIAL NOT NULL,
    idsezione23 bigint,
    normativa character varying(255) NOT NULL,
    azione character varying(255) NOT NULL,
    x_validity_in boolean DEFAULT true NOT NULL,
    x_createdby character varying(20) NOT NULL,
    x_created_ts date DEFAULT CURRENT_DATE NOT NULL,
    x_updatedby character varying(20),
    x_updated_ts date,
    x_createdbyrole character varying(50),
    x_updatedbyrole character varying(50),
    x_createdbynamesurname character varying(100),
    x_updatedbynamesurname character varying(100),
    x_active boolean DEFAULT true,
    x_deactivationtime timestamp without time zone
);


--
-- Name: adempimentinormativi_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE piao_private.adempimentinormativi_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: adempimentinormativi_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE piao_private.adempimentinormativi_id_seq OWNED BY piao_private.adempimentinormativi.id;


--
-- Name: adempimento; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE piao_private.adempimento (
    id BIGSERIAL NOT NULL,
    idsezione22 bigint,
    tipologia character varying(50) NOT NULL,
    denominazione character varying(255) NOT NULL,
    x_validity_in boolean DEFAULT true NOT NULL,
    x_createdby character varying(20) NOT NULL,
    x_created_ts date DEFAULT CURRENT_DATE NOT NULL,
    x_updatedby character varying(20),
    x_updated_ts date,
    x_createdbyrole character varying(50),
    x_updatedbyrole character varying(50),
    x_createdbynamesurname character varying(100),
    x_updatedbynamesurname character varying(100),
    x_active boolean DEFAULT true,
    x_deactivationtime timestamp without time zone
);


--
-- Name: adempimento_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE piao_private.adempimento_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: adempimento_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE piao_private.adempimento_id_seq OWNED BY piao_private.adempimento.id;


--
-- Name: allegato; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE piao_private.allegato (
    id BIGSERIAL NOT NULL,
    identitafk bigint,
    coddocumento character varying(500),
    codtipologiafk character varying(50) NOT NULL,
    codtipologiaallegato character varying(50) NOT NULL,
    descrizione text,
    x_validity_in boolean DEFAULT true NOT NULL,
    x_createdby character varying(20),
    x_created_ts date DEFAULT CURRENT_DATE NOT NULL,
    x_updatedby character varying(20),
    x_updated_ts date,
    sizeallegato character varying(20),
    x_createdbyrole character varying(50),
    x_updatedbyrole character varying(50),
    x_createdbynamesurname character varying(100),
    x_updatedbynamesurname character varying(100),
    x_active boolean DEFAULT true,
    x_deactivationtime timestamp without time zone,
    statusallegato character varying
);


--
-- Name: allegato_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE piao_private.allegato_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: allegato_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE piao_private.allegato_id_seq OWNED BY piao_private.allegato.id;


--
-- Name: ambitocompetenza; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE piao_private.ambitocompetenza (
    id BIGSERIAL NOT NULL,
    codice character varying(255) NOT NULL,
    testo text NOT NULL,
    x_validity_in boolean DEFAULT true NOT NULL,
    x_createdby character varying(20) DEFAULT 'ADMIN'::character varying NOT NULL,
    x_created_ts date DEFAULT CURRENT_DATE NOT NULL,
    x_updatedby character varying(20),
    x_updated_ts date,
    x_createdbyrole character varying(50),
    x_updatedbyrole character varying(50),
    x_createdbynamesurname character varying(100),
    x_updatedbynamesurname character varying(100),
    x_active boolean DEFAULT true,
    x_deactivationtime timestamp without time zone
);


--
-- Name: ambitocompetenza_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE piao_private.ambitocompetenza_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: ambitocompetenza_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE piao_private.ambitocompetenza_id_seq OWNED BY piao_private.ambitocompetenza.id;


--
-- Name: ampiezzaorganizzativa; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE piao_private.ampiezzaorganizzativa (
    id BIGSERIAL NOT NULL,
    idsezione31 bigint NOT NULL,
    unitaorganizzativa character varying(255),
    numrisorseumane character varying(255),
    x_validity_in boolean DEFAULT true NOT NULL,
    x_createdby character varying(20) NOT NULL,
    x_created_ts date NOT NULL,
    x_updatedby character varying(20),
    x_updated_ts date,
    x_createdbyrole character varying(50),
    x_updatedbyrole character varying(50),
    x_createdbynamesurname character varying(100),
    x_updatedbynamesurname character varying(100),
    x_active boolean DEFAULT true,
    x_deactivationtime timestamp without time zone
);


--
-- Name: ampiezzaorganizzativa_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE piao_private.ampiezzaorganizzativa_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: ampiezzaorganizzativa_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE piao_private.ampiezzaorganizzativa_id_seq OWNED BY piao_private.ampiezzaorganizzativa.id;


--
-- Name: anagrafica; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE piao_private.anagrafica (
    id BIGSERIAL NOT NULL,
    idpiao bigint NOT NULL,
    denominazioneente character varying(255) NOT NULL,
    acronimopa character varying(50),
    codicefiscale character varying(16),
    codiceipa character varying(50),
    tipologiapa character varying(100),
    tipologiaistat character varying(100),
    piva character varying(20),
    indirizzosedelegale character varying(255),
    indirizzourp character varying(255),
    www character varying(255),
    mail character varying(255),
    telefono character varying(50),
    pec character varying(255),
    nomerpct character varying(255),
    cognomerpct character varying(255),
    ruolorpct character varying(255),
    datanominarpct date,
    nomertd character varying(255),
    strutturarifrtd character varying(255),
    social character varying(255),
    x_validity_in boolean DEFAULT true NOT NULL,
    x_createdby character varying(20) NOT NULL,
    x_created_ts date DEFAULT CURRENT_DATE NOT NULL,
    x_updatedby character varying(20),
    x_updated_ts date,
    x_createdbyrole character varying(50),
    x_updatedbyrole character varying(50),
    x_createdbynamesurname character varying(100),
    x_updatedbynamesurname character varying(100),
    x_active boolean DEFAULT true,
    x_deactivationtime timestamp without time zone
);


--
-- Name: anagrafica_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE piao_private.anagrafica_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: anagrafica_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE piao_private.anagrafica_id_seq OWNED BY piao_private.anagrafica.id;


--
-- Name: app_log; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE piao_private.app_log (
    id BIGSERIAL NOT NULL,
    level character varying(20) NOT NULL,
    logger character varying(255) NOT NULL,
    message text NOT NULL,
    thread character varying(100),
    "timestamp" timestamp without time zone NOT NULL
);


--
-- Name: app_log_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE piao_private.app_log_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: app_log_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE piao_private.app_log_id_seq OWNED BY piao_private.app_log.id;


--
-- Name: areaorganizzativa; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE piao_private.areaorganizzativa (
    id BIGSERIAL NOT NULL,
    idsezione1 bigint NOT NULL,
    nomearea character varying(255),
    descrizionearea text,
    x_validity_in boolean DEFAULT true NOT NULL,
    x_createdby character varying(20),
    x_created_ts date DEFAULT CURRENT_DATE NOT NULL,
    x_updatedby character varying(20),
    x_updated_ts date,
    x_createdbyrole character varying(50),
    x_updatedbyrole character varying(50),
    x_createdbynamesurname character varying(100),
    x_updatedbynamesurname character varying(100),
    x_active boolean DEFAULT true,
    x_deactivationtime timestamp without time zone
);


--
-- Name: areaorganizzativa_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE piao_private.areaorganizzativa_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: areaorganizzativa_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE piao_private.areaorganizzativa_id_seq OWNED BY piao_private.areaorganizzativa.id;


--
-- Name: areatematica; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE piao_private.areatematica (
    id BIGSERIAL NOT NULL,
    codice character varying(255) NOT NULL,
    testo text NOT NULL,
    x_validity_in boolean DEFAULT true NOT NULL,
    x_createdby character varying(20) DEFAULT 'ADMIN'::character varying NOT NULL,
    x_created_ts date DEFAULT CURRENT_DATE NOT NULL,
    x_updatedby character varying(20),
    x_updated_ts date,
    x_createdbyrole character varying(50),
    x_updatedbyrole character varying(50),
    x_createdbynamesurname character varying(100),
    x_updatedbynamesurname character varying(100),
    x_active boolean DEFAULT true,
    x_deactivationtime timestamp without time zone
);


--
-- Name: areatematica_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE piao_private.areatematica_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: areatematica_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE piao_private.areatematica_id_seq OWNED BY piao_private.areatematica.id;


--
-- Name: attivitaformative; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE piao_private.attivitaformative (
    id BIGSERIAL NOT NULL,
    idsezione332 bigint NOT NULL,
    idtipologiaattivita bigint NOT NULL,
    idambitocompetenza bigint NOT NULL,
    idareatematica bigint NOT NULL,
    numerodirigenti numeric NOT NULL,
    oreformazione double precision NOT NULL,
    verificaapprendimento boolean,
    x_validity_in boolean DEFAULT true NOT NULL,
    x_createdby character varying(20) NOT NULL,
    x_created_ts date DEFAULT CURRENT_DATE NOT NULL,
    x_updatedby character varying(20),
    x_updated_ts date,
    x_createdbyrole character varying(50),
    x_updatedbyrole character varying(50),
    x_createdbynamesurname character varying(100),
    x_updatedbynamesurname character varying(100),
    numeronondirigenti numeric,
    x_active boolean DEFAULT true,
    x_deactivationtime timestamp without time zone
);


--
-- Name: attivitaformative_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE piao_private.attivitaformative_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: attivitaformative_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE piao_private.attivitaformative_id_seq OWNED BY piao_private.attivitaformative.id;


--
-- Name: attivitasensibile; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE piao_private.attivitasensibile (
    id BIGSERIAL NOT NULL,
    idsezione23 bigint NOT NULL,
    denominazione character varying(255) NOT NULL,
    descrizione text,
    processocollegato character varying(255),
    x_validity_in boolean DEFAULT true NOT NULL,
    x_createdby character varying(20) DEFAULT 'ADMIN'::character varying NOT NULL,
    x_created_ts date DEFAULT CURRENT_DATE NOT NULL,
    x_updatedby character varying(20),
    x_updated_ts date,
    x_createdbyrole character varying(50),
    x_updatedbyrole character varying(50),
    x_createdbynamesurname character varying(100),
    x_updatedbynamesurname character varying(100),
    x_active boolean DEFAULT true,
    x_deactivationtime timestamp without time zone
);


--
-- Name: attivitasensibile_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE piao_private.attivitasensibile_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: attivitasensibile_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE piao_private.attivitasensibile_id_seq OWNED BY piao_private.attivitasensibile.id;


--
-- Name: autoritaapprovatore; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE piao_private.autoritaapprovatore (
    id BIGSERIAL NOT NULL,
    codice character varying(255),
    testo character varying(255),
    x_validity_in boolean DEFAULT true NOT NULL,
    x_createdby character varying(20) NOT NULL,
    x_created_ts date NOT NULL,
    x_updatedby character varying(20),
    x_updated_ts date,
    x_createdbyrole character varying(50),
    x_updatedbyrole character varying(50),
    x_createdbynamesurname character varying(100),
    x_updatedbynamesurname character varying(100),
    x_active boolean DEFAULT true,
    x_deactivationtime timestamp without time zone
);


--
-- Name: autoritaapprovatore_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE piao_private.autoritaapprovatore_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: autoritaapprovatore_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE piao_private.autoritaapprovatore_id_seq OWNED BY piao_private.autoritaapprovatore.id;


--
-- Name: categoriaobiettivi; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE piao_private.categoriaobiettivi (
    id BIGSERIAL NOT NULL,
    idsezione4 bigint NOT NULL,
    idsottofase bigint NOT NULL,
    idcategoriaobiettivi bigint NOT NULL,
    codtipologiafk character varying(255),
    x_validity_in boolean DEFAULT true NOT NULL,
    x_createdby character varying(20) DEFAULT 'ADMIN'::character varying NOT NULL,
    x_created_ts date DEFAULT CURRENT_DATE NOT NULL,
    x_updatedby character varying(20),
    x_updated_ts date,
    x_createdbyrole character varying(50),
    x_updatedbyrole character varying(50),
    x_createdbynamesurname character varying(100),
    x_updatedbynamesurname character varying(100),
    x_active boolean DEFAULT true,
    x_deactivationtime timestamp without time zone
);


--
-- Name: categoriaobiettivi_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE piao_private.categoriaobiettivi_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: categoriaobiettivi_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE piao_private.categoriaobiettivi_id_seq OWNED BY piao_private.categoriaobiettivi.id;


--
-- Name: categoriaobiettivitip; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE piao_private.categoriaobiettivitip (
    id BIGSERIAL NOT NULL,
    testo character varying(255) NOT NULL,
    codtipologiafk character varying(50) NOT NULL,
    x_validity_in boolean DEFAULT true NOT NULL,
    x_createdby character varying(20) DEFAULT 'ADMIN'::character varying NOT NULL,
    x_created_ts date DEFAULT CURRENT_DATE NOT NULL,
    x_updatedby character varying(20),
    x_updated_ts date,
    x_createdbyrole character varying(50),
    x_updatedbyrole character varying(50),
    x_createdbynamesurname character varying(100),
    x_updatedbynamesurname character varying(100),
    x_active boolean DEFAULT true,
    x_deactivationtime timestamp without time zone
);


--
-- Name: categoriaobiettivitip_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE piao_private.categoriaobiettivitip_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: categoriaobiettivitip_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE piao_private.categoriaobiettivitip_id_seq OWNED BY piao_private.categoriaobiettivitip.id;


--
-- Name: piao_private.configurazioni; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE piao_private.configurazioni (
    id BIGSERIAL NOT NULL,
    codice character varying(100),
    valore text,
    typedato character varying(20),
    isconfigui boolean DEFAULT false,
    x_validity_in boolean DEFAULT true NOT NULL,
    x_createdby character varying(20) NOT NULL,
    x_created_ts date NOT NULL,
    x_updatedby character varying(20),
    x_updated_ts date,
    x_createdbyrole character varying(50),
    x_updatedbyrole character varying(50),
    x_createdbynamesurname character varying(100),
    x_updatedbynamesurname character varying(100),
    x_active boolean DEFAULT true,
    x_deactivationtime timestamp without time zone
);


--
-- Name: piao_private.configurazioni_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE piao_private.configurazioni_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: piao_private.configurazioni_id_seq1; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE piao_private.configurazioni_id_seq1
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: piao_private.configurazioni_id_seq1; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE piao_private.configurazioni_id_seq1 OWNED BY piao_private.configurazioni.id;


--
-- Name: datipubblicati; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE piao_private.datipubblicati (
    id BIGSERIAL NOT NULL,
    idobbligolegge bigint NOT NULL,
    denominazione character varying(255) NOT NULL,
    tipologia character varying(200) NOT NULL,
    responsabile character varying(255),
    terminiscadenza character varying(255),
    modalitamonitoraggio character varying(255),
    motivazioneimpossibilita text,
    x_validity_in boolean DEFAULT true NOT NULL,
    x_createdby character varying(20) DEFAULT 'ADMIN'::character varying NOT NULL,
    x_created_ts date DEFAULT CURRENT_DATE NOT NULL,
    x_updatedby character varying(20),
    x_updated_ts date,
    x_createdbyrole character varying(50),
    x_updatedbyrole character varying(50),
    x_createdbynamesurname character varying(100),
    x_updatedbynamesurname character varying(100),
    x_active boolean DEFAULT true,
    x_deactivationtime timestamp without time zone
);


--
-- Name: datipubblicati_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE piao_private.datipubblicati_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: datipubblicati_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE piao_private.datipubblicati_id_seq OWNED BY piao_private.datipubblicati.id;


--
-- Name: dichiarazionescadenza; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE piao_private.dichiarazionescadenza (
    id BIGSERIAL NOT NULL,
    annoriferimento integer NOT NULL,
    datapubblicazione date NOT NULL,
    note text,
    idmotivazionedichiarazione bigint NOT NULL,
    descrizione text,
    responsabile character varying(255),
    stato bool DEFAULT false,
    x_validity_in boolean DEFAULT true NOT NULL,
    x_createdby character varying(20) NOT NULL,
    x_created_ts date DEFAULT CURRENT_DATE NOT NULL,
    x_updatedby character varying(20),
    x_updated_ts date,
    idpiao bigint NOT NULL,
    x_createdbyrole character varying(50),
    x_updatedbyrole character varying(50),
    x_createdbynamesurname character varying(100),
    x_updatedbynamesurname character varying(100),
    x_active boolean DEFAULT true,
    x_deactivationtime timestamp without time zone
);


--
-- Name: dichiarazionescadenza_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE piao_private.dichiarazionescadenza_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: dichiarazionescadenza_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE piao_private.dichiarazionescadenza_id_seq OWNED BY piao_private.dichiarazionescadenza.id;


--
-- Name: dimensioneindicatore; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE piao_private.dimensioneindicatore (
    id BIGSERIAL NOT NULL,
    value character varying(200) NOT NULL,
    codtipologiafk character varying(20) NOT NULL
);


--
-- Name: eventorischio; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE piao_private.eventorischio (
    id BIGSERIAL NOT NULL,
    idattivitasensibile bigint NOT NULL,
    denominazione character varying(255) NOT NULL,
    probabilita integer,
    impatto integer,
    controlli text,
    valutazione integer,
    idlivellorischio bigint,
    x_validity_in boolean DEFAULT true NOT NULL,
    x_createdby character varying(20) DEFAULT 'ADMIN'::character varying NOT NULL,
    x_created_ts date DEFAULT CURRENT_DATE NOT NULL,
    x_updatedby character varying(20),
    x_updated_ts date,
    idsezione23 bigint,
    motivazione text,
    x_createdbyrole character varying(50),
    x_updatedbyrole character varying(50),
    x_createdbynamesurname character varying(100),
    x_updatedbynamesurname character varying(100),
    x_active boolean DEFAULT true,
    x_deactivationtime timestamp without time zone
);


--
-- Name: eventorischio_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE piao_private.eventorischio_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: eventorischio_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE piao_private.eventorischio_id_seq OWNED BY piao_private.eventorischio.id;


--
-- Name: fase; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE piao_private.fase (
    id BIGSERIAL NOT NULL,
    idsezione22 bigint NOT NULL,
    denominazione character varying(255),
    descrizione text,
    tempi character varying(100),
    x_validity_in boolean DEFAULT true NOT NULL,
    x_createdby character varying(20) NOT NULL,
    x_created_ts date DEFAULT CURRENT_DATE NOT NULL,
    x_updatedby character varying(20),
    x_updated_ts date,
    x_createdbyrole character varying(50),
    x_updatedbyrole character varying(50),
    x_createdbynamesurname character varying(100),
    x_updatedbynamesurname character varying(100),
    x_active boolean DEFAULT true,
    x_deactivationtime timestamp without time zone
);


--
-- Name: fase_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE piao_private.fase_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: fase_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE piao_private.fase_id_seq OWNED BY piao_private.fase.id;


--
-- Name: fondieuropei; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE piao_private.fondieuropei (
    id BIGSERIAL NOT NULL,
    idsezione21 bigint NOT NULL,
    progettofinanziato character varying(255),
    descrizione text,
    fondistanziati numeric,
    x_validity_in boolean DEFAULT true NOT NULL,
    x_createdby character varying(20),
    x_created_ts date DEFAULT CURRENT_DATE NOT NULL,
    x_updatedby character varying(20),
    x_updated_ts date,
    x_createdbyrole character varying(50),
    x_updatedbyrole character varying(50),
    x_createdbynamesurname character varying(100),
    x_updatedbynamesurname character varying(100),
    x_active boolean DEFAULT true,
    x_deactivationtime timestamp without time zone
);


--
-- Name: fondieuropei_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE piao_private.fondieuropei_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: fondieuropei_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE piao_private.fondieuropei_id_seq OWNED BY piao_private.fondieuropei.id;

--
-- Name: impattoindicatore_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE piao_private.impattoindicatore_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: impattoindicatore_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE piao_private.impattoindicatore_id_seq OWNED BY piao_private.dimensioneindicatore.id;


--
-- Name: indicatore; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE piao_private.indicatore (
    id BIGSERIAL NOT NULL,
    denominazione character varying(255),
    iddimensionefk bigint,
    idsubdimensionefk bigint,
    unitamisura character varying(100),
    formula text,
    peso numeric,
    polarita character varying(50),
    baseline numeric,
    consuntivo numeric,
    fontedati character varying(100),
    idtipandvalannocorrente bigint,
    idtipandvalanno1 bigint,
    idtipandvalanno2 bigint,
    rilevante boolean,
    x_validity_in boolean DEFAULT true NOT NULL,
    x_createdby character varying(20) NOT NULL,
    x_created_ts date DEFAULT CURRENT_DATE NOT NULL,
    x_updatedby character varying(20),
    x_updated_ts date,
    idpiao bigint,
    identitafk bigint,
    codtipologiafk character varying(150),
    x_createdbyrole character varying(50),
    x_updatedbyrole character varying(50),
    x_createdbynamesurname character varying(100),
    x_updatedbynamesurname character varying(100),
    x_active boolean DEFAULT true,
    x_deactivationtime timestamp without time zone
);


--
-- Name: indicatore_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE piao_private.indicatore_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: indicatore_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE piao_private.indicatore_id_seq OWNED BY piao_private.indicatore.id;


--
-- Name: integrationteam; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE piao_private.integrationteam (
    id BIGSERIAL NOT NULL,
    idsezione1 bigint NOT NULL,
    membro character varying(255),
    ruolo character varying(255),
    x_validity_in boolean DEFAULT true NOT NULL,
    x_createdby character varying(20) NOT NULL,
    x_created_ts date DEFAULT CURRENT_DATE NOT NULL,
    x_updatedby character varying(20),
    x_updated_ts date,
    x_createdbyrole character varying(50),
    x_updatedbyrole character varying(50),
    x_createdbynamesurname character varying(100),
    x_updatedbynamesurname character varying(100),
    x_active boolean DEFAULT true,
    x_deactivationtime timestamp without time zone
);


--
-- Name: integrationteam_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE piao_private.integrationteam_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: integrationteam_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE piao_private.integrationteam_id_seq OWNED BY piao_private.integrationteam.id;


--
-- Name: livellorischio; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE piao_private.livellorischio (
    id BIGSERIAL NOT NULL,
    testo character varying(255) NOT NULL,
    x_validity_in boolean DEFAULT true NOT NULL,
    x_createdby character varying(20) DEFAULT 'ADMIN'::character varying NOT NULL,
    x_created_ts date DEFAULT CURRENT_DATE NOT NULL,
    x_updatedby character varying(20),
    x_updated_ts date,
    x_createdbyrole character varying(50),
    x_updatedbyrole character varying(50),
    x_createdbynamesurname character varying(100),
    x_updatedbynamesurname character varying(100),
    x_active boolean DEFAULT true,
    x_deactivationtime timestamp without time zone
);


--
-- Name: livellorischio_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE piao_private.livellorischio_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: livellorischio_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE piao_private.livellorischio_id_seq OWNED BY piao_private.livellorischio.id;


--
-- Name: log_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE piao_private.log_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: milestone; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE piao_private.milestone (
    id BIGSERIAL NOT NULL,
    idsottofasemonitoraggio bigint NOT NULL,
    data timestamp without time zone NOT NULL,
    descrizione text NOT NULL,
    ispromemoria boolean,
    idpromemoria bigint,
    x_validity_in boolean DEFAULT true NOT NULL,
    x_createdby character varying(20) DEFAULT 'ADMIN'::character varying NOT NULL,
    x_created_ts date DEFAULT CURRENT_DATE NOT NULL,
    x_updatedby character varying(20),
    x_updated_ts date,
    x_createdbyrole character varying(50),
    x_updatedbyrole character varying(50),
    x_createdbynamesurname character varying(100),
    x_updatedbynamesurname character varying(100),
    datapromemoria timestamp without time zone,
    x_active boolean DEFAULT true,
    x_deactivationtime timestamp without time zone
);


--
-- Name: milestone_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE piao_private.milestone_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: milestone_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE piao_private.milestone_id_seq OWNED BY piao_private.milestone.id;


--
-- Name: misuraprevenzione; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE piao_private.misuraprevenzione (
    id BIGSERIAL NOT NULL,
    idsezione23 bigint,
    idobiettivoprevenzione bigint,
    denominazione character varying(255),
    descrizione text,
    x_validity_in boolean DEFAULT true NOT NULL,
    x_createdby character varying(20) NOT NULL,
    x_created_ts date DEFAULT CURRENT_DATE NOT NULL,
    x_updatedby character varying(20),
    x_updated_ts date,
    codice character varying(50),
    responsabilemisura character varying(255),
    x_createdbyrole character varying(50),
    x_updatedbyrole character varying(50),
    x_createdbynamesurname character varying(100),
    x_updatedbynamesurname character varying(100),
    x_active boolean DEFAULT true,
    x_deactivationtime timestamp without time zone
);


--
-- Name: misuraprevenzione_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE piao_private.misuraprevenzione_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: misuraprevenzione_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE piao_private.misuraprevenzione_id_seq OWNED BY piao_private.misuraprevenzione.id;


--
-- Name: misuraprevenzioneeventorischio; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE piao_private.misuraprevenzioneeventorischio (
    id BIGSERIAL NOT NULL,
    ideventorischio bigint NOT NULL,
    idobiettivoprevenzionecorruzionetrasparenza bigint,
    codice character varying(50) NOT NULL,
    denominazione character varying(255),
    descrizione text,
    responsabile character varying(255),
    x_validity_in boolean DEFAULT true NOT NULL,
    x_createdby character varying(20) DEFAULT 'ADMIN'::character varying NOT NULL,
    x_created_ts date DEFAULT CURRENT_DATE NOT NULL,
    x_updatedby character varying(20),
    x_updated_ts date,
    x_createdbyrole character varying(50),
    x_updatedbyrole character varying(50),
    x_createdbynamesurname character varying(100),
    x_updatedbynamesurname character varying(100),
    x_active boolean DEFAULT true,
    x_deactivationtime timestamp without time zone
);


--
-- Name: misuraprevenzioneeventorischio_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE piao_private.misuraprevenzioneeventorischio_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: misuraprevenzioneeventorischio_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE piao_private.misuraprevenzioneeventorischio_id_seq OWNED BY piao_private.misuraprevenzioneeventorischio.id;


--
-- Name: misuraprevenzioneeventorischioindicatore; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE piao_private.misuraprevenzioneeventorischioindicatore (
    id BIGSERIAL NOT NULL,
    idmisuraprevenzioneeventorischio bigint NOT NULL,
    idindicatore bigint NOT NULL,
    x_validity_in boolean DEFAULT true NOT NULL,
    x_createdby character varying(20) DEFAULT 'ADMIN'::character varying NOT NULL,
    x_created_ts date DEFAULT CURRENT_DATE NOT NULL,
    x_updatedby character varying(20),
    x_updated_ts date,
    x_createdbyrole character varying(50),
    x_updatedbyrole character varying(50),
    x_createdbynamesurname character varying(100),
    x_updatedbynamesurname character varying(100),
    x_active boolean DEFAULT true,
    x_deactivationtime timestamp without time zone
);


--
-- Name: misuraprevenzioneeventorischioindicatore_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE piao_private.misuraprevenzioneeventorischioindicatore_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: misuraprevenzioneeventorischioindicatore_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE piao_private.misuraprevenzioneeventorischioindicatore_id_seq OWNED BY piao_private.misuraprevenzioneeventorischioindicatore.id;


--
-- Name: misuraprevenzioneeventorischiostakeholder; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE piao_private.misuraprevenzioneeventorischiostakeholder (
    id BIGSERIAL NOT NULL,
    idmisuraprevenzioneeventorischio bigint NOT NULL,
    idstakeholder bigint NOT NULL,
    x_validity_in boolean DEFAULT true NOT NULL,
    x_createdby character varying(20) DEFAULT 'ADMIN'::character varying NOT NULL,
    x_created_ts date DEFAULT CURRENT_DATE NOT NULL,
    x_updatedby character varying(20),
    x_updated_ts date,
    x_createdbyrole character varying(50),
    x_updatedbyrole character varying(50),
    x_createdbynamesurname character varying(100),
    x_updatedbynamesurname character varying(100),
    x_active boolean DEFAULT true,
    x_deactivationtime timestamp without time zone
);


--
-- Name: misuraprevenzioneeventorischiostakeholder_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE piao_private.misuraprevenzioneeventorischiostakeholder_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: misuraprevenzioneeventorischiostakeholder_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE piao_private.misuraprevenzioneeventorischiostakeholder_id_seq OWNED BY piao_private.misuraprevenzioneeventorischiostakeholder.id;


--
-- Name: misuraprevenzioneindicatore; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE piao_private.misuraprevenzioneindicatore (
    id BIGSERIAL NOT NULL,
    idmisuraprevenzione bigint NOT NULL,
    idindicatore bigint NOT NULL,
    x_validity_in boolean DEFAULT true NOT NULL,
    x_createdby character varying(20) DEFAULT 'ADMIN'::character varying NOT NULL,
    x_created_ts date DEFAULT CURRENT_DATE NOT NULL,
    x_updatedby character varying(20),
    x_updated_ts date,
    x_createdbyrole character varying(50),
    x_updatedbyrole character varying(50),
    x_createdbynamesurname character varying(100),
    x_updatedbynamesurname character varying(100),
    x_active boolean DEFAULT true,
    x_deactivationtime timestamp without time zone
);


--
-- Name: misuraprevenzioneindicatore_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE piao_private.misuraprevenzioneindicatore_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: misuraprevenzioneindicatore_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE piao_private.misuraprevenzioneindicatore_id_seq OWNED BY piao_private.misuraprevenzioneindicatore.id;


--
-- Name: misuraprevenzionestakeholder; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE piao_private.misuraprevenzionestakeholder (
    id BIGSERIAL NOT NULL,
    idmisuraprevenzione bigint NOT NULL,
    idstakeholder bigint NOT NULL,
    x_validity_in boolean DEFAULT true NOT NULL,
    x_createdby character varying(20) NOT NULL,
    x_created_ts date DEFAULT CURRENT_DATE NOT NULL,
    x_updatedby character varying(20),
    x_updated_ts date,
    x_createdbyrole character varying(50),
    x_updatedbyrole character varying(50),
    x_createdbynamesurname character varying(100),
    x_updatedbynamesurname character varying(100),
    x_active boolean DEFAULT true,
    x_deactivationtime timestamp without time zone
);


--
-- Name: misuraprevenzionestakeholder_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE piao_private.misuraprevenzionestakeholder_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: misuraprevenzionestakeholder_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE piao_private.misuraprevenzionestakeholder_id_seq OWNED BY piao_private.misuraprevenzionestakeholder.id;


--
-- Name: monitoraggioprevenzione; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE piao_private.monitoraggioprevenzione (
    id BIGSERIAL NOT NULL,
    idmisuraprevenzioneeventorischio bigint,
    tipologia character varying(255),
    descrizione text NOT NULL,
    responsabile character varying(255),
    tempistiche character varying(255),
    x_validity_in boolean DEFAULT true NOT NULL,
    x_createdby character varying(20) NOT NULL,
    x_created_ts date DEFAULT CURRENT_DATE NOT NULL,
    x_updatedby character varying(20),
    x_updated_ts date,
    x_createdbyrole character varying(50),
    x_updatedbyrole character varying(50),
    x_createdbynamesurname character varying(100),
    x_updatedbynamesurname character varying(100),
    x_active boolean DEFAULT true,
    x_deactivationtime timestamp without time zone
);


--
-- Name: monitoraggioprevenzione_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE piao_private.monitoraggioprevenzione_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: monitoraggioprevenzione_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE piao_private.monitoraggioprevenzione_id_seq OWNED BY piao_private.monitoraggioprevenzione.id;


--
-- Name: motivazionedichiarazione; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE piao_private.motivazionedichiarazione (
    id BIGSERIAL NOT NULL,
    testo text,
    x_validity_in boolean DEFAULT true NOT NULL,
    x_createdby character varying(20) NOT NULL,
    x_created_ts date DEFAULT CURRENT_DATE NOT NULL,
    x_updatedby character varying(20),
    x_updated_ts date,
    x_createdbyrole character varying(50),
    x_updatedbyrole character varying(50),
    x_createdbynamesurname character varying(100),
    x_updatedbynamesurname character varying(100),
    x_active boolean DEFAULT true,
    x_deactivationtime timestamp without time zone
);


--
-- Name: motivazionedichiarazione_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE piao_private.motivazionedichiarazione_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: motivazionedichiarazione_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE piao_private.motivazionedichiarazione_id_seq OWNED BY piao_private.motivazionedichiarazione.id;


--
-- Name: notify_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE piao_private.notify_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: obbligolegge; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE piao_private.obbligolegge (
    id BIGSERIAL NOT NULL,
    idsezione23 bigint NOT NULL,
    denominazione character varying(255),
    descrizione text,
    x_validity_in boolean DEFAULT true NOT NULL,
    x_createdby character varying(20) DEFAULT 'ADMIN'::character varying NOT NULL,
    x_created_ts date DEFAULT CURRENT_DATE NOT NULL,
    x_updatedby character varying(20),
    x_updated_ts date,
    x_createdbyrole character varying(50),
    x_updatedbyrole character varying(50),
    x_createdbynamesurname character varying(100),
    x_updatedbynamesurname character varying(100),
    x_active boolean DEFAULT true,
    x_deactivationtime timestamp without time zone
);


--
-- Name: obbligolegge_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE piao_private.obbligolegge_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: obbligolegge_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE piao_private.obbligolegge_id_seq OWNED BY piao_private.obbligolegge.id;


--
-- Name: obiettivirisultatifotografia; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE piao_private.obiettivirisultatifotografia (
    id BIGSERIAL NOT NULL,
    idsezione332 bigint NOT NULL,
    codtipologiafk character varying(255) NOT NULL,
    idtipologiaattivita bigint,
    idambitocompetenza bigint,
    idareatematica bigint,
    idtipologiadestinatari bigint,
    codice character varying(50),
    titolo text,
    carattereobbligatorio boolean,
    riferimentonormativo text,
    targetdirigenti character varying(255),
    targetnondirigenti character varying(255),
    numerodirigenti numeric,
    numeronondirigenti numeric,
    oreformazione double precision,
    verificaapprendimento boolean,
    creditiformativi double precision,
    modalitagestioneformazione character varying(255),
    enteerogatore text,
    costoattivita character varying(255),
    datainizio date,
    datafine date,
    x_validity_in boolean DEFAULT true NOT NULL,
    x_createdby character varying(20) NOT NULL,
    x_created_ts date DEFAULT CURRENT_DATE NOT NULL,
    x_updatedby character varying(20),
    x_updated_ts date,
    x_createdbyrole character varying(50),
    x_updatedbyrole character varying(50),
    x_createdbynamesurname character varying(100),
    x_updatedbynamesurname character varying(100),
    x_active boolean DEFAULT true,
    x_deactivationtime timestamp without time zone
);


--
-- Name: obiettivirisultatifotografia_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE piao_private.obiettivirisultatifotografia_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: obiettivirisultatifotografia_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE piao_private.obiettivirisultatifotografia_id_seq OWNED BY piao_private.obiettivirisultatifotografia.id;


--
-- Name: obiettivoperformance; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE piao_private.obiettivoperformance (
    id BIGSERIAL NOT NULL,
    idsezione22 bigint NOT NULL,
    idovp bigint,
    idstrategiaovp bigint,
    idobiettivopeformance bigint,
    codice character varying(255),
    codtipologiafk character varying(50) NOT NULL,
    denominazione character varying(255),
    responsabileamministrativo character varying(255),
    risorseumane character varying(255),
    risorseeconomicafinanziaria character varying(255),
    risorsestrumentali character varying(255),
    tipologiarisorsa character varying(255),
    x_validity_in boolean DEFAULT true NOT NULL,
    x_createdby character varying(20) DEFAULT 'ADMIN'::character varying NOT NULL,
    x_created_ts date DEFAULT CURRENT_DATE NOT NULL,
    x_updatedby character varying(20),
    x_updated_ts date,
    x_createdbyrole character varying(50),
    x_updatedbyrole character varying(50),
    x_createdbynamesurname character varying(100),
    x_updatedbynamesurname character varying(100),
    x_active boolean DEFAULT true,
    x_deactivationtime timestamp without time zone
);


--
-- Name: obiettivoperformance_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE piao_private.obiettivoperformance_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: obiettivoperformance_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE piao_private.obiettivoperformance_id_seq OWNED BY piao_private.obiettivoperformance.id;


--
-- Name: obiettivoperformanceindicatore; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE piao_private.obiettivoperformanceindicatore (
    id BIGSERIAL NOT NULL,
    idobiettivoperformance bigint NOT NULL,
    idindicatore bigint NOT NULL,
    x_validity_in boolean DEFAULT true NOT NULL,
    x_createdby character varying(20) DEFAULT 'ADMIN'::character varying NOT NULL,
    x_created_ts date DEFAULT CURRENT_DATE NOT NULL,
    x_updatedby character varying(20),
    x_updated_ts date,
    x_createdbyrole character varying(50),
    x_updatedbyrole character varying(50),
    x_createdbynamesurname character varying(100),
    x_updatedbynamesurname character varying(100),
    x_active boolean DEFAULT true,
    x_deactivationtime timestamp without time zone
);


--
-- Name: obiettivoperformanceindicatore_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE piao_private.obiettivoperformanceindicatore_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: obiettivoperformanceindicatore_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE piao_private.obiettivoperformanceindicatore_id_seq OWNED BY piao_private.obiettivoperformanceindicatore.id;


--
-- Name: obiettivoperformancestakeholder; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE piao_private.obiettivoperformancestakeholder (
    id BIGSERIAL NOT NULL,
    idobiettivoperformance bigint NOT NULL,
    idstakeholder bigint NOT NULL,
    x_validity_in boolean DEFAULT true NOT NULL,
    x_createdby character varying(20) DEFAULT 'ADMIN'::character varying NOT NULL,
    x_created_ts date DEFAULT CURRENT_DATE NOT NULL,
    x_updatedby character varying(20),
    x_updated_ts date,
    x_createdbyrole character varying(50),
    x_updatedbyrole character varying(50),
    x_createdbynamesurname character varying(100),
    x_updatedbynamesurname character varying(100),
    x_active boolean DEFAULT true,
    x_deactivationtime timestamp without time zone
);


--
-- Name: obiettivoperformancestakeholder_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE piao_private.obiettivoperformancestakeholder_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: obiettivoperformancestakeholder_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE piao_private.obiettivoperformancestakeholder_id_seq OWNED BY piao_private.obiettivoperformancestakeholder.id;


--
-- Name: obiettivoprevenzione; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE piao_private.obiettivoprevenzione (
    id BIGSERIAL NOT NULL,
    idsezione23 bigint NOT NULL,
    denominazione character varying(255),
    descrizione text,
    x_validity_in boolean DEFAULT true NOT NULL,
    x_createdby character varying(20) NOT NULL,
    x_created_ts date DEFAULT CURRENT_DATE NOT NULL,
    x_updatedby character varying(20),
    x_updated_ts date,
    codice character varying(50) NOT NULL,
    x_createdbyrole character varying(50),
    x_updatedbyrole character varying(50),
    x_createdbynamesurname character varying(100),
    x_updatedbynamesurname character varying(100),
    x_active boolean DEFAULT true,
    x_deactivationtime timestamp without time zone
);


--
-- Name: obiettivoprevenzione_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE piao_private.obiettivoprevenzione_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: obiettivoprevenzione_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE piao_private.obiettivoprevenzione_id_seq OWNED BY piao_private.obiettivoprevenzione.id;


--
-- Name: obiettivoprevenzionecorruzionetrasparenza; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE piao_private.obiettivoprevenzionecorruzionetrasparenza (
    id BIGSERIAL NOT NULL,
    idsezione23 bigint NOT NULL,
    idovp bigint,
    codice character varying(50) NOT NULL,
    denominazione character varying(255),
    descrizione character varying(255),
    x_validity_in boolean DEFAULT true NOT NULL,
    x_createdby character varying(20) DEFAULT 'ADMIN'::character varying NOT NULL,
    x_created_ts date DEFAULT CURRENT_DATE NOT NULL,
    x_updatedby character varying(20),
    x_updated_ts date,
    idstrategiaovp bigint,
    idobbiettivoperformance bigint,
    x_createdbyrole character varying(50),
    x_updatedbyrole character varying(50),
    x_createdbynamesurname character varying(100),
    x_updatedbynamesurname character varying(100),
    x_active boolean DEFAULT true,
    x_deactivationtime timestamp without time zone
);


--
-- Name: obiettivoprevenzionecorruzionetrasparenza_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE piao_private.obiettivoprevenzionecorruzionetrasparenza_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: obiettivoprevenzionecorruzionetrasparenza_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE piao_private.obiettivoprevenzionecorruzionetrasparenza_id_seq OWNED BY piao_private.obiettivoprevenzionecorruzionetrasparenza.id;


--
-- Name: obiettivoprevenzionecorruzionetrasparenzaindicatori; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE piao_private.obiettivoprevenzionecorruzionetrasparenzaindicatori (
    id BIGSERIAL NOT NULL,
    idobiettivoprevenzionecorruzionetrasparenza bigint NOT NULL,
    idindicatore bigint NOT NULL,
    x_validity_in boolean DEFAULT true NOT NULL,
    x_createdby character varying(20) DEFAULT 'ADMIN'::character varying NOT NULL,
    x_created_ts date DEFAULT CURRENT_DATE NOT NULL,
    x_updatedby character varying(20),
    x_updated_ts date,
    x_createdbyrole character varying(50),
    x_updatedbyrole character varying(50),
    x_createdbynamesurname character varying(100),
    x_updatedbynamesurname character varying(100),
    x_active boolean DEFAULT true,
    x_deactivationtime timestamp without time zone
);


--
-- Name: obiettivoprevenzionecorruzionetrasparenzaindicatori_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE piao_private.obiettivoprevenzionecorruzionetrasparenzaindicatori_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: obiettivoprevenzionecorruzionetrasparenzaindicatori_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE piao_private.obiettivoprevenzionecorruzionetrasparenzaindicatori_id_seq OWNED BY piao_private.obiettivoprevenzionecorruzionetrasparenzaindicatori.id;


--
-- Name: obiettivoprevenzioneindicatore; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE piao_private.obiettivoprevenzioneindicatore (
    id BIGSERIAL NOT NULL,
    idobiettivoprevenzione bigint NOT NULL,
    idindicatore bigint NOT NULL,
    x_validity_in boolean DEFAULT true NOT NULL,
    x_createdby character varying(20) DEFAULT 'ADMIN'::character varying NOT NULL,
    x_created_ts date DEFAULT CURRENT_DATE NOT NULL,
    x_updatedby character varying(20),
    x_updated_ts date,
    x_createdbyrole character varying(50),
    x_updatedbyrole character varying(50),
    x_createdbynamesurname character varying(100),
    x_updatedbynamesurname character varying(100),
    x_active boolean DEFAULT true,
    x_deactivationtime timestamp without time zone
);


--
-- Name: obiettivoprevenzioneindicatore_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE piao_private.obiettivoprevenzioneindicatore_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: obiettivoprevenzioneindicatore_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE piao_private.obiettivoprevenzioneindicatore_id_seq OWNED BY piao_private.obiettivoprevenzioneindicatore.id;


--
-- Name: organopolitico; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE piao_private.organopolitico (
    id BIGSERIAL NOT NULL,
    idsezione1 bigint NOT NULL,
    organo character varying(255),
    ruolo text,
    x_validity_in boolean DEFAULT true NOT NULL,
    x_createdby character varying(20) NOT NULL,
    x_created_ts date DEFAULT CURRENT_DATE NOT NULL,
    x_updatedby character varying(20),
    x_updated_ts date,
    x_createdbyrole character varying(50),
    x_updatedbyrole character varying(50),
    x_createdbynamesurname character varying(100),
    x_updatedbynamesurname character varying(100),
    x_active boolean DEFAULT true,
    x_deactivationtime timestamp without time zone
);


--
-- Name: organopolitico_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE piao_private.organopolitico_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: organopolitico_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE piao_private.organopolitico_id_seq OWNED BY piao_private.organopolitico.id;


--
-- Name: ovp; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE piao_private.ovp (
    id BIGSERIAL NOT NULL,
    idsezione21 bigint NOT NULL,
    codice character varying(100),
    descrizione text,
    contesto text,
    ambito text,
    responsabilepolitico character varying(255),
    responsabileamministrativo character varying(255),
    valoreindice numeric,
    descrizioneindice text,
    x_validity_in boolean DEFAULT true NOT NULL,
    x_createdby character varying(20) NOT NULL,
    x_created_ts date DEFAULT CURRENT_DATE NOT NULL,
    x_updatedby character varying(20),
    x_updated_ts date,
    denominazione character varying(255),
    x_createdbyrole character varying(50),
    x_updatedbyrole character varying(50),
    x_createdbynamesurname character varying(100),
    x_updatedbynamesurname character varying(100),
    x_active boolean DEFAULT true,
    x_deactivationtime timestamp without time zone
);


--
-- Name: ovp_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE piao_private.ovp_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: ovp_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE piao_private.ovp_id_seq OWNED BY piao_private.ovp.id;


--
-- Name: ovp_sto; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE piao_private.ovp_sto (
    id BIGSERIAL NOT NULL,
    idsezione21 bigint,
    codice character varying(100),
    descrizione text,
    contesto text,
    ambito text,
    responsabilepolitico character varying(255),
    responsabileamministrativo character varying(255),
    valoreindice numeric,
    descrizioneindice text,
    x_validity_in boolean DEFAULT true,
    x_createdby character varying(20),
    x_created_ts date DEFAULT CURRENT_DATE,
    x_updatedby character varying(20),
    x_updated_ts date,
    rev integer NOT NULL,
    revtype smallint NOT NULL,
    denominazione character varying,
    x_createdbyrole character varying(50),
    x_updatedbyrole character varying(50),
    x_createdbynamesurname character varying(100),
    x_updatedbynamesurname character varying(100)
);


--
-- Name: ovp_sto_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE piao_private.ovp_sto_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: ovp_sto_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE piao_private.ovp_sto_id_seq OWNED BY piao_private.ovp_sto.id;


--
-- Name: ovpareaorganizzativa; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE piao_private.ovpareaorganizzativa (
    id BIGSERIAL NOT NULL,
    idovp bigint NOT NULL,
    idareaorganizzativa bigint NOT NULL,
    x_validity_in boolean DEFAULT true NOT NULL,
    x_createdby character varying(20) NOT NULL,
    x_created_ts date DEFAULT CURRENT_DATE NOT NULL,
    x_updatedby character varying(20),
    x_updated_ts date,
    x_createdbyrole character varying(50),
    x_updatedbyrole character varying(50),
    x_createdbynamesurname character varying(100),
    x_updatedbynamesurname character varying(100),
    x_active boolean DEFAULT true,
    x_deactivationtime timestamp without time zone
);


--
-- Name: ovpareaorganizzativa_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE piao_private.ovpareaorganizzativa_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: ovpareaorganizzativa_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE piao_private.ovpareaorganizzativa_id_seq OWNED BY piao_private.ovpareaorganizzativa.id;


--
-- Name: ovpprioritapolitica; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE piao_private.ovpprioritapolitica (
    id BIGSERIAL NOT NULL,
    idovp bigint NOT NULL,
    idprioritapolitica bigint NOT NULL,
    x_validity_in boolean DEFAULT true NOT NULL,
    x_createdby character varying(20) NOT NULL,
    x_created_ts date DEFAULT CURRENT_DATE NOT NULL,
    x_updatedby character varying(20),
    x_updated_ts date,
    x_createdbyrole character varying(50),
    x_updatedbyrole character varying(50),
    x_createdbynamesurname character varying(100),
    x_updatedbynamesurname character varying(100),
    x_active boolean DEFAULT true,
    x_deactivationtime timestamp without time zone
);


--
-- Name: ovpprioritapolitica_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE piao_private.ovpprioritapolitica_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: ovpprioritapolitica_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE piao_private.ovpprioritapolitica_id_seq OWNED BY piao_private.ovpprioritapolitica.id;


--
-- Name: ovprisorsafinanziaria; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE piao_private.ovprisorsafinanziaria (
    id BIGSERIAL NOT NULL,
    idovp bigint NOT NULL,
    iniziativa character varying(255),
    descrizione text,
    dotazionefinanziaria numeric,
    fontefinanziamento character varying(255),
    x_validity_in boolean DEFAULT true NOT NULL,
    x_createdby character varying(20) NOT NULL,
    x_created_ts date DEFAULT CURRENT_DATE NOT NULL,
    x_updatedby character varying(20),
    x_updated_ts date,
    x_createdbyrole character varying(50),
    x_updatedbyrole character varying(50),
    x_createdbynamesurname character varying(100),
    x_updatedbynamesurname character varying(100),
    x_active boolean DEFAULT true,
    x_deactivationtime timestamp without time zone
);


--
-- Name: ovprisorsafinanziaria_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE piao_private.ovprisorsafinanziaria_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: ovprisorsafinanziaria_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE piao_private.ovprisorsafinanziaria_id_seq OWNED BY piao_private.ovprisorsafinanziaria.id;


--
-- Name: ovpstakeholder; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE piao_private.ovpstakeholder (
    id BIGSERIAL NOT NULL,
    idovp bigint NOT NULL,
    idstakeholder bigint NOT NULL,
    x_validity_in boolean DEFAULT true NOT NULL,
    x_createdby character varying(20) NOT NULL,
    x_created_ts date DEFAULT CURRENT_DATE NOT NULL,
    x_updatedby character varying(20),
    x_updated_ts date,
    x_createdbyrole character varying(50),
    x_updatedbyrole character varying(50),
    x_createdbynamesurname character varying(100),
    x_updatedbynamesurname character varying(100),
    x_active boolean DEFAULT true,
    x_deactivationtime timestamp without time zone
);


--
-- Name: ovpstakeholder_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE piao_private.ovpstakeholder_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: ovpstakeholder_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE piao_private.ovpstakeholder_id_seq OWNED BY piao_private.ovpstakeholder.id;


--
-- Name: ovpstrategia; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE piao_private.ovpstrategia (
    id BIGSERIAL NOT NULL,
    idovp bigint NOT NULL,
    codstrategia character varying(100),
    denominazionestrategia character varying(255),
    descrizionestrategia text,
    soggettoresponsabile character varying(255),
    x_validity_in boolean DEFAULT true NOT NULL,
    x_createdby character varying(20) NOT NULL,
    x_created_ts date DEFAULT CURRENT_DATE NOT NULL,
    x_updatedby character varying(20),
    x_updated_ts date,
    x_createdbyrole character varying(50),
    x_updatedbyrole character varying(50),
    x_createdbynamesurname character varying(100),
    x_updatedbynamesurname character varying(100),
    x_active boolean DEFAULT true,
    x_deactivationtime timestamp without time zone
);


--
-- Name: ovpstrategia_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE piao_private.ovpstrategia_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: ovpstrategia_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE piao_private.ovpstrategia_id_seq OWNED BY piao_private.ovpstrategia.id;


--
-- Name: ovpstrategiaindicatore; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE piao_private.ovpstrategiaindicatore (
    id BIGSERIAL NOT NULL,
    idovpstrategia bigint,
    idindicatore bigint,
    x_validity_in boolean DEFAULT true NOT NULL,
    x_createdby character varying(20) NOT NULL,
    x_created_ts date DEFAULT CURRENT_DATE NOT NULL,
    x_updatedby character varying(20),
    x_updated_ts date,
    x_createdbyrole character varying(50),
    x_updatedbyrole character varying(50),
    x_createdbynamesurname character varying(100),
    x_updatedbynamesurname character varying(100),
    x_active boolean DEFAULT true,
    x_deactivationtime timestamp without time zone
);


--
-- Name: ovpstrategiaindicatore_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE piao_private.ovpstrategiaindicatore_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: ovpstrategiaindicatore_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE piao_private.ovpstrategiaindicatore_id_seq OWNED BY piao_private.ovpstrategiaindicatore.id;


--
-- Name: piao; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE piao_private.piao (
    id BIGSERIAL NOT NULL,
    codpafk character varying(255) NOT NULL,
    denominazione character varying(255) NOT NULL,
    versione character varying(50),
    tipologia character varying(50),
    tipologiaonline character varying(50),
    datascadenza date,
    idstato bigint,
    x_validity_in boolean DEFAULT true NOT NULL,
    x_createdby character varying(20),
    x_created_ts date DEFAULT CURRENT_DATE NOT NULL,
    x_updatedby character varying(20),
    x_updated_ts date,
    dataapprovazione date,
    url character varying(255),
    x_createdbyrole character varying(50),
    x_updatedbyrole character varying(50),
    x_createdbynamesurname character varying(100),
    x_updatedbynamesurname character varying(100),
    iscompilatonormativa boolean DEFAULT false,
    idautoritaapprovatore bigint,
    estremiattoapprovazione character varying(255),
    triennioriferimento character varying(35),
    datapubblicazione date,
    x_active boolean DEFAULT true,
    x_deactivationtime timestamp without time zone,
    denominazionepa text,
    changedtipologia boolean DEFAULT false,
    CONSTRAINT piao_tipologia_check CHECK ((upper((tipologia)::text) = ANY (ARRAY['ONLINE'::text, 'PDF'::text]))),
    CONSTRAINT piao_tipologiaonline_check CHECK ((upper((tipologiaonline)::text) = ANY (ARRAY['ORDINARIO'::text, 'SEMPLIFICATO'::text])))
);

-- INDEXES table PIAO
CREATE INDEX piao_tipologia_idx ON piao.private (tipologia);
CREATE INDEX piao_x_createdby_idx ON piao.private (x_createdby);


--
-- Name: piao_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE piao_private.piao_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: piao_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE piao_private.piao_id_seq OWNED BY piao_private.piao.id;


--
-- Name: prevenzioneeventorischio; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE piao_private.prevenzioneeventorischio (
    id BIGSERIAL NOT NULL,
    ideventorischio bigint NOT NULL,
    idobiettivoprevenzione bigint NOT NULL,
    codice character varying(50) NOT NULL,
    denominazione character varying(255) NOT NULL,
    descrizione text,
    responsabile character varying(255),
    x_validity_in boolean DEFAULT true NOT NULL,
    x_createdby character varying(20) DEFAULT 'ADMIN'::character varying NOT NULL,
    x_created_ts date DEFAULT CURRENT_DATE NOT NULL,
    x_updatedby character varying(20),
    x_updated_ts date
);


--
-- Name: prevenzioneeventorischio_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE piao_private.prevenzioneeventorischio_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: prevenzioneeventorischio_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE piao_private.prevenzioneeventorischio_id_seq OWNED BY piao_private.prevenzioneeventorischio.id;


--
-- Name: principioguida; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE piao_private.principioguida (
    id BIGSERIAL NOT NULL,
    idsezione1 bigint NOT NULL,
    nomeprincipioguida character varying(255),
    descrizioneprincipioguida text,
    x_active boolean DEFAULT true,
    x_deactivationtime timestamp without time zone,
    x_validity_in boolean DEFAULT true NOT NULL,
    x_createdby character varying(20) NOT NULL,
    x_created_ts date DEFAULT CURRENT_DATE NOT NULL,
    x_updatedby character varying(20),
    x_updated_ts date,
    x_createdbyrole character varying(50),
    x_updatedbyrole character varying(50),
    x_createdbynamesurname character varying(100),
    x_updatedbynamesurname character varying(100)
);


--
-- Name: principioguida_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE piao_private.principioguida_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: principioguida_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE piao_private.principioguida_id_seq OWNED BY piao_private.principioguida.id;


--
-- Name: prioritapolitica; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE piao_private.prioritapolitica (
    id BIGSERIAL NOT NULL,
    idsezione1 bigint NOT NULL,
    nomeprioritapolitica character varying(255),
    descrizioneprioritapolitica text,
    x_active boolean DEFAULT true,
    x_deactivationtime timestamp without time zone,
    x_validity_in boolean DEFAULT true NOT NULL,
    x_createdby character varying(20) NOT NULL,
    x_created_ts date DEFAULT CURRENT_DATE NOT NULL,
    x_updatedby character varying(20),
    x_updated_ts date,
    x_createdbyrole character varying(50),
    x_updatedbyrole character varying(50),
    x_createdbynamesurname character varying(100),
    x_updatedbynamesurname character varying(100)
);


--
-- Name: prioritapolitica_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE piao_private.prioritapolitica_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: prioritapolitica_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE piao_private.prioritapolitica_id_seq OWNED BY piao_private.prioritapolitica.id;


--
-- Name: procedura; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE piao_private.procedura (
    id BIGSERIAL NOT NULL,
    idsezione21 bigint NOT NULL,
    denominazione character varying(255),
    descrizione text,
    unitamisura character varying(100),
    misurazione character varying(100),
    target character varying(100),
    uffresponsabile character varying(255),
    x_validity_in boolean DEFAULT true NOT NULL,
    x_createdby character varying(20) NOT NULL,
    x_created_ts date DEFAULT CURRENT_DATE NOT NULL,
    x_updatedby character varying(20),
    x_updated_ts date,
    x_createdbyrole character varying(50),
    x_updatedbyrole character varying(50),
    x_createdbynamesurname character varying(100),
    x_updatedbynamesurname character varying(100),
    x_active boolean DEFAULT true,
    x_deactivationtime timestamp without time zone
);


--
-- Name: procedura_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE piao_private.procedura_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: procedura_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE piao_private.procedura_id_seq OWNED BY piao_private.procedura.id;


--
-- Name: promemoria; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE piao_private.promemoria (
    id BIGSERIAL NOT NULL,
    descrizione text NOT NULL,
    x_validity_in boolean DEFAULT true NOT NULL,
    x_createdby character varying(20) DEFAULT 'ADMIN'::character varying NOT NULL,
    x_created_ts date DEFAULT CURRENT_DATE NOT NULL,
    x_updatedby character varying(20),
    x_updated_ts date,
    x_createdbyrole character varying(50),
    x_updatedbyrole character varying(50),
    x_createdbynamesurname character varying(100),
    x_updatedbynamesurname character varying(100),
    x_active boolean DEFAULT true,
    x_deactivationtime timestamp without time zone
);


--
-- Name: promemoria_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE piao_private.promemoria_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: promemoria_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE piao_private.promemoria_id_seq OWNED BY piao_private.promemoria.id;


--
-- Name: revinfo; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE piao_private.revinfo (
    id integer NOT NULL,
    "timestamp" bigint
);


--
-- Name: revinfo_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE piao_private.revinfo_seq
    START WITH 1
    INCREMENT BY 50
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: richiestaapprovazione; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE piao_private.richiestaapprovazione (
    id BIGSERIAL NOT NULL,
    idpiao bigint NOT NULL,
    mail character varying(255) NOT NULL,
    oggetto text NOT NULL,
    testo text NOT NULL,
    x_validity_in boolean DEFAULT true NOT NULL,
    x_createdby character varying(20) DEFAULT 'ADMIN'::character varying NOT NULL,
    x_created_ts date DEFAULT CURRENT_DATE NOT NULL,
    x_updatedby character varying(20),
    x_updated_ts date,
    x_createdbyrole character varying(50),
    x_updatedbyrole character varying(50),
    x_createdbynamesurname character varying(100),
    x_updatedbynamesurname character varying(100),
    x_active boolean DEFAULT true,
    x_deactivationtime timestamp without time zone
);


--
-- Name: richiestaapprovazione_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE piao_private.richiestaapprovazione_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: richiestaapprovazione_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE piao_private.richiestaapprovazione_id_seq OWNED BY piao_private.richiestaapprovazione.id;


--
-- Name: ruolo; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE piao_private.ruolo (
    id BIGSERIAL NOT NULL,
    codruolo character varying(50) NOT NULL,
    descrizione character varying(255),
    tipologia character varying
);


--
-- Name: ruolo_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE piao_private.ruolo_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: ruolo_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE piao_private.ruolo_id_seq OWNED BY piao_private.ruolo.id;


--
-- Name: sezione1; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE piao_private.sezione1 (
    id BIGSERIAL NOT NULL,
    idpiao bigint NOT NULL,
    idstato bigint,
    quadronormativo text,
    strutturaprogrammatica text,
    cronoprogramma text,
    missione text,
    x_validity_in boolean DEFAULT true NOT NULL,
    x_createdby character varying(20),
    x_created_ts date DEFAULT CURRENT_DATE NOT NULL,
    x_updatedby character varying(20),
    x_updated_ts date,
    x_createdbyrole character varying(50),
    x_updatedbyrole character varying(50),
    x_createdbynamesurname character varying(100),
    x_updatedbynamesurname character varying(100),
    x_active boolean DEFAULT true,
    x_deactivationtime timestamp without time zone
);


--
-- Name: sezione1_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE piao_private.sezione1_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: sezione1_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE piao_private.sezione1_id_seq OWNED BY piao_private.sezione1.id;


--
-- Name: sezione21; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE piao_private.sezione21 (
    id BIGSERIAL NOT NULL,
    idpiao bigint NOT NULL,
    idstato bigint,
    contestoint text,
    contestoext text,
    descrizionevalorepubblico text,
    descrizioneaccessidigitale text,
    descrizioneaccessifisica text,
    descrizionesemplificazione text,
    descrizionepariopportunita text,
    x_validity_in boolean DEFAULT true NOT NULL,
    x_createdby character varying(20),
    x_created_ts date DEFAULT CURRENT_DATE NOT NULL,
    x_updatedby character varying(20),
    x_updated_ts date,
    introrisorsefinanziarie text,
    introfondieuropei text,
    introprocedure text,
    x_createdbyrole character varying(50),
    x_updatedbyrole character varying(50),
    x_createdbynamesurname character varying(100),
    x_updatedbynamesurname character varying(100),
    x_active boolean DEFAULT true,
    x_deactivationtime timestamp without time zone
);


--
-- Name: sezione21_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE piao_private.sezione21_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: sezione21_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE piao_private.sezione21_id_seq OWNED BY piao_private.sezione21.id;


--
-- Name: sezione22; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE piao_private.sezione22 (
    id BIGSERIAL NOT NULL,
    idpiao bigint NOT NULL,
    idstato bigint,
    introperformance text,
    introobiettiviperformance text,
    introadempimenti text,
    introperformanceorganizzativa text,
    descrizionecollegamentoperformance text,
    introperformanceindividuale text,
    x_validity_in boolean DEFAULT true NOT NULL,
    x_createdby character varying(20) NOT NULL,
    x_created_ts date DEFAULT CURRENT_DATE NOT NULL,
    x_updatedby character varying(20),
    x_updated_ts date,
    x_createdbyrole character varying(50),
    x_updatedbyrole character varying(50),
    x_createdbynamesurname character varying(100),
    x_updatedbynamesurname character varying(100),
    x_active boolean DEFAULT true,
    x_deactivationtime timestamp without time zone
);


--
-- Name: sezione22_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE piao_private.sezione22_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: sezione22_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE piao_private.sezione22_id_seq OWNED BY piao_private.sezione22.id;


--
-- Name: sezione23; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE piao_private.sezione23 (
    id BIGSERIAL NOT NULL,
    idpiao bigint NOT NULL,
    idstato bigint,
    introadempimentinormativi text,
    impattocontestoext text,
    impattocontestoint text,
    descrgestionerischio text,
    descridentificazionerischio text,
    descranalisirischio text,
    descrmisurazionerischio text,
    descrtrattamentorischio text,
    descrmonitoraggiorischio text,
    introobiettivoprevenzione text,
    descrtrasparenza text,
    x_validity_in boolean DEFAULT true NOT NULL,
    x_createdby character varying(20) NOT NULL,
    x_created_ts date DEFAULT CURRENT_DATE NOT NULL,
    x_updatedby character varying(20),
    x_updated_ts date,
    intromisureprevenzione text,
    introvalorepubblico text,
    introattivitasensibili text,
    introvalutazionerischio text,
    introgestionerischio text,
    intromonitoraggio text,
    x_createdbyrole character varying(50),
    x_updatedbyrole character varying(50),
    x_createdbynamesurname character varying(100),
    x_updatedbynamesurname character varying(100),
    x_active boolean DEFAULT true,
    x_deactivationtime timestamp without time zone
);


--
-- Name: sezione23_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE piao_private.sezione23_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: sezione23_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE piao_private.sezione23_id_seq OWNED BY piao_private.sezione23.id;


--
-- Name: sezione31; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE piao_private.sezione31 (
    id BIGSERIAL NOT NULL,
    idpiao bigint NOT NULL,
    idstato bigint,
    strutturaorganizzativaap text,
    ampiezzaorganica text,
    incarichidirigenziali text,
    profiliprofessionali text,
    lineeorganizzazione text,
    x_validity_in boolean DEFAULT true NOT NULL,
    x_createdby character varying(20) NOT NULL,
    x_created_ts date DEFAULT CURRENT_DATE NOT NULL,
    x_updatedby character varying(20),
    x_updated_ts date,
    x_createdbyrole character varying(50),
    x_updatedbyrole character varying(50),
    x_createdbynamesurname character varying(100),
    x_updatedbynamesurname character varying(100),
    graficominerva boolean DEFAULT false,
    x_active boolean DEFAULT true,
    x_deactivationtime timestamp without time zone
);


--
-- Name: sezione31_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE piao_private.sezione31_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: sezione31_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE piao_private.sezione31_id_seq OWNED BY piao_private.sezione31.id;


--
-- Name: sezione32; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE piao_private.sezione32 (
    id BIGSERIAL NOT NULL,
    idpiao bigint NOT NULL,
    idstato bigint,
    descrizionecontestoriferimento text,
    descrizioneobiettivilavoroagile text,
    descrizionestatoattuazione text,
    descrizionefattoriabilitanti text,
    descrizionepersonaleagile text,
    descrizionegiornatelavorate text,
    descrizionelivellosoddisfazione text,
    descrizionecontributi text,
    descrizioneimpatti text,
    x_validity_in boolean DEFAULT true NOT NULL,
    x_createdby character varying(20) NOT NULL,
    x_created_ts date DEFAULT CURRENT_DATE NOT NULL,
    x_updatedby character varying(20),
    x_updated_ts date,
    x_createdbyrole character varying(50),
    x_updatedbyrole character varying(50),
    x_createdbynamesurname character varying(100),
    x_updatedbynamesurname character varying(100),
    x_active boolean DEFAULT true,
    x_deactivationtime timestamp without time zone
);


--
-- Name: sezione32_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE piao_private.sezione32_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: sezione32_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE piao_private.sezione32_id_seq OWNED BY piao_private.sezione32.id;


--
-- Name: sezione331; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE piao_private.sezione331 (
    id BIGSERIAL NOT NULL,
    idpiao bigint,
    contesto text,
    descrizionequalitativa text,
    strategiaprogrammazione text,
    obiettivotrasformazione text,
    rimodulazione boolean,
    strategiacopertura text,
    descrizionestrategia text,
    stimaevoluzione text,
    idstato bigint,
    x_validity_in boolean DEFAULT true NOT NULL,
    x_createdby character varying(20) DEFAULT 'ADMIN'::character varying NOT NULL,
    x_created_ts date DEFAULT CURRENT_DATE NOT NULL,
    x_updatedby character varying(20),
    x_updated_ts date,
    x_createdbyrole character varying(50),
    x_updatedbyrole character varying(50),
    x_createdbynamesurname character varying(100),
    x_updatedbynamesurname character varying(100),
    x_active boolean DEFAULT true,
    x_deactivationtime timestamp without time zone
);


--
-- Name: sezione331_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE piao_private.sezione331_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: sezione331_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE piao_private.sezione331_id_seq OWNED BY piao_private.sezione331.id;


--
-- Name: sezione332; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE piao_private.sezione332 (
    id BIGSERIAL NOT NULL,
    idpiao bigint NOT NULL,
    idstato bigint,
    x_validity_in boolean DEFAULT true NOT NULL,
    x_createdby character varying(20) NOT NULL,
    x_created_ts date DEFAULT CURRENT_DATE NOT NULL,
    x_updatedby character varying(20),
    x_updated_ts date,
    x_createdbyrole character varying(50),
    x_updatedbyrole character varying(50),
    x_createdbynamesurname character varying(100),
    x_updatedbynamesurname character varying(100),
    contestonormativo character varying(255),
    descrizionequalitativa text,
    descrizionestrategia text,
    descrizionerisorse text,
    descrizioneincentivi text,
    x_active boolean DEFAULT true,
    x_deactivationtime timestamp without time zone
);


--
-- Name: sezione332_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE piao_private.sezione332_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: sezione332_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE piao_private.sezione332_id_seq OWNED BY piao_private.sezione332.id;


--
-- Name: sezione4; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE piao_private.sezione4 (
    id BIGSERIAL NOT NULL,
    idpiao bigint NOT NULL,
    descrstrumenti text,
    descrmodalitarilevazione text,
    intro text,
    intro21 text,
    intro22 text,
    descr22 text,
    descr23 text,
    descr31 text,
    descr32 text,
    descr331 text,
    descr332 text,
    descrmonitoraggio text,
    idstato bigint,
    x_validity_in boolean DEFAULT true NOT NULL,
    x_createdby character varying(20) NOT NULL,
    x_created_ts date DEFAULT CURRENT_DATE NOT NULL,
    x_updatedby character varying(20),
    x_updated_ts date,
    x_createdbyrole character varying(50),
    x_updatedbyrole character varying(50),
    x_createdbynamesurname character varying(100),
    x_updatedbynamesurname character varying(100),
    x_active boolean DEFAULT true,
    x_deactivationtime timestamp without time zone
);


--
-- Name: sezione4_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE piao_private.sezione4_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: sezione4_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE piao_private.sezione4_id_seq OWNED BY piao_private.sezione4.id;


--
-- Name: sottofasemonitoraggio; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE piao_private.sottofasemonitoraggio (
    id BIGSERIAL NOT NULL,
    idsezione4 bigint NOT NULL,
    denominazione character varying(255) NOT NULL,
    descrizione text,
    datainizio date NOT NULL,
    datafine date NOT NULL,
    strumenti character varying(255),
    fontedato character varying(255),
    x_validity_in boolean DEFAULT true NOT NULL,
    x_createdby character varying(20) DEFAULT 'ADMIN'::character varying NOT NULL,
    x_created_ts date DEFAULT CURRENT_DATE NOT NULL,
    x_updatedby character varying(20),
    x_updated_ts date,
    x_createdbyrole character varying(50),
    x_updatedbyrole character varying(50),
    x_createdbynamesurname character varying(100),
    x_updatedbynamesurname character varying(100),
    x_active boolean DEFAULT true,
    x_deactivationtime timestamp without time zone
);


--
-- Name: sottofasemonitoraggio_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE piao_private.sottofasemonitoraggio_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: sottofasemonitoraggio_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE piao_private.sottofasemonitoraggio_id_seq OWNED BY piao_private.sottofasemonitoraggio.id;


--
-- Name: stakeholder; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE piao_private.stakeholder (
    id BIGSERIAL NOT NULL,
    idpiao bigint NOT NULL,
    nomestakeholder character varying(255),
    relazionepa text,
    x_validity_in boolean DEFAULT true NOT NULL,
    x_createdby character varying(20) NOT NULL,
    x_created_ts date DEFAULT CURRENT_DATE NOT NULL,
    x_updatedby character varying(20),
    x_updated_ts date,
    x_createdbyrole character varying(50),
    x_updatedbyrole character varying(50),
    x_createdbynamesurname character varying(100),
    x_updatedbynamesurname character varying(100),
    x_active boolean DEFAULT true,
    x_deactivationtime timestamp without time zone
);


--
-- Name: stakeholder_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE piao_private.stakeholder_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: stakeholder_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE piao_private.stakeholder_id_seq OWNED BY piao_private.stakeholder.id;


--
-- Name: statopiao; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE piao_private.statopiao (
    id BIGSERIAL NOT NULL,
    testo character varying(255) NOT NULL
);


--
-- Name: statopiao_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE piao_private.statopiao_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: statopiao_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE piao_private.statopiao_id_seq OWNED BY piao_private.statopiao.id;


--
-- Name: statosezione; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE piao_private.statosezione (
    id BIGSERIAL NOT NULL,
    testo character varying(255) NOT NULL
);


--
-- Name: statosezione_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE piao_private.statosezione_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: statosezione_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE piao_private.statosezione_id_seq OWNED BY piao_private.statosezione.id;


--
-- Name: storicomodifica; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE piao_private.storicomodifica (
    id BIGSERIAL NOT NULL,
    idpiao bigint,
    idsezione bigint,
    codtipologiafk character varying(255),
    nomecognome character varying(255),
    profilo character varying(255),
    datamodifica date,
    sezione character varying(255),
    testosezione text,
    campimodificati text,
    x_validity_in boolean DEFAULT true NOT NULL,
    x_createdby character varying(20) NOT NULL,
    x_created_ts date DEFAULT CURRENT_DATE NOT NULL,
    x_updatedby character varying(20),
    x_updated_ts date,
    x_createdbyrole character varying(50),
    x_updatedbyrole character varying(50),
    x_createdbynamesurname character varying(100),
    x_updatedbynamesurname character varying(100),
    x_active boolean DEFAULT true,
    x_deactivationtime timestamp without time zone
);


--
-- Name: storicomodifica_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE piao_private.storicomodifica ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME piao_private.storicomodifica_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: storicostatosezione; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE piao_private.storicostatosezione (
    id BIGSERIAL NOT NULL,
    idstato bigint,
    identitafk bigint NOT NULL,
    codtipologiafk character varying(20) NOT NULL,
    testo character varying(255) NOT NULL,
    x_validity_in boolean DEFAULT true NOT NULL,
    x_createdby character varying(20) NOT NULL,
    x_created_ts date DEFAULT CURRENT_DATE NOT NULL,
    x_updatedby character varying(20),
    x_updated_ts date,
    x_createdbyrole character varying(50),
    x_updatedbyrole character varying(50),
    x_createdbynamesurname character varying(100),
    x_updatedbynamesurname character varying(100),
    rifiutato boolean,
    revocato boolean,
    annullato boolean,
    osservazioni text,
    x_active boolean DEFAULT true,
    x_deactivationtime timestamp without time zone
);


--
-- Name: storicostatosezione_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE piao_private.storicostatosezione_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: storicostatosezione_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE piao_private.storicostatosezione_id_seq OWNED BY piao_private.storicostatosezione.id;


--
-- Name: strutturapiao; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE piao_private.strutturapiao (
    id BIGSERIAL NOT NULL,
    idparent bigint,
    numerosezione character varying(50),
    testo text,
    x_validity_in boolean DEFAULT true NOT NULL,
    x_createdby character varying(20) NOT NULL,
    x_created_ts date DEFAULT CURRENT_DATE NOT NULL,
    x_updatedby character varying(20),
    x_updated_ts date,
    x_createdbyrole character varying(50),
    x_updatedbyrole character varying(50),
    x_createdbynamesurname character varying(100),
    x_updatedbynamesurname character varying(100),
    x_active boolean DEFAULT true,
    x_deactivationtime timestamp without time zone
);


--
-- Name: strutturapiao_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE piao_private.strutturapiao_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: strutturapiao_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE piao_private.strutturapiao_id_seq OWNED BY piao_private.strutturapiao.id;


--
-- Name: tabellafunzionale; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE piao_private.tabellafunzionale (
    id BIGSERIAL NOT NULL,
    identitafk bigint NOT NULL,
    codtipologiafk character varying(255) NOT NULL,
    codice character varying(255),
    idovp bigint,
    denominazionesintetica character varying(255),
    responsabileamministrativo character varying(255),
    idstakeholder bigint,
    dimensioni character varying(255),
    formula character varying(255),
    polarita character varying(255),
    baseline character varying(255),
    targetannon1 character varying(255),
    targetannon2 character varying(255),
    targetannon3 character varying(255),
    fonte character varying(255),
    x_validity_in boolean DEFAULT true NOT NULL,
    x_createdby character varying(20) NOT NULL,
    x_created_ts date NOT NULL,
    x_updatedby character varying(20),
    x_updated_ts date,
    x_createdbyrole character varying(50),
    x_updatedbyrole character varying(50),
    x_createdbynamesurname character varying(100),
    x_updatedbynamesurname character varying(100),
    x_active boolean DEFAULT true,
    x_deactivationtime timestamp without time zone
);


--
-- Name: tabellafunzionale_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE piao_private.tabellafunzionale_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: tabellafunzionale_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE piao_private.tabellafunzionale_id_seq OWNED BY piao_private.tabellafunzionale.id;


--
-- Name: targetindicatore; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE piao_private.targetindicatore (
    id BIGSERIAL NOT NULL,
    value character varying(200) NOT NULL
);


--
-- Name: targetindicatore_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE piao_private.targetindicatore_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: targetindicatore_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE piao_private.targetindicatore_id_seq OWNED BY piao_private.targetindicatore.id;


--
-- Name: test_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE piao_private.test_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: tipologiaandamentovaloreindicatore; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE piao_private.tipologiaandamentovaloreindicatore (
    id BIGSERIAL NOT NULL,
    idtargetfk bigint,
    valore character varying(255)
);


--
-- Name: tipologiaandamentovaloreindicatore_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE piao_private.tipologiaandamentovaloreindicatore_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: tipologiaandamentovaloreindicatore_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE piao_private.tipologiaandamentovaloreindicatore_id_seq OWNED BY piao_private.tipologiaandamentovaloreindicatore.id;


--
-- Name: tipologiaattivita; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE piao_private.tipologiaattivita (
    id BIGSERIAL NOT NULL,
    codice character varying(255) NOT NULL,
    testo text NOT NULL,
    x_validity_in boolean DEFAULT true NOT NULL,
    x_createdby character varying(20) DEFAULT 'ADMIN'::character varying NOT NULL,
    x_created_ts date DEFAULT CURRENT_DATE NOT NULL,
    x_updatedby character varying(20),
    x_updated_ts date,
    x_createdbyrole character varying(50),
    x_updatedbyrole character varying(50),
    x_createdbynamesurname character varying(100),
    x_updatedbynamesurname character varying(100),
    x_active boolean DEFAULT true,
    x_deactivationtime timestamp without time zone
);


--
-- Name: tipologiaattivita_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE piao_private.tipologiaattivita_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: tipologiaattivita_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE piao_private.tipologiaattivita_id_seq OWNED BY piao_private.tipologiaattivita.id;


--
-- Name: tipologiadestinatari; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE piao_private.tipologiadestinatari (
    id BIGSERIAL NOT NULL,
    codice character varying(255) NOT NULL,
    testo text NOT NULL,
    x_validity_in boolean DEFAULT true NOT NULL,
    x_createdby character varying(20) DEFAULT 'ADMIN'::character varying NOT NULL,
    x_created_ts date DEFAULT CURRENT_DATE NOT NULL,
    x_updatedby character varying(20),
    x_updated_ts date,
    x_createdbyrole character varying(50),
    x_updatedbyrole character varying(50),
    x_createdbynamesurname character varying(100),
    x_updatedbynamesurname character varying(100),
    x_active boolean DEFAULT true,
    x_deactivationtime timestamp without time zone
);


--
-- Name: tipologiadestinatari_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE piao_private.tipologiadestinatari_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: tipologiadestinatari_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE piao_private.tipologiadestinatari_id_seq OWNED BY piao_private.tipologiadestinatari.id;


--
-- Name: utenteruolipasezione; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE piao_private.utenteruolipasezione (
    id BIGSERIAL NOT NULL,
    idutenteruolipa text NOT NULL,
    idAmministrazione text NOT NULL,
    idstruttura bigint NOT NULL,
    idruolo bigint NOT NULL
);


--
-- Name: utenteruolipasezione_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE piao_private.utenteruolipasezione_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: utenteruolipasezione_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE piao_private.utenteruolipasezione_id_seq OWNED BY piao_private.utenteruolipasezione.id;

CREATE TABLE piao_private.Avviso (
    id BIGSERIAL PRIMARY KEY,
    tipologiaContenuto      VARCHAR(50)   ,
    dataPubblicazione       DATE,
    oggetto                 VARCHAR(255)   ,
    tipologiaAmministrazione VARCHAR(255),
    amministrazione         VARCHAR(255),
    messaggio               VARCHAR(2000),
    stato                   VARCHAR(50)  DEFAULT 'BOZZA',
 	  X_VALIDITY_IN BOOLEAN NOT NULL DEFAULT TRUE,
    X_CREATEDBY VARCHAR(20) DEFAULT 'ADMIN',
    X_CREATED_TS DATE NOT NULL DEFAULT CURRENT_DATE,
    X_UPDATEDBY VARCHAR(20),
    X_UPDATED_TS DATE,
	  X_CREATEDBYROLE VARCHAR(50),
    X_UPDATEDBYROLE VARCHAR(50),
    X_CREATEDBYNAMESURNAME VARCHAR(100),
	  X_UPDATEDBYNAMESURNAME VARCHAR(100),
	  X_ACTIVE BOOLEAN DEFAULT TRUE,
	  X_DEACTIVATIONTIME TIMESTAMP
);

CREATE INDEX idx_avviso_stato ON piao_private.Avviso (stato);

CREATE INDEX idx_avviso_tipologia_contenuto ON piao_private.Avviso (tipologiacontenuto);

CREATE TABLE piao_private.storageminerva (
   id BIGSERIAL PRIMARY KEY NOT NULL,
   identitafk bigint NOT NULL,
   codtipologiafk varchar(100) NOT NULL,
   valore text NOT NULL,
   codiceipa varchar(50) NULL
);
--
-- Name: storageminerva_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--
--
-- Name: storageminerva_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--


ALTER SEQUENCE piao_private.storageminerva_id_seq OWNED BY piao_private.storageminerva.id;






--
-- Name: allegati_piao id; Type: DEFAULT; Schema: common_services; Owner: -
--

ALTER TABLE ONLY common_services.allegati_piao ALTER COLUMN id SET DEFAULT nextval('common_services.allegati_piao_id_seq'::regclass);


--
-- Name: allegato id; Type: DEFAULT; Schema: common_services; Owner: -
--

ALTER TABLE ONLY common_services.allegato_ticket ALTER COLUMN id SET DEFAULT nextval('common_services.allegato_ticket_id_seq'::regclass);


--
-- Name: adempimentinormativi id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.adempimentinormativi ALTER COLUMN id SET DEFAULT nextval('piao_private.adempimentinormativi_id_seq'::regclass);


--
-- Name: adempimento id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.adempimento ALTER COLUMN id SET DEFAULT nextval('piao_private.adempimento_id_seq'::regclass);


--
-- Name: allegato id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.allegato ALTER COLUMN id SET DEFAULT nextval('piao_private.allegato_id_seq'::regclass);


--
-- Name: ambitocompetenza id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.ambitocompetenza ALTER COLUMN id SET DEFAULT nextval('piao_private.ambitocompetenza_id_seq'::regclass);


--
-- Name: ampiezzaorganizzativa id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.ampiezzaorganizzativa ALTER COLUMN id SET DEFAULT nextval('piao_private.ampiezzaorganizzativa_id_seq'::regclass);


--
-- Name: anagrafica id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.anagrafica ALTER COLUMN id SET DEFAULT nextval('piao_private.anagrafica_id_seq'::regclass);


--
-- Name: app_log id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.app_log ALTER COLUMN id SET DEFAULT nextval('piao_private.app_log_id_seq'::regclass);


--
-- Name: areaorganizzativa id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.areaorganizzativa ALTER COLUMN id SET DEFAULT nextval('piao_private.areaorganizzativa_id_seq'::regclass);


--
-- Name: areatematica id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.areatematica ALTER COLUMN id SET DEFAULT nextval('piao_private.areatematica_id_seq'::regclass);


--
-- Name: attivitaformative id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.attivitaformative ALTER COLUMN id SET DEFAULT nextval('piao_private.attivitaformative_id_seq'::regclass);


--
-- Name: attivitasensibile id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.attivitasensibile ALTER COLUMN id SET DEFAULT nextval('piao_private.attivitasensibile_id_seq'::regclass);


--
-- Name: autoritaapprovatore id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.autoritaapprovatore ALTER COLUMN id SET DEFAULT nextval('piao_private.autoritaapprovatore_id_seq'::regclass);


--
-- Name: categoriaobiettivi id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.categoriaobiettivi ALTER COLUMN id SET DEFAULT nextval('piao_private.categoriaobiettivi_id_seq'::regclass);


--
-- Name: categoriaobiettivitip id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.categoriaobiettivitip ALTER COLUMN id SET DEFAULT nextval('piao_private.categoriaobiettivitip_id_seq'::regclass);


--
-- Name: piao_private.configurazioni id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.configurazioni ALTER COLUMN id SET DEFAULT nextval('piao_private.configurazioni_id_seq1'::regclass);


--
-- Name: datipubblicati id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.datipubblicati ALTER COLUMN id SET DEFAULT nextval('piao_private.datipubblicati_id_seq'::regclass);


--
-- Name: dichiarazionescadenza id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.dichiarazionescadenza ALTER COLUMN id SET DEFAULT nextval('piao_private.dichiarazionescadenza_id_seq'::regclass);


--
-- Name: dimensioneindicatore id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.dimensioneindicatore ALTER COLUMN id SET DEFAULT nextval('piao_private.impattoindicatore_id_seq'::regclass);


--
-- Name: eventorischio id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.eventorischio ALTER COLUMN id SET DEFAULT nextval('piao_private.eventorischio_id_seq'::regclass);


--
-- Name: fase id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.fase ALTER COLUMN id SET DEFAULT nextval('piao_private.fase_id_seq'::regclass);


--
-- Name: fondieuropei id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.fondieuropei ALTER COLUMN id SET DEFAULT nextval('piao_private.fondieuropei_id_seq'::regclass);

--
-- Name: indicatore id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.indicatore ALTER COLUMN id SET DEFAULT nextval('piao_private.indicatore_id_seq'::regclass);


--
-- Name: integrationteam id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.integrationteam ALTER COLUMN id SET DEFAULT nextval('piao_private.integrationteam_id_seq'::regclass);


--
-- Name: livellorischio id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.livellorischio ALTER COLUMN id SET DEFAULT nextval('piao_private.livellorischio_id_seq'::regclass);


--
-- Name: milestone id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.milestone ALTER COLUMN id SET DEFAULT nextval('piao_private.milestone_id_seq'::regclass);


--
-- Name: misuraprevenzione id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.misuraprevenzione ALTER COLUMN id SET DEFAULT nextval('piao_private.misuraprevenzione_id_seq'::regclass);


--
-- Name: misuraprevenzioneeventorischio id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.misuraprevenzioneeventorischio ALTER COLUMN id SET DEFAULT nextval('piao_private.misuraprevenzioneeventorischio_id_seq'::regclass);


--
-- Name: misuraprevenzioneeventorischioindicatore id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.misuraprevenzioneeventorischioindicatore ALTER COLUMN id SET DEFAULT nextval('piao_private.misuraprevenzioneeventorischioindicatore_id_seq'::regclass);


--
-- Name: misuraprevenzioneeventorischiostakeholder id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.misuraprevenzioneeventorischiostakeholder ALTER COLUMN id SET DEFAULT nextval('piao_private.misuraprevenzioneeventorischiostakeholder_id_seq'::regclass);


--
-- Name: misuraprevenzioneindicatore id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.misuraprevenzioneindicatore ALTER COLUMN id SET DEFAULT nextval('piao_private.misuraprevenzioneindicatore_id_seq'::regclass);


--
-- Name: misuraprevenzionestakeholder id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.misuraprevenzionestakeholder ALTER COLUMN id SET DEFAULT nextval('piao_private.misuraprevenzionestakeholder_id_seq'::regclass);


--
-- Name: monitoraggioprevenzione id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.monitoraggioprevenzione ALTER COLUMN id SET DEFAULT nextval('piao_private.monitoraggioprevenzione_id_seq'::regclass);


--
-- Name: motivazionedichiarazione id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.motivazionedichiarazione ALTER COLUMN id SET DEFAULT nextval('piao_private.motivazionedichiarazione_id_seq'::regclass);


--
-- Name: obbligolegge id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.obbligolegge ALTER COLUMN id SET DEFAULT nextval('piao_private.obbligolegge_id_seq'::regclass);


--
-- Name: obiettivirisultatifotografia id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.obiettivirisultatifotografia ALTER COLUMN id SET DEFAULT nextval('piao_private.obiettivirisultatifotografia_id_seq'::regclass);


--
-- Name: obiettivoperformance id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.obiettivoperformance ALTER COLUMN id SET DEFAULT nextval('piao_private.obiettivoperformance_id_seq'::regclass);


--
-- Name: obiettivoperformanceindicatore id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.obiettivoperformanceindicatore ALTER COLUMN id SET DEFAULT nextval('piao_private.obiettivoperformanceindicatore_id_seq'::regclass);


--
-- Name: obiettivoperformancestakeholder id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.obiettivoperformancestakeholder ALTER COLUMN id SET DEFAULT nextval('piao_private.obiettivoperformancestakeholder_id_seq'::regclass);


--
-- Name: obiettivoprevenzione id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.obiettivoprevenzione ALTER COLUMN id SET DEFAULT nextval('piao_private.obiettivoprevenzione_id_seq'::regclass);


--
-- Name: obiettivoprevenzionecorruzionetrasparenza id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.obiettivoprevenzionecorruzionetrasparenza ALTER COLUMN id SET DEFAULT nextval('piao_private.obiettivoprevenzionecorruzionetrasparenza_id_seq'::regclass);


--
-- Name: obiettivoprevenzionecorruzionetrasparenzaindicatori id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.obiettivoprevenzionecorruzionetrasparenzaindicatori ALTER COLUMN id SET DEFAULT nextval('piao_private.obiettivoprevenzionecorruzionetrasparenzaindicatori_id_seq'::regclass);


--
-- Name: obiettivoprevenzioneindicatore id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.obiettivoprevenzioneindicatore ALTER COLUMN id SET DEFAULT nextval('piao_private.obiettivoprevenzioneindicatore_id_seq'::regclass);


--
-- Name: organopolitico id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.organopolitico ALTER COLUMN id SET DEFAULT nextval('piao_private.organopolitico_id_seq'::regclass);


--
-- Name: ovp id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.ovp ALTER COLUMN id SET DEFAULT nextval('piao_private.ovp_id_seq'::regclass);


--
-- Name: ovp_sto id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.ovp_sto ALTER COLUMN id SET DEFAULT nextval('piao_private.ovp_sto_id_seq'::regclass);


--
-- Name: ovpareaorganizzativa id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.ovpareaorganizzativa ALTER COLUMN id SET DEFAULT nextval('piao_private.ovpareaorganizzativa_id_seq'::regclass);


--
-- Name: ovpprioritapolitica id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.ovpprioritapolitica ALTER COLUMN id SET DEFAULT nextval('piao_private.ovpprioritapolitica_id_seq'::regclass);


--
-- Name: ovprisorsafinanziaria id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.ovprisorsafinanziaria ALTER COLUMN id SET DEFAULT nextval('piao_private.ovprisorsafinanziaria_id_seq'::regclass);


--
-- Name: ovpstakeholder id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.ovpstakeholder ALTER COLUMN id SET DEFAULT nextval('piao_private.ovpstakeholder_id_seq'::regclass);


--
-- Name: ovpstrategia id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.ovpstrategia ALTER COLUMN id SET DEFAULT nextval('piao_private.ovpstrategia_id_seq'::regclass);


--
-- Name: ovpstrategiaindicatore id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.ovpstrategiaindicatore ALTER COLUMN id SET DEFAULT nextval('piao_private.ovpstrategiaindicatore_id_seq'::regclass);


--
-- Name: piao id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.piao ALTER COLUMN id SET DEFAULT nextval('piao_private.piao_id_seq'::regclass);


--
-- Name: prevenzioneeventorischio id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.prevenzioneeventorischio ALTER COLUMN id SET DEFAULT nextval('piao_private.prevenzioneeventorischio_id_seq'::regclass);


--
-- Name: principioguida id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.principioguida ALTER COLUMN id SET DEFAULT nextval('piao_private.principioguida_id_seq'::regclass);


--
-- Name: prioritapolitica id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.prioritapolitica ALTER COLUMN id SET DEFAULT nextval('piao_private.prioritapolitica_id_seq'::regclass);


--
-- Name: procedura id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.procedura ALTER COLUMN id SET DEFAULT nextval('piao_private.procedura_id_seq'::regclass);


--
-- Name: promemoria id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.promemoria ALTER COLUMN id SET DEFAULT nextval('piao_private.promemoria_id_seq'::regclass);


--
-- Name: richiestaapprovazione id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.richiestaapprovazione ALTER COLUMN id SET DEFAULT nextval('piao_private.richiestaapprovazione_id_seq'::regclass);


--
-- Name: ruolo id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.ruolo ALTER COLUMN id SET DEFAULT nextval('piao_private.ruolo_id_seq'::regclass);


--
-- Name: sezione1 id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.sezione1 ALTER COLUMN id SET DEFAULT nextval('piao_private.sezione1_id_seq'::regclass);


--
-- Name: sezione21 id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.sezione21 ALTER COLUMN id SET DEFAULT nextval('piao_private.sezione21_id_seq'::regclass);


--
-- Name: sezione22 id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.sezione22 ALTER COLUMN id SET DEFAULT nextval('piao_private.sezione22_id_seq'::regclass);


--
-- Name: sezione23 id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.sezione23 ALTER COLUMN id SET DEFAULT nextval('piao_private.sezione23_id_seq'::regclass);


--
-- Name: sezione31 id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.sezione31 ALTER COLUMN id SET DEFAULT nextval('piao_private.sezione31_id_seq'::regclass);


--
-- Name: sezione32 id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.sezione32 ALTER COLUMN id SET DEFAULT nextval('piao_private.sezione32_id_seq'::regclass);


--
-- Name: sezione331 id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.sezione331 ALTER COLUMN id SET DEFAULT nextval('piao_private.sezione331_id_seq'::regclass);


--
-- Name: sezione332 id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.sezione332 ALTER COLUMN id SET DEFAULT nextval('piao_private.sezione332_id_seq'::regclass);


--
-- Name: sezione4 id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.sezione4 ALTER COLUMN id SET DEFAULT nextval('piao_private.sezione4_id_seq'::regclass);


--
-- Name: sottofasemonitoraggio id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.sottofasemonitoraggio ALTER COLUMN id SET DEFAULT nextval('piao_private.sottofasemonitoraggio_id_seq'::regclass);


--
-- Name: stakeholder id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.stakeholder ALTER COLUMN id SET DEFAULT nextval('piao_private.stakeholder_id_seq'::regclass);


--
-- Name: statopiao id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.statopiao ALTER COLUMN id SET DEFAULT nextval('piao_private.statopiao_id_seq'::regclass);


--
-- Name: statosezione id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.statosezione ALTER COLUMN id SET DEFAULT nextval('piao_private.statosezione_id_seq'::regclass);


--
-- Name: storicostatosezione id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.storicostatosezione ALTER COLUMN id SET DEFAULT nextval('piao_private.storicostatosezione_id_seq'::regclass);


--
-- Name: strutturapiao id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.strutturapiao ALTER COLUMN id SET DEFAULT nextval('piao_private.strutturapiao_id_seq'::regclass);


--
-- Name: tabellafunzionale id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.tabellafunzionale ALTER COLUMN id SET DEFAULT nextval('piao_private.tabellafunzionale_id_seq'::regclass);


--
-- Name: targetindicatore id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.targetindicatore ALTER COLUMN id SET DEFAULT nextval('piao_private.targetindicatore_id_seq'::regclass);


--
-- Name: tipologiaandamentovaloreindicatore id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.tipologiaandamentovaloreindicatore ALTER COLUMN id SET DEFAULT nextval('piao_private.tipologiaandamentovaloreindicatore_id_seq'::regclass);


--
-- Name: tipologiaattivita id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.tipologiaattivita ALTER COLUMN id SET DEFAULT nextval('piao_private.tipologiaattivita_id_seq'::regclass);


--
-- Name: tipologiadestinatari id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.tipologiadestinatari ALTER COLUMN id SET DEFAULT nextval('piao_private.tipologiadestinatari_id_seq'::regclass);


--
-- Name: utenteruolipasezione id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.utenteruolipasezione ALTER COLUMN id SET DEFAULT nextval('piao_private.utenteruolipasezione_id_seq'::regclass);


--
-- Name: storageminerva id; Type: DEFAULT; Schema: piao_private; Owner: -
--

ALTER TABLE ONLY piao_private.storageminerva ALTER COLUMN id SET DEFAULT nextval('piao_private.storageminerva_id_seq'::regclass);


--
-- Name: allegati_piao allegati_piao_pkey; Type: CONSTRAINT; Schema: common_services; Owner: -
--

ALTER TABLE ONLY common_services.allegati_piao
    ADD CONSTRAINT allegati_piao_pkey PRIMARY KEY (id);


--
-- Name: allegato allegato_pkey; Type: CONSTRAINT; Schema: common_services; Owner: -
--

ALTER TABLE ONLY common_services.allegato_ticket
    ADD CONSTRAINT allegato_ticket_pkey PRIMARY KEY (id);


--
-- Name: amministrazioni amministrazioni_pkey; Type: CONSTRAINT; Schema: common_services; Owner: -
--

ALTER TABLE ONLY common_services.amministrazioni
    ADD CONSTRAINT amministrazioni_pkey PRIMARY KEY (codice_ipa);


--
-- Name: categoriaticket categoriaticket_pk; Type: CONSTRAINT; Schema: common_services; Owner: -
--

ALTER TABLE ONLY common_services.categoriaticket
    ADD CONSTRAINT categoriaticket_pk PRIMARY KEY (id);


--
-- Name: documenti_piao documenti_piao_pkey; Type: CONSTRAINT; Schema: common_services; Owner: -
--

ALTER TABLE ONLY common_services.documenti_piao
    ADD CONSTRAINT documenti_piao_pkey PRIMARY KEY (id);


--
-- Name: notification notification_pk; Type: CONSTRAINT; Schema: common_services; Owner: -
--

ALTER TABLE ONLY common_services.notification
    ADD CONSTRAINT notification_pk PRIMARY KEY (id);


--
-- Name: ticket ticket_pk; Type: CONSTRAINT; Schema: common_services; Owner: -
--

ALTER TABLE ONLY common_services.ticket
    ADD CONSTRAINT ticket_pk PRIMARY KEY (id);


--
-- Name: adempimentinormativi adempimentinormativi_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.adempimentinormativi
    ADD CONSTRAINT adempimentinormativi_pkey PRIMARY KEY (id);


--
-- Name: adempimento adempimento_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.adempimento
    ADD CONSTRAINT adempimento_pkey PRIMARY KEY (id);


--
-- Name: allegato allegato_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.allegato
    ADD CONSTRAINT allegato_pkey PRIMARY KEY (id);


--
-- Name: ambitocompetenza ambitocompetenza_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.ambitocompetenza
    ADD CONSTRAINT ambitocompetenza_pkey PRIMARY KEY (id);


--
-- Name: ampiezzaorganizzativa ampiezzaorganizzativa_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.ampiezzaorganizzativa
    ADD CONSTRAINT ampiezzaorganizzativa_pkey PRIMARY KEY (id);


--
-- Name: anagrafica anagrafica_idpiao_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.anagrafica
    ADD CONSTRAINT anagrafica_idpiao_key UNIQUE (idpiao);


--
-- Name: anagrafica anagrafica_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.anagrafica
    ADD CONSTRAINT anagrafica_pkey PRIMARY KEY (id);


--
-- Name: app_log app_log_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.app_log
    ADD CONSTRAINT app_log_pkey PRIMARY KEY (id);


--
-- Name: areaorganizzativa areaorganizzativa_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.areaorganizzativa
    ADD CONSTRAINT areaorganizzativa_pkey PRIMARY KEY (id);


--
-- Name: areatematica areatematica_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.areatematica
    ADD CONSTRAINT areatematica_pkey PRIMARY KEY (id);


--
-- Name: attivitaformative attivitaformative_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.attivitaformative
    ADD CONSTRAINT attivitaformative_pkey PRIMARY KEY (id);


--
-- Name: attivitasensibile attivitasensibile_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.attivitasensibile
    ADD CONSTRAINT attivitasensibile_pkey PRIMARY KEY (id);


--
-- Name: autoritaapprovatore autoritaapprovatore_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.autoritaapprovatore
    ADD CONSTRAINT autoritaapprovatore_pkey PRIMARY KEY (id);


--
-- Name: categoriaobiettivi categoriaobiettivi_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.categoriaobiettivi
    ADD CONSTRAINT categoriaobiettivi_pkey PRIMARY KEY (id);


--
-- Name: categoriaobiettivitip categoriaobiettivitip_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.categoriaobiettivitip
    ADD CONSTRAINT categoriaobiettivitip_pkey PRIMARY KEY (id);


--
-- Name: piao_private.configurazioni piao_private.configurazioni_codice_key; Type: CONSTRAINT; Schema: public; Owner: -
--



--
-- Name: piao_private.configurazioni piao_private.configurazioni_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.configurazioni
    ADD CONSTRAINT configurazioni_pkey PRIMARY KEY (id);


--
-- Name: datipubblicati datipubblicati_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.datipubblicati
    ADD CONSTRAINT datipubblicati_pkey PRIMARY KEY (id);


--
-- Name: dichiarazionescadenza dichiarazionescadenza_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.dichiarazionescadenza
    ADD CONSTRAINT dichiarazionescadenza_pkey PRIMARY KEY (id);


--
-- Name: dimensioneindicatore dimensioneindicatore_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.dimensioneindicatore
    ADD CONSTRAINT dimensioneindicatore_pk PRIMARY KEY (id);


--
-- Name: eventorischio eventorischio_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.eventorischio
    ADD CONSTRAINT eventorischio_pkey PRIMARY KEY (id);


--
-- Name: fase fase_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.fase
    ADD CONSTRAINT fase_pkey PRIMARY KEY (id);


--
-- Name: fondieuropei fondieuropei_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.fondieuropei
    ADD CONSTRAINT fondieuropei_pkey PRIMARY KEY (id);


--
-- Name: indicatore indicatore_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.indicatore
    ADD CONSTRAINT indicatore_pkey PRIMARY KEY (id);


--
-- Name: integrationteam integrationteam_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.integrationteam
    ADD CONSTRAINT integrationteam_pkey PRIMARY KEY (id);


--
-- Name: livellorischio livellorischio_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.livellorischio
    ADD CONSTRAINT livellorischio_pkey PRIMARY KEY (id);


--
-- Name: milestone milestone_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.milestone
    ADD CONSTRAINT milestone_pkey PRIMARY KEY (id);


--
-- Name: misuraprevenzione misuraprevenzione_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.misuraprevenzione
    ADD CONSTRAINT misuraprevenzione_pkey PRIMARY KEY (id);


--
-- Name: misuraprevenzioneeventorischio misuraprevenzioneeventorischio_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.misuraprevenzioneeventorischio
    ADD CONSTRAINT misuraprevenzioneeventorischio_pkey PRIMARY KEY (id);


--
-- Name: misuraprevenzioneeventorischioindicatore misuraprevenzioneeventorischioindicatore_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.misuraprevenzioneeventorischioindicatore
    ADD CONSTRAINT misuraprevenzioneeventorischioindicatore_pkey PRIMARY KEY (id);


--
-- Name: misuraprevenzioneeventorischiostakeholder misuraprevenzioneeventorischiostakeholder_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.misuraprevenzioneeventorischiostakeholder
    ADD CONSTRAINT misuraprevenzioneeventorischiostakeholder_pkey PRIMARY KEY (id);


--
-- Name: misuraprevenzioneindicatore misuraprevenzioneindicatore_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.misuraprevenzioneindicatore
    ADD CONSTRAINT misuraprevenzioneindicatore_pkey PRIMARY KEY (id);


--
-- Name: misuraprevenzionestakeholder misuraprevenzionestakeholder_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.misuraprevenzionestakeholder
    ADD CONSTRAINT misuraprevenzionestakeholder_pkey PRIMARY KEY (id);


--
-- Name: monitoraggioprevenzione monitoraggioprevenzione_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.monitoraggioprevenzione
    ADD CONSTRAINT monitoraggioprevenzione_pkey PRIMARY KEY (id);


--
-- Name: motivazionedichiarazione motivazionedichiarazione_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.motivazionedichiarazione
    ADD CONSTRAINT motivazionedichiarazione_pkey PRIMARY KEY (id);


--
-- Name: obbligolegge obbligolegge_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.obbligolegge
    ADD CONSTRAINT obbligolegge_pkey PRIMARY KEY (id);


--
-- Name: obiettivirisultatifotografia obiettivirisultatifotografia_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.obiettivirisultatifotografia
    ADD CONSTRAINT obiettivirisultatifotografia_pkey PRIMARY KEY (id);


--
-- Name: obiettivoperformance obiettivoperformance_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.obiettivoperformance
    ADD CONSTRAINT obiettivoperformance_pkey PRIMARY KEY (id);


--
-- Name: obiettivoperformanceindicatore obiettivoperformanceindicatore_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.obiettivoperformanceindicatore
    ADD CONSTRAINT obiettivoperformanceindicatore_pkey PRIMARY KEY (id);


--
-- Name: obiettivoperformancestakeholder obiettivoperformancestakeholder_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.obiettivoperformancestakeholder
    ADD CONSTRAINT obiettivoperformancestakeholder_pkey PRIMARY KEY (id);


--
-- Name: obiettivoprevenzione obiettivoprevenzione_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.obiettivoprevenzione
    ADD CONSTRAINT obiettivoprevenzione_pkey PRIMARY KEY (id);


--
-- Name: obiettivoprevenzionecorruzionetrasparenza obiettivoprevenzionecorruzionetrasparenza_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.obiettivoprevenzionecorruzionetrasparenza
    ADD CONSTRAINT obiettivoprevenzionecorruzionetrasparenza_pkey PRIMARY KEY (id);


--
-- Name: obiettivoprevenzionecorruzionetrasparenzaindicatori obiettivoprevenzionecorruzionetrasparenzaindicatori_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.obiettivoprevenzionecorruzionetrasparenzaindicatori
    ADD CONSTRAINT obiettivoprevenzionecorruzionetrasparenzaindicatori_pkey PRIMARY KEY (id);


--
-- Name: obiettivoprevenzioneindicatore obiettivoprevenzioneindicatore_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.obiettivoprevenzioneindicatore
    ADD CONSTRAINT obiettivoprevenzioneindicatore_pkey PRIMARY KEY (id);


--
-- Name: organopolitico organopolitico_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.organopolitico
    ADD CONSTRAINT organopolitico_pkey PRIMARY KEY (id);


--
-- Name: ovp ovp_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.ovp
    ADD CONSTRAINT ovp_pkey PRIMARY KEY (id);


--
-- Name: ovp_sto ovp_sto_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.ovp_sto
    ADD CONSTRAINT ovp_sto_pkey PRIMARY KEY (id, rev);


--
-- Name: ovpareaorganizzativa ovpareaorganizzativa_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.ovpareaorganizzativa
    ADD CONSTRAINT ovpareaorganizzativa_pkey PRIMARY KEY (id);


--
-- Name: ovpprioritapolitica ovpprioritapolitica_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.ovpprioritapolitica
    ADD CONSTRAINT ovpprioritapolitica_pkey PRIMARY KEY (id);


--
-- Name: ovprisorsafinanziaria ovprisorsafinanziaria_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.ovprisorsafinanziaria
    ADD CONSTRAINT ovprisorsafinanziaria_pkey PRIMARY KEY (id);


--
-- Name: ovpstakeholder ovpstakeholder_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.ovpstakeholder
    ADD CONSTRAINT ovpstakeholder_pkey PRIMARY KEY (id);


--
-- Name: ovpstrategia ovpstrategia_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.ovpstrategia
    ADD CONSTRAINT ovpstrategia_pkey PRIMARY KEY (id);


--
-- Name: ovpstrategiaindicatore ovpstrategiaindicatore_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.ovpstrategiaindicatore
    ADD CONSTRAINT ovpstrategiaindicatore_pkey PRIMARY KEY (id);


--
-- Name: piao piao_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.piao
    ADD CONSTRAINT piao_pkey PRIMARY KEY (id);


--
-- Name: prevenzioneeventorischio prevenzioneeventorischio_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.prevenzioneeventorischio
    ADD CONSTRAINT prevenzioneeventorischio_pkey PRIMARY KEY (id);


--
-- Name: principioguida principioguida_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.principioguida
    ADD CONSTRAINT principioguida_pkey PRIMARY KEY (id);


--
-- Name: prioritapolitica prioritapolitica_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.prioritapolitica
    ADD CONSTRAINT prioritapolitica_pkey PRIMARY KEY (id);


--
-- Name: procedura procedura_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.procedura
    ADD CONSTRAINT procedura_pkey PRIMARY KEY (id);


--
-- Name: promemoria promemoria_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.promemoria
    ADD CONSTRAINT promemoria_pkey PRIMARY KEY (id);


--
-- Name: revinfo revinfo_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.revinfo
    ADD CONSTRAINT revinfo_pkey PRIMARY KEY (id);


--
-- Name: richiestaapprovazione richiestaapprovazione_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.richiestaapprovazione
    ADD CONSTRAINT richiestaapprovazione_pkey PRIMARY KEY (id);


--
-- Name: ruolo ruolo_codruolo_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.ruolo
    ADD CONSTRAINT ruolo_codruolo_key UNIQUE (codruolo);


--
-- Name: ruolo ruolo_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.ruolo
    ADD CONSTRAINT ruolo_pkey PRIMARY KEY (id);


--
-- Name: sezione1 sezione1_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.sezione1
    ADD CONSTRAINT sezione1_pkey PRIMARY KEY (id);


--
-- Name: sezione21 sezione21_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.sezione21
    ADD CONSTRAINT sezione21_pkey PRIMARY KEY (id);


--
-- Name: sezione22 sezione22_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.sezione22
    ADD CONSTRAINT sezione22_pkey PRIMARY KEY (id);


--
-- Name: sezione23 sezione23_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.sezione23
    ADD CONSTRAINT sezione23_pkey PRIMARY KEY (id);


--
-- Name: sezione31 sezione31_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.sezione31
    ADD CONSTRAINT sezione31_pkey PRIMARY KEY (id);


--
-- Name: sezione32 sezione32_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.sezione32
    ADD CONSTRAINT sezione32_pkey PRIMARY KEY (id);


--
-- Name: sezione331 sezione331_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.sezione331
    ADD CONSTRAINT sezione331_pkey PRIMARY KEY (id);


--
-- Name: sezione332 sezione332_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.sezione332
    ADD CONSTRAINT sezione332_pkey PRIMARY KEY (id);


--
-- Name: sezione4 sezione4_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.sezione4
    ADD CONSTRAINT sezione4_pkey PRIMARY KEY (id);


--
-- Name: sottofasemonitoraggio sottofasemonitoraggio_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.sottofasemonitoraggio
    ADD CONSTRAINT sottofasemonitoraggio_pkey PRIMARY KEY (id);


--
-- Name: stakeholder stakeholder_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.stakeholder
    ADD CONSTRAINT stakeholder_pkey PRIMARY KEY (id);


--
-- Name: statopiao statopiao_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.statopiao
    ADD CONSTRAINT statopiao_pkey PRIMARY KEY (id);


--
-- Name: statosezione statosezione_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.statosezione
    ADD CONSTRAINT statosezione_pkey PRIMARY KEY (id);


--
-- Name: storicomodifica storicomodifica_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.storicomodifica
    ADD CONSTRAINT storicomodifica_pkey PRIMARY KEY (id);


--
-- Name: storicostatosezione storicostatosezione_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.storicostatosezione
    ADD CONSTRAINT storicostatosezione_pkey PRIMARY KEY (id);


--
-- Name: strutturapiao strutturapiao_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.strutturapiao
    ADD CONSTRAINT strutturapiao_pkey PRIMARY KEY (id);


--
-- Name: tabellafunzionale tabellafunzionale_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.tabellafunzionale
    ADD CONSTRAINT tabellafunzionale_pkey PRIMARY KEY (id);


--
-- Name: targetindicatore targetindicatore_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.targetindicatore
    ADD CONSTRAINT targetindicatore_pk PRIMARY KEY (id);


--
-- Name: tipologiaandamentovaloreindicatore tipologiaandamentovaloreindicatore_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.tipologiaandamentovaloreindicatore
    ADD CONSTRAINT tipologiaandamentovaloreindicatore_pkey PRIMARY KEY (id);


--
-- Name: tipologiaattivita tipologiaattivita_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.tipologiaattivita
    ADD CONSTRAINT tipologiaattivita_pkey PRIMARY KEY (id);


--
-- Name: tipologiadestinatari tipologiadestinatari_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.tipologiadestinatari
    ADD CONSTRAINT tipologiadestinatari_pkey PRIMARY KEY (id);


--
-- Name: utenteruolipasezione utenteruolipasezione_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.utenteruolipasezione
    ADD CONSTRAINT utenteruolipasezione_pkey PRIMARY KEY (id);


--
-- Name: idx_allegato_tipologia_codtipo_entita; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_allegato_tipologia_codtipo_entita ON piao_private.allegato USING btree (codtipologiafk, codtipologiaallegato, identitafk);


--
-- Name: idx_area_organizzativa_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_area_organizzativa_id ON piao_private.areaorganizzativa USING btree (id);


--
-- Name: idx_areaorganizzativa_sezione1_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_areaorganizzativa_sezione1_id ON piao_private.areaorganizzativa USING btree (idsezione1);


--
-- Name: idx_fondi_europei_sezione21; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_fondi_europei_sezione21 ON piao_private.fondieuropei USING btree (idsezione21);


--
-- Name: idx_indicatore_dimensioni; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_indicatore_dimensioni ON piao_private.indicatore USING btree (iddimensionefk, idsubdimensionefk) WHERE (iddimensionefk IS NOT NULL);


--
-- Name: idx_indicatore_multi_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_indicatore_multi_id ON piao_private.indicatore USING btree (id) INCLUDE (denominazione, codtipologiafk, identitafk);


--
-- Name: idx_indicatore_tipologia_entita; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_indicatore_tipologia_entita ON piao_private.indicatore USING btree (codtipologiafk, identitafk);


--
-- Name: idx_indicatore_validity; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_indicatore_validity ON piao_private.indicatore USING btree (x_validity_in, x_updated_ts);


--
-- Name: idx_ovp_areaorganizzativa_ovp_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_ovp_areaorganizzativa_ovp_id ON piao_private.ovpareaorganizzativa USING btree (idovp);


--
-- Name: idx_ovp_prioritapolitica_ovp_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_ovp_prioritapolitica_ovp_id ON piao_private.ovpprioritapolitica USING btree (idovp);


--
-- Name: idx_ovp_risorsafinanziaria_ovp_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_ovp_risorsafinanziaria_ovp_id ON piao_private.ovprisorsafinanziaria USING btree (idovp);


--
-- Name: idx_ovp_sezione21_id_ovp_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_ovp_sezione21_id_ovp_id ON piao_private.ovp USING btree (idsezione21, id);


--
-- Name: idx_ovp_sezione21_sorted; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_ovp_sezione21_sorted ON piao_private.ovp USING btree (idsezione21, id);


--
-- Name: idx_ovp_stakeholder_ovp_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_ovp_stakeholder_ovp_id ON piao_private.ovpstakeholder USING btree (idovp);


--
-- Name: idx_ovp_strategia_indicatore_idindicatore; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_ovp_strategia_indicatore_idindicatore ON piao_private.ovpstrategiaindicatore USING btree (idindicatore);


--
-- Name: idx_ovp_strategia_indicatore_strategia_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_ovp_strategia_indicatore_strategia_id ON piao_private.ovpstrategiaindicatore USING btree (idovpstrategia);


--
-- Name: idx_ovp_strategia_ovp_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_ovp_strategia_ovp_id ON piao_private.ovpstrategia USING btree (idovp);


--
-- Name: idx_piao_codpafk; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_piao_codpafk ON piao_private.piao USING btree (codpafk);


--
-- Name: idx_piao_codpafk_createdts_versione; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_piao_codpafk_createdts_versione ON piao_private.piao USING btree (codpafk, x_created_ts DESC, versione DESC);


--
-- Name: idx_priorita_politica_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_priorita_politica_id ON piao_private.prioritapolitica USING btree (id);


--
-- Name: idx_prioritapolitica_sezione1_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_prioritapolitica_sezione1_id ON piao_private.prioritapolitica USING btree (idsezione1);


--
-- Name: idx_procedura_sezione21; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_procedura_sezione21 ON piao_private.procedura USING btree (idsezione21);


--
-- Name: idx_sezione1_idpiao; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_sezione1_idpiao ON piao_private.sezione1 USING btree (idpiao);


--
-- Name: idx_sezione1_piao_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_sezione1_piao_id ON piao_private.sezione1 USING btree (idpiao);


--
-- Name: idx_sezione1_piao_pp_covering; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_sezione1_piao_pp_covering ON piao_private.sezione1 USING btree (idpiao, id);


--
-- Name: idx_sezione21_idpiao; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_sezione21_idpiao ON piao_private.sezione21 USING btree (idpiao);


--
-- Name: idx_sezione21_piao_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_sezione21_piao_id ON piao_private.sezione21 USING btree (idpiao);


--
-- Name: idx_sezione21_piao_ovp_covering; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_sezione21_piao_ovp_covering ON piao_private.sezione21 USING btree (idpiao, id);


--
-- Name: idx_sezione22_idpiao; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_sezione22_idpiao ON piao_private.sezione22 USING btree (idpiao);


--
-- Name: idx_sezione23_idpiao; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_sezione23_idpiao ON piao_private.sezione23 USING btree (idpiao);


--
-- Name: idx_stakeholder_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_stakeholder_id ON piao_private.stakeholder USING btree (id);


--
-- Name: idx_stakeholder_piao; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_stakeholder_piao ON piao_private.stakeholder USING btree (idpiao);


--
-- Name: idx_stato_sezione_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_stato_sezione_id ON piao_private.statosezione USING btree (id);


--
-- Name: idx_storico_stato_codtip_identita_createdts; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_storico_stato_codtip_identita_createdts ON piao_private.storicostatosezione USING btree (codtipologiafk, identitafk DESC);


--
-- Name: idx_storico_stato_identita_codtip_createdts; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_storico_stato_identita_codtip_createdts ON piao_private.storicostatosezione USING btree (identitafk, codtipologiafk DESC);


--
-- Name: idx_storico_stato_identita_createdts; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_storico_stato_identita_createdts ON piao_private.storicostatosezione USING btree (identitafk DESC);


--
-- Name: idx_storicomodifica_idpiao; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_storicomodifica_idpiao ON piao_private.storicomodifica USING btree (idpiao);


--
-- Name: idx_storicomodifica_idsezione_codtip; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_storicomodifica_idsezione_codtip ON piao_private.storicomodifica USING btree (idsezione, codtipologiafk);


--
-- Name: idx_struttura_piao_all; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_struttura_piao_all ON piao_private.strutturapiao USING btree (id, idparent, numerosezione);


--
-- Name: idx_tipologia_andamento_valore_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_tipologia_andamento_valore_id ON piao_private.tipologiaandamentovaloreindicatore USING btree (id);


--
-- Name: tabellafunzionale_identitafk_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX tabellafunzionale_identitafk_idx ON piao_private.tabellafunzionale USING btree (identitafk, idovp, idstakeholder);


--
-- Name: allegato fk_allegato_ticket; Type: FK CONSTRAINT; Schema: common_services; Owner: -
--

ALTER TABLE ONLY common_services.allegato_ticket
    ADD CONSTRAINT fk_allegato_ticket FOREIGN KEY (idticketfk) REFERENCES common_services.ticket(id);


--
-- Name: documenti_piao fk_ipa; Type: FK CONSTRAINT; Schema: common_services; Owner: -
--

ALTER TABLE ONLY common_services.documenti_piao
    ADD CONSTRAINT fk_ipa FOREIGN KEY (codice_ipa_rif) REFERENCES common_services.amministrazioni(codice_ipa);


--
-- Name: allegati_piao fk_piao; Type: FK CONSTRAINT; Schema: common_services; Owner: -
--

ALTER TABLE ONLY common_services.allegati_piao
    ADD CONSTRAINT fk_piao FOREIGN KEY (id_piao) REFERENCES common_services.documenti_piao(id) ON DELETE CASCADE;


--
-- Name: ticket fk_ticket_categoriaticket; Type: FK CONSTRAINT; Schema: common_services; Owner: -
--

ALTER TABLE ONLY common_services.ticket
    ADD CONSTRAINT fk_ticket_categoriaticket FOREIGN KEY (idcategoriaticketfk) REFERENCES common_services.categoriaticket(id) ON DELETE CASCADE;


--
-- Name: documenti_piao fkp8hr9hysswawit921co4ih13i; Type: FK CONSTRAINT; Schema: common_services; Owner: -
--

ALTER TABLE ONLY common_services.documenti_piao
    ADD CONSTRAINT fkp8hr9hysswawit921co4ih13i FOREIGN KEY (codice_ipa_rif) REFERENCES common_services.amministrazioni(codice_ipa);


--
-- Name: adempimentinormativi fk_adempimento_normativo_sezione23; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.adempimentinormativi
    ADD CONSTRAINT fk_adempimento_normativo_sezione23 FOREIGN KEY (idsezione23) REFERENCES piao_private.sezione23(id) ON DELETE CASCADE;


--
-- Name: adempimento fk_adempimento_sezione22; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.adempimento
    ADD CONSTRAINT fk_adempimento_sezione22 FOREIGN KEY (idsezione22) REFERENCES piao_private.sezione22(id) ON DELETE CASCADE;


--
-- Name: ampiezzaorganizzativa fk_amp_org_sezione31; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.ampiezzaorganizzativa
    ADD CONSTRAINT fk_amp_org_sezione31 FOREIGN KEY (idsezione31) REFERENCES piao_private.sezione31(id);


--
-- Name: anagrafica fk_anagrafica_piao; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.anagrafica
    ADD CONSTRAINT fk_anagrafica_piao FOREIGN KEY (idpiao) REFERENCES piao_private.piao(id) ON DELETE CASCADE;


--
-- Name: areaorganizzativa fk_areaorganizzativa_sezione1; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.areaorganizzativa
    ADD CONSTRAINT fk_areaorganizzativa_sezione1 FOREIGN KEY (idsezione1) REFERENCES piao_private.sezione1(id) ON DELETE CASCADE;


--
-- Name: attivitasensibile fk_attivitasensibile_sezione23; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.attivitasensibile
    ADD CONSTRAINT fk_attivitasensibile_sezione23 FOREIGN KEY (idsezione23) REFERENCES piao_private.sezione23(id) ON DELETE CASCADE;


--
-- Name: obiettivoprevenzionecorruzionetrasparenza fk_attivitasensibile_sezione23; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.obiettivoprevenzionecorruzionetrasparenza
    ADD CONSTRAINT fk_attivitasensibile_sezione23 FOREIGN KEY (idsezione23) REFERENCES piao_private.sezione23(id) ON DELETE CASCADE;


--
-- Name: categoriaobiettivi fk_categoriaobiettivi_categoriaobiettivitip; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.categoriaobiettivi
    ADD CONSTRAINT fk_categoriaobiettivi_categoriaobiettivitip FOREIGN KEY (idcategoriaobiettivi) REFERENCES piao_private.categoriaobiettivitip(id) ON DELETE CASCADE;


--
-- Name: categoriaobiettivi fk_categoriaobiettivi_sezione4; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.categoriaobiettivi
    ADD CONSTRAINT fk_categoriaobiettivi_sezione4 FOREIGN KEY (idsezione4) REFERENCES piao_private.sezione4(id) ON DELETE CASCADE;


--
-- Name: categoriaobiettivi fk_categoriaobiettivi_sottofasemonitoraggio; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.categoriaobiettivi
    ADD CONSTRAINT fk_categoriaobiettivi_sottofasemonitoraggio FOREIGN KEY (idsottofase) REFERENCES piao_private.sottofasemonitoraggio(id) ON DELETE CASCADE;


--
-- Name: datipubblicati fk_datipubblicati_obbligolegge; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.datipubblicati
    ADD CONSTRAINT fk_datipubblicati_obbligolegge FOREIGN KEY (idobbligolegge) REFERENCES piao_private.obbligolegge(id) ON DELETE CASCADE;


--
-- Name: dichiarazionescadenza fk_dichiarazionescadenza_motivazione; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.dichiarazionescadenza
    ADD CONSTRAINT fk_dichiarazionescadenza_motivazione FOREIGN KEY (idmotivazionedichiarazione) REFERENCES piao_private.motivazionedichiarazione(id) ON DELETE CASCADE;


--
-- Name: dichiarazionescadenza fk_dichiarazionescadenza_piao; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.dichiarazionescadenza
    ADD CONSTRAINT fk_dichiarazionescadenza_piao FOREIGN KEY (idpiao) REFERENCES piao_private.piao(id) ON DELETE CASCADE;


--
-- Name: eventorischio fk_eventorischio_attivitasensibile; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.eventorischio
    ADD CONSTRAINT fk_eventorischio_attivitasensibile FOREIGN KEY (idattivitasensibile) REFERENCES piao_private.attivitasensibile(id) ON DELETE CASCADE;


--
-- Name: eventorischio fk_eventorischio_livellorischio; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.eventorischio
    ADD CONSTRAINT fk_eventorischio_livellorischio FOREIGN KEY (idlivellorischio) REFERENCES piao_private.livellorischio(id);


--
-- Name: eventorischio fk_eventorischio_sezione23; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.eventorischio
    ADD CONSTRAINT fk_eventorischio_sezione23 FOREIGN KEY (idsezione23) REFERENCES piao_private.sezione23(id) ON DELETE CASCADE;


--
-- Name: fase fk_fase_sezione22; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.fase
    ADD CONSTRAINT fk_fase_sezione22 FOREIGN KEY (idsezione22) REFERENCES piao_private.sezione22(id) ON DELETE CASCADE;


--
-- Name: fondieuropei fk_fondieuropei_sezione21; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.fondieuropei
    ADD CONSTRAINT fk_fondieuropei_sezione21 FOREIGN KEY (idsezione21) REFERENCES piao_private.sezione21(id) ON DELETE CASCADE;


--
-- Name: attivitaformative fk_fotografiaformazione_ambitocompetenza; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.attivitaformative
    ADD CONSTRAINT fk_fotografiaformazione_ambitocompetenza FOREIGN KEY (idambitocompetenza) REFERENCES piao_private.ambitocompetenza(id) ON DELETE CASCADE;


--
-- Name: attivitaformative fk_fotografiaformazione_areatematica; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.attivitaformative
    ADD CONSTRAINT fk_fotografiaformazione_areatematica FOREIGN KEY (idareatematica) REFERENCES piao_private.areatematica(id) ON DELETE CASCADE;


--
-- Name: attivitaformative fk_fotografiaformazione_sezione332; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.attivitaformative
    ADD CONSTRAINT fk_fotografiaformazione_sezione332 FOREIGN KEY (idsezione332) REFERENCES piao_private.sezione332(id) ON DELETE CASCADE;


--
-- Name: attivitaformative fk_fotografiaformazione_tipologiaattivita; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.attivitaformative
    ADD CONSTRAINT fk_fotografiaformazione_tipologiaattivita FOREIGN KEY (idtipologiaattivita) REFERENCES piao_private.tipologiaattivita(id) ON DELETE CASCADE;


--
-- Name: misuraprevenzioneindicatore fk_indicatore; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.misuraprevenzioneindicatore
    ADD CONSTRAINT fk_indicatore FOREIGN KEY (idindicatore) REFERENCES piao_private.indicatore(id) ON DELETE CASCADE;


--
-- Name: obiettivoprevenzionecorruzionetrasparenzaindicatori fk_indicatore; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.obiettivoprevenzionecorruzionetrasparenzaindicatori
    ADD CONSTRAINT fk_indicatore FOREIGN KEY (idindicatore) REFERENCES piao_private.indicatore(id) ON DELETE CASCADE;


--
-- Name: indicatore fk_indicatoretipologia_anno1; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.indicatore
    ADD CONSTRAINT fk_indicatoretipologia_anno1 FOREIGN KEY (idtipandvalanno1) REFERENCES piao_private.tipologiaandamentovaloreindicatore(id) ON DELETE CASCADE;


--
-- Name: indicatore fk_indicatoretipologia_anno2; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.indicatore
    ADD CONSTRAINT fk_indicatoretipologia_anno2 FOREIGN KEY (idtipandvalanno2) REFERENCES piao_private.tipologiaandamentovaloreindicatore(id) ON DELETE CASCADE;


--
-- Name: indicatore fk_indicatoretipologia_annocorrente; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.indicatore
    ADD CONSTRAINT fk_indicatoretipologia_annocorrente FOREIGN KEY (idtipandvalannocorrente) REFERENCES piao_private.tipologiaandamentovaloreindicatore(id) ON DELETE CASCADE;


--
-- Name: integrationteam fk_integrationteam_sezione1; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.integrationteam
    ADD CONSTRAINT fk_integrationteam_sezione1 FOREIGN KEY (idsezione1) REFERENCES piao_private.sezione1(id) ON DELETE CASCADE;


--
-- Name: milestone fk_milestone_promemoria; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.milestone
    ADD CONSTRAINT fk_milestone_promemoria FOREIGN KEY (idpromemoria) REFERENCES piao_private.promemoria(id) ON DELETE CASCADE;


--
-- Name: milestone fk_milestone_sottofase_monitoraggio; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.milestone
    ADD CONSTRAINT fk_milestone_sottofase_monitoraggio FOREIGN KEY (idsottofasemonitoraggio) REFERENCES piao_private.sottofasemonitoraggio(id) ON DELETE CASCADE;


--
-- Name: misuraprevenzioneindicatore fk_misuraprevenzione; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.misuraprevenzioneindicatore
    ADD CONSTRAINT fk_misuraprevenzione FOREIGN KEY (idmisuraprevenzione) REFERENCES piao_private.misuraprevenzione(id) ON DELETE CASCADE;


--
-- Name: misuraprevenzione fk_misuraprevenzione_obiettivoprevenzione; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.misuraprevenzione
    ADD CONSTRAINT fk_misuraprevenzione_obiettivoprevenzione FOREIGN KEY (idobiettivoprevenzione) REFERENCES piao_private.obiettivoprevenzione(id) ON DELETE CASCADE;


--
-- Name: misuraprevenzione fk_misuraprevenzione_sezione23; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.misuraprevenzione
    ADD CONSTRAINT fk_misuraprevenzione_sezione23 FOREIGN KEY (idsezione23) REFERENCES piao_private.sezione23(id) ON DELETE CASCADE;


--
-- Name: misuraprevenzionestakeholder fk_misuraprevenzione_stakeholder_misura; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.misuraprevenzionestakeholder
    ADD CONSTRAINT fk_misuraprevenzione_stakeholder_misura FOREIGN KEY (idmisuraprevenzione) REFERENCES piao_private.misuraprevenzione(id) ON DELETE CASCADE;


--
-- Name: misuraprevenzionestakeholder fk_misuraprevenzione_stakeholder_stakeholder; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.misuraprevenzionestakeholder
    ADD CONSTRAINT fk_misuraprevenzione_stakeholder_stakeholder FOREIGN KEY (idstakeholder) REFERENCES piao_private.stakeholder(id) ON DELETE CASCADE;


--
-- Name: monitoraggioprevenzione fk_monitoraggio_prevenzione_evento_rischio; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.monitoraggioprevenzione
    ADD CONSTRAINT fk_monitoraggio_prevenzione_evento_rischio FOREIGN KEY (idmisuraprevenzioneeventorischio) REFERENCES piao_private.misuraprevenzioneeventorischio(id) ON DELETE CASCADE;


--
-- Name: obbligolegge fk_obbligolegge_sezione23; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.obbligolegge
    ADD CONSTRAINT fk_obbligolegge_sezione23 FOREIGN KEY (idsezione23) REFERENCES piao_private.sezione23(id) ON DELETE CASCADE;


--
-- Name: obiettivirisultatifotografia fk_obiettivirisultati_ambitocompetenza; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.obiettivirisultatifotografia
    ADD CONSTRAINT fk_obiettivirisultati_ambitocompetenza FOREIGN KEY (idambitocompetenza) REFERENCES piao_private.ambitocompetenza(id) ON DELETE CASCADE;


--
-- Name: obiettivirisultatifotografia fk_obiettivirisultati_areatematica; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.obiettivirisultatifotografia
    ADD CONSTRAINT fk_obiettivirisultati_areatematica FOREIGN KEY (idareatematica) REFERENCES piao_private.areatematica(id) ON DELETE CASCADE;


--
-- Name: obiettivirisultatifotografia fk_obiettivirisultati_sezione332; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.obiettivirisultatifotografia
    ADD CONSTRAINT fk_obiettivirisultati_sezione332 FOREIGN KEY (idsezione332) REFERENCES piao_private.sezione332(id) ON DELETE CASCADE;


--
-- Name: obiettivirisultatifotografia fk_obiettivirisultati_tipologiaattivita; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.obiettivirisultatifotografia
    ADD CONSTRAINT fk_obiettivirisultati_tipologiaattivita FOREIGN KEY (idtipologiaattivita) REFERENCES piao_private.tipologiaattivita(id) ON DELETE CASCADE;


--
-- Name: obiettivirisultatifotografia fk_obiettivirisultati_tipologiadestinatari; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.obiettivirisultatifotografia
    ADD CONSTRAINT fk_obiettivirisultati_tipologiadestinatari FOREIGN KEY (idtipologiadestinatari) REFERENCES piao_private.tipologiadestinatari(id) ON DELETE CASCADE;


--
-- Name: obiettivoperformance fk_obiettivoperformance_ovp; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.obiettivoperformance
    ADD CONSTRAINT fk_obiettivoperformance_ovp FOREIGN KEY (idovp) REFERENCES piao_private.ovp(id) ON DELETE CASCADE;


--
-- Name: obiettivoperformance fk_obiettivoperformance_sezione22; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.obiettivoperformance
    ADD CONSTRAINT fk_obiettivoperformance_sezione22 FOREIGN KEY (idsezione22) REFERENCES piao_private.sezione22(id) ON DELETE CASCADE;


--
-- Name: obiettivoperformance fk_obiettivoperformance_strategia; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.obiettivoperformance
    ADD CONSTRAINT fk_obiettivoperformance_strategia FOREIGN KEY (idstrategiaovp) REFERENCES piao_private.ovpstrategia(id) ON DELETE CASCADE;


--
-- Name: obiettivoperformanceindicatore fk_obiettivoperformanceindicatore_indicatore; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.obiettivoperformanceindicatore
    ADD CONSTRAINT fk_obiettivoperformanceindicatore_indicatore FOREIGN KEY (idindicatore) REFERENCES piao_private.indicatore(id) ON DELETE CASCADE;


--
-- Name: obiettivoperformanceindicatore fk_obiettivoperformanceindicatore_obiettivoperformance; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.obiettivoperformanceindicatore
    ADD CONSTRAINT fk_obiettivoperformanceindicatore_obiettivoperformance FOREIGN KEY (idobiettivoperformance) REFERENCES piao_private.obiettivoperformance(id) ON DELETE CASCADE;


--
-- Name: obiettivoperformancestakeholder fk_obiettivoperformancestakeholder_obiettivoperformance; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.obiettivoperformancestakeholder
    ADD CONSTRAINT fk_obiettivoperformancestakeholder_obiettivoperformance FOREIGN KEY (idobiettivoperformance) REFERENCES piao_private.obiettivoperformance(id) ON DELETE CASCADE;


--
-- Name: obiettivoperformancestakeholder fk_obiettivoperformancestakeholder_stakeholder; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.obiettivoperformancestakeholder
    ADD CONSTRAINT fk_obiettivoperformancestakeholder_stakeholder FOREIGN KEY (idstakeholder) REFERENCES piao_private.stakeholder(id) ON DELETE CASCADE;


--
-- Name: obiettivoprevenzione fk_obiettivoprevenzione_sezione23; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.obiettivoprevenzione
    ADD CONSTRAINT fk_obiettivoprevenzione_sezione23 FOREIGN KEY (idsezione23) REFERENCES piao_private.sezione23(id) ON DELETE CASCADE;


--
-- Name: obiettivoprevenzionecorruzionetrasparenzaindicatori fk_obiettivoprevenzionecorruzionetrasparenza; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.obiettivoprevenzionecorruzionetrasparenzaindicatori
    ADD CONSTRAINT fk_obiettivoprevenzionecorruzionetrasparenza FOREIGN KEY (idobiettivoprevenzionecorruzionetrasparenza) REFERENCES piao_private.obiettivoprevenzionecorruzionetrasparenza(id) ON DELETE CASCADE;


--
-- Name: obiettivoprevenzionecorruzionetrasparenza fk_obiettivoprevenzionecorruzionetrasparenza_obiettivoperforman; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.obiettivoprevenzionecorruzionetrasparenza
    ADD CONSTRAINT fk_obiettivoprevenzionecorruzionetrasparenza_obiettivoperforman FOREIGN KEY (idobbiettivoperformance) REFERENCES piao_private.obiettivoperformance(id) ON DELETE CASCADE;


--
-- Name: obiettivoprevenzionecorruzionetrasparenza fk_obiettivoprevenzionecorruzionetrasparenza_ovp; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.obiettivoprevenzionecorruzionetrasparenza
    ADD CONSTRAINT fk_obiettivoprevenzionecorruzionetrasparenza_ovp FOREIGN KEY (idovp) REFERENCES piao_private.ovp(id) ON DELETE CASCADE;


--
-- Name: obiettivoprevenzionecorruzionetrasparenza fk_obiettivoprevenzionecorruzionetrasparenza_strategia; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.obiettivoprevenzionecorruzionetrasparenza
    ADD CONSTRAINT fk_obiettivoprevenzionecorruzionetrasparenza_strategia FOREIGN KEY (idstrategiaovp) REFERENCES piao_private.ovpstrategia(id) ON DELETE CASCADE;


--
-- Name: obiettivoprevenzioneindicatore fk_obiettivoprevenzioneindicatore_indicatore; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.obiettivoprevenzioneindicatore
    ADD CONSTRAINT fk_obiettivoprevenzioneindicatore_indicatore FOREIGN KEY (idindicatore) REFERENCES piao_private.indicatore(id) ON DELETE CASCADE;


--
-- Name: obiettivoprevenzioneindicatore fk_obiettivoprevenzioneindicatore_obiettivoprevenzione; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.obiettivoprevenzioneindicatore
    ADD CONSTRAINT fk_obiettivoprevenzioneindicatore_obiettivoprevenzione FOREIGN KEY (idobiettivoprevenzione) REFERENCES piao_private.obiettivoprevenzione(id) ON DELETE CASCADE;


--
-- Name: organopolitico fk_organopolitico_sezione1; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.organopolitico
    ADD CONSTRAINT fk_organopolitico_sezione1 FOREIGN KEY (idsezione1) REFERENCES piao_private.sezione1(id) ON DELETE CASCADE;


--
-- Name: ovp fk_ovp_sezione21; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.ovp
    ADD CONSTRAINT fk_ovp_sezione21 FOREIGN KEY (idsezione21) REFERENCES piao_private.sezione21(id) ON DELETE CASCADE;


--
-- Name: ovp_sto fk_ovp_sezione21; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.ovp_sto
    ADD CONSTRAINT fk_ovp_sezione21 FOREIGN KEY (idsezione21) REFERENCES piao_private.sezione21(id) ON DELETE CASCADE;


--
-- Name: ovpareaorganizzativa fk_ovpareaorganizzativa_areaorganizzativa; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.ovpareaorganizzativa
    ADD CONSTRAINT fk_ovpareaorganizzativa_areaorganizzativa FOREIGN KEY (idareaorganizzativa) REFERENCES piao_private.areaorganizzativa(id) ON DELETE CASCADE;


--
-- Name: ovpareaorganizzativa fk_ovpareaorganizzativa_ovp; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.ovpareaorganizzativa
    ADD CONSTRAINT fk_ovpareaorganizzativa_ovp FOREIGN KEY (idovp) REFERENCES piao_private.ovp(id) ON DELETE CASCADE;


--
-- Name: ovpprioritapolitica fk_ovpprioritapolitica_ovp; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.ovpprioritapolitica
    ADD CONSTRAINT fk_ovpprioritapolitica_ovp FOREIGN KEY (idovp) REFERENCES piao_private.ovp(id) ON DELETE CASCADE;


--
-- Name: ovpprioritapolitica fk_ovpprioritapolitica_prioritapolitica; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.ovpprioritapolitica
    ADD CONSTRAINT fk_ovpprioritapolitica_prioritapolitica FOREIGN KEY (idprioritapolitica) REFERENCES piao_private.prioritapolitica(id) ON DELETE CASCADE;


--
-- Name: ovpstakeholder fk_ovpstakeholder_ovp; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.ovpstakeholder
    ADD CONSTRAINT fk_ovpstakeholder_ovp FOREIGN KEY (idovp) REFERENCES piao_private.ovp(id) ON DELETE CASCADE;


--
-- Name: ovpstakeholder fk_ovpstakeholder_stakeholder; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.ovpstakeholder
    ADD CONSTRAINT fk_ovpstakeholder_stakeholder FOREIGN KEY (idstakeholder) REFERENCES piao_private.stakeholder(id) ON DELETE CASCADE;


--
-- Name: piao fk_piao_autorita_approvatore; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.piao
    ADD CONSTRAINT fk_piao_autorita_approvatore FOREIGN KEY (idautoritaapprovatore) REFERENCES piao_private.autoritaapprovatore(id);


--
-- Name: misuraprevenzioneeventorischio fk_preveventorischio_eventorischio; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.misuraprevenzioneeventorischio
    ADD CONSTRAINT fk_preveventorischio_eventorischio FOREIGN KEY (ideventorischio) REFERENCES piao_private.eventorischio(id) ON DELETE CASCADE;


--
-- Name: prevenzioneeventorischio fk_preveventorischio_eventorischio; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.prevenzioneeventorischio
    ADD CONSTRAINT fk_preveventorischio_eventorischio FOREIGN KEY (ideventorischio) REFERENCES piao_private.eventorischio(id) ON DELETE CASCADE;


--
-- Name: misuraprevenzioneeventorischio fk_preveventorischio_obiettivoprev; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.misuraprevenzioneeventorischio
    ADD CONSTRAINT fk_preveventorischio_obiettivoprev FOREIGN KEY (idobiettivoprevenzionecorruzionetrasparenza) REFERENCES piao_private.obiettivoprevenzionecorruzionetrasparenza(id) ON DELETE CASCADE;


--
-- Name: prevenzioneeventorischio fk_preveventorischio_obiettivoprev; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.prevenzioneeventorischio
    ADD CONSTRAINT fk_preveventorischio_obiettivoprev FOREIGN KEY (idobiettivoprevenzione) REFERENCES piao_private.obiettivoprevenzione(id) ON DELETE CASCADE;


--
-- Name: misuraprevenzioneeventorischioindicatore fk_preveventorischioind_indicatore; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.misuraprevenzioneeventorischioindicatore
    ADD CONSTRAINT fk_preveventorischioind_indicatore FOREIGN KEY (idindicatore) REFERENCES piao_private.indicatore(id) ON DELETE CASCADE;


--
-- Name: misuraprevenzioneeventorischioindicatore fk_preveventorischioind_misura; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.misuraprevenzioneeventorischioindicatore
    ADD CONSTRAINT fk_preveventorischioind_misura FOREIGN KEY (idmisuraprevenzioneeventorischio) REFERENCES piao_private.misuraprevenzioneeventorischio(id) ON DELETE CASCADE;


--
-- Name: misuraprevenzioneeventorischiostakeholder fk_preveventorischiosh_misura; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.misuraprevenzioneeventorischiostakeholder
    ADD CONSTRAINT fk_preveventorischiosh_misura FOREIGN KEY (idmisuraprevenzioneeventorischio) REFERENCES piao_private.misuraprevenzioneeventorischio(id) ON DELETE CASCADE;


--
-- Name: misuraprevenzioneeventorischiostakeholder fk_preveventorischiosh_stakeholder; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.misuraprevenzioneeventorischiostakeholder
    ADD CONSTRAINT fk_preveventorischiosh_stakeholder FOREIGN KEY (idstakeholder) REFERENCES piao_private.stakeholder(id) ON DELETE CASCADE;


--
-- Name: principioguida fk_principioguida_sezione1; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.principioguida
    ADD CONSTRAINT fk_principioguida_sezione1 FOREIGN KEY (idsezione1) REFERENCES piao_private.sezione1(id) ON DELETE CASCADE;


--
-- Name: prioritapolitica fk_prioritapolitica_sezione1; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.prioritapolitica
    ADD CONSTRAINT fk_prioritapolitica_sezione1 FOREIGN KEY (idsezione1) REFERENCES piao_private.sezione1(id) ON DELETE CASCADE;


--
-- Name: procedura fk_procedure_sezione21; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.procedura
    ADD CONSTRAINT fk_procedure_sezione21 FOREIGN KEY (idsezione21) REFERENCES piao_private.sezione21(id) ON DELETE CASCADE;


--
-- Name: richiestaapprovazione fk_richiestaapprovazione_piao; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.richiestaapprovazione
    ADD CONSTRAINT fk_richiestaapprovazione_piao FOREIGN KEY (idpiao) REFERENCES piao_private.piao(id) ON DELETE CASCADE;


--
-- Name: ovprisorsafinanziaria fk_risorsefinanziariaovp_ovp; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.ovprisorsafinanziaria
    ADD CONSTRAINT fk_risorsefinanziariaovp_ovp FOREIGN KEY (idovp) REFERENCES piao_private.ovp(id) ON DELETE CASCADE;


--
-- Name: sezione1 fk_sezione1_piao; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.sezione1
    ADD CONSTRAINT fk_sezione1_piao FOREIGN KEY (idpiao) REFERENCES piao_private.piao(id) ON DELETE CASCADE;


--
-- Name: sezione21 fk_sezione21_piao; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.sezione21
    ADD CONSTRAINT fk_sezione21_piao FOREIGN KEY (idpiao) REFERENCES piao_private.piao(id) ON DELETE CASCADE;


--
-- Name: sezione22 fk_sezione22_piao; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.sezione22
    ADD CONSTRAINT fk_sezione22_piao FOREIGN KEY (idpiao) REFERENCES piao_private.piao(id) ON DELETE CASCADE;


--
-- Name: sezione23 fk_sezione23_piao; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.sezione23
    ADD CONSTRAINT fk_sezione23_piao FOREIGN KEY (idpiao) REFERENCES piao_private.piao(id) ON DELETE CASCADE;


--
-- Name: sezione31 fk_sezione31_piao; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.sezione31
    ADD CONSTRAINT fk_sezione31_piao FOREIGN KEY (idpiao) REFERENCES piao_private.piao(id) ON DELETE CASCADE;


--
-- Name: sezione32 fk_sezione32_piao; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.sezione32
    ADD CONSTRAINT fk_sezione32_piao FOREIGN KEY (idpiao) REFERENCES piao_private.piao(id) ON DELETE CASCADE;


--
-- Name: sezione331 fk_sezione331_piao; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.sezione331
    ADD CONSTRAINT fk_sezione331_piao FOREIGN KEY (idpiao) REFERENCES piao_private.piao(id) ON DELETE CASCADE;


--
-- Name: sezione332 fk_sezione332_piao; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.sezione332
    ADD CONSTRAINT fk_sezione332_piao FOREIGN KEY (idpiao) REFERENCES piao_private.piao(id) ON DELETE CASCADE;


--
-- Name: sezione4 fk_sezione4_piao; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.sezione4
    ADD CONSTRAINT fk_sezione4_piao FOREIGN KEY (idpiao) REFERENCES piao_private.piao(id) ON DELETE CASCADE;


--
-- Name: storicostatosezione fk_sezione_stato; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.storicostatosezione
    ADD CONSTRAINT fk_sezione_stato FOREIGN KEY (idstato) REFERENCES piao_private.statosezione(id);


--
-- Name: stakeholder fk_stakeholder_idpiao; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.stakeholder
    ADD CONSTRAINT fk_stakeholder_idpiao FOREIGN KEY (idpiao) REFERENCES piao_private.piao(id) ON DELETE CASCADE;


--
-- Name: storicomodifica fk_storicomodifica_piao; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.storicomodifica
    ADD CONSTRAINT fk_storicomodifica_piao FOREIGN KEY (idpiao) REFERENCES piao_private.piao(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: ovpstrategia fk_strategiaovp_ovp; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.ovpstrategia
    ADD CONSTRAINT fk_strategiaovp_ovp FOREIGN KEY (idovp) REFERENCES piao_private.ovp(id) ON DELETE CASCADE;


--
-- Name: ovpstrategiaindicatore fk_strategiaovpindicatore_indicatore; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.ovpstrategiaindicatore
    ADD CONSTRAINT fk_strategiaovpindicatore_indicatore FOREIGN KEY (idindicatore) REFERENCES piao_private.indicatore(id) ON DELETE CASCADE;


--
-- Name: ovpstrategiaindicatore fk_strategiaovpindicatore_strategiaovp; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.ovpstrategiaindicatore
    ADD CONSTRAINT fk_strategiaovpindicatore_strategiaovp FOREIGN KEY (idovpstrategia) REFERENCES piao_private.ovpstrategia(id) ON DELETE CASCADE;


--
-- Name: utenteruolipasezione fk_struttura; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.utenteruolipasezione
    ADD CONSTRAINT fk_struttura FOREIGN KEY (idstruttura) REFERENCES piao_private.strutturapiao(id);


--
-- Name: strutturapiao fk_struttura_parent; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.strutturapiao
    ADD CONSTRAINT fk_struttura_parent FOREIGN KEY (idparent) REFERENCES piao_private.strutturapiao(id) ON DELETE CASCADE;


--
-- Name: tabellafunzionale fk_tab_funz_ovp; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.tabellafunzionale
    ADD CONSTRAINT fk_tab_funz_ovp FOREIGN KEY (idovp) REFERENCES piao_private.ovp(id);


--
-- Name: tabellafunzionale fk_tab_funz_stakeholder; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.tabellafunzionale
    ADD CONSTRAINT fk_tab_funz_stakeholder FOREIGN KEY (idstakeholder) REFERENCES piao_private.stakeholder(id);


--
-- Name: tipologiaandamentovaloreindicatore fk_tipologiaandamentovaloreindicatore_target; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.tipologiaandamentovaloreindicatore
    ADD CONSTRAINT fk_tipologiaandamentovaloreindicatore_target FOREIGN KEY (idtargetfk) REFERENCES piao_private.targetindicatore(id);


--
-- Name: utenteruolipasezione fk_urp_ruolo; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.utenteruolipasezione
    ADD CONSTRAINT fk_urp_ruolo FOREIGN KEY (idruolo) REFERENCES piao_private.ruolo(id);


--
-- Name: indicatore indicatore_dimensioneindicatore_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.indicatore
    ADD CONSTRAINT indicatore_dimensioneindicatore_fk FOREIGN KEY (iddimensionefk) REFERENCES piao_private.dimensioneindicatore(id);


--
-- Name: indicatore indicatore_dimensioneindicatore_fk_1; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.indicatore
    ADD CONSTRAINT indicatore_dimensioneindicatore_fk_1 FOREIGN KEY (idsubdimensionefk) REFERENCES piao_private.dimensioneindicatore(id);


--
-- Name: indicatore indicatore_piao_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY piao_private.indicatore
    ADD CONSTRAINT indicatore_piao_fk FOREIGN KEY (idpiao) REFERENCES piao_private.piao(id);

--
-- INSERT ambitocompetenza
--

INSERT INTO piao_private.ambitocompetenza
(id, codice, testo, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES(13, 'LEADERSHIP_SOFT_SKILL', 'Competenze di leadership e soft skill', true, 'ADMIN', '2026-03-03', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);
INSERT INTO piao_private.ambitocompetenza
(id, codice, testo, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES(14, 'TRANSIZIONE_AMMINISTRATIVA', 'Competenze per l’attuazione della transizione amministrativa', true, 'ADMIN', '2026-03-03', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);
INSERT INTO piao_private.ambitocompetenza
(id, codice, testo, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES(15, 'TRANSIZIONE_DIGITALE', 'Competenze per l’attuazione della transizione digitale', true, 'ADMIN', '2026-03-03', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);
INSERT INTO piao_private.ambitocompetenza
(id, codice, testo, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES(16, 'TRANSIZIONE_ECOLOGICA', 'Competenze per l’attuazione della transizione ecologica', true, 'ADMIN', '2026-03-03', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);
INSERT INTO piao_private.ambitocompetenza
(id, codice, testo, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES(17, 'VALORI_PA', 'Valori e ai principi della PA', true, 'ADMIN', '2026-03-03', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);

--
-- INSERT areatematica
--

INSERT INTO piao_private.areatematica
(id, codice, testo, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES(13, 'SOFT_SKILL', 'Soft skill', true, 'ADMIN', '2026-03-03', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);
INSERT INTO piao_private.areatematica
(id, codice, testo, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES(14, 'MANAGERIALE', 'Manageriale', true, 'ADMIN', '2026-03-03', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);
INSERT INTO piao_private.areatematica
(id, codice, testo, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES(15, 'LAVORO_AGILE', 'Gestione e sviluppo del lavoro agile', true, 'ADMIN', '2026-03-03', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);
INSERT INTO piao_private.areatematica
(id, codice, testo, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES(16, 'PIANIFICAZIONE_CONTROLLO', 'Pianificazione, programmazione e Controllo', true, 'ADMIN', '2026-03-03', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);
INSERT INTO piao_private.areatematica
(id, codice, testo, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES(17, 'MISURAZIONE_PERFORMANCE', 'Misurazione e valutazione della performance individuale e organizzativa', true, 'ADMIN', '2026-03-03', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);
INSERT INTO piao_private.areatematica
(id, codice, testo, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES(18, 'RISORSE_UMANE', 'Reclutamento, formazione, gestione e sviluppo delle risorse umane', true, 'ADMIN', '2026-03-03', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);
INSERT INTO piao_private.areatematica
(id, codice, testo, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES(19, 'COMUNICAZIONE_ESTERNA', 'Comunicazione esterna', true, 'ADMIN', '2026-03-03', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);
INSERT INTO piao_private.areatematica
(id, codice, testo, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES(20, 'ECONOMICO_FINANZIARIA', 'Economico–finanziaria', true, 'ADMIN', '2026-03-03', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);
INSERT INTO piao_private.areatematica
(id, codice, testo, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES(21, 'LINGUE_STRANIERE', 'Lingue straniere', true, 'ADMIN', '2026-03-03', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);
INSERT INTO piao_private.areatematica
(id, codice, testo, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES(22, 'INTERNAZIONALE', 'Attività di carattere internazionale', true, 'ADMIN', '2026-03-03', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);
INSERT INTO piao_private.areatematica
(id, codice, testo, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES(23, 'GIURIDICO_NORMATIVA', 'Giuridico-normativa', true, 'ADMIN', '2026-03-03', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);
INSERT INTO piao_private.areatematica
(id, codice, testo, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES(24, 'TECNICO_SPECIALISTICA', 'Tecnico-specialistica', true, 'ADMIN', '2026-03-03', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);
INSERT INTO piao_private.areatematica
(id, codice, testo, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES(25, 'RIFORME_PNRR', 'Riforme e innovazioni amministrative promosse dal PNRR (transizione amministrativa)', true, 'ADMIN', '2026-03-03', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);
INSERT INTO piao_private.areatematica
(id, codice, testo, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES(26, 'STRUMENTI_PNRR', 'Procedure e strumenti previsti per l’attuazione del PNRR (sistemi di rendicontazione, Regis, attuazione delle circolari MEF, etc)', true, 'ADMIN', '2026-03-03', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);
INSERT INTO piao_private.areatematica
(id, codice, testo, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES(27, 'INFORMATICA_DIGITALE', 'Informatica e trasformazione digitale', true, 'ADMIN', '2026-03-03', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);
INSERT INTO piao_private.areatematica
(id, codice, testo, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES(28, 'TRANSIZIONE_ECOLOGICA', 'Transizione ecologica', true, 'ADMIN', '2026-03-03', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);
INSERT INTO piao_private.areatematica
(id, codice, testo, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES(29, 'GIURIDICO_SICUREZZA_ETICA', 'Giuridico normativa: Salute e sicurezza sul lavoro, anticorruzione, trasparenza e etica', true, 'ADMIN', '2026-03-03', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);
INSERT INTO piao_private.areatematica
(id, codice, testo, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES(30, 'ALTRO', 'Altro', true, 'ADMIN', '2026-03-03', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);

--
-- INSERT Autoritaapprovatore
--
INSERT INTO piao_private.autoritaapprovatore
(id, codice, testo, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES(1, 'GC', 'Giunta Comunale', true, 'ADMIN', '2026-03-25', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);
INSERT INTO piao_private.autoritaapprovatore
(id, codice, testo, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES(2, 'MIN', 'Ministro', true, 'ADMIN', '2026-03-25', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);
INSERT INTO piao_private.autoritaapprovatore
(id, codice, testo, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES(3, 'DT', 'Direttore', true, 'ADMIN', '2026-03-25', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);
INSERT INTO piao_private.autoritaapprovatore
(id, codice, testo, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES(4, 'PR', 'Presidente', true, 'ADMIN', '2026-03-25', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);
--
-- INSERT categoriaobiettivitip
--
INSERT INTO piao_private.categoriaobiettivitip
(id, testo, codtipologiafk, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES(1, 'Obiettivi di Valore Pubblico', 'SEZIONE_21', true, 'ADMIN', '2026-03-23', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);
INSERT INTO piao_private.categoriaobiettivitip
(id, testo, codtipologiafk, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES(2, 'Strategie Attuative', 'SEZIONE_21', true, 'ADMIN', '2026-03-23', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);
INSERT INTO piao_private.categoriaobiettivitip
(id, testo, codtipologiafk, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES(3, 'Obiettivi Trasversali', 'SEZIONE_22', true, 'ADMIN', '2026-03-23', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);
INSERT INTO piao_private.categoriaobiettivitip
(id, testo, codtipologiafk, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES(4, 'Obiettivi di Performance relativi a tutta la PA', 'SEZIONE_22', true, 'ADMIN', '2026-03-23', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);
INSERT INTO piao_private.categoriaobiettivitip
(id, testo, codtipologiafk, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES(5, 'Obiettivi di Performance Organizzativa', 'SEZIONE_22', true, 'ADMIN', '2026-03-23', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);
--
-- INSERT piao_private.configurazioni
--
INSERT INTO piao_private.configurazioni
( codice, valore, typedato, isconfigui, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES( 'DATA_SCADENZA_PIAO', '31/12/2026', 'Date', false, true, 'ADMIN', '2026-04-16', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);
INSERT INTO piao_private.configurazioni
( codice, valore, typedato, isconfigui, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES( 'DATA_COMPILAZIONE_PIAO', '01/12/2026', 'Date', false, true, 'ADMIN', '2026-04-16', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);
INSERT INTO piao_private.configurazioni
( codice, valore, typedato, isconfigui, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES( 'TEMPLATE_EMAIL_SOLLECITI', NULL, 'String', false, true, 'ADMIN', '2026-05-13', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);
--
-- INSERT dimensioneindicatore
--
INSERT INTO piao_private.dimensioneindicatore
(id, value, codtipologiafk)
VALUES(1, 'Impatto sociale', 'OVP');
INSERT INTO piao_private.dimensioneindicatore
(id, value, codtipologiafk)
VALUES(2, 'Impatto economico', 'OVP');
INSERT INTO piao_private.dimensioneindicatore
(id, value, codtipologiafk)
VALUES(3, 'Impatto ambientale', 'OVP');
INSERT INTO piao_private.dimensioneindicatore
(id, value, codtipologiafk)
VALUES(4, 'Impatto sanitario', 'OVP');
INSERT INTO piao_private.dimensioneindicatore
(id, value, codtipologiafk)
VALUES(5, 'Impatto scientifico', 'OVP');
INSERT INTO piao_private.dimensioneindicatore
(id, value, codtipologiafk)
VALUES(6, 'Impatto istituzionale', 'OVP');
INSERT INTO piao_private.dimensioneindicatore
(id, value, codtipologiafk)
VALUES(7, 'Impatto infrastrutturale', 'OVP');
INSERT INTO piao_private.dimensioneindicatore
(id, value, codtipologiafk)
VALUES(8, 'Impatto digitale', 'OVP');
INSERT INTO piao_private.dimensioneindicatore
(id, value, codtipologiafk)
VALUES(9, 'Efficacia', 'OBB');
INSERT INTO piao_private.dimensioneindicatore
(id, value, codtipologiafk)
VALUES(10, 'Efficienza', 'OBB');
INSERT INTO piao_private.dimensioneindicatore
(id, value, codtipologiafk)
VALUES(12, 'Efficacia quantitativa fruita', 'OBB_SUB');
INSERT INTO piao_private.dimensioneindicatore
(id, value, codtipologiafk)
VALUES(13, 'Efficacia qualitativa erogata', 'OBB_SUB');
INSERT INTO piao_private.dimensioneindicatore
(id, value, codtipologiafk)
VALUES(14, 'Efficacia qualitativa percepita', 'OBB_SUB');
INSERT INTO piao_private.dimensioneindicatore
(id, value, codtipologiafk)
VALUES(15, 'Efficenza finanziaria', 'OBB_SUB');
INSERT INTO piao_private.dimensioneindicatore
(id, value, codtipologiafk)
VALUES(16, 'Efficenza gestionale', 'OBB_SUB');
INSERT INTO piao_private.dimensioneindicatore
(id, value, codtipologiafk)
VALUES(17, 'Efficenza produttiva', 'OBB_SUB');
INSERT INTO piao_private.dimensioneindicatore
(id, value, codtipologiafk)
VALUES(18, 'Efficenza temporale', 'OBB_SUB');
--
-- INSERT livellorischio
--
INSERT INTO piao_private.livellorischio
(id, testo, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES(1, 'Basso', true, 'ADMIN', '2026-02-06', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);
INSERT INTO piao_private.livellorischio
(id, testo, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES(2, 'Medio', true, 'ADMIN', '2026-02-06', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);
INSERT INTO piao_private.livellorischio
(id, testo, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES(3, 'Alto', true, 'ADMIN', '2026-02-06', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);
--
-- INSERT motivazionedichiarazione
--
INSERT INTO piao_private.motivazionedichiarazione
(id, testo, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES(1, 'Ritardi amministrativi', true, 'ADMIN', '2026-02-10', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);
INSERT INTO piao_private.motivazionedichiarazione
(id, testo, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES(2, 'Problemi tecnici', true, 'ADMIN', '2026-02-10', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);
INSERT INTO piao_private.motivazionedichiarazione
(id, testo, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES(3, 'Mancata approvazione', true, 'ADMIN', '2026-02-10', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);
INSERT INTO piao_private.motivazionedichiarazione
(id, testo, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES(4, 'Altre cause', true, 'ADMIN', '2026-02-10', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);
--
-- INSERT promemoria
--
INSERT INTO piao_private.promemoria
(id, descrizione, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES(1, 'Un giorno prima', true, 'ADMIN', '2026-02-12', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);
INSERT INTO piao_private.promemoria
(id, descrizione, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES(2, 'In quel giorno alle 9:00', true, 'ADMIN', '2026-02-12', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);
INSERT INTO piao_private.promemoria
(id, descrizione, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES(3, 'Il giorno prima alle 9:00', true, 'ADMIN', '2026-02-12', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);
INSERT INTO piao_private.promemoria
(id, descrizione, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES(4, '2 giorni prima alle 9:00', true, 'ADMIN', '2026-02-12', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);
INSERT INTO piao_private.promemoria
(id, descrizione, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES(5, '1 settimana prima alle 9:00', true, 'ADMIN', '2026-02-12', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);
INSERT INTO piao_private.promemoria
(id, descrizione, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES(6, 'Personalizzato', true, 'ADMIN', '2026-02-12', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);
--
-- INSERT ruolo
--
INSERT INTO piao_private.ruolo
(id, codruolo, descrizione, tipologia)
VALUES(1, 'ROLE_SUPER_USER', 'Super User', 'DFP');
INSERT INTO piao_private.ruolo
(id, codruolo, descrizione, tipologia)
VALUES(2, 'ROLE_AMMINISTRATORE', 'Amministratore', 'DFP');
INSERT INTO piao_private.ruolo
(id, codruolo, descrizione, tipologia)
VALUES(3, 'ROLE_SUPERVISORE', 'Supervisore', 'DFP');
INSERT INTO piao_private.ruolo
(id, codruolo, descrizione, tipologia)
VALUES(4, 'ROLE_REFERENTE', 'Referente', 'PA');
INSERT INTO piao_private.ruolo
(id, codruolo, descrizione, tipologia)
VALUES(5, 'ROLE_COORDINATORE_AMMINISTRATIVO', 'Coordinatore e Amministrativo', 'PA');
INSERT INTO piao_private.ruolo
(id, codruolo, descrizione, tipologia)
VALUES(6, 'ROLE_VALIDATORE', 'Validatore', 'PA');
INSERT INTO piao_private.ruolo
(id, codruolo, descrizione, tipologia)
VALUES(7, 'ROLE_REDATTORE', 'Redattore', 'PA');
--
-- INSERT statosezione
--
INSERT INTO piao_private.statosezione
(id, testo)
VALUES(1, 'Da compilare');
INSERT INTO piao_private.statosezione
(id, testo)
VALUES(2, 'In compilazione');
INSERT INTO piao_private.statosezione
(id, testo)
VALUES(3, 'Compilata');
INSERT INTO piao_private.statosezione
(id, testo)
VALUES(5, 'Validata');
INSERT INTO piao_private.statosezione
(id, testo)
VALUES(6, 'Richiesta Approvazione');
INSERT INTO piao_private.statosezione
(id, testo)
VALUES(7, 'Approvata');
INSERT INTO piao_private.statosezione
(id, testo)
VALUES(8, 'Pubblicata');
INSERT INTO piao_private.statosezione
(id, testo)
VALUES(4, 'In validazione');
--
-- INSERT strutturapiao
--
INSERT INTO piao_private.strutturapiao
(id, idparent, numerosezione, testo, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES(3, NULL, '2', 'Valore pubblico, performance, rischi corruttivi e trasparenza', true, 'SYSTEM', '2025-11-21', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);
INSERT INTO piao_private.strutturapiao
(id, idparent, numerosezione, testo, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES(7, NULL, '3', 'Organizzazione e capitale umano', true, 'SYSTEM', '2025-12-10', 'SYSTEM', NULL, NULL, NULL, NULL, NULL, true, NULL);
INSERT INTO piao_private.strutturapiao
(id, idparent, numerosezione, testo, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES(8, 7, '31', 'Organizzazione', true, 'SYSTEM', '2025-12-10', 'SYSTEM', NULL, NULL, NULL, NULL, NULL, true, NULL);
INSERT INTO piao_private.strutturapiao
(id, idparent, numerosezione, testo, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES(10, 7, '331', 'Fabbisogno del personale', true, 'SYSTEM', '2025-12-10', 'SYSTEM', NULL, NULL, NULL, NULL, NULL, true, NULL);
INSERT INTO piao_private.strutturapiao
(id, idparent, numerosezione, testo, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES(11, 7, '332', 'Formazione del personale', true, 'SYSTEM', '2025-12-10', 'SYSTEM', NULL, NULL, NULL, NULL, NULL, true, NULL);
INSERT INTO piao_private.strutturapiao
(id, idparent, numerosezione, testo, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES(12, NULL, '4', 'Monitoraggio', true, 'SYSTEM', '2025-12-10', 'SYSTEM', NULL, NULL, NULL, NULL, NULL, true, NULL);
INSERT INTO piao_private.strutturapiao
(id, idparent, numerosezione, testo, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES(2, NULL, '1', 'Presentazione metodologica e Anagrafica', true, 'SYSTEM', '2026-02-25', 'SYSTEM', '2026-02-25', NULL, NULL, NULL, NULL, true, NULL);
INSERT INTO piao_private.strutturapiao
(id, idparent, numerosezione, testo, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES(4, 3, '21', 'Valore pubblico', true, 'SYSTEM', '2025-12-10', 'SYSTEM', NULL, NULL, NULL, NULL, NULL, true, NULL);
INSERT INTO piao_private.strutturapiao
(id, idparent, numerosezione, testo, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES(15, NULL, '5', 'Approvazione e pubblicazione', true, 'SYSTEM', '2026-03-11', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);
INSERT INTO piao_private.strutturapiao
(id, idparent, numerosezione, testo, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES(5, 3, '22', 'Performance', true, 'SYSTEM', '2025-12-10', 'SYSTEM', NULL, NULL, NULL, NULL, NULL, true, NULL);
INSERT INTO piao_private.strutturapiao
(id, idparent, numerosezione, testo, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES(6, 3, '23', 'Rischi corruttivi  e trasparenza', true, 'SYSTEM', '2025-12-10', 'SYSTEM', NULL, NULL, NULL, NULL, NULL, true, NULL);
INSERT INTO piao_private.strutturapiao
(id, idparent, numerosezione, testo, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES(19, NULL, '0', 'PIAO', true, 'SYSTEM', '2026-03-24', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);
INSERT INTO piao_private.strutturapiao
(id, idparent, numerosezione, testo, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES(9, 7, '32', 'Lavoro agile', true, 'SYSTEM', '2025-12-10', 'SYSTEM', NULL, NULL, NULL, NULL, NULL, true, NULL);
--
-- INSERT targetindicatore
--
INSERT INTO piao_private.targetindicatore
(id, value)
VALUES(1, 'Uguale a');
INSERT INTO piao_private.targetindicatore
(id, value)
VALUES(2, 'Maggiore o uguale a');
INSERT INTO piao_private.targetindicatore
(id, value)
VALUES(3, 'Maggiore di');
INSERT INTO piao_private.targetindicatore
(id, value)
VALUES(4, 'Minore o uguale a');
INSERT INTO piao_private.targetindicatore
(id, value)
VALUES(5, 'Minore di');
--
-- INSERT tipologiaattivita
--
INSERT INTO piao_private.tipologiaattivita
(id, codice, testo, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES(4, 'AULA', 'Attività di aula', true, 'ADMIN', '2026-03-03', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);
INSERT INTO piao_private.tipologiaattivita
(id, codice, testo, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES(5, 'REMOTO_SYNC', 'Formazione erogata da remoto in modalità sincrona (es. webinar)', true, 'ADMIN', '2026-03-03', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);
INSERT INTO piao_private.tipologiaattivita
(id, codice, testo, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES(6, 'REMOTO_ASYNC', 'Formazione erogata da remoto in modalità asincrona (es. MOOC)', true, 'ADMIN', '2026-03-03', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);
INSERT INTO piao_private.tipologiaattivita
(id, codice, testo, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES(7, 'IBRIDA', 'Formazione erogata in modalità ibrida', true, 'ADMIN', '2026-03-03', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);
INSERT INTO piao_private.tipologiaattivita
(id, codice, testo, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES(8, 'COM_PRATICA', 'Comunità di pratica', true, 'ADMIN', '2026-03-03', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);
INSERT INTO piao_private.tipologiaattivita
(id, codice, testo, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES(9, 'MENTORING', 'Mentoring', true, 'ADMIN', '2026-03-03', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);
INSERT INTO piao_private.tipologiaattivita
(id, codice, testo, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES(10, 'STAGE', 'Stage', true, 'ADMIN', '2026-03-03', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);
INSERT INTO piao_private.tipologiaattivita
(id, codice, testo, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES(11, 'SEMINARI_PRES', 'Seminari, convegni e conferenze in presenza', true, 'ADMIN', '2026-03-03', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);
INSERT INTO piao_private.tipologiaattivita
(id, codice, testo, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES(12, 'SEMINARI_REMOTO', 'Seminari, convegni e conferenze da remoto', true, 'ADMIN', '2026-03-03', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);
--
-- INSERT tipologiadestinatari
--
INSERT INTO piao_private.tipologiadestinatari
(id, codice, testo, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES(4, 'DIRIGENTE', 'Dirigente', true, 'ADMIN', '2026-03-03', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);
INSERT INTO piao_private.tipologiadestinatari
(id, codice, testo, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES(5, 'NON_DIRIGENTE', 'Non dirigente', true, 'ADMIN', '2026-03-03', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);
INSERT INTO piao_private.tipologiadestinatari
(id, codice, testo, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES(6, 'ENTRAMBI', 'Entrambi', true, 'ADMIN', '2026-03-03', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);
--
-- INSERT statopiao
--
INSERT INTO piao_private.statopiao
(id, testo)
VALUES(1, 'Da compilare');
INSERT INTO piao_private.statopiao
(id, testo)
VALUES(2, 'In compilazione');
INSERT INTO piao_private.statopiao
(id, testo)
VALUES(8, 'Pubblicata');
INSERT INTO piao_private.statopiao
(id, testo)
VALUES(7, 'Approvata');
INSERT INTO piao_private.statopiao
(id, testo)
VALUES(6, 'Richiesta Approvazione');
INSERT INTO piao_private.statopiao
(id, testo)
VALUES(5, 'Validata');
INSERT INTO piao_private.statopiao
(id, testo)
VALUES(3, 'Compilata');
INSERT INTO piao_private.statopiao
(id, testo)
VALUES(4, 'In validazione');

--Insert CategoriaTicket common_services

INSERT INTO common_services.categoriaticket
(id, codice, testo, id_modulo, externalid)
VALUES(1, 'TECNICA_PIAO', 'Tecnica Piao', 'PIAO', 51);
INSERT INTO common_services.categoriaticket
(id, codice, testo, id_modulo, externalid)
VALUES(2, 'REG_ONB_PIAO', 'Registrazione/Onboarding PIAO', 'PIAO', 50);
INSERT INTO common_services.categoriaticket
(id, codice, testo, id_modulo, externalid)
VALUES(3, 'LOAD_EDIT_PIAO', 'Caricamento e modifica del PIAO', 'PIAO', 53);
INSERT INTO common_services.categoriaticket
(id, codice, testo, id_modulo, externalid)
VALUES(4, 'MORE_INFO_PIAO', 'Altre informazioni PIAO', 'PIAO', 52);
INSERT INTO common_services.categoriaticket
(id, codice, testo, id_modulo, externalid)
VALUES(5, 'INFO_UFF_Q_PERF', 'Informazioni Ufficio Qualità Performance e Riforme', 'PP_PERFORMANCE', 10);
INSERT INTO common_services.categoriaticket
(id, codice, testo, id_modulo, externalid)
VALUES(6, 'DOCS_CICLO', 'Documenti del ciclo', 'PERFORMANCE', 4);
INSERT INTO common_services.categoriaticket
(id, codice, testo, id_modulo, externalid)
VALUES(7, 'MONITORAGGIO_OIV', 'Monitoraggio OIV', 'PERFORMANCE', 7);
INSERT INTO common_services.categoriaticket
(id, codice, testo, id_modulo, externalid)
VALUES(8, 'SYS_MIS_VAL', 'Sistema di misurazione e valutazione', 'PERFORMANCE', 6);
INSERT INTO common_services.categoriaticket
(id, codice, testo, id_modulo, externalid)
VALUES(9, 'TECNICA_PERFORMANCE', 'Tecnica portale Performance', 'PERFORMANCE', 16);

--Insert Config per email
INSERT INTO piao_private.configurazioni
(codice, valore, typedato, isconfigui, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES('DATA_SCADENZA_PIAO', '31/12/2026', 'Date', false, true, 'ADMIN', '2026-04-16', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);
INSERT INTO piao_private.configurazioni
(codice, valore, typedato, isconfigui, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES('DATA_COMPILAZIONE_PIAO', '12/09/2026', 'Date', false, true, 'ADMIN', '2026-04-16', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);
INSERT INTO piao_private.configurazioni
(codice, valore, typedato, isconfigui, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES('NOTIFICA_GENERA_PDF', 'Il documento è stato creato correttamente ed è pronto per essere scaricato', 'String', true, true, 'ADMIN', '2026-04-16', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);
INSERT INTO piao_private.configurazioni
(codice, valore, typedato, isconfigui, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES('NOTIFICA_GENERA_EXCEL', 'Il documento è stato creato correttamente ed è pronto per essere scaricato', 'String', true, true, 'ADMIN', '2026-04-16', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);
INSERT INTO piao_private.configurazioni
(codice, valore, typedato, isconfigui, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES('BODY_EMAIL_DEFAULT', '<h1>Email di default</h1><p>Questa è un''email di prova inviata dal sistema PIAO.</p>', 'String', true, true, 'ADMIN', '2026-04-16', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);
INSERT INTO piao_private.configurazioni
(codice, valore, typedato, isconfigui, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES('FROM_ADDRESS', 'collaudo@performance.gov.it', 'String', true, true, 'ADMIN', '2026-04-16', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);
INSERT INTO piao_private.configurazioni
(codice, valore, typedato, isconfigui, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES('FROM_ADDRESS_NAME', 'Sistema PIAO', 'String', true, true, 'ADMIN', '2026-04-16', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);
INSERT INTO piao_private.configurazioni
(codice, valore, typedato, isconfigui, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES('BODY_EMAIL_SOLLECITO', '<p>Testo Sollecito</p>', 'String', false, true, 'ADMIN', '2026-05-13', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);
INSERT INTO piao_private.configurazioni
(codice, valore, typedato, isconfigui, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES('BODY_EMAIL_SEZIONE_1_RICHIEDI_VALIDAZIONE', '<h1>Richiedi Validazione</h1><p>Per la Sezione 1 è stata richiesta la validazione.</p>', 'String', true, true, 'ADMIN', '2026-04-16', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);
INSERT INTO piao_private.configurazioni
(codice, valore, typedato, isconfigui, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES('BODY_EMAIL_SEZIONE_1_REVOCA_VALIDAZIONE', '<h1>Revoca Validazione</h1><p>Per la Sezione 1 è stata revocata la validazione.</p>', 'String', true, true, 'ADMIN', '2026-04-16', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);
INSERT INTO piao_private.configurazioni
(codice, valore, typedato, isconfigui, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES('BODY_EMAIL_SEZIONE_1_ANNULLA_VALIDAZIONE', '<h1>Annulla Validazione</h1><p>Per la Sezione 1 è stata annullata la validazione.</p>', 'String', true, true, 'ADMIN', '2026-04-16', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);
INSERT INTO piao_private.configurazioni
(codice, valore, typedato, isconfigui, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES('BODY_EMAIL_SEZIONE_1_ACCETTA_VALIDAZIONE', '<h1>Accetta Validazione</h1><p>Per la Sezione 1 è stata accettata la validazione</p>', 'String', true, true, 'ADMIN', '2026-04-16', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);
INSERT INTO piao_private.configurazioni
(codice, valore, typedato, isconfigui, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES('BODY_EMAIL_SEZIONE_1_RIFIUTA_VALIDAZIONE', '<h1>Rifiuta Validazione</h1><p>Per la Sezione 1 è stata rifiutata la validazione</p>', 'String', true, true, 'ADMIN', '2026-04-16', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);
INSERT INTO piao_private.configurazioni
(codice, valore, typedato, isconfigui, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES('BODY_EMAIL_SEZIONE_21_RICHIEDI_VALIDAZIONE', '<h1>Richiedi Validazione</h1><p>Per la Sezione 2.1 è stata richiesta la validazione.</p>', 'String', true, true, 'ADMIN', '2026-04-16', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);
INSERT INTO piao_private.configurazioni
(codice, valore, typedato, isconfigui, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES('BODY_EMAIL_SEZIONE_21_REVOCA_VALIDAZIONE', '<h1>Revoca Validazione</h1><p>Per la Sezione 2.1 è stata revocata la validazione.</p>', 'String', true, true, 'ADMIN', '2026-04-16', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);
INSERT INTO piao_private.configurazioni
(codice, valore, typedato, isconfigui, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES('BODY_EMAIL_SEZIONE_21_ANNULLA_VALIDAZIONE', '<h1>Annulla Validazione</h1><p>Per la Sezione 2.1 è stata annullata la validazione.</p>', 'String', true, true, 'ADMIN', '2026-04-16', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);
INSERT INTO piao_private.configurazioni
(codice, valore, typedato, isconfigui, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES('BODY_EMAIL_SEZIONE_21_ACCETTA_VALIDAZIONE', '<h1>Accetta Validazione</h1><p>Per la Sezione 2.1 è stata accettata la validazione</p>', 'String', true, true, 'ADMIN', '2026-04-16', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);
INSERT INTO piao_private.configurazioni
(codice, valore, typedato, isconfigui, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES('BODY_EMAIL_SEZIONE_21_RIFIUTA_VALIDAZIONE', '<h1>Rifiuta Validazione</h1><p>Per la Sezione 2.1 è stata rifiutata la validazione</p>', 'String', true, true, 'ADMIN', '2026-04-16', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);
INSERT INTO piao_private.configurazioni
(codice, valore, typedato, isconfigui, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES('BODY_EMAIL_SEZIONE_22_RICHIEDI_VALIDAZIONE', '<h1>Richiedi Validazione</h1><p>Per la Sezione 2.2 è stata richiesta la validazione.</p>', 'String', true, true, 'ADMIN', '2026-04-16', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);
INSERT INTO piao_private.configurazioni
(codice, valore, typedato, isconfigui, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES('BODY_EMAIL_SEZIONE_22_REVOCA_VALIDAZIONE', '<h1>Revoca Validazione</h1><p>Per la Sezione 2.2 è stata revocata la validazione.</p>', 'String', true, true, 'ADMIN', '2026-04-16', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);
INSERT INTO piao_private.configurazioni
(codice, valore, typedato, isconfigui, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES('BODY_EMAIL_SEZIONE_22_ANNULLA_VALIDAZIONE', '<h1>Annulla Validazione</h1><p>Per la Sezione 2.2 è stata annullata la validazione.</p>', 'String', true, true, 'ADMIN', '2026-04-16', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);
INSERT INTO piao_private.configurazioni
(codice, valore, typedato, isconfigui, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES('BODY_EMAIL_SEZIONE_22_ACCETTA_VALIDAZIONE', '<h1>Accetta Validazione</h1><p>Per la Sezione 2.2 è stata accettata la validazione</p>', 'String', true, true, 'ADMIN', '2026-04-16', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);
INSERT INTO piao_private.configurazioni
(codice, valore, typedato, isconfigui, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES('BODY_EMAIL_SEZIONE_22_RIFIUTA_VALIDAZIONE', '<h1>Rifiuta Validazione</h1><p>Per la Sezione 2.2 è stata rifiutata la validazione</p>', 'String', true, true, 'ADMIN', '2026-04-16', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);
INSERT INTO piao_private.configurazioni
(codice, valore, typedato, isconfigui, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES('BODY_EMAIL_SEZIONE_23_RICHIEDI_VALIDAZIONE', '<h1>Richiedi Validazione</h1><p>Per la Sezione 2.3 è stata richiesta la validazione.</p>', 'String', true, true, 'ADMIN', '2026-04-16', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);
INSERT INTO piao_private.configurazioni
(codice, valore, typedato, isconfigui, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES('BODY_EMAIL_SEZIONE_23_REVOCA_VALIDAZIONE', '<h1>Revoca Validazione</h1><p>Per la Sezione 2.3 è stata revocata la validazione.</p>', 'String', true, true, 'ADMIN', '2026-04-16', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);
INSERT INTO piao_private.configurazioni
(codice, valore, typedato, isconfigui, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES('BODY_EMAIL_SEZIONE_23_ANNULLA_VALIDAZIONE', '<h1>Annulla Validazione</h1><p>Per la Sezione 2.3 è stata annullata la validazione.</p>', 'String', true, true, 'ADMIN', '2026-04-16', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);
INSERT INTO piao_private.configurazioni
(codice, valore, typedato, isconfigui, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES('BODY_EMAIL_SEZIONE_23_ACCETTA_VALIDAZIONE', '<h1>Accetta Validazione</h1><p>Per la Sezione 2.3 è stata accettata la validazione</p>', 'String', true, true, 'ADMIN', '2026-04-16', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);
INSERT INTO piao_private.configurazioni
(codice, valore, typedato, isconfigui, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES('BODY_EMAIL_SEZIONE_23_RIFIUTA_VALIDAZIONE', '<h1>Rifiuta Validazione</h1><p>Per la Sezione 2.3 è stata rifiutata la validazione</p>', 'String', true, true, 'ADMIN', '2026-04-16', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);
INSERT INTO piao_private.configurazioni
(codice, valore, typedato, isconfigui, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES('BODY_EMAIL_SEZIONE_31_RICHIEDI_VALIDAZIONE', '<h1>Richiedi Validazione</h1><p>Per la Sezione 3.1 è stata richiesta la validazione.</p>', 'String', true, true, 'ADMIN', '2026-04-16', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);
INSERT INTO piao_private.configurazioni
(codice, valore, typedato, isconfigui, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES('BODY_EMAIL_SEZIONE_31_REVOCA_VALIDAZIONE', '<h1>Revoca Validazione</h1><p>Per la Sezione 3.1 è stata revocata la validazione.</p>', 'String', true, true, 'ADMIN', '2026-04-16', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);
INSERT INTO piao_private.configurazioni
(codice, valore, typedato, isconfigui, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES('BODY_EMAIL_SEZIONE_31_ANNULLA_VALIDAZIONE', '<h1>Annulla Validazione</h1><p>Per la Sezione 3.1 è stata annullata la validazione.</p>', 'String', true, true, 'ADMIN', '2026-04-16', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);
INSERT INTO piao_private.configurazioni
(codice, valore, typedato, isconfigui, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES('BODY_EMAIL_SEZIONE_31_ACCETTA_VALIDAZIONE', '<h1>Accetta Validazione</h1><p>Per la Sezione 3.1 è stata accettata la validazione</p>', 'String', true, true, 'ADMIN', '2026-04-16', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);
INSERT INTO piao_private.configurazioni
(codice, valore, typedato, isconfigui, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES('BODY_EMAIL_SEZIONE_31_RIFIUTA_VALIDAZIONE', '<h1>Rifiuta Validazione</h1><p>Per la Sezione 3.1 è stata rifiutata la validazione</p>', 'String', true, true, 'ADMIN', '2026-04-16', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);
INSERT INTO piao_private.configurazioni
(codice, valore, typedato, isconfigui, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES('BODY_EMAIL_SEZIONE_32_RICHIEDI_VALIDAZIONE', '<h1>Richiedi Validazione</h1><p>Per la Sezione 3.2 è stata richiesta la validazione.</p>', 'String', true, true, 'ADMIN', '2026-04-16', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);
INSERT INTO piao_private.configurazioni
(codice, valore, typedato, isconfigui, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES('BODY_EMAIL_SEZIONE_32_REVOCA_VALIDAZIONE', '<h1>Revoca Validazione</h1><p>Per la Sezione 3.2 è stata revocata la validazione.</p>', 'String', true, true, 'ADMIN', '2026-04-16', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);
INSERT INTO piao_private.configurazioni
(codice, valore, typedato, isconfigui, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES('BODY_EMAIL_SEZIONE_32_ANNULLA_VALIDAZIONE', '<h1>Annulla Validazione</h1><p>Per la Sezione 3.2 è stata annullata la validazione.</p>', 'String', true, true, 'ADMIN', '2026-04-16', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);
INSERT INTO piao_private.configurazioni
(codice, valore, typedato, isconfigui, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES('BODY_EMAIL_SEZIONE_32_ACCETTA_VALIDAZIONE', '<h1>Accetta Validazione</h1><p>Per la Sezione 3.2 è stata accettata la validazione</p>', 'String', true, true, 'ADMIN', '2026-04-16', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);
INSERT INTO piao_private.configurazioni
(codice, valore, typedato, isconfigui, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES('BODY_EMAIL_SEZIONE_32_RIFIUTA_VALIDAZIONE', '<h1>Rifiuta Validazione</h1><p>Per la Sezione 3.2 è stata rifiutata la validazione</p>', 'String', true, true, 'ADMIN', '2026-04-16', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);
INSERT INTO piao_private.configurazioni
(codice, valore, typedato, isconfigui, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES('BODY_EMAIL_SEZIONE_331_RICHIEDI_VALIDAZIONE', '<h1>Richiedi Validazione</h1><p>Per la Sezione 3.3.1 è stata richiesta la validazione.</p>', 'String', true, true, 'ADMIN', '2026-04-16', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);
INSERT INTO piao_private.configurazioni
(codice, valore, typedato, isconfigui, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES('BODY_EMAIL_SEZIONE_331_REVOCA_VALIDAZIONE', '<h1>Revoca Validazione</h1><p>Per la Sezione 3.3.1 è stata revocata la validazione.</p>', 'String', true, true, 'ADMIN', '2026-04-16', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);
INSERT INTO piao_private.configurazioni
(codice, valore, typedato, isconfigui, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES('BODY_EMAIL_SEZIONE_331_ANNULLA_VALIDAZIONE', '<h1>Annulla Validazione</h1><p>Per la Sezione 3.3.1 è stata annullata la validazione.</p>', 'String', true, true, 'ADMIN', '2026-04-16', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);
INSERT INTO piao_private.configurazioni
(codice, valore, typedato, isconfigui, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES('BODY_EMAIL_SEZIONE_331_ACCETTA_VALIDAZIONE', '<h1>Accetta Validazione</h1><p>Per la Sezione 3.3.1 è stata accettata la validazione</p>', 'String', true, true, 'ADMIN', '2026-04-16', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);
INSERT INTO piao_private.configurazioni
(codice, valore, typedato, isconfigui, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES('BODY_EMAIL_SEZIONE_331_RIFIUTA_VALIDAZIONE', '<h1>Rifiuta Validazione</h1><p>Per la Sezione 3.3.1 è stata rifiutata la validazione</p>', 'String', true, true, 'ADMIN', '2026-04-16', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);
INSERT INTO piao_private.configurazioni
(codice, valore, typedato, isconfigui, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES('BODY_EMAIL_SEZIONE_332_RICHIEDI_VALIDAZIONE', '<h1>Richiedi Validazione</h1><p>Per la Sezione 3.3.2 è stata richiesta la validazione.</p>', 'String', true, true, 'ADMIN', '2026-04-16', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);
INSERT INTO piao_private.configurazioni
(codice, valore, typedato, isconfigui, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES('BODY_EMAIL_SEZIONE_332_REVOCA_VALIDAZIONE', '<h1>Revoca Validazione</h1><p>Per la Sezione 3.3.2 è stata revocata la validazione.</p>', 'String', true, true, 'ADMIN', '2026-04-16', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);
INSERT INTO piao_private.configurazioni
(codice, valore, typedato, isconfigui, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES('BODY_EMAIL_SEZIONE_332_ANNULLA_VALIDAZIONE', '<h1>Annulla Validazione</h1><p>Per la Sezione 3.3.2 è stata annullata la validazione.</p>', 'String', true, true, 'ADMIN', '2026-04-16', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);
INSERT INTO piao_private.configurazioni
(codice, valore, typedato, isconfigui, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES('BODY_EMAIL_SEZIONE_332_ACCETTA_VALIDAZIONE', '<h1>Accetta Validazione</h1><p>Per la Sezione 3.3.2 è stata accettata la validazione</p>', 'String', true, true, 'ADMIN', '2026-04-16', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);
INSERT INTO piao_private.configurazioni
(codice, valore, typedato, isconfigui, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES('BODY_EMAIL_SEZIONE_332_RIFIUTA_VALIDAZIONE', '<h1>Rifiuta Validazione</h1><p>Per la Sezione 3.3.2 è stata rifiutata la validazione</p>', 'String', true, true, 'ADMIN', '2026-04-16', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);
INSERT INTO piao_private.configurazioni
(codice, valore, typedato, isconfigui, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES('MAIL_OBJECT_SEZIONE_1_RIFIUTA_VALIDAZIONE', 'Rifiutata la richiesta di validazione Sezione 1', 'String', true, true, 'ADMIN', '2026-04-16', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);
INSERT INTO piao_private.configurazioni
(codice, valore, typedato, isconfigui, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES('MAIL_OBJECT_SEZIONE_1_RICHIEDI_VALIDAZIONE', 'Richiesta la validazione  Sezione 1', 'String', true, true, 'ADMIN', '2026-04-16', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);
INSERT INTO piao_private.configurazioni
(codice, valore, typedato, isconfigui, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES('MAIL_OBJECT_SEZIONE_1_REVOCA_VALIDAZIONE', 'Revocata la richiesta di validazione Sezione 1', 'String', true, true, 'ADMIN', '2026-04-16', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);
INSERT INTO piao_private.configurazioni
(codice, valore, typedato, isconfigui, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES('MAIL_OBJECT_SEZIONE_1_ANNULLA_VALIDAZIONE', 'Annullata la richiesta di validazione Sezione 1', 'String', true, true, 'ADMIN', '2026-04-16', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);
INSERT INTO piao_private.configurazioni
(codice, valore, typedato, isconfigui, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES('MAIL_OBJECT_SEZIONE_1_ACCETTA_VALIDAZIONE', 'Accettata la richiesta di validazione Sezione 1', 'String', true, true, 'ADMIN', '2026-04-16', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);
INSERT INTO piao_private.configurazioni
(codice, valore, typedato, isconfigui, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES('MAIL_OBJECT_SEZIONE_21_RIFIUTA_VALIDAZIONE', 'Rifiutata la richiesta di validazione Sezione 2.1', 'String', true, true, 'ADMIN', '2026-04-16', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);
INSERT INTO piao_private.configurazioni
(codice, valore, typedato, isconfigui, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES('MAIL_OBJECT_SEZIONE_21_RICHIEDI_VALIDAZIONE', 'Richiesta la validazione  Sezione 2.1', 'String', true, true, 'ADMIN', '2026-04-16', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);
INSERT INTO piao_private.configurazioni
(codice, valore, typedato, isconfigui, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES('MAIL_OBJECT_SEZIONE_21_REVOCA_VALIDAZIONE', 'Revocata la richiesta di validazione Sezione 2.1', 'String', true, true, 'ADMIN', '2026-04-16', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);
INSERT INTO piao_private.configurazioni
(codice, valore, typedato, isconfigui, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES('MAIL_OBJECT_SEZIONE_21_ANNULLA_VALIDAZIONE', 'Annullata la richiesta di validazione Sezione 2.1', 'String', true, true, 'ADMIN', '2026-04-16', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);
INSERT INTO piao_private.configurazioni
(codice, valore, typedato, isconfigui, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES('MAIL_OBJECT_SEZIONE_21_ACCETTA_VALIDAZIONE', 'Accettata la richiesta di validazione Sezione 2.1', 'String', true, true, 'ADMIN', '2026-04-16', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);
INSERT INTO piao_private.configurazioni
(codice, valore, typedato, isconfigui, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES('MAIL_OBJECT_SEZIONE_22_RIFIUTA_VALIDAZIONE', 'Rifiutata la richiesta di validazione Sezione 2.2', 'String', true, true, 'ADMIN', '2026-04-16', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);
INSERT INTO piao_private.configurazioni
(codice, valore, typedato, isconfigui, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES('MAIL_OBJECT_SEZIONE_22_RICHIEDI_VALIDAZIONE', 'Richiesta la validazione  Sezione 2.2', 'String', true, true, 'ADMIN', '2026-04-16', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);
INSERT INTO piao_private.configurazioni
(codice, valore, typedato, isconfigui, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES('MAIL_OBJECT_SEZIONE_22_REVOCA_VALIDAZIONE', 'Revocata la richiesta di validazione Sezione 2.2', 'String', true, true, 'ADMIN', '2026-04-16', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);
INSERT INTO piao_private.configurazioni
(codice, valore, typedato, isconfigui, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES('MAIL_OBJECT_SEZIONE_22_ANNULLA_VALIDAZIONE', 'Annullata la richiesta di validazione Sezione 2.2', 'String', true, true, 'ADMIN', '2026-04-16', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);
INSERT INTO piao_private.configurazioni
(codice, valore, typedato, isconfigui, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES('MAIL_OBJECT_SEZIONE_22_ACCETTA_VALIDAZIONE', 'Accettata la richiesta di validazione Sezione 2.2', 'String', true, true, 'ADMIN', '2026-04-16', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);
INSERT INTO piao_private.configurazioni
(codice, valore, typedato, isconfigui, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES('MAIL_OBJECT_SEZIONE_23_RIFIUTA_VALIDAZIONE', 'Rifiutata la richiesta di validazione Sezione 2.3', 'String', true, true, 'ADMIN', '2026-04-16', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);
INSERT INTO piao_private.configurazioni
(codice, valore, typedato, isconfigui, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES('MAIL_OBJECT_SEZIONE_23_RICHIEDI_VALIDAZIONE', 'Richiesta la validazione  Sezione 2.3', 'String', true, true, 'ADMIN', '2026-04-16', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);
INSERT INTO piao_private.configurazioni
(codice, valore, typedato, isconfigui, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES('MAIL_OBJECT_SEZIONE_23_REVOCA_VALIDAZIONE', 'Revocata la richiesta di validazione Sezione 2.3', 'String', true, true, 'ADMIN', '2026-04-16', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);
INSERT INTO piao_private.configurazioni
(codice, valore, typedato, isconfigui, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES('MAIL_OBJECT_SEZIONE_23_ANNULLA_VALIDAZIONE', 'Annullata la richiesta di validazione Sezione 2.3', 'String', true, true, 'ADMIN', '2026-04-16', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);
INSERT INTO piao_private.configurazioni
(codice, valore, typedato, isconfigui, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES('MAIL_OBJECT_SEZIONE_23_ACCETTA_VALIDAZIONE', 'Accettata la richiesta di validazione Sezione 2.3', 'String', true, true, 'ADMIN', '2026-04-16', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);
INSERT INTO piao_private.configurazioni
(codice, valore, typedato, isconfigui, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES('MAIL_OBJECT_SEZIONE_31_RIFIUTA_VALIDAZIONE', 'Rifiutata la richiesta di validazione Sezione 3.1', 'String', true, true, 'ADMIN', '2026-04-16', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);
INSERT INTO piao_private.configurazioni
(codice, valore, typedato, isconfigui, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES('MAIL_OBJECT_SEZIONE_31_RICHIEDI_VALIDAZIONE', 'Richiesta la validazione  Sezione 3.1', 'String', true, true, 'ADMIN', '2026-04-16', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);
INSERT INTO piao_private.configurazioni
(codice, valore, typedato, isconfigui, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES('MAIL_OBJECT_SEZIONE_31_REVOCA_VALIDAZIONE', 'Revocata la richiesta di validazione Sezione 3.1', 'String', true, true, 'ADMIN', '2026-04-16', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);
INSERT INTO piao_private.configurazioni
(codice, valore, typedato, isconfigui, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES('MAIL_OBJECT_SEZIONE_31_ANNULLA_VALIDAZIONE', 'Annullata la richiesta di validazione Sezione 3.1', 'String', true, true, 'ADMIN', '2026-04-16', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);
INSERT INTO piao_private.configurazioni
(codice, valore, typedato, isconfigui, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES('MAIL_OBJECT_SEZIONE_31_ACCETTA_VALIDAZIONE', 'Accettata la richiesta di validazione Sezione 3.1', 'String', true, true, 'ADMIN', '2026-04-16', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);
INSERT INTO piao_private.configurazioni
(codice, valore, typedato, isconfigui, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES('MAIL_OBJECT_SEZIONE_32_RIFIUTA_VALIDAZIONE', 'Rifiutata la richiesta di validazione Sezione 3.2', 'String', true, true, 'ADMIN', '2026-04-16', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);
INSERT INTO piao_private.configurazioni
(codice, valore, typedato, isconfigui, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES('MAIL_OBJECT_SEZIONE_32_RICHIEDI_VALIDAZIONE', 'Richiesta la validazione  Sezione 3.2', 'String', true, true, 'ADMIN', '2026-04-16', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);
INSERT INTO piao_private.configurazioni
(codice, valore, typedato, isconfigui, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES('MAIL_OBJECT_SEZIONE_32_REVOCA_VALIDAZIONE', 'Revocata la richiesta di validazione Sezione 3.2', 'String', true, true, 'ADMIN', '2026-04-16', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);
INSERT INTO piao_private.configurazioni
(codice, valore, typedato, isconfigui, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES('MAIL_OBJECT_SEZIONE_32_ANNULLA_VALIDAZIONE', 'Annullata la richiesta di validazione Sezione 3.2', 'String', true, true, 'ADMIN', '2026-04-16', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);
INSERT INTO piao_private.configurazioni
(codice, valore, typedato, isconfigui, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES('MAIL_OBJECT_SEZIONE_32_ACCETTA_VALIDAZIONE', 'Accettata la richiesta di validazione Sezione 3.2', 'String', true, true, 'ADMIN', '2026-04-16', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);
INSERT INTO piao_private.configurazioni
(codice, valore, typedato, isconfigui, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES('MAIL_OBJECT_SEZIONE_331_RIFIUTA_VALIDAZIONE', 'Rifiutata la richiesta di validazione Sezione 3.3.1', 'String', true, true, 'ADMIN', '2026-04-16', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);
INSERT INTO piao_private.configurazioni
(codice, valore, typedato, isconfigui, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES('MAIL_OBJECT_SEZIONE_331_RICHIEDI_VALIDAZIONE', 'Richiesta la validazione  Sezione 3.3.1', 'String', true, true, 'ADMIN', '2026-04-16', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);
INSERT INTO piao_private.configurazioni
(codice, valore, typedato, isconfigui, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES('MAIL_OBJECT_SEZIONE_331_REVOCA_VALIDAZIONE', 'Revocata la richiesta di validazione Sezione 3.3.1', 'String', true, true, 'ADMIN', '2026-04-16', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);
INSERT INTO piao_private.configurazioni
(codice, valore, typedato, isconfigui, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES('MAIL_OBJECT_SEZIONE_331_ANNULLA_VALIDAZIONE', 'Annullata la richiesta di validazione Sezione 3.3.1', 'String', true, true, 'ADMIN', '2026-04-16', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);
INSERT INTO piao_private.configurazioni
(codice, valore, typedato, isconfigui, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES('MAIL_OBJECT_SEZIONE_331_ACCETTA_VALIDAZIONE', 'Accettata la richiesta di validazione Sezione 3.3.1', 'String', true, true, 'ADMIN', '2026-04-16', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);
INSERT INTO piao_private.configurazioni
(codice, valore, typedato, isconfigui, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES('MAIL_OBJECT_SEZIONE_332_RIFIUTA_VALIDAZIONE', 'Rifiutata la richiesta di validazione Sezione 3.3.2', 'String', true, true, 'ADMIN', '2026-04-16', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);
INSERT INTO piao_private.configurazioni
(codice, valore, typedato, isconfigui, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES('MAIL_OBJECT_SEZIONE_332_RICHIEDI_VALIDAZIONE', 'Richiesta la validazione  Sezione 3.3.2', 'String', true, true, 'ADMIN', '2026-04-16', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);
INSERT INTO piao_private.configurazioni
(codice, valore, typedato, isconfigui, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES('MAIL_OBJECT_SEZIONE_332_REVOCA_VALIDAZIONE', 'Revocata la richiesta di validazione Sezione 3.3.2', 'String', true, true, 'ADMIN', '2026-04-16', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);
INSERT INTO piao_private.configurazioni
(codice, valore, typedato, isconfigui, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES('MAIL_OBJECT_SEZIONE_332_ANNULLA_VALIDAZIONE', 'Annullata la richiesta di validazione Sezione 3.3.2', 'String', true, true, 'ADMIN', '2026-04-16', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);
INSERT INTO piao_private.configurazioni
(codice, valore, typedato, isconfigui, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES('MAIL_OBJECT_SEZIONE_332_ACCETTA_VALIDAZIONE', 'Accettata la richiesta di validazione Sezione 3.3.2', 'String', true, true, 'ADMIN', '2026-04-16', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);
INSERT INTO piao_private.configurazioni
(codice, valore, typedato, isconfigui, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES('BODY_EMAIL_SEZIONE_4_RICHIEDI_VALIDAZIONE', '<h1>Richiedi Validazione</h1><p>Per la Sezione 4 è stata richiesta la validazione.</p>', 'String', true, true, 'ADMIN', '2026-04-16', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);
INSERT INTO piao_private.configurazioni
(codice, valore, typedato, isconfigui, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES('BODY_EMAIL_SEZIONE_4_REVOCA_VALIDAZIONE', '<h1>Revoca Validazione</h1><p>Per la Sezione 4 è stata revocata la validazione.</p>', 'String', true, true, 'ADMIN', '2026-04-16', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);
INSERT INTO piao_private.configurazioni
(codice, valore, typedato, isconfigui, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES('BODY_EMAIL_SEZIONE_4_ANNULLA_VALIDAZIONE', '<h1>Annulla Validazione</h1><p>Per la Sezione 4 è stata annullata la validazione.</p>', 'String', true, true, 'ADMIN', '2026-04-16', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);
INSERT INTO piao_private.configurazioni
(codice, valore, typedato, isconfigui, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES('BODY_EMAIL_SEZIONE_4_ACCETTA_VALIDAZIONE', '<h1>Accetta Validazione</h1><p>Per la Sezione 4 è stata accettata la validazione</p>', 'String', true, true, 'ADMIN', '2026-04-16', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);
INSERT INTO piao_private.configurazioni
(codice, valore, typedato, isconfigui, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES('BODY_EMAIL_SEZIONE_4_RIFIUTA_VALIDAZIONE', '<h1>Rifiuta Validazione</h1><p>Per la Sezione 4 è stata rifiutata la validazione</p>', 'String', true, true, 'ADMIN', '2026-04-16', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);
INSERT INTO piao_private.configurazioni
(codice, valore, typedato, isconfigui, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES('MAIL_OBJECT_SEZIONE_4_RIFIUTA_VALIDAZIONE', 'Rifiutata la richiesta di validazione Sezione 4', 'String', true, true, 'ADMIN', '2026-04-16', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);
INSERT INTO piao_private.configurazioni
(codice, valore, typedato, isconfigui, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES('MAIL_OBJECT_SEZIONE_4_RICHIEDI_VALIDAZIONE', 'Richiesta la validazione  Sezione 4', 'String', true, true, 'ADMIN', '2026-04-16', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);
INSERT INTO piao_private.configurazioni
(codice, valore, typedato, isconfigui, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES('MAIL_OBJECT_SEZIONE_4_REVOCA_VALIDAZIONE', 'Revocata la richiesta di validazione Sezione 4', 'String', true, true, 'ADMIN', '2026-04-16', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);
INSERT INTO piao_private.configurazioni
(codice, valore, typedato, isconfigui, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES('MAIL_OBJECT_SEZIONE_4_ANNULLA_VALIDAZIONE', 'Annullata la richiesta di validazione Sezione 4', 'String', true, true, 'ADMIN', '2026-04-16', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);
INSERT INTO piao_private.configurazioni
(codice, valore, typedato, isconfigui, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES('MAIL_OBJECT_SEZIONE_4_ACCETTA_VALIDAZIONE', 'Accettata la richiesta di validazione Sezione 4', 'String', true, true, 'ADMIN', '2026-04-16', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);
INSERT INTO piao_private.configurazioni
(codice, valore, typedato, isconfigui, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES('BODY_EMAIL_APPROVA_PIAO', '<h1>Richiesta di Approvazione PIAO</h1>
<p>È stata inviata una richiesta di approvazione per il PIAO.</p>
<p><a href="{{URL_APPROVAZIONE}}">Clicca qui per visualizzare e approvare il PIAO</a></p>', 'String', true, true, 'ADMIN', '2026-04-16', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);
INSERT INTO piao_private.configurazioni
(codice, valore, typedato, isconfigui, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES('MAIL_OBJECT_APPROVA_PIAO', 'Richiesta di approvazione Piao', 'String', true, true, 'ADMIN', '2026-04-16', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);
INSERT INTO piao_private.configurazioni
(codice, valore, typedato, isconfigui, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES('URL_APPROVAZIONE_PIAO', 'https://piaocoll.dfp.gov.it/area-riservata/pubblicato', 'String', true, true, 'ADMIN', '2026-04-16', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);
INSERT INTO piao_private.configurazioni
(codice, valore, typedato, isconfigui, x_validity_in, x_createdby, x_created_ts, x_updatedby, x_updated_ts, x_createdbyrole, x_updatedbyrole, x_createdbynamesurname, x_updatedbynamesurname, x_active, x_deactivationtime)
VALUES('MAIL_OBJECT_SOLLECITO', 'Sollecito', 'String', true, true, 'ADMIN', '2026-04-16', NULL, NULL, NULL, NULL, NULL, NULL, true, NULL);
