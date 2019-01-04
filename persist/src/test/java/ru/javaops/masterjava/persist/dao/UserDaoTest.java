package ru.javaops.masterjava.persist.dao;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import ru.javaops.masterjava.persist.UserTestData;
import ru.javaops.masterjava.persist.model.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static ru.javaops.masterjava.persist.UserTestData.*;

public class UserDaoTest extends AbstractDaoTest<UserDao> {

    public UserDaoTest() {
        super(UserDao.class);
    }

    @BeforeClass
    public static void init() throws Exception {
        UserTestData.init();
    }

    @Before
    public void setUp() throws Exception {
        UserTestData.setUp();
    }

    @Test
    public void getWithLimit() {
        List<User> users = dao.getWithLimit(5);
        Assert.assertEquals(FIST5_USERS, users);
    }

    @Test
    public void insertBatch() {
        List<User> mailDuplicates = dao.insertBatch(NEXT4_USERS, 2);
        List<User> first9TestData = new ArrayList<>(FIST5_USERS);
        first9TestData.addAll(NEXT4_USERS);
        List<User> first9Db = dao.getWithLimit(9);
        Assert.assertEquals(first9TestData, first9Db);
        Assert.assertEquals(Collections.singletonList(USER3), mailDuplicates);
    }
}