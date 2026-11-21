import com.google.gson.Gson;
import java.io.File;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

class FinanceCliIntegrationTest {
    @TempDir
    Path directory;

    @Test
    void exitSavesWalletAndNextLoginRestoresIt() throws Exception {
        runCli("register anna pass", "login anna pass", "category expense Еда",
                "budget Еда 100", "expense 45 Еда обед", "rename expense Еда Продукты", "exit");

        String output = runCli("login anna pass", "stats", "history", "balance", "exit");
        assertTrue(output.contains("Продукты: лимит 100.00, потрачено 45.00, осталось 55.00"), output);
        assertTrue(output.contains("Продукты | 45.00 (обед)"), output);
        assertTrue(output.contains("Баланс: -45.00"), output);
    }

    @Test
    void wrongPasswordLeavesWalletInaccessible() throws Exception {
        String output = runCli("register anna pass", "login anna wrong", "balance", "exit");
        assertTrue(output.contains("Неверный логин или пароль"), output);
        assertTrue(output.contains("Сначала войдите"), output);
    }

    @Test
    void secondUserHasIndependentBalance() throws Exception {
        runCli("register anna pass", "login anna pass", "category income Работа",
                "income 500 Работа", "logout", "register ivan pass", "exit");
        String output = runCli("login ivan pass", "balance", "categories", "exit");
        assertTrue(output.contains("Баланс: 0.00"), output);
        assertFalse(output.contains("Работа"), output);
    }

    @Test
    void budgetWarningsDistinguishThresholdAndOverspending() throws Exception {
        String output = runCli("register anna pass", "login anna pass", "category expense Еда",
                "budget Еда 100", "expense 80 Еда", "expense 21 Еда", "exit");
        assertTrue(output.contains("осталось 20.00 из 100.00"), output);
        assertTrue(output.contains("перерасход 1.00"), output);
        assertTrue(output.contains("превышают доходы"), output);
    }

    @Test
    void statsIncludesOnlySelectedCategoriesAndReportsMissingOnes() throws Exception {
        String output = runCli("register anna pass", "login anna pass",
                "category expense Еда", "category expense Кино", "category expense Дом",
                "expense 20 Еда", "expense 30 Кино", "expense 70 Дом",
                "stats Еда Кино Спорт", "exit");
        assertTrue(output.contains("Итого расходов: 50.00"), output);
        assertTrue(output.contains("Категория не найдена: Спорт"), output);
    }

    // Отдельный процесс нужен, потому что FileStorage ищет data в рабочей папке.
    private String runCli(String... commands) throws Exception {
        String classpath = classLocation(Main.class) + File.pathSeparator + classLocation(Gson.class);
        File log = Files.createTempFile(directory, "cli-", ".log").toFile();
        String java = ProcessHandle.current().info().command().orElseThrow();
        Process process = new ProcessBuilder(java, "-Dfile.encoding=UTF-8", "-cp", classpath, "Main")
                .directory(directory.toFile())
                .redirectErrorStream(true)
                .redirectOutput(log)
                .start();
        try {
            try (var input = new OutputStreamWriter(process.getOutputStream(), StandardCharsets.UTF_8)) {
                input.write(String.join("\n", commands) + "\n");
            }
            assertTrue(process.waitFor(15, TimeUnit.SECONDS), "CLI не завершился за 15 секунд");
            String output = Files.readString(log.toPath());
            assertEquals(0, process.exitValue(), output);
            return output;
        } finally {
            process.destroyForcibly();
        }
    }

    private String classLocation(Class<?> type) throws Exception {
        return Path.of(type.getProtectionDomain().getCodeSource().getLocation().toURI()).toString();
    }
}
