package com.example.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


public class HikariCPDataSourceProvider {

    private final HikariDataSource dataSource;

    public HikariCPDataSourceProvider(String propertiesFile, int maxPoolSize) throws IOException {
        try(InputStream inputStream = HikariCPDataSourceProvider.class
                .getClassLoader()
                .getResourceAsStream(propertiesFile)) {
            if (inputStream == null) {
                throw new FileNotFoundException(propertiesFile);
            }
            Properties props = new Properties();
            props.load(inputStream);

            String url = props.getProperty("db.url");
            String user = props.getProperty("db.username");
            String pass = props.getProperty("db.password");

            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(url);
            config.setUsername(user);
            config.setPassword(pass);
            config.setMaximumPoolSize(maxPoolSize);

            dataSource = new HikariDataSource(config);
        } catch (IOException e) {
            throw new IOException(e.getMessage());
        }
    }

    public HikariCPDataSourceProvider(String jdbcUrl, String username, String password, int maxPoolSize) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(username);
        config.setPassword(password);
        config.setMaximumPoolSize(maxPoolSize);

        dataSource = new HikariDataSource(config);
    }

    public HikariDataSource getDataSource() {
        return dataSource;
    }

    public void close() {
        if (dataSource != null) {
            dataSource.close();
        }
    }
}

