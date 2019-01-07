package ru.javaops.masterjava.persist.mapper;

import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;
import ru.javaops.masterjava.persist.model.City;
import ru.javaops.masterjava.persist.model.User;
import ru.javaops.masterjava.persist.model.UserFlag;

import java.sql.ResultSet;
import java.sql.SQLException;

public class UserMapper implements ResultSetMapper<User> {

    @Override
    public User map(int i, ResultSet resultSet, StatementContext statementContext) throws SQLException {
        City city = new City(resultSet.getString("city_code"), resultSet.getString("city_name"));
        User user = new User(resultSet.getString("full_name"),
                resultSet.getString("email"),
                UserFlag.valueOf(resultSet.getString("flag")),
                city);
        return user;
    }
}