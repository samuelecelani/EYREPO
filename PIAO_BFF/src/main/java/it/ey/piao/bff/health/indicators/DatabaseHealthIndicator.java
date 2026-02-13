/*package it.example.piao.bff.health.indicators;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class DatabaseHealthIndicator implements HealthIndicator {
  //OracleConnectorService oracleConnectorService;
   public DatabaseHealthIndicator() {
      // this.oracleConnectorService;
   }
   @Override
   public Health health() {
       Health.Builder status = Health.up();
//        Connection conn = oracleConnectorService.connect();
//        if (conn==null) {
//            status = Health.down();
//        }
//        else {
//            try {
//                conn.close();
//            } catch (SQLException e) {
//                throw new RuntimeException(e);
//            }
//
//        }

       return status.build();
   }
}*/
