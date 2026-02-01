package com.example.smallworld.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

/**
 * Render 등에서 DATABASE_URL( postgres://... )이 설정되면 PostgreSQL DataSource 사용.
 * 없으면 application.properties 의 H2 사용.
 */
@Configuration
public class DataSourceConfig {

    @Bean
    @Primary
    @ConditionalOnProperty(name = "DATABASE_URL")
    public DataSource dataSource(Environment env) {
        String databaseUrl = env.getProperty("DATABASE_URL");
        if (databaseUrl == null || databaseUrl.isBlank()) {
            return null;
        }
        return createDataSourceFromUrl(databaseUrl);
    }

    private static DataSource createDataSourceFromUrl(String url) {
        try {
            // postgres://user:password@host:port/database
            if (url.startsWith("postgres://")) {
                url = "postgresql://" + url.substring(11);
            }
            URI uri = new URI(url);
            String username = uri.getUserInfo() != null ? uri.getUserInfo().split(":")[0] : "";
            String password = uri.getUserInfo() != null && uri.getUserInfo().contains(":")
                    ? URLDecoder.decode(uri.getUserInfo().substring(uri.getUserInfo().indexOf(':') + 1), StandardCharsets.UTF_8)
                    : "";
            String host = uri.getHost();
            int port = uri.getPort() > 0 ? uri.getPort() : 5432;
            String path = uri.getPath();
            String database = path != null && path.length() > 1 ? path.substring(1) : "";

            String jdbcUrl = "jdbc:postgresql://" + host + ":" + port + "/" + database;

            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(jdbcUrl);
            config.setUsername(username);
            config.setPassword(password);
            config.setDriverClassName("org.postgresql.Driver");
            return new HikariDataSource(config);
        } catch (Exception e) {
            throw new IllegalStateException("DATABASE_URL 파싱 실패: " + e.getMessage(), e);
        }
    }
}
