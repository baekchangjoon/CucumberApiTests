package com.example.database;

import org.junit.*;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.Assert.*;

/**
 * HikariCPDataSourceProvider 테스트
 */
public class HikariCPDataSourceProviderTest {

    private static HikariCPDataSourceProvider dataSourceProvider;

    @BeforeClass
    public static void setUpClass() {
        // 테스트용으로 H2 인메모리 DB 설정
        String jdbcUrl = "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1";
        String username = "sa";
        String password = "";
        int maxPoolSize = 5;

        // DataSourceProvider 인스턴스 생성
        dataSourceProvider = new HikariCPDataSourceProvider(jdbcUrl, username, password, maxPoolSize);

        // 간단히 테이블 생성
        try (Connection conn = dataSourceProvider.getDataSource().getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS USERS (ID INT AUTO_INCREMENT, NAME VARCHAR(50))");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize test schema.", e);
        }
    }

    @AfterClass
    public static void tearDownClass() {
        // 테스트가 끝나면 풀 리소스 해제
        if (dataSourceProvider != null) {
            dataSourceProvider.close();
        }
    }

    @Test
    public void testGetConnection() throws SQLException {
        // 커넥션 풀에서 Connection을 정상적으로 가져올 수 있는지 확인
        try (Connection conn = dataSourceProvider.getDataSource().getConnection()) {
            assertNotNull(conn);
            assertFalse(conn.isClosed());
        }
    }

    @Test
    public void testInsertAndSelect() throws SQLException {
        // 1) Insert
        try (Connection conn = dataSourceProvider.getDataSource().getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("INSERT INTO USERS (NAME) VALUES ('Alice')");
        }

        // 2) Select
        try (Connection conn = dataSourceProvider.getDataSource().getConnection();
             Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM USERS");
            assertTrue(rs.next());
            int count = rs.getInt(1);

            // Insert가 잘 되었는지 확인
            assertEquals(1, count);
        }
    }
}
