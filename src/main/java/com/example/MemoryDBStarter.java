package com.example;

import picocli.CommandLine.Command;

@Command(
        name = "MemoryDBStarter",
        description = "Execute MemoryDB servers using H2,\nMemoryDB should be executed before CucumberStarter",
        mixinStandardHelpOptions = true,
        subcommands = {
                H2StartCommand.class,
                H2StopCommand.class
        }
)
public class MemoryDBStarter implements Runnable {

    @Override
    public void run() {
        System.out.println("No subcommand specified. Use --help for usage.");
    }
}