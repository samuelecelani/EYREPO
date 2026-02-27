package it.ey.piao.api.log;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import it.ey.entity.AppLog;
import it.ey.piao.api.service.impl.LogService;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.concurrent.*;

public class JpaLogAppender extends AppenderBase<ILoggingEvent> {

    @Setter
    private static LogService logService;

    private final ExecutorService executor = new ThreadPoolExecutor(
        1, 1, //Numero massimo di THREAD che crea in parallelo
        0L, TimeUnit.MILLISECONDS, //non tiene thread extra in vita
        new LinkedBlockingQueue<>(1000), // coda con capacità massima di 1000 task
        new ThreadPoolExecutor.DiscardPolicy() // scarta i task se la coda è piena
    );

    @Override
    protected void append(ILoggingEvent event) {
        if (logService == null || event == null) return;

        if (shouldLog(event)) {
            executor.submit(() -> { //Salva i log in un thread separato da quello principale per gestire al meglio il carico
                try {
                    AppLog log = new AppLog();
                    log.setLevel(event.getLevel().toString());
                    log.setLogger(event.getLoggerName());
                    log.setMessage(event.getFormattedMessage());
                    log.setThread(event.getThreadName());
                    log.setTimestamp(LocalDateTime.now());

                    logService.saveLog(log);
                } catch (Exception e) {
                    System.err.println("Errore nel salvataggio log: " + e.getMessage());
                }
            });
        }
    }
//Salva solo log applicativi e qualsiasi wargin o errore
    private boolean shouldLog(ILoggingEvent event) {
        Level level = event.getLevel();
        return level.isGreaterOrEqual(Level.ERROR)
            || level.isGreaterOrEqual(Level.WARN)
            || (level == Level.INFO && isApplicationLog(event));
    }

    private boolean isApplicationLog(ILoggingEvent event) {
        return event.getLoggerName() != null && event.getLoggerName().startsWith("it.ey.piao");
    }

    @Override
    public void stop() {
        super.stop();
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        System.out.println("JpaLogAppender terminato.");
    }
}
