package com.example;

import com.example.htmlparser.CucumberHtmlParser;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.IOException;

@Command(
        name = "HtmlParser",
        description = "Generate statistics from Cucumber.html",
        mixinStandardHelpOptions = true
)
public class HtmlParser implements Runnable {

    @Option(names = {"-f", "--file"}, description = "Specified the path of Cucumber.html", required = true)
    private String file;

    @Option(names = {"--csv"}, description = "Generate csv formatted report")
    private boolean csv;

    @Option(names = {"--html"}, description = "Generate html formatted report")
    private boolean html;

    @Override
    public void run() {
        CucumberHtmlParser cucumberHtmlParser = new CucumberHtmlParser();
        try {
            cucumberHtmlParser.parseFile(file);
            cucumberHtmlParser.printResults();
            if (csv) cucumberHtmlParser.exportResultsToCsv("CucumberResult.csv");
            if (html) cucumberHtmlParser.exportResultsToHtml("CucumberResult.html");
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }
}
