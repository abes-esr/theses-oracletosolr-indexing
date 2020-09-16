package fr.abes.indexationsolr.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import fr.abes.indexationsolr.controller.IndexationSolrController;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
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

    private Logger logger = LogManager.getLogger(PortailConfig.class);

    @Autowired
    private Environment env;

    @Bean
    @ConfigurationProperties(prefix="portail.datasource")
    public DataSourceProperties portailDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    public DataSource portailDataSource() {
        //Essentials
        HikariConfig config = new HikariConfig();
        logger.info("env =" + env.getProperty("portail.datasource.driver-class-name"));
        config.setDriverClassName(env.getProperty("portail.datasource.driver-class-name"));
        //config.setDataSourceClassName(env.getProperty("portail.datasource.class-name"));
        config.setJdbcUrl(env.getProperty("portail.datasource.url"));
        config.setUsername(env.getProperty("portail.datasource.username"));
        config.setPassword(env.getProperty("portail.datasource.password"));
        //Frequently used
        config.setAutoCommit(false);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(35000);
        config.setMaxLifetime(45000);
        config.setMinimumIdle(1);
        config.setMaximumPoolSize(2);
        config.getMetricsTrackerFactory();
        config.getMetricRegistry();
        config.getHealthCheckProperties();
        config.setPoolName("poolPortaiIndexationSolr");
        //Infrequently used
        config.setInitializationFailTimeout(0);
        //config.setIsolateInternalQueries(true);
        config.setAllowPoolSuspension(true);
        config.setLeakDetectionThreshold(40000);
        config.setValidationTimeout(2500);
        config.setConnectionTestQuery("SELECT 1 FROM DUAL");

        //ces propriétés ne sont pas définies dans oracle.jdbc.pool.OracleDataSource
        //par contre sont ok qd on utilise driver class name
        //config.addDataSourceProperty("validationInterval", env.getProperty("portail.datasource.validationInterval"));
        //config.addDataSourceProperty("testOnBorrow", env.getProperty("portail.datasource.testOnBorrow"));
        //config.addDataSourceProperty("testWhileIdle", env.getProperty("portail.datasource.testWhileIdle"));
        //config.addDataSourceProperty("testOnReturn", env.getProperty("portail.datasource.testOnReturn"));
        //config.addDataSourceProperty("timeBetweenEvictionRunsMillis", env.getProperty("portail.datasource.timeBetweenEvictionRunsMillis"));
        //config.addDataSourceProperty("validationQuery", env.getProperty("portail.datasource.validationQuery"));
        config.addDataSourceProperty("implicitCachingEnabled", "true"); //spec oracle
        config.addDataSourceProperty("maxStatements", "250"); //spec oracle
        return new HikariDataSource(config);

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
        jpaProperties.put("hibernate.show-sql", env.getProperty("spring.jpa.show-sql"));
        factory.setJpaProperties(jpaProperties);

        return factory;
    }

    @Bean
    public DataSourceInitializer portailDataSourceInitializer()
    {
        DataSourceInitializer dataSourceInitializer = new DataSourceInitializer();
        dataSourceInitializer.setDataSource(portailDataSource());
        dataSourceInitializer.setEnabled(env.getProperty("portail.datasource.initialize", Boolean.class, false));
        return dataSourceInitializer;
    }
}