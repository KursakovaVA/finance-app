package service;

import model.User;
import repository.FileStorage;
import repository.UserRepository;
import util.PasswordHasher;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AuthService {

    private UserRepository userRepository;
    private FileStorage fileStorage;
    private User currentUser;
    private Set<String> loadedLogins;

    public AuthService(UserRepository userRepository, FileStorage fileStorage) {
        this.userRepository = userRepository;
        this.fileStorage = fileStorage;
        loadedLogins = new HashSet<>();
    }

    public void register(String login, String password) {
        if (!login.matches("[a-zA-Z0-9_]{3,20}")) {
            throw new IllegalArgumentException("Логин: от 3 до 20 символов, только латинские буквы, цифры и _");
        }
        if (password.length() < 4) {
            throw new IllegalArgumentException("Пароль должен быть не короче 4 символов");
        }
        if (userRepository.exists(login)) {
            throw new IllegalArgumentException("Пользователь " + login + " уже существует");
        }
        userRepository.add(new User(login, PasswordHasher.hash(password)));
        fileStorage.saveUsers(userRepository.getAll());
    }

    public void login(String login, String password) {
        if (currentUser != null) {
            throw new IllegalArgumentException("Вы уже вошли как " + currentUser.getLogin() + ". Сначала выполните logout.");
        }
        User user = userRepository.findByLogin(login);
        if (user == null || !PasswordHasher.check(password, user.getPasswordHash())) {
            throw new IllegalArgumentException("Неверный логин или пароль");
        }
        if (!loadedLogins.contains(login)) {
            fileStorage.loadWallet(user);
            loadedLogins.add(login);
        }
        currentUser = user;
    }

    public void logout() {
        if (currentUser == null) {
            throw new IllegalArgumentException("Вы не вошли в аккаунт");
        }
        fileStorage.saveWallet(currentUser);
        currentUser = null;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public User getCurrentUser() {
        if (currentUser == null) {
            throw new IllegalArgumentException("Сначала войдите: login <логин> <пароль>");
        }
        return currentUser;
    }
}