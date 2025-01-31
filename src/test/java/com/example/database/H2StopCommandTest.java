package com.example.database;

import com.example.H2StopCommand;
import org.junit.*;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileWriter;
import java.util.Arrays;

import static org.junit.Assert.*;
import static picocli.CommandLine.populateCommand;

/**
 * H2StopCommand에 대한 단위 테스트 예시
 */
public class H2StopCommandTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    private H2StopCommand stopCommand;

    @Before
    public void setUp() {
    }

    @Test
    public void testStopWithoutPidFile() throws Exception {
        // given
        // pid 파일이 존재하지 않는 경우
        stopCommand = populateCommand(new H2StopCommand());

        // when
        int exitCode = stopCommand.call();

        // then
        assertEquals("파일이 없으므로 1 리턴", 1, exitCode);
    }

    @Test
    public void testStopWithPidFile() throws Exception {
        // given
        File pidFile = tempFolder.newFile("h2db_test.pids");
        // pid 파일에 임의의 PID들(예: 1234, 9999) 기록
        try (FileWriter fw = new FileWriter(pidFile)) {
            fw.write("1234\n");
            fw.write("9999\n");
        }
        String[] args = {
                "--pid-file", pidFile.getAbsolutePath()
        };
        stopCommand = populateCommand(new H2StopCommand() {
            @Override
            protected void stopProcess(long pid) {
                System.out.println("[Test-Mock] stopProcess called. pid=" + pid);
            }
        }, args);

        // when
        int exitCode = stopCommand.call();

        // then
        assertEquals(0, exitCode);
        // pid 파일이 삭제되었는지 확인
        assertFalse("pid 파일은 삭제되어야 함", pidFile.exists());
    }
}
