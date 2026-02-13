package it.ey.piao.api.service;


import it.ey.dto.GetTestResponse;
import it.ey.dto.TestDTO;

public interface ITestService {

    GetTestResponse getAllTest();
     void Save(TestDTO test);
     public  TestDTO getTestById(Long id);
     public void deleteTestById(Long id);

}
