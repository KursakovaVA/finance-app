package model;

public class User {

    private String login;
    private String passwordHash;
    private Wallet wallet;

    public User(String login, String passwordHash) {
        this.login = login;
        this.passwordHash = passwordHash;
        wallet = new Wallet();
    }

    public String getLogin() {
        return login;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public Wallet getWallet() {
        return wallet;
    }
}