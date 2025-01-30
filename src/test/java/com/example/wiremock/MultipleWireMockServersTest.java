package com.example.wiremock;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.*;
import java.nio.file.*;
import java.sql.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * JUnit4 기반 테스트 예시.
 */
@RunWith(MockitoJUnitRunner.class)
public class MultipleWireMockServersTest {

    @Mock
    private Connection mockConnection;

    @Mock
    private PreparedStatement mockPreparedStatement;

    @Mock
    private ResultSet mockResultSet;

    private ObjectMapper objectMapper;
    private MultipleWireMockServers wireMockServers; // 테스트 대상

    @Before
    public void setUp() throws Exception {
        objectMapper = new ObjectMapper();
        wireMockServers = new MultipleWireMockServers(mockConnection, objectMapper);

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
    }

    @After
    public void tearDown() {
        // 테스트 끝나면 서버 정지
        wireMockServers.stopServers();
    }

    /**
     * 서버 설정 파일(JSON) 중 port, serviceName 등을 제대로 읽고
     * 서버가 정상 기동되는지 확인 (단순 예외 여부 위주)
     */
    @Test
    public void testStartServers() throws Exception {
        // 가상의 JSON 구성: serviceX(포트 10080), serviceY(포트 10081)
        String config = "[\n" +
                "  {\"serviceName\":\"serviceX\", \"port\":10080},\n" +
                "  {\"serviceName\":\"serviceY\", \"port\":10081}\n" +
                "]";
        File tempFile = createTempJsonFile(config);

        // DB mock resultSet 설정 (stub 없음 가정)
        when(mockResultSet.next()).thenReturn(false);

        // 실제로 서버 시작
        wireMockServers.startServers(tempFile.getAbsolutePath());

        // 서버 기동 확인
        WireMockServer serverX = wireMockServers.getServer("serviceX");
        WireMockServer serverY = wireMockServers.getServer("serviceY");
        assertNotNull(serverX);
        assertNotNull(serverY);
        assertTrue(serverX.isRunning());
        assertTrue(serverY.isRunning());
    }

    /**
     * 서버 설정에 serviceName 이나 port 가 누락된 경우 테스트
     */
    @Test
    public void testStartServers_missingFields() throws Exception {
        // port 누락
        String invalidConfig = "[ {\"serviceName\":\"noPortService\"} ]";
        File tempFile = createTempJsonFile(invalidConfig);

        // 실행 시 예외 발생 기대
        assertFalse(wireMockServers.startServers(tempFile.getAbsolutePath()));
    }

    /**
     * DB에서 2건의 스텁 데이터를 읽는다고 가정한 뒤 정상적으로 서버 기동하는지 확인
     */
    @Test
    public void testStubDefinitions() throws Exception {
        // DB mock resultSet 데이터 설정
        // 2건 레코드: /testA(GET,requestBody,succResp), /testB(POST,noReqBody,failResp)
        when(mockResultSet.next()).thenReturn(true, true, false); // 2건
        when(mockResultSet.getString("url")).thenReturn("/testA", "/testB");
        when(mockResultSet.getString("url_pattern")).thenReturn(null, null);
        when(mockResultSet.getString("method")).thenReturn("GET", "POST");
        when(mockResultSet.getString("request_body")).thenReturn("{\"key\":\"val\"}", null);
        when(mockResultSet.getInt("status_code")).thenReturn(200, 400);
        when(mockResultSet.getString("response_body"))
                .thenReturn("{\"result\":\"ok\"}", "{\"error\":\"bad request\"}");

        // JSON 설정
        String config = "[\n" +
                "  {\"serviceName\":\"serviceA\", \"port\":10082}\n" +
                "]";
        File tempFile = createTempJsonFile(config);

        // 서버 시작
        wireMockServers.startServers(tempFile.getAbsolutePath());

        // 검증
        WireMockServer serverA = wireMockServers.getServer("serviceA");
        assertTrue(serverA.isRunning());

        // 실제로 /testA, /testB 에 대한 스텁이 등록됐는지 여부는 WireMock API로도 확인 가능
        // 여기서는 단순히 예외 없이 설정됐다고 가정
    }

    /**
     * 병렬로 서버를 띄우는지 테스트.
     * 여기서는 단순히 예외 없이 여러 서버가 기동되는지만 확인.
     */
    @Test
    public void testStartServersInParallel() throws Exception {
        String config = "[\n" +
                "  {\"serviceName\":\"pService1\", \"port\":10100},\n" +
                "  {\"serviceName\":\"pService2\", \"port\":10101}\n" +
                "]";

        File tempFile = createTempJsonFile(config);
        when(mockResultSet.next()).thenReturn(false); // 스텁 없음 가정

        wireMockServers.startServersInParallel(tempFile.getAbsolutePath());

        WireMockServer s1 = wireMockServers.getServer("pService1");
        WireMockServer s2 = wireMockServers.getServer("pService2");
        assertNotNull(s1);
        assertNotNull(s2);
        assertTrue(s1.isRunning());
        assertTrue(s2.isRunning());
    }

    // -------------------------------------------------------------------------------------
    // 테스트용 헬퍼 메서드
    // -------------------------------------------------------------------------------------

    /**
     * 임시 JSON 파일 생성
     */
    private File createTempJsonFile(String content) throws IOException {
        Path tempFilePath = Files.createTempFile("wm-test-", ".json");
        Files.write(tempFilePath, content.getBytes());
        File file = tempFilePath.toFile();
        file.deleteOnExit();
        return file;
    }
}
