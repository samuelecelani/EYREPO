/*package it.example.piao.api.configuration;

import javax.sql.DataSource;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import jakarta.persistence.EntityManagerFactory;


@Configuration
public class AutenticazioneDBConfig {

    @Bean(name = "autenticazioneDataSource")
    @ConfigurationProperties(prefix = "app.second-datasource")
    public DataSource autenticazioneDataSource() {
        return DataSourceBuilder.create()
            .type(com.zaxxer.hikari.HikariDataSource.class)
            .build();
    }

    @Bean(name = "autenticazioneJdbcTemplate")
    public JdbcTemplate autenticazioneJdbcTemplate(@Qualifier("autenticazioneDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

}

*/
