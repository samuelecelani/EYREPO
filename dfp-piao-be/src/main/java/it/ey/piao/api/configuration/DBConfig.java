package it.ey.piao.api.configuration;

import com.zaxxer.hikari.HikariDataSource;
import it.ey.repository.BaseRepositoryImpl;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
@EnableConfigurationProperties(PostgresProperties.class)
@EnableJpaRepositories(
    basePackages = "it.ey.piao.api.repository",
    repositoryBaseClass = BaseRepositoryImpl.class,
    entityManagerFactoryRef = "primaryEntityManagerFactory",
    transactionManagerRef = "primaryTransactionManager"
)
@EntityScan(basePackages = "it.ey.entity")
public class DBConfig {

    private static final Logger log = LoggerFactory.getLogger(DBConfig.class);

    private final PostgresProperties postgresProperties;
    private final Environment environment;

    public DBConfig(PostgresProperties postgresProperties, Environment environment) {
        this.postgresProperties = postgresProperties;
        this.environment = environment;
    }

    /**
     * Costruisce il DataSource Hikari esplicitamente a partire dai singoli campi
     * (host, port, db, username, password). Nessuna connection string composta.
     * Le property del pool Hikari (spring.datasource.hikari.*) vengono applicate
     * tramite binding automatico dall'Environment.
     * <p>
     * In Kubernetes: host/port/db → ConfigMap | username/password → Secret.
     */
    @Bean
    @Primary
    public DataSource primaryDataSource() {
        // Applica le property spring.datasource.hikari.* (pool size, timeout, ecc.)
        HikariDataSource ds = Binder.get(environment)
            .bindOrCreate("spring.datasource.hikari", HikariDataSource.class);

        // Sovrascrive esplicitamente i parametri di connessione con le variabili atomiche
        ds.setJdbcUrl(postgresProperties.buildJdbcUrl());
        ds.setUsername(postgresProperties.getUsername());
        ds.setPassword(postgresProperties.getPassword());
        ds.setDriverClassName("org.postgresql.Driver");

        // Imposta lo schema se configurato
        if (org.springframework.util.StringUtils.hasText(postgresProperties.getSchema())) {
            ds.setSchema(postgresProperties.getSchema());
            ds.setConnectionInitSql("SET search_path TO \"" + postgresProperties.getSchema() + "\"");
        }

        return ds;
    }

    @Bean
    @Primary
    public LocalContainerEntityManagerFactoryBean primaryEntityManagerFactory(DataSource primaryDataSource) {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(primaryDataSource);
        em.setPackagesToScan("it.ey.entity");
        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        vendorAdapter.setDatabase(org.springframework.orm.jpa.vendor.Database.POSTGRESQL);
        em.setJpaVendorAdapter(vendorAdapter);

        if (org.springframework.util.StringUtils.hasText(postgresProperties.getSchema())) {
            java.util.Map<String, Object> props = new java.util.HashMap<>();
            props.put("hibernate.default_schema", postgresProperties.getSchema());
            em.setJpaPropertyMap(props);
        }

        return em;
    }

    @Bean
    @Primary
    public PlatformTransactionManager primaryTransactionManager(
            LocalContainerEntityManagerFactoryBean primaryEntityManagerFactory) {
        return new JpaTransactionManager(primaryEntityManagerFactory.getObject());
    }

    @PostConstruct
    public void logPostgresConfig() {
        boolean directUrl = org.springframework.util.StringUtils.hasText(postgresProperties.getUrl())
                && postgresProperties.getUrl().startsWith("jdbc:");
        if (directUrl) {
            log.info("PostgreSQL config -> using direct URL (POSTGRES_URL), user={}",
                postgresProperties.getUsername());
        } else {
            log.info("PostgreSQL config -> built from fields: host={}, port={}, db={}, user={}, schema={}",
                postgresProperties.getHost(),
                postgresProperties.getPort(),
                postgresProperties.getDb(),
                postgresProperties.getUsername(),
                postgresProperties.getSchema());
        }

    }
}
