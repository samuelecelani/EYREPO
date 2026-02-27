package it.ey.piao.bff.service;



import it.ey.dto.GenericResponseDTO;
import it.ey.dto.TestDTO;
import reactor.core.publisher.Mono;

import java.util.List;

public interface ITestService {

    public Mono<GenericResponseDTO<List<TestDTO>>> getAllTest();
    public Mono<Void> Save(TestDTO test);
     public Mono<GenericResponseDTO<TestDTO>> getTestById(Long id);
     public  Mono<Void> deleteTestById(Long id);

}
