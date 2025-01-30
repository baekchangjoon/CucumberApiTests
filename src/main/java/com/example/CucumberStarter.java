package com.example;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(
        name = "CucumberStarter",
        description = "Execute cucumber test suites",
        mixinStandardHelpOptions = true
)
public class CucumberStarter implements Runnable {

    @Option(names = {"-t", "--thread"},
            description = "Specified the number of threads",
            defaultValue = "4",
            showDefaultValue = CommandLine.Help.Visibility.ALWAYS)
    private String numberOfThreads;

    @Option(names = {"--target-db-url"},
            description = "Specified target service's database url",
            required = true,
            defaultValue = "${env.TARGET_DB_URL}",
            showDefaultValue = CommandLine.Help.Visibility.ALWAYS)
    private String targetDbUrl;

    @Option(names = {"--target-db-username"},
            description = "Specified target service's database username",
            defaultValue = "sa",
            required = true,
            showDefaultValue = CommandLine.Help.Visibility.ALWAYS)
    private String targetDbUsername;

    @Option(names = {"--target-db-password"},
            description = "Specified target service's database password",
            defaultValue = "1234",
            required = true,
            showDefaultValue = CommandLine.Help.Visibility.ALWAYS)
    private String targetDbPassword;

    @Option(names = {"--test-db-url"},
            description = "Specified Cucumber test database url",
            required = true,
            defaultValue = "${env.TEST_DB_URL}",
            showDefaultValue = CommandLine.Help.Visibility.ALWAYS)
    private String testDbUrl;

    @Option(names = {"--test-db-username"},
            description = "Specified Cucumber test database username",
            defaultValue = "sa",
            required = true,
            showDefaultValue = CommandLine.Help.Visibility.ALWAYS)
    private String testDbUsername;

    @Option(names = {"--test-db-password"},
            description = "Specified Cucumber test service's database password",
            defaultValue = "1234",
            required = true,
            showDefaultValue = CommandLine.Help.Visibility.ALWAYS)
    private String testDbPassword;

    @Override
    public void run() {

    }
}