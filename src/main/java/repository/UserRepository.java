package repository;

import model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserRepository {
    private Map<String, User> users;

    public UserRepository() {
        users = new HashMap<>();
    }

    public void add(User user) {
        users.put(user.getLogin(), user);
    }

    public User findByLogin(String login) {
        return users.get(login);
    }

    public boolean exists(String login) {
        return users.containsKey(login);
    }

    public List<User> getAll() {
        return new ArrayList<>(users.values());
    }
}