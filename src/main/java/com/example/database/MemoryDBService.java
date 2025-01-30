package com.example.database;

import org.h2.tools.Server;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class MemoryDBService {

    // 포트별로 H2 Server 객체를 보관하기 위한 리스트
    private final List<Server> h2Servers = new ArrayList<>();

    /**
     * ports, dbUsername, dbPassword, initSql 정보를 받아 여러 H2 서버(TCP 모드)를 실행한다.
     *
     * @param ports      쉼표(,)로 구분된 포트번호 문자열 (e.g. "33306,33307")
     * @param dbUsername H2 접속 username
     * @param dbPassword H2 접속 password
     * @param initSql    초기화 스크립트 경로 (없으면 null 이나 빈 문자열)
     */
    public void startServers(String ports, String dbUsername, String dbPassword, String initSql) {
        // 만약 ports가 주어지지 않았다면 기본값 설정
        if (ports == null || ports.isEmpty()) {
            ports = "33306,33307,33308,33309";
        }

        String[] portArray = ports.split(",");

        for (String portStr : portArray) {
            try {
                String port = portStr.trim();

                // 1) H2 Server 객체 생성 및 시작
                Server server = Server.createTcpServer(
                        "-tcpPort", port,
                        "-tcpAllowOthers",
                        "-ifNotExists"
                ).start();

                // 2) 서버 실행 성공 확인
                if (server.isRunning(true)) {
                    System.out.println("H2 서버 시작 - 포트: " + port);
                } else {
                    System.err.println("H2 서버 시작 실패 - 포트: " + port);
                }
                h2Servers.add(server);

                // 3) init-sql 경로가 존재한다면 초기화 스크립트 실행
                if (initSql != null && !initSql.isEmpty()) {
                    runInitScript(port, dbUsername, dbPassword, initSql);
                }

            } catch (Exception e) {
                System.err.println("포트 " + portStr + "에서 H2 서버 시작 중 오류가 발생했습니다.");
                e.printStackTrace();
            }
        }
    }

    /**
     * initSql 경로의 파일을 읽어 JDBC를 통해 SQL을 실행하는 메서드
     *
     * @param port       H2 서버가 실행 중인 포트
     * @param dbUsername DB 접속 계정
     * @param dbPassword DB 접속 비밀번호
     * @param sqlPath    초기화 스크립트 파일 경로
     */
    private void runInitScript(String port, String dbUsername, String dbPassword, String sqlPath) {
        // jdbc:h2:tcp://localhost:<port>/mem:testdb 와 같이 구성
        // 여기서는 DB 이름을 mem:testdb로 가정
        String url = "jdbc:h2:tcp://localhost:" + port + "/mem:testdb";

        try (Connection conn = DriverManager.getConnection(url, dbUsername, dbPassword);
             BufferedReader br = new BufferedReader(new FileReader(sqlPath, StandardCharsets.UTF_8));
             Statement stmt = conn.createStatement()) {

            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }

            // 한 번에 전체 스크립트를 실행 (필요하다면 세미콜론 단위로 나눠서 각각 실행)
            stmt.execute(sb.toString());

            System.out.println("포트 " + port + " 데이터베이스에 초기화 스크립트 실행 완료.");

        } catch (Exception e) {
            System.err.println("초기화 스크립트 실행 중 오류 발생 - 포트: " + port);
            e.printStackTrace();
        }
    }

    /**
     * 현재 실행 중인 모든 서버를 정지시킨다.
     */
    public void stopServers() {
        for (Server server : h2Servers) {
            if (server.isRunning(false)) {
                server.stop();
                System.out.println("H2 서버 중지 - 포트: " + server.getPort());
            }
        }
        h2Servers.clear();
    }

    /**
     * 현재 실행 중인 서버 목록을 반환 (테스트나 상태 확인용)
     */
    public List<Server> getH2Servers() {
        return h2Servers;
    }
}
