package com.example.database;

import com.example.MemoryDBStarter;
import org.junit.*;
import org.junit.rules.TemporaryFolder;
import picocli.CommandLine;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

import static org.junit.Assert.*;

/**
 * 실제로 Picocli로 start/stop 서브커맨드를 호출하여
 * PID 파일 생성/삭제를 검증하는 통합 테스트 예시
 *
 * 주의: 실제 자식 프로세스가 생성될 수 있으므로,
 *       CI 환경에서 포트 충돌 등에 유의하십시오.
 */
public class H2CommandsIntegrationTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    private File pidFile;

    @Before
    public void setUp() {
        pidFile = new File(tempFolder.getRoot(), "h2db_integration_test.pids");
    }

    @Test
    public void testStartAndStop() throws Exception {
        // 1. start 서브커맨드 실행
        String[] startArgs = {
                "start",
                "--ports=0,0",              // 임의 포트 2개
                "--pid-file=" + pidFile.getAbsolutePath(),
                "--dbusername=sa",
                "--dbpassword=1234"
        };
        int startExit = new CommandLine(new MemoryDBStarter()).execute(startArgs);
        assertEquals("start 서브커맨드는 0으로 종료되어야 함", 0, startExit);
        assertTrue("start 후에 pidFile이 생성되어야 함", pidFile.exists());

        // pidFile 내부 라인 확인
        List<String> lines = Files.readAllLines(pidFile.toPath());
        // 포트 2개 -> 자식 프로세스 2개 -> pidFile 2줄
        assertEquals(2, lines.size());
        System.out.println("PID file lines: " + lines);

        // 2. stop 서브커맨드 실행
        String[] stopArgs = {
                "stop",
                "--pid-file=" + pidFile.getAbsolutePath()
        };
        int stopExit = new CommandLine(new MemoryDBStarter()).execute(stopArgs);
        assertEquals("stop 서브커맨드는 0으로 종료되어야 함", 0, stopExit);

        // pidFile 삭제되었는지
        assertFalse("stop 후 pidFile이 삭제되어야 함", pidFile.exists());
    }
}
