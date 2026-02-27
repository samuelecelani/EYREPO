package it.ey.piao.api.service.impl;

import it.ey.dto.AdditionalInfoDTO;
import it.ey.dto.GetTestResponse;

import it.ey.dto.TestDTO;
import it.ey.piao.api.repository.TestRepository;
import it.ey.entity.Test;
import it.ey.piao.api.service.IAdditionalInfoService;
import it.ey.piao.api.service.ITestService;
import it.ey.piao.api.service.event.TransactionFailureEvent;
import it.ey.piao.api.service.event.TransactionSuccessEvent;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Transactional
@Service
public class TestServiceImpl implements ITestService {
private final static Logger logger = LoggerFactory.getLogger(TestServiceImpl.class);

    private final  TestRepository rep;
    private final  IAdditionalInfoService additionalInfoService;
    private final  ApplicationEventPublisher eventPublisher;

    public TestServiceImpl(TestRepository rep, IAdditionalInfoService additionalInfoService, ApplicationEventPublisher eventPublisher) {
        this.rep = rep;
        this.additionalInfoService = additionalInfoService;
        this.eventPublisher = eventPublisher;
    }


    @Override
    public GetTestResponse getAllTest() {

            List<Test> tests = rep.findAll();
            GetTestResponse response = new GetTestResponse( new ArrayList<>());
             for (Test test : tests) {
                 if (test != null) {
                     TestDTO testDTO = new TestDTO(test);
                     //Recupero eventuali informazioni su mongo
                     AdditionalInfoDTO additionalInfos = additionalInfoService.findByExternalId(test.getId());
                     if (additionalInfos != null) {
                         testDTO.setAdditionalInfo(additionalInfos);
                     }
                     response.getTests().add(testDTO);
                 }
             }
        logger.info("Recuperati {} test " , response.getTests().size());

        return response;
    }

    public void Save(TestDTO test) {
        Test response = null;
        try {
            Test entity = new Test(test);
             response = rep.save(entity);
            if (test.getAdditionalInfo() != null && test.getAdditionalInfo().getProperties() != null && !test.getAdditionalInfo().getProperties().isEmpty()) {
                test.getAdditionalInfo().setExternalId(response.getId());
                additionalInfoService.save(test.getAdditionalInfo());
            }
            eventPublisher.publishEvent(new TransactionSuccessEvent<>(new TestDTO(response)));

        } catch (Exception e) {
            eventPublisher.publishEvent(new TransactionFailureEvent<>(new TestDTO(response), e));
            throw new RuntimeException("Errore nella transazione, rollback manuale richiesto", e);
        }
    }

    @Override
    public TestDTO getTestById(Long id) {
        return new TestDTO( rep.getReferenceById(id));
    }

    @Override
    public void deleteTestById(Long id) {
        rep.deleteById(id);
    }
}
