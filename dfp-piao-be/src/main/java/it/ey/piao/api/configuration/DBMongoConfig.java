package it.ey.piao.api.configuration;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import it.ey.repository.mongo.BaseMongoRepositoryImpl;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableConfigurationProperties(MongoProperties.class)
@EnableMongoRepositories(
    basePackages = {
        "com.ey.repository",
        "it.ey.piao.api.repository.mongo"},
    repositoryBaseClass = BaseMongoRepositoryImpl.class,
    mongoTemplateRef = "mongoTemplate"
)
public class DBMongoConfig {

    private static final Logger log = LoggerFactory.getLogger(DBMongoConfig.class);

    private final MongoProperties mongoProperties;

    public DBMongoConfig(MongoProperties mongoProperties) {
        this.mongoProperties = mongoProperties;
    }

    @Bean
    public MongoClient mongoClient() {
        return MongoClients.create(mongoProperties.getUri());
    }

    @Bean(name = "mongoTemplate")
    public MongoTemplate mongoTemplate(MongoClient mongoClient) {
        return new MongoTemplate(mongoClient, mongoProperties.getDatabase());
    }

    @PostConstruct
    public void logMongoConfig() {
        boolean directUri = org.springframework.util.StringUtils.hasText(mongoProperties.getDirectUri());
        if (directUri) {
            log.info("MongoDB config → using direct URI (MONGODB_URL_AUTH), db={}",
                mongoProperties.getDatabase());
        } else {
            log.info("MongoDB config → built from fields: scheme={}, host={}, port={}, db={}, user={}",
                mongoProperties.getScheme(),
                mongoProperties.getHost(),
                mongoProperties.getPort(),
                mongoProperties.getDatabase(),
                mongoProperties.getUsername());
        }
        // La password e la URI completa NON vengono loggate per sicurezza
    }
}


