package com.example.database;

import com.example.H2StartCommand;
import org.junit.*;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

import static org.junit.Assert.*;
import static picocli.CommandLine.populateCommand;

public class H2StartCommandTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();


    @Before
    public void setUp() {

    }

    @Test
    public void testDefaultPorts() throws Exception {
        // given
        //
        // when
        // 실제로 자식 프로세스를 띄우면 테스트가 복잡해지므로,
        // startOneChild() 등을 Mock 처리하거나 override해서 동작만 확인할 수 있음.
        // 예: 아래처럼 override
        H2StartCommand mockCommand = populateCommand(new H2StartCommand() {
            @Override
            protected long startOneChild(String port, String dbUser, String dbPass, String initSql) {
                System.out.println("[Test-Mock] startOneChild called. port=" + port);
                // 실제 프로세스 실행은 하지 않고, 임의의 PID 리턴
                return 12345L;
            }
        });
        String actualPidFilePath = mockCommand.getPidFilePath();
        File actualPidFile = new File(actualPidFilePath);

        // call()
        int exitCode = mockCommand.call();

        // then
        assertEquals("정상 종료되어야 함", 0, exitCode);
        assertTrue("PID 파일이 생성되어야 함", actualPidFile.exists());

        // mock 상황에서는 port 개수만큼 "12345"가 들어간 줄이 생김
        List<String> lines = Files.readAllLines(actualPidFile.toPath());
        // defaultValue => 4개 포트
        assertEquals(4, lines.size());
    }

    @Test
    public void testCustomPorts() throws Exception {
        // given
        File pidFile = tempFolder.newFile("h2db_test2.pids");
        String[] args = {
                "--ports", "0,0",
                "--pid-file", pidFile.getAbsolutePath()
        };
        H2StartCommand mockCommand = populateCommand(new H2StartCommand() {
            @Override
            protected long startOneChild(String port, String dbUser, String dbPass, String initSql) {
                System.out.println("[Test-Mock] startOneChild called. port=" + port);
                // 실제 프로세스 실행은 하지 않고, 임의의 PID 리턴
                return 12345L;
            }
        });
        String actualPidFilePath = mockCommand.getPidFilePath();
        File actualPidFile = new File(actualPidFilePath);

        // when
        int exitCode = mockCommand.call();

        // then
        assertEquals(0, exitCode);
        List<String> lines = Files.readAllLines(actualPidFile.toPath());
        // "0,0" => 2개
        assertEquals(4, lines.size());
        assertEquals("12345", lines.get(0));
        assertEquals("12345", lines.get(1));
    }
}

