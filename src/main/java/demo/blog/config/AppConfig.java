package demo.blog.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(DataConfig.class)
@ComponentScan({"demo.blog.service", "demo.blog.dao"})
public class AppConfig {}