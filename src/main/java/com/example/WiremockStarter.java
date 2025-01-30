package com.example;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(
        name = "WiremockStarter",
        description = "Execute Wiremock servers,\nWiremock should be executed before CucumberStarter",
        mixinStandardHelpOptions = true
)
public class WiremockStarter implements Runnable {

    @Option(names = {"-c", "--config"},
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
    public void run() {

    }
}