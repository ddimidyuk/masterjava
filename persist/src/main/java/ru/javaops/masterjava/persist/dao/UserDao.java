package ru.javaops.masterjava.persist.dao;

import one.util.streamex.IntStreamEx;
import org.skife.jdbi.v2.PreparedBatch;
import org.skife.jdbi.v2.sqlobject.*;
import org.skife.jdbi.v2.sqlobject.customizers.BatchChunkSize;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;
import ru.javaops.masterjava.persist.DBIProvider;
import ru.javaops.masterjava.persist.mapper.UserMapper;
import ru.javaops.masterjava.persist.model.User;

import java.util.List;

@RegisterMapper(UserMapper.class)
public abstract class UserDao implements AbstractDao {

    public User insert(User user) {
        if (user.isNew()) {
            int id = insertGeneratedId(user, user.getCity().getCode());
            user.setId(id);
        } else {
            insertWitId(user, user.getCity().getCode());
        }
        return user;
    }

    @SqlQuery("SELECT nextval('user_seq')")
    abstract int getNextVal();

    @Transaction
    public int getSeqAndSkip(int step) {
        int id = getNextVal();
        DBIProvider.getDBI().useHandle(h -> h.execute("SELECT setval('user_seq', " + (id + step - 1) + ")"));
        return id;
    }

    @SqlUpdate("INSERT INTO users (full_name, email, flag, city_id) VALUES (:fullName, :email, CAST(:flag AS USER_FLAG), (SELECT id FROM cities where code = :cityCode)) ")
    @GetGeneratedKeys
    abstract int insertGeneratedId(@BindBean User user, @Bind("cityCode") String cityCode);

    @SqlUpdate("INSERT INTO users (id, full_name, email, flag, city_id) VALUES (:id, :fullName, :email, CAST(:flag AS USER_FLAG), (SELECT id FROM cities where code = :cityCode)) ")
    abstract void insertWitId(@BindBean User user, @Bind("cityCode") String cityCode);

    @SqlQuery("SELECT u.*, c.code as city_code, c.name as city_name FROM users u INNER JOIN cities c ON u.city_id = c.id ORDER BY full_name, email LIMIT :it")
    public abstract List<User> getWithLimit(@Bind int limit);

    //   http://stackoverflow.com/questions/13223820/postgresql-delete-all-content
    @SqlUpdate("TRUNCATE users CASCADE")
    @Override
    public abstract void clean();

    public int[] insertBatch(@BindBean List<User> users, @BatchChunkSize int chunkSize) {
        final int[][] ids = {new int[users.size()]};
        DBIProvider.getDBI().useHandle(h -> {
            PreparedBatch batch = h.prepareBatch("    INSERT INTO users (id, full_name, email, flag, city_id) VALUES (:id, :fullName, :email, CAST(:flag AS USER_FLAG), (SELECT id FROM cities where code = :cityCode)) ON CONFLICT DO NOTHING");
            users.forEach(user -> batch
                    .bind("id", user.getId())
                    .bind("fullName", user.getFullName())
                    .bind("email", user.getEmail())
                    .bind("flag", user.getFlag())
                    .bind("cityCode", user.getCity().getCode())
                    .add());
            ids[0] = batch.execute();
        });
        return ids[0];
    }

    public List<String> insertAndGetConflictEmails(List<User> users) {
        int[] result = insertBatch(users, users.size());
        return IntStreamEx.range(0, users.size())
                .filter(i -> result[i] == 0)
                .mapToObj(index -> users.get(index).getEmail())
                .toList();
    }
}