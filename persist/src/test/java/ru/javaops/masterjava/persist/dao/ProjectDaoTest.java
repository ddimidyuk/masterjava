package ru.javaops.masterjava.persist.dao;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import ru.javaops.masterjava.persist.ProjectGroupTestData;
import ru.javaops.masterjava.persist.UserTestData;
import ru.javaops.masterjava.persist.model.Project;
import ru.javaops.masterjava.persist.model.User;

import java.util.List;

import static org.junit.Assert.*;
import static ru.javaops.masterjava.persist.ProjectGroupTestData.TOPJAVA;
import static ru.javaops.masterjava.persist.UserTestData.FIST5_USERS;

public class ProjectDaoTest extends AbstractDaoTest<ProjectDao> {

    public ProjectDaoTest() {
        super(ProjectDao.class);
    }

    @BeforeClass
    public static void init() throws Exception {
        ProjectGroupTestData.init();
    }

    @Before
    public void setUp() throws Exception {
        ProjectGroupTestData.setUp();
    }

    @Test
    public void getById() {
        Project project = dao.getById(TOPJAVA.getId());
        Assert.assertEquals(TOPJAVA, project);
    }
}