package demo.blog.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

@Configuration
@EnableTransactionManagement
public class DataConfig {
    @Bean(destroyMethod = "close")
    public HikariDataSource dataSource(Environment environment) {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName("org.h2.Driver");
        String directory = environment.getProperty("blog.data.directory", "./data");
        config.setJdbcUrl(environment.getProperty("BLOG_DB_URL",
                "jdbc:h2:file:" + directory + "/blog;DATABASE_TO_LOWER=TRUE"));
        config.setUsername(environment.getProperty("BLOG_DB_USER", ""));
        config.setPassword(environment.getProperty("BLOG_DB_PASSWORD", ""));
        config.setMaximumPoolSize(5);
        config.setPoolName("blog-pool");
        return new HikariDataSource(config);
    }

    @Bean
    public JdbcClient jdbcClient(DataSource dataSource) {
        return JdbcClient.create(dataSource);
    }

    @Bean
    public NamedParameterJdbcTemplate namedParameterJdbcTemplate(DataSource dataSource) {
        return new NamedParameterJdbcTemplate(dataSource);
    }

    @Bean
    public PlatformTransactionManager transactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean
    public DataSourceInitializer databaseInitializer(DataSource dataSource) {
        DataSourceInitializer initializer = new DataSourceInitializer();
        initializer.setDataSource(dataSource);
        initializer.setDatabasePopulator(new ResourceDatabasePopulator(new ClassPathResource("schema.sql")));
        return initializer;
    }
}