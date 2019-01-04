package ru.javaops.masterjava.persist.dao;

import com.bertoncelj.jdbi.entitymapper.EntityMapperFactory;
import org.skife.jdbi.v2.sqlobject.*;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapperFactory;
import org.skife.jdbi.v2.sqlobject.stringtemplate.UseStringTemplate3StatementLocator;
import org.skife.jdbi.v2.unstable.BindIn;
import ru.javaops.masterjava.persist.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@RegisterMapperFactory(EntityMapperFactory.class)
@UseStringTemplate3StatementLocator
public abstract class UserDao implements AbstractDao {

    public User insert(User user) {
        if (user.isNew()) {
            int id = insertGeneratedId(user);
            user.setId(id);
        } else {
            insertWitId(user);
        }
        return user;
    }

    @SqlUpdate("INSERT INTO users (full_name, email, flag) VALUES (:fullName, :email, CAST(:flag AS user_flag)) ")
    @GetGeneratedKeys
    abstract int insertGeneratedId(@BindBean User user);

    @SqlUpdate("INSERT INTO users (id, full_name, email, flag) VALUES (:id, :fullName, :email, CAST(:flag AS user_flag)) ")
    abstract void insertWitId(@BindBean User user);

    @SqlQuery("SELECT * FROM users ORDER BY full_name, email LIMIT :it")
    public abstract List<User> getWithLimit(@Bind int limit);

    @SqlQuery("SELECT * FROM users WHERE email IN (<emails>) ORDER BY full_name, email")
    public abstract List<User> getByEmailList(@BindIn("emails") List<String> emails);

    //   http://stackoverflow.com/questions/13223820/postgresql-delete-all-content
    @SqlUpdate("TRUNCATE users")
    @Override
    public abstract void clean();

    @SqlBatch("INSERT INTO users (full_name, email, flag) VALUES (:fullName, :email, CAST(:flag AS user_flag)) ON CONFLICT(email) DO NOTHING;")
    @GetGeneratedKeys
    abstract int[] insertAllGeneratedId(@BindBean List<User> users);

    @SqlBatch("INSERT INTO users (id, full_name, email, flag) VALUES (:id, :fullName, :email, CAST(:flag AS user_flag)) ON CONFLICT (email) DO NOTHING")
    @GetGeneratedKeys
    abstract void insertAllWithId(@BindBean List<User> users);

    public List<User> insertAll(List<User> users) {
        List<User> usersWithoutId = new ArrayList<>();
        List<User> usersWithId = new ArrayList<>();
        users.forEach(user -> {
            if (user.isNew()) usersWithoutId.add(user);
            else usersWithId.add(user);
        });
        if (!usersWithId.isEmpty()) {
            insertAllWithId(usersWithId);
        }
        if (!usersWithoutId.isEmpty()) {
            int[] generatedIds = insertAllGeneratedId(usersWithoutId);
            for (int i = 0; i < generatedIds.length; i++) {
                if (generatedIds[i] != 0) {
                    User user = usersWithoutId.get(i);
                    user.setId(generatedIds[i]);
                    usersWithId.add(user);
                }
            }
        }
        return usersWithId;
    }

    public List<User> insertBatch(List<User> users, int chunkSize) {
        if (users == null || users.isEmpty() || chunkSize == 0) {
            return null;
        }
        List<String> emailsToInsert = users.stream().map(User::getEmail).collect(Collectors.toList());
        Map<String, User> existedMailUsers = getByEmailList(emailsToInsert).stream().collect(Collectors.toMap(User::getEmail, Function.identity()));
        List<User> chunk = new ArrayList<>();
        users.forEach(user -> {
            chunk.add(user);
            if (existedMailUsers.get(user.getEmail()) != null) {
                existedMailUsers.put(user.getEmail(), user);
            }
            if (chunk.size() == chunkSize) {
                insertAll(chunk);
                chunk.clear();
            }
        });
        insertAll(chunk);
        return new ArrayList<>(existedMailUsers.values());
    }
}
