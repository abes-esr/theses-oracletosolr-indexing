package fr.abes.indexationsolr.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.Properties;


@Configuration
@EnableJpaRepositories(
        basePackages = "fr.abes.indexationsolr.portail.repositories",
        entityManagerFactoryRef = "portailEntityManagerFactory",
        transactionManagerRef = "portailTransactionManager"
)
public class PortailConfig
{
    @Autowired
    private Environment env;

    @Bean
    @ConfigurationProperties(prefix="portail.datasource")
    public DataSourceProperties portailDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    public DataSource portailDataSource() {
        DataSourceProperties portailDataSourceProperties = portailDataSourceProperties();
        return DataSourceBuilder.create()
                .driverClassName(portailDataSourceProperties.getDriverClassName())
                .url(portailDataSourceProperties.getUrl())
                .username(portailDataSourceProperties.getUsername())
                .password(portailDataSourceProperties.getPassword())
                .build();
    }

    @Bean
    public PlatformTransactionManager portailTransactionManager()
    {
        EntityManagerFactory factory = portailEntityManagerFactory().getObject();
        return new JpaTransactionManager(factory);
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean portailEntityManagerFactory()
    {
        LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
        factory.setDataSource(portailDataSource());
        factory.setPackagesToScan(new String[]{"fr.abes.indexationsolr.portail.entities"});
        factory.setJpaVendorAdapter(new HibernateJpaVendorAdapter());

        Properties jpaProperties = new Properties();
        //jpaProperties.put("hibernate.hbm2ddl.auto", env.getProperty("spring.jpa.hibernate.ddl-auto"));
        jpaProperties.put("hibernate.show-sql", env.getProperty("spring.jpa.show-sql"));
        factory.setJpaProperties(jpaProperties);

        return factory;
    }

    @Bean
    public DataSourceInitializer portailDataSourceInitializer()
    {
        DataSourceInitializer dataSourceInitializer = new DataSourceInitializer();
        dataSourceInitializer.setDataSource(portailDataSource());
        /*ResourceDatabasePopulator databasePopulator = new ResourceDatabasePopulator();
        databasePopulator.addScript(new ClassPathResource("portail-data.sql"));
        dataSourceInitializer.setDatabasePopulator(databasePopulator);*/
        dataSourceInitializer.setEnabled(env.getProperty("portail.datasource.initialize", Boolean.class, false));
        return dataSourceInitializer;
    }
}