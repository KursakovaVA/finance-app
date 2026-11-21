package cli;

import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class Application {

    private final CommandHandler commandHandler;

    public Application(CommandHandler commandHandler) {
        this.commandHandler = commandHandler;
    }

    public void run() {
        System.out.println("Управление личными финансами. Введите help, чтобы увидеть команды.");
        Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8);

        boolean running = true;
        while (running) {
            System.out.print(commandHandler.getPrompt());
            if (!scanner.hasNextLine()) {
                execute("exit");
                break;
            }
            running = execute(scanner.nextLine());
        }
    }

    private boolean execute(String line) {
        try {
            return commandHandler.handle(line);
        } catch (RuntimeException e) {
            System.out.println("Ошибка: " + e.getMessage());
            return true;
        }
    }
}