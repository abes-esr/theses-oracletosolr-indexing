package fr.abes.indexationsolr.config;
import java.util.Properties;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
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

    @Bean
    @ConfigurationProperties(prefix="sujets.datasource")
    public DataSourceProperties sujetsDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    public DataSource sujetsDataSource() {
        DataSourceProperties sujetsDataSourceProperties = sujetsDataSourceProperties();
        return DataSourceBuilder.create()
                .driverClassName(sujetsDataSourceProperties.getDriverClassName())
                .url(sujetsDataSourceProperties.getUrl())
                .username(sujetsDataSourceProperties.getUsername())
                .password(sujetsDataSourceProperties.getPassword())
                .build();
    }

    @Bean
    public PlatformTransactionManager sujetsTransactionManager()
    {
        EntityManagerFactory factory = sujetsEntityManagerFactory().getObject();
        return new JpaTransactionManager(factory);
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean sujetsEntityManagerFactory()
    {
        LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
        factory.setDataSource(sujetsDataSource());
        factory.setPackagesToScan(new String[]{"fr.abes.indexationsolr.sujets.entities"});
        factory.setJpaVendorAdapter(new HibernateJpaVendorAdapter());

        Properties jpaProperties = new Properties();
        //jpaProperties.put("hibernate.hbm2ddl.auto", env.getProperty("spring.jpa.hibernate.ddl-auto"));
        jpaProperties.put("hibernate.show-sql", env.getProperty("spring.jpa.show-sql"));
        factory.setJpaProperties(jpaProperties);

        return factory;
    }

    @Bean
    public DataSourceInitializer sujetsDataSourceInitializer()
    {
        DataSourceInitializer dataSourceInitializer = new DataSourceInitializer();
        dataSourceInitializer.setDataSource(sujetsDataSource());
        /*ResourceDatabasePopulator databasePopulator = new ResourceDatabasePopulator();
        databasePopulator.addScript(new ClassPathResource("sujets-data.sql"));
        dataSourceInitializer.setDatabasePopulator(databasePopulator);*/
        dataSourceInitializer.setEnabled(env.getProperty("sujets.datasource.initialize", Boolean.class, false));
        return dataSourceInitializer;
    }
}