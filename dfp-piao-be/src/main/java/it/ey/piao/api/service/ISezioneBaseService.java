package it.ey.piao.api.service;

import it.ey.dto.PiaoDTO;
import it.ey.dto.SezioneBaseDTO;
import org.springframework.web.bind.annotation.RequestParam;


// Questa è l'interfaccia di base per tutte le Sezioni.
// Quindi sono presenti i metodi ricorrenti per ogni sezione

/* Per fare in modo di cnetralizzare la logica ma lasciare alle classi che implementano questa interfaccia la possibilità
    di gestire metodi con tipo di ritorno e parametri specifici
    Estendendo SezioneBaseDTO possiamo quindi utilizzare il DTO specifico per ogni sezione proprio perchè ogni SezioneDTO saranno di tipo SezioneBaseDTO
*/
public interface ISezioneBaseService<T extends SezioneBaseDTO> {

    T getOrCreateSezione(PiaoDTO piao);
    T findByIdPiao(Long idPiao);
    void saveOrUpdate(  T request );
    void saveMongoData( T request );
    T loadMongoData ( T sezione );

    T richiediValidazione(Long id, String userNameSurname, String userRole,String fiscalCode , String testoSezione, String campiModificati);
    T validaSezione(Long id, String userNameSurname, String userRole,String fiscalCode, String testoSezione, String campiModificati);
    T rifiutaValidazione(Long id, String osservazioni, String userNameSurname, String userRole,String fiscalCode,String testoSezione, String campiModificati);
    T revocaValidazione(Long id, String osservazioni, String userNameSurname, String userRole,String fiscalCode, String testoSezione, String campiModificati);
    T annullaValidazione(Long id, String userNameSurname, String userRole,String fiscalCode, String testoSezione, String campiModificati);





}
