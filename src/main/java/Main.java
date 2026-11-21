import cli.Application;
import cli.CommandHandler;
import repository.FileStorage;
import repository.UserRepository;
import service.AuthService;
import service.NotificationService;
import service.ReportService;
import service.StatisticsService;
import service.WalletService;

public class Main {

    public static void main(String[] args) {
        UserRepository userRepository = new UserRepository();
        FileStorage fileStorage = new FileStorage();

        try {
            fileStorage.loadUsers(userRepository);
        } catch (RuntimeException e) {
            System.out.println("Ошибка при загрузке пользователей: " + e.getMessage());
            return;
        }

        AuthService authService = new AuthService(userRepository, fileStorage);
        WalletService walletService = new WalletService(authService);
        StatisticsService statisticsService = new StatisticsService(authService);
        NotificationService notificationService = new NotificationService(authService, statisticsService);
        ReportService reportService = new ReportService(authService, statisticsService);

        CommandHandler commandHandler = new CommandHandler(
                authService, walletService, notificationService, reportService);
        new Application(commandHandler).run();
    }
}