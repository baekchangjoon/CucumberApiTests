package com.example.wiremock;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.matching.UrlPattern;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.sql.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

/**
 * 여러 개의 WireMock 서버를 구성하고 관리하는 클래스.
 * 싱글턴을 사용하지 않으며, Connection 로부터 Connection 을 받도록 수정됨.
 */
public class MultipleWireMockServers {

    // 외부에서 전달받을 의존성
    private final Connection connection;
    private final ObjectMapper objectMapper;

    // 서버 관리용 맵: serviceName -> WireMockServer
    private final Map<String, WireMockServer> servers = new ConcurrentHashMap<>();

    /**
     * @param connection          Database Connection
     * @param objectMapper        JSON 파싱용 ObjectMapper
     */
    public MultipleWireMockServers(Connection connection, ObjectMapper objectMapper) {
        this.connection = connection;
        this.objectMapper = objectMapper;
    }

    /**
     * 서버 설정 파일을 읽어들여 순차적으로 서버 시작
     * @param configFilePath JSON 구성 파일 경로
     */
    public boolean startServers(String configFilePath) {
        stopServers(); // 이미 서버가 떠 있을 경우 초기화
        try {
            List<ServerConfig> serverConfigs = readConfigFile(configFilePath);
            // DB에서 스텁 데이터 추출 (모든 서버가 같은 스텁을 쓸 경우)
            List<StubDefinition> cachedStubDefinitions = fetchStubDefinitions();

            // 순차적으로 서버 시작
            for (ServerConfig config : serverConfigs) {
                startSingleServer(config, cachedStubDefinitions);
            }
            System.out.println("[startServers] 모든 WireMock 서버가 순차적으로 시작되었습니다.");

        } catch (Exception e) {
            stopServers();
            System.err.println("[startServers] 서버 시작 중 에러가 발생했습니다: " + e.getMessage());
            return false;
        }
        return true;
    }

    /**
     * 서버 설정 파일을 읽어들여 병렬로 서버 시작
     * @param configFilePath JSON 구성 파일 경로
     */
    public void startServersInParallel(String configFilePath) {
        stopServers(); // 이미 서버가 떠 있을 경우 초기화
        try {
            List<ServerConfig> serverConfigs = readConfigFile(configFilePath);
            List<StubDefinition> cachedStubDefinitions = fetchStubDefinitions();

            ExecutorService executor = Executors.newFixedThreadPool(serverConfigs.size());
            List<Callable<Void>> tasks = serverConfigs.stream()
                    .map(config -> (Callable<Void>) () -> {
                        startSingleServer(config, cachedStubDefinitions);
                        return null;
                    })
                    .collect(Collectors.toList());

            List<Future<Void>> futures = executor.invokeAll(tasks, 60, TimeUnit.SECONDS);
            executor.shutdown();

            // 태스크 결과 확인
            for (Future<Void> future : futures) {
                try {
                    future.get(); // 실행 결과에 예외가 있었는지 확인
                } catch (ExecutionException | CancellationException ex) {
                    System.err.println("[startServersInParallel] 일부 서버 시작 실패: " + ex.getMessage());
                }
            }
            if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                System.err.println("[startServersInParallel] 일부 스레드가 정상 종료되지 않았습니다.");
            }
            System.out.println("[startServersInParallel] 모든 WireMock 서버가 병렬로 시작되었습니다.");

        } catch (Exception e) {
            stopServers();
            System.err.println("[startServersInParallel] 서버 시작 중 에러가 발생했습니다: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 모든 WireMock 서버 정지
     */
    public void stopServers() {
        for (Map.Entry<String, WireMockServer> entry : servers.entrySet()) {
            try {
                entry.getValue().stop();
                System.out.println("[stopServers] 서버 중지: " + entry.getKey());
            } catch (Exception e) {
                System.err.println("[stopServers] 서버 중지 에러 (" + entry.getKey() + "): " + e.getMessage());
            }
        }
        servers.clear();
        System.out.println("[stopServers] 모든 WireMock 서버가 정지되었습니다.");
    }

    /**
     * 특정 서비스 이름으로 실행 중인 WireMock 서버를 가져옴
     * @param serviceName 서비스 이름
     * @return WireMockServer 인스턴스(없으면 null)
     */
    public WireMockServer getServer(String serviceName) {
        return servers.get(serviceName);
    }

    // -------------------------------------------------------------------------------------
    // 내부 메서드 (private or package-private)
    // -------------------------------------------------------------------------------------

    private List<ServerConfig> readConfigFile(String configFilePath) throws IOException {
        File file = new File(configFilePath);
        if (!file.exists()) {
            throw new IOException("지정된 설정 파일이 존재하지 않습니다: " + configFilePath);
        }

        // 예: [ { "serviceName": "A", "port": 8080 }, ... ]
        List<Map<String, Object>> raw =
                objectMapper.readValue(file, new TypeReference<List<Map<String, Object>>>() {});
        List<ServerConfig> serverConfigs = new ArrayList<>();

        for (Map<String, Object> map : raw) {
            if (!map.containsKey("serviceName") || !map.containsKey("port")) {
                throw new IllegalArgumentException("JSON 설정에 serviceName 또는 port가 누락되었습니다: " + map);
            }
            String serviceName = map.get("serviceName").toString();
            int port = Integer.parseInt(map.get("port").toString());
            serverConfigs.add(new ServerConfig(serviceName, port));
        }
        return serverConfigs;
    }

    private List<StubDefinition> fetchStubDefinitions() throws SQLException {
        List<StubDefinition> result = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(
                     "SELECT url, url_pattern, method, request_body, status_code, response_body " +
                             "FROM tb_wiremock"
             );
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String url         = rs.getString("url");
                String urlPattern  = rs.getString("url_pattern");
                String method      = rs.getString("method");
                String requestBody = rs.getString("request_body");
                int    statusCode  = rs.getInt("status_code");
                String responseBody= rs.getString("response_body");

                result.add(new StubDefinition(url, urlPattern, method, requestBody, statusCode, responseBody));
            }
        }
        return result;
    }

    private void startSingleServer(ServerConfig config, List<StubDefinition> stubDefinitions) {
        String serviceName = config.serviceName;
        int port = config.port;

        if (servers.containsKey(serviceName)) {
            System.out.println("[startSingleServer] 이미 실행 중인 서버: " + serviceName);
            return;
        }

        // 포트 사용 가능 여부 체크
        if (!isPortAvailable(port)) {
            throw new IllegalStateException("포트(" + port + ")가 사용 중입니다. [" + serviceName + "]");
        }

        WireMockServer wireMockServer = new WireMockServer(
                WireMockConfiguration.options()
                        .port(port)
                // 필요 시, 커스텀 로거 지정 가능
                // .notifier(new WireMockFileLogger(serviceName))
        );

        try {
            // 스텁 등록
            applyStubs(wireMockServer, stubDefinitions);

            // 서버 시작
            wireMockServer.start();
            servers.put(serviceName, wireMockServer);
            System.out.println("[startSingleServer] 서버 시작: " + serviceName + " (포트: " + port + ")");

        } catch (Exception e) {
            wireMockServer.stop();
            throw new RuntimeException("[startSingleServer] 서버 시작 실패: " + serviceName, e);
        }
    }

    private boolean isPortAvailable(int port) {
        try (ServerSocket socket = new ServerSocket(port)) {
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private void applyStubs(WireMockServer server, List<StubDefinition> stubs) {
        for (StubDefinition stub : stubs) {
            stubRequest(
                    stub.method, stub.url, stub.urlPattern,
                    stub.statusCode, stub.responseBody, stub.requestBody,
                    server
            );
        }
    }

    private void stubRequest(String method,
                             String url,
                             String urlPattern,
                             int status,
                             String responseBody,
                             String requestBody,
                             WireMockServer server) {
        if (url == null && urlPattern == null) {
            throw new IllegalArgumentException("url과 urlPattern이 동시에 null 일 수 없습니다.");
        }

        UrlPattern pattern = (url != null)
                ? urlEqualTo(url)
                : urlMatching(urlPattern);

        MappingBuilder builder = buildMapping(method, pattern);

        if (requestBody != null && !requestBody.isEmpty()) {
            builder.withRequestBody(equalToJson(requestBody));
        }

        builder.willReturn(
                aResponse()
                        .withStatus(status)
                        .withBody(responseBody)
                        .withHeader("Content-Type", "application/json")
        );

        server.stubFor(builder);
    }

    private MappingBuilder buildMapping(String method, UrlPattern pattern) {
        switch (method.toUpperCase()) {
            case "GET":    return get(pattern);
            case "POST":   return post(pattern);
            case "PUT":    return put(pattern);
            case "DELETE": return delete(pattern);
            default:       throw new UnsupportedOperationException("지원되지 않는 메서드: " + method);
        }
    }

    // -------------------------------------------------------------------------------------
    // DTO / Helper Classes
    // -------------------------------------------------------------------------------------
    static class ServerConfig {
        final String serviceName;
        final int port;

        ServerConfig(String serviceName, int port) {
            this.serviceName = serviceName;
            this.port = port;
        }
    }

    static class StubDefinition {
        final String url;
        final String urlPattern;
        final String method;
        final String requestBody;
        final int    statusCode;
        final String responseBody;

        StubDefinition(String url,
                       String urlPattern,
                       String method,
                       String requestBody,
                       int statusCode,
                       String responseBody) {
            this.url = url;
            this.urlPattern = urlPattern;
            this.method = method;
            this.requestBody = requestBody;
            this.statusCode = statusCode;
            this.responseBody = responseBody;
        }
    }
}
