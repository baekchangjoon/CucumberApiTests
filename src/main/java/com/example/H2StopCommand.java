package com.example;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;

@Command(name = "stop",
        description = "Stop all H2 child processes using the pid file.")
class H2StopCommand implements Callable<Integer> {

    @Option(names = {"--pid-file"}, defaultValue = "h2db-child.pid")
    private String pidFilePath;

    @Override
    public Integer call() throws Exception {
        File pidFile = new File(pidFilePath);
        if (!pidFile.exists()) {
            System.err.println("[Parent] PID file not found: " + pidFilePath);
            return 1;
        }

        // 파일에서 PID들을 읽어 종료
        for (String line : Files.readAllLines(Path.of(pidFilePath))) {
            String pidStr = line.trim();
            if (pidStr.isEmpty()) {
                continue;
            }
            long pid = Long.parseLong(pidStr);
            stopProcess(pid);
        }

        // 종료 후 pid 파일 삭제
        pidFile.delete();
        System.out.println("[Parent] All children stopped. pid file removed.");

        return 0;
    }

    private void stopProcess(long pid) {
        // Java 9+에서 지원
        ProcessHandle.of(pid).ifPresentOrElse(ph -> {
            System.out.println("[Parent] Destroying PID=" + pid);
            // ph.destroy(); // 소프트 종료
            ph.destroyForcibly(); // 강제 종료
        }, () -> {
            System.err.println("[Parent] No process found with PID=" + pid);
        });
    }
}