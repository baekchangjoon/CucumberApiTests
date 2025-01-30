package com.example.database;

import org.h2.tools.Server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class MemoryDBChild {

    public static void main(String[] args) throws Exception {
        String port = args[0];
        String dbUsername = args[1];
        String dbPassword = args[2];
        String initSql = args[3];

        Long pid = ProcessHandle.current().pid();
        System.out.println("[Child][" + pid + "] Starting H2 on port=" + port
                + ", user=" + dbUsername + ", password=" + dbPassword
                + ", init=" + initSql);

        // 1) H2 서버 시작
        Server server = Server.createTcpServer(
                "-tcpPort", port,
                "-tcpAllowOthers",
                "-ifNotExists"
        ).start();

        if (server.isRunning(true)) {
            System.out.println("[Child] H2 server started. port=" + port);
        } else {
            System.err.println("[Child] Failed to start H2 server. port=" + port);
            System.exit(1);
        }

        // 2) init-sql 있으면 실행
        if (!initSql.isEmpty()) {
            runInitScript(port, dbUsername, dbPassword, initSql);
        }

        // 3) 무한 대기 -> kill(혹은 stop 명령) 시그널이 오면 종료됨
        System.out.println("[Child] H2 process is now running. Press Ctrl+C or kill to stop.");
        Thread.currentThread().join();

        // 4) 종료 직전 H2 서버 중지
        server.stop();
        System.out.println("[Child] H2 server stopped. Goodbye!");
    }

    /**
     * 초기화 스크립트 실행 메서드 (간단 버전)
     */
    private static void runInitScript(String port, String dbUsername, String dbPassword, String sqlPath) {
        String url = "jdbc:h2:tcp://localhost:" + port + "/mem:testdb";
        try (Connection conn = DriverManager.getConnection(url, dbUsername, dbPassword);
             BufferedReader br = new BufferedReader(new FileReader(new File(sqlPath), StandardCharsets.UTF_8));
             Statement stmt = conn.createStatement()) {

            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
            stmt.execute(sb.toString());
            System.out.println("[Child] init-sql executed for port=" + port);

        } catch (Exception e) {
            System.err.println("[Child] Failed to run init-sql on port=" + port);
            e.printStackTrace();
        }
    }
}
