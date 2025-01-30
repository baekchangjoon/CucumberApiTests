package com.example;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(
        name = "CucumberApiTests",
        mixinStandardHelpOptions = true, // --help, --version 자동 생성
        version = "CucumberApiTests 0.1",
        subcommands = {
                HtmlParser.class,
                WiremockStarter.class,
                MemoryDBStarter.class,
                CucumberStarter.class
        }
)
public class CucumberApiTests implements Runnable {

    @Option(names = {"-v", "--verbose"}, description = "Verbose")
    boolean verbose = false;

    @Override
    public void run() {
        System.out.println("No subcommand specified. Use --help for usage.");
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new CucumberApiTests()).execute(args);
        System.exit(exitCode);
    }
}

