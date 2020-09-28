package fr.abes.indexationsolr.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
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
        basePackages = "fr.abes.indexationsolr.sujets.repositories",
        entityManagerFactoryRef = "sujetsEntityManagerFactory",
        transactionManagerRef = "sujetsTransactionManager"
)
public class SujetsConfig
{
    @Autowired
    private Environment env;

    @Primary
    @Bean(name= "sujetsDb")
    @ConfigurationProperties(prefix="sujets.datasource")
    public DataSourceProperties sujetsDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Primary
    @Bean(name= "sujetsDataSource")
    public DataSource sujetsDataSource() {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName(env.getProperty("sujets.datasource.driver-class-name"));
        config.setJdbcUrl(env.getProperty("sujets.datasource.url"));
        config.setUsername(env.getProperty("sujets.datasource.username"));
        config.setPassword(env.getProperty("sujets.datasource.password"));
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
        config.setPoolName("poolSujetsOnGoingToDelete");
        //Infrequently used
        config.setInitializationFailTimeout(0);
        //config.setIsolateInternalQueries(true);
        config.setAllowPoolSuspension(true);
        config.setLeakDetectionThreshold(40000);
        config.setValidationTimeout(2500);
        config.setConnectionTestQuery("SELECT 1 FROM DUAL");

        config.addDataSourceProperty("implicitCachingEnabled", "true"); //spec oracle
        config.addDataSourceProperty("maxStatements", "250"); //spec oracle

        return new HikariDataSource(config);

    }


    @Primary
    @Bean
    public PlatformTransactionManager sujetsTransactionManager()
    {
        EntityManagerFactory factory = sujetsEntityManagerFactory().getObject();
        return new JpaTransactionManager(factory);
    }

    @Primary
    @Bean
    public LocalContainerEntityManagerFactoryBean sujetsEntityManagerFactory()
    {
        LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
        factory.setDataSource(sujetsDataSource());
        factory.setPackagesToScan(new String[]{"fr.abes.indexationsolr.sujets.entities"});
        factory.setJpaVendorAdapter(new HibernateJpaVendorAdapter());

        Properties jpaProperties = new Properties();
        jpaProperties.put("hibernate.show-sql", env.getProperty("spring.jpa.show-sql"));
        factory.setJpaProperties(jpaProperties);

        return factory;
    }

    @Primary
    @Bean
    public DataSourceInitializer sujetsDataSourceInitializer()
    {
        DataSourceInitializer dataSourceInitializer = new DataSourceInitializer();
        dataSourceInitializer.setDataSource(sujetsDataSource());
        dataSourceInitializer.setEnabled(env.getProperty("sujets.datasource.initialize", Boolean.class, false));
        return dataSourceInitializer;
    }
}