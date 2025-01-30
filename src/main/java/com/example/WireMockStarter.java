package com.example;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(
        name = "WireMockStarter",
        description = "Execute WireMock servers,\nWireMock should be executed before CucumberStarter",
        mixinStandardHelpOptions = true,
        subcommands = {
                WireMockStartCommand.class,
                WireMockStopCommand.class
        }
)
public class WireMockStarter implements Runnable {

    @Option(names = {"-v", "--verbose"}, description = "Verbose")
    boolean verbose = false;

    @Override
    public void run() {
        System.out.println("No subcommand specified. Use --help for usage.");
    }
}