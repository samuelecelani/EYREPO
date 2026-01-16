package it.ey.piao.api.service.impl;

import it.ey.entity.AppLog;
import it.ey.piao.api.repository.AppLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LogService {
private final AppLogRepository appLogRepository;
@Autowired
public LogService(AppLogRepository appLogRepository) {
        this.appLogRepository = appLogRepository;
}
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveLog(AppLog log) {
        appLogRepository.save(log);
    }

}
