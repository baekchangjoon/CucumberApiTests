package com.example;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Callable;

@Command(name = "stop",
        description = "Stop all WireMock child processes using the pid file.")
class WireMockStopCommand implements Callable<Integer> {

    @Option(names = {"--pid-file"}, defaultValue = "wiremock-child.pid")
    private String pidFilePath;

    @Override
    public Integer call() throws Exception {
        return stopChildProcess(pidFilePath);
    }

    /**
     * child.pid 파일을 읽어 자식 프로세스 종료.
     * - 여기서는 HTTP http://localhost:9999/shutdown 호출을 예시로 사용
     */
    private int stopChildProcess(String pidFilePath) throws IOException {
        // (1) pid 파일 확인
        File pidFile = new File(pidFilePath);
        if (!pidFile.exists()) {
            System.err.println("[Controller] child.pid 파일이 없습니다. 이미 종료된 것 같거나, 실행한 적이 없을 수 있음.");
            return -1;
        }

        // (2) HTTP 요청으로 자식 서버 종료
        System.out.println("[Controller] 자식 프로세스에 종료 요청을 보냅니다.");
        // WireMockChildMain이 9999 포트에서 /shutdown 대기 중이라고 가정
        sendHttpShutdown("http://localhost:9999/shutdown");

        // (3) pid 파일 삭제 (필요 시 남겨둘 수도 있음)
        pidFile.delete();
        System.out.println("[Controller] 자식 프로세스 종료 완료 (가정).");
        return 0;
    }

    /**
     * HTTP 요청으로 /shutdown을 호출 (간단 예시)
     */
    private void sendHttpShutdown(String endpoint) throws IOException {
        URL url = new URL(endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        int code = conn.getResponseCode();
        System.out.println("[Controller] /shutdown response code: " + code);
        conn.disconnect();
    }
}
