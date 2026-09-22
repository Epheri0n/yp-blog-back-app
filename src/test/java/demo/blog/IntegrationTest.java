package demo.blog;

import demo.blog.config.AppConfig;
import demo.blog.config.WebConfig;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Transactional;

@SpringJUnitConfig({AppConfig.class, WebConfig.class})
@WebAppConfiguration
@TestPropertySource(properties = {
        "BLOG_DB_URL=jdbc:h2:mem:blog-test;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE",
        "BLOG_DB_USER=", "BLOG_DB_PASSWORD="
})
@Transactional
public abstract class IntegrationTest {
}
