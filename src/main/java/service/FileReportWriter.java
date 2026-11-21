package service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileReportWriter implements ReportWriter {

    private Path file;

    public FileReportWriter(String fileName) {
        this.file = Paths.get(fileName);
    }

    @Override
    public void write(String report) {
        try {
            Path folder = file.getParent();
            if (folder != null) {
                Files.createDirectories(folder);
            }
            Files.writeString(file, report, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Не удалось записать отчёт в файл " + file + ": " + e.getMessage());
        }
    }

    public Path getFile() {
        return file;
    }
}