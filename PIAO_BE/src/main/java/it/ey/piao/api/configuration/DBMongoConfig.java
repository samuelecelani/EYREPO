package it.ey.piao.api.configuration;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import it.ey.repository.mongo.BaseMongoRepositoryImpl;
import jakarta.annotation.PostConstruct;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableConfigurationProperties(MongoProperties.class)
@EnableMongoRepositories(
    basePackages ={
        "com.ey.repository",
        "it.ey.piao.api.repository.mongo"},

repositoryBaseClass = BaseMongoRepositoryImpl.class,
    mongoTemplateRef = "mongoTemplate"
)
public class DBMongoConfig {

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
        System.out.println("Mongo URI: " + mongoProperties.getUri());
        System.out.println("Mongo DB: " + mongoProperties.getDatabase());
    }
}
