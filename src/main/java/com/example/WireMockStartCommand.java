package com.example;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.Callable;

@Command(name = "start",
        description = "Start multiple WireMock servers (child processes) for given ports and exit.")
public class WireMockStartCommand implements Callable<Integer> {
    @Option(names = {"-f", "--config"},
            description = "Specified the path of config file",
            required = true)
    private String file;

    @Option(names = {"--dburl"},
            description = "Specified database url",
            required = true)
    private String dbUrl;

    @Option(names = {"--dbusername"},
            description = "Specified database username",
            defaultValue = "sa",
            required = true)
    private String dbUsername;

    @Option(names = {"--dbpassword"},
            description = "Specified database password",
            defaultValue = "1234",
            required = true)
    private String dbPassword;

    @Override
    public Integer call() throws Exception {
        return startChildProcess();
    }

    private int startChildProcess() throws IOException {
        // OS 환경에 따라 다를 수 있으나, 여기서는 자기 자신 JAR을 또 실행한다고 가정
        // 예: java -cp myapp.jar com.example.WireMockChildMain
        // 실제로는 classpath 등 상황에 따라 달라질 수 있음
        // JAR 전체 경로가 필요하다면 별도 로직으로 추론하거나, 매개변수로 받을 수도 있음
        String javaHome = System.getProperty("java.home");
        String javaBin = javaHome + File.separator + "bin" + File.separator + "java";
        // 현재 클래스패스
        String classpath = System.getProperty("java.class.path");

        // (1) ProcessBuilder 설정
        ProcessBuilder pb = new ProcessBuilder(
                javaBin,
                "-cp", classpath,
                "com.example.wiremock.WireMockChildMain"      // 자식 프로세스에서 실행할 Main
        );
        pb.inheritIO(); // 자식 프로세스의 콘솔 입출력을 부모와 공유 (필요 시 redirect 가능)

        // (2) 자식 프로세스 실행
        Process process = pb.start();

        // (3) 자식 프로세스 PID를 파일에 기록 (Java 9+ 에서는 process.pid() 사용 가능)
        long pid = getPidOfProcess(process);
        if (pid != -1) {
            Files.write(Paths.get("wiremock-child.pid"), String.valueOf(pid).getBytes());
            System.out.println("[Controller] 자식 프로세스 PID: " + pid + " (child.pid에 저장)");
        } else {
            System.out.println("[Controller] PID 추출 불가. (Java 9 미만이거나 OS 미지원)");
            return -1;
        }

        // (4) 부모 프로세스 종료
        System.out.println("[Controller] 부모 프로세스는 종료됩니다.");
        // 부모가 끝나면, 자식이 백그라운드에서 계속 동작
        return 0;
    }

    /**
     * 현재 실행 중인 JAR 경로를 추론하는 간단 메서드 예시
     * - 실제 운영환경에서 원하는 방식으로 구현
     */
    private static String getCurrentJarPath() {
        // 간단히 해당 클래스가 속한 ProtectionDomain에서 가져오기
        String path = WireMockStartCommand.class
                .getProtectionDomain()
                .getCodeSource()
                .getLocation()
                .getPath();
        // URL 인코딩을 감안해 decode 처리 필요할 수도 있음
        return path;
    }

    /**
     * Java 9+ 환경에서는 process.pid() 사용 가능.
     * Java 8 이하나 OS별 호환성 이슈가 있다면 -1 리턴
     */
    private static long getPidOfProcess(Process process) {
        try {
            // Java 9 이상
            return process.pid();
        } catch (NoSuchMethodError | UnsupportedOperationException e) {
            // Java 8 이하
            return -1;
        }
    }
}
