package net.java.spring_security.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
@EnableJpaRepositories(
        basePackages = "net.java.spring_security.banking.repository",
        entityManagerFactoryRef = "bankingEntityManagerFactory",
        transactionManagerRef = "bankingTransactionManager"
)
public class BankingDataSourceConfig {

    @Bean(name = "bankingDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.banking")
    public DataSource bankingDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "bankingEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean bankingEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("bankingDataSource") DataSource dataSource) {
        return builder
                .dataSource(dataSource)
                .packages("net.java.spring_security.banking.model")
                .persistenceUnit("banking")
                .build();
    }

    @Bean(name = "bankingTransactionManager")
    public PlatformTransactionManager bankingTransactionManager(
            @Qualifier("bankingEntityManagerFactory")
            LocalContainerEntityManagerFactoryBean factory) {
        return new JpaTransactionManager(factory.getObject());
    }
}