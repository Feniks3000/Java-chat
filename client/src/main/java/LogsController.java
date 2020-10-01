import org.apache.commons.io.FileUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class LogsController {
    private static PrintWriter printWriter;
    private static String logFile;

    public static void start(String filePath) {
        try {
            logFile = filePath;
            printWriter = new PrintWriter(FileUtils.openOutputStream(new File(logFile), true), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void stop() {
        if (printWriter != null) {
            printWriter.close();
        }
    }

    public static String getLastRows(int count) {
        StringBuilder stringBuilder = new StringBuilder();
        if (Files.exists(Paths.get(logFile))) {
            try {
                List<String> log = Files.readAllLines(Paths.get(logFile));
                int startPosition = (log.size() <= count) ? 0 : log.size() - count;;
                for (int i = startPosition; i < log.size(); i++) {
                    stringBuilder.append(log.get(i)).append(System.lineSeparator());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return stringBuilder.toString();
    }

    public static void write(String line) {
        printWriter.println(line);
    }
}
