package com.example.database;

import org.h2.tools.Server;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

public class MemoryDBServiceTest {

    private MemoryDBService memoryDBService;

    @Before
    public void setUp() {
        memoryDBService = new MemoryDBService();
    }

    @After
    public void tearDown() {
        // 각 테스트가 끝난 후 실행 중인 서버들을 정지
        memoryDBService.stopServers();
    }

    /**
     * 서버가 정상적으로 기동되는지 테스트
     * 포트 0을 사용하면 임의의 에페멀(ephemeral) 포트로 띄울 수 있으나,
     * 테스트에서 포트 충돌이 우려된다면 필요한 포트나 0(자동 할당)을 사용하세요.
     */
    @Test
    public void testStartServers_withSinglePort() {
        // given
        String ports = "0"; // 0이면 OS가 임의 포트 할당
        String dbUsername = "sa";
        String dbPassword = "1234";
        String initSql = null; // 스크립트 없음

        // when
        memoryDBService.startServers(ports, dbUsername, dbPassword, initSql);

        // then
        List<Server> servers = memoryDBService.getH2Servers();
        Assert.assertEquals("서버가 1개 기동되어야 함", 1, servers.size());
        Assert.assertTrue("서버가 실행 중이어야 함", servers.get(0).isRunning(false));
    }

    /**
     * 쉼표 구분 포트를 여러 개 넣어 서버가 여러 개 기동되는지 테스트
     */
    @Test
    public void testStartServers_withMultiplePorts() {
        // given
        String ports = "0,0"; // 2개의 임의 포트
        String dbUsername = "sa";
        String dbPassword = "1234";
        String initSql = null;

        // when
        memoryDBService.startServers(ports, dbUsername, dbPassword, initSql);

        // then
        List<Server> servers = memoryDBService.getH2Servers();
        Assert.assertEquals("서버가 2개 기동되어야 함", 2, servers.size());

        for (Server server : servers) {
            Assert.assertTrue("각 서버가 실행 중이어야 함", server.isRunning(false));
        }
    }

    /**
     * init-sql 파일이 있는 경우, DB가 정상적으로 초기화되는지 테스트
     * 실제로는 테스트 리소스로부터 SQL을 읽어 확인하거나, 쿼리를 날려 확인하는 절차가 필요할 수 있음
     * 여기서는 스크립트 경로를 가짜로 넣고 예외 발생 여부만 확인 (혹은 실제 스크립트를 준비)
     */
    @Test
    public void testStartServers_withInitSql() {
        // given
        String ports = "0";
        String dbUsername = "sa";
        String dbPassword = "1234";
        // 실제 테스트 시에는 프로젝트 내 리소스 폴더에 sql 파일을 두고 경로 지정
        String initSql = "src/test/resources/init_test.sql";

        // when
        memoryDBService.startServers(ports, dbUsername, dbPassword, initSql);

        // then
        // 만약 실제 init_test.sql이 없으면 예외가 발생할 수 있으므로,
        // 해당 파일을 준비하거나 try-catch로 처리 로직 검증을 해야 함
        List<Server> servers = memoryDBService.getH2Servers();
        Assert.assertEquals("서버가 1개 기동되어야 함", 1, servers.size());
    }
}

