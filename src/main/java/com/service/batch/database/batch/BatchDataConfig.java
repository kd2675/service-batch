package com.service.batch.database.batch;

import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManagerFactory;
import org.example.database.common.datasource.RoutingDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        basePackages = "com.example.batch.database.batch.repository",
        entityManagerFactoryRef = "batchEntityManagerFactory",
        transactionManagerRef = "batchTransactionManager"
)
public class BatchDataConfig {
//    private DataSource createHikariDataSource(String propertiesPrefix) {
//        DataSourceProperties dataSourceProperties = new DataSourceProperties();
//        dataSourceProperties.setName(propertiesPrefix);
//        return dataSourceProperties
//                .initializeDataSourceBuilder()
//                .type(HikariDataSource.class)
//                .build();
//    }
//
//    @Primary
//    @Bean
//    @ConfigurationProperties("database.datasource.batch.master.configure")
//    public DataSource batchMasterDatasource() {
//        return createHikariDataSource("database.datasource.batch.master");
//    }
//
//    @Bean
//    @ConfigurationProperties("database.datasource.batch.slave1.configure")
//    public DataSource batchSlave1Datasource() {
//        return createHikariDataSource("database.datasource.batch.slave1");
//    }

    @Bean
    @ConfigurationProperties("database.datasource.batch.master")
    public DataSourceProperties batchMasterDatasourceProperties() {
        return new DataSourceProperties();
    }

    @Primary
    @Bean
    @ConfigurationProperties("database.datasource.batch.master.configure")
    public DataSource batchMasterDatasource() {
        return batchMasterDatasourceProperties()
                .initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
    }

    @Bean
    @ConfigurationProperties("database.datasource.batch.slave1")
    public DataSourceProperties batchSlave1DatasourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @ConfigurationProperties("database.datasource.batch.slave1.configure")
    public DataSource batchSlave1Datasource() {
        return batchSlave1DatasourceProperties()
                .initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
    }

    @Primary
    @Bean(name = "batchEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean batchEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("batchMasterDatasource") DataSource masterDataSource,
            @Qualifier("batchSlave1Datasource") DataSource slave1DataSource
    ) {
        RoutingDataSource routingDataSource = new RoutingDataSource();
        Map<Object, Object> datasourceMap = new HashMap<Object, Object>() {
            {
                put("master", masterDataSource);
                put("slave", slave1DataSource);
            }
        };

        routingDataSource.setTargetDataSources(datasourceMap);
        routingDataSource.setDefaultTargetDataSource(masterDataSource);
        routingDataSource.afterPropertiesSet();

        HashMap<String, Object> properties = new HashMap<>();
        properties.put("hibernate.hbm2ddl.auto", "validate");
//        properties.put("hibernate.hbm2ddl.auto", "create");
        properties.put("hibernate.default_batch_fetch_size", 1000);
        properties.put("hibernate.show_sql", false);
        properties.put("hibernate.format_sql", true);
        properties.put("hibernate.use_sql_comments", true);

        return builder.dataSource(new LazyConnectionDataSourceProxy(routingDataSource))
                .packages("com.example.batch.database.batch.entity")
                .properties(properties)
                .persistenceUnit("batchEntityManagerFactory")
                .build();
    }

    @Primary
    @Bean(name = "batchTransactionManager")
    public PlatformTransactionManager batchTransactionManager(
            final @Qualifier("batchEntityManagerFactory") EntityManagerFactory localContainerEntityManagerFactoryBean
    ) {
        return new JpaTransactionManager(localContainerEntityManagerFactoryBean);
    }
}