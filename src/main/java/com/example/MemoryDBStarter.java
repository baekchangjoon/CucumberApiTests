package com.example;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(
        name = "MemoryDBStarter",
        description = "Execute MemoryDB servers using H2,\nMemoryDB should be executed before CucumberStarter",
        mixinStandardHelpOptions = true
)
public class MemoryDBStarter implements Runnable {

    @Option(names = {"-p", "--ports"},
            description = "Specified the port numbers of databases, Default ports: 33306, 33307, 33308, 33309")
    private String ports;

    @Option(names = {"--dbusername"},
            description = "Specified database username",
            defaultValue = "sa",
            showDefaultValue = CommandLine.Help.Visibility.ALWAYS)
    private String dbUsername;

    @Option(names = {"--dbpassword"},
            description = "Specified database password",
            defaultValue = "1234",
            showDefaultValue = CommandLine.Help.Visibility.ALWAYS)
    private String dbPassword;

    @Option(names = {"--init-sql"},
            description = "Specified initialize sql file path")
    private String initSql;

    @Override
    public void run() {

    }
}