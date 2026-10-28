package autoparts.dao.file;

import autoparts.dao.UserDAO;
import autoparts.model.User;
import autoparts.util.FileUtil;
import autoparts.util.FlatFiles;
import java.util.ArrayList;
import java.util.List;

/**
 * Guarda y consulta los usuarios en el archivo plano data/users.csv.
 *
 * @author Jahdiel Torres
 */
public class UserFileDAO implements UserDAO {

    @Override
    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        for (String[] fields : FileUtil.readFields(FlatFiles.USERS)) {
            users.add(new User(fields));
        }
        return users;
    }

    @Override
    public User findByEmail(String email) {
        for (User user : findAll()) {
            if (user.getEmail().equalsIgnoreCase(email)) {
                return user;
            }
        }
        return null;
    }

    @Override
    public int save(User user) {
        int id = FileUtil.nextId(FlatFiles.USERS, 0);
        User newUser = new User(id, user.getName(), user.getEmail(), user.getPasswordHash(),
                user.getRole(), user.getStatus(), user.getRegistrationDate(), user.getPartnerId());
        FileUtil.append(FlatFiles.USERS, newUser.toLine());
        return id;
    }

    @Override
    public void update(User user) {
        List<String> lines = new ArrayList<>();
        for (User current : findAll()) {
            if (current.getId() == user.getId()) {
                lines.add(user.toLine());
            } else {
                lines.add(current.toLine());
            }
        }
        FileUtil.rewrite(FlatFiles.USERS, lines);
    }
}
