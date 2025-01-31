package com.example;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.Callable;

@Command(name = "start",
        description = "Start multiple H2 servers (child processes) for given ports and exit.")
public class H2StartCommand implements Callable<Integer> {

    @Option(names = {"-p", "--ports"},
            description = "Comma-separated port numbers (ex: 33306,33307).",
            defaultValue = "33306,33307,33308,33309")
    private String ports;

    @Option(names = {"--dbusername"},
            defaultValue = "sa",
            showDefaultValue = CommandLine.Help.Visibility.ALWAYS)
    private String dbUsername;

    @Option(names = {"--dbpassword"},
            defaultValue = "1234",
            showDefaultValue = CommandLine.Help.Visibility.ALWAYS)
    private String dbPassword;

    @Option(names = {"--init-sql"},
            description = "Initialize SQL path",
            defaultValue = "",
            showDefaultValue = CommandLine.Help.Visibility.ALWAYS)
    private String initSql;

    @Option(names = {"--pid-file"},
            description = "Where to save child PIDs",
            defaultValue = "h2db-child.pid",
            showDefaultValue = CommandLine.Help.Visibility.ALWAYS)
    private String pidFilePath;

    @Override
    public Integer call() throws Exception {
        System.out.println("[Parent] Starting child processes for ports: " + ports);

        // 1) 기존 pid 파일이 있다면 삭제(또는 append 모드 사용 가능)
        File pidFile = new File(pidFilePath);
        if (pidFile.exists()) {
            pidFile.delete();
        }

        // 2) 포트 문자열 파싱
        String[] portArray = ports.split(",");
        for (String portStr : portArray) {
            String port = portStr.trim();
            // 자식 프로세스 하나 생성
            long pid = startOneChild(port, dbUsername, dbPassword, initSql);
            // pid 파일에 기록
            appendPidToFile(pidFile, pid);
        }

        System.out.println("[Parent] All children started. PIDs saved to " + pidFilePath);
        System.out.println("[Parent] Exiting parent. H2 servers are running in child processes.");
        return 0;
    }

    protected long startOneChild(String port, String dbUser, String dbPass, String initSql) throws IOException {
        // 자바 실행 경로
        String javaHome = System.getProperty("java.home");
        String javaBin = javaHome + File.separator + "bin" + File.separator + "java";
        // 현재 클래스패스
        String classpath = System.getProperty("java.class.path");

        // 자식 프로세스 명령
        ProcessBuilder pb = new ProcessBuilder(
                javaBin,
                "-cp", classpath,
                // 자식 메인 클래스
                "com.example.database.MemoryDBChild",
                // 만약 프로그램 인자를 주고 싶으면 여기에 추가
                // 이번에는 System Property로 전달할 것이므로 인자 없음
                // "--someArg", "someVal"
                port,
                dbUser,
                dbPass,
                initSql
        );

        // 자식 JVM에 System Property 전달
        pb.command().add("-Dh2.port=" + port);
        pb.command().add("-Dh2.username=" + dbUser);
        pb.command().add("-Dh2.password=" + dbPass);
        pb.command().add("-Dh2.initSql=" + initSql);

        // 표준입출력 연결(생략 가능)
        // pb.inheritIO();

        // 프로세스 실행
        Process child = pb.start();
        long pid = child.pid();
        System.out.println("[Parent] Child for port=" + port + " started. PID=" + pid);

        return pid;
    }

    private void appendPidToFile(File pidFile, long pid) throws IOException {
        try (FileWriter fw = new FileWriter(pidFile, true)) {
            fw.write(String.valueOf(pid));
            fw.write("\n");
        }
    }

    public String getPidFilePath() {
        return pidFilePath;
    }
}
