package service;

public class ConsoleReportWriter implements ReportWriter {

    @Override
    public void write(String report) {
        System.out.println(report);
    }
}