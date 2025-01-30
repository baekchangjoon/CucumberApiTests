package com.example.wiremock;

import com.example.database.HikariCPDataSourceProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.sql.SQLException;
import java.util.concurrent.Executors;

/**
 * 자식 프로세스에서 WireMock 서버를 기동하고, /shutdown 엔드포인트로 종료를 받는 예시
 */
public class WireMockChildMain {

    public static void main(String[] args) throws IOException, SQLException {
        // 1) WireMock 서버 시작
        //    실제로는 JSON 파일 경로, DataSourceProvider 등 환경설정이 필요할 것.
        HikariCPDataSourceProvider sourceProvider = new HikariCPDataSourceProvider("db.properties", 1);
        MultipleWireMockServers servers = new MultipleWireMockServers(
                sourceProvider.getDataSource().getConnection(), // 예시
                new ObjectMapper()
        );
        // 여기서는 단순히 config JSON이 정해져 있다고 가정 (예: wiremock-config.json)
        String configPath = "src/main/resources/SampleWireMockConfig.json";
        servers.startServersInParallel(configPath);

        // 2) 종료 API용 미니 HTTP 서버 오픈 (/shutdown)
        HttpServer httpServer = HttpServer.create(new InetSocketAddress(9999), 0);
        httpServer.createContext("/shutdown", exchange -> {
            String response = "Shutting down WireMock...";
            exchange.sendResponseHeaders(200, response.getBytes().length);
            exchange.getResponseBody().write(response.getBytes());
            exchange.close();

            // WireMock 서버 우아하게 종료
            servers.stopServers();

            // 자식 프로세스 종료
            System.out.println("[ChildMain] 이제 프로세스를 종료합니다.");
            System.exit(0);
        });

        // 스레드 풀 설정
        httpServer.setExecutor(Executors.newCachedThreadPool());
        httpServer.start();

        System.out.println("[ChildMain] WireMock 서버가 기동되었습니다. /shutdown을 호출하면 종료됩니다.");
        // 여기서부터 자식 프로세스는 main 스레드가 끝나지 않고 계속 대기
    }
}

