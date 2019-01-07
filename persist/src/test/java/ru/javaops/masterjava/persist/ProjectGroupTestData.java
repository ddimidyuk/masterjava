package ru.javaops.masterjava.persist;

import ru.javaops.masterjava.persist.dao.GroupDao;
import ru.javaops.masterjava.persist.dao.ProjectDao;
import ru.javaops.masterjava.persist.model.Group;
import ru.javaops.masterjava.persist.model.GroupType;
import ru.javaops.masterjava.persist.model.Project;

import java.util.Arrays;

public class ProjectGroupTestData {

    public static Group TOPJAVA01;
    public static Group TOPJAVA02;
    public static Group TOPJAVA03;
    public static Group MASTERJAVA01;
    public static Group MASTERJAVA02;
    public static Project TOPJAVA;
    public static Project MASTERJAVA;


    public static void init() {
        TOPJAVA01 = new Group("topjava01", GroupType.FINISHED);
        TOPJAVA02 = new Group("topjava02", GroupType.CURRENT);
        TOPJAVA03 = new Group("topjava03", GroupType.REGISTERING);
        MASTERJAVA01 = new Group("masterjava01", GroupType.CURRENT);
        MASTERJAVA02 = new Group("masterjava02", GroupType.REGISTERING);
        TOPJAVA = new Project("topjava", "topjava descr", Arrays.asList(TOPJAVA01, TOPJAVA02, TOPJAVA03));
        MASTERJAVA = new Project("masterjava", "masterjava descr", Arrays.asList(MASTERJAVA01, MASTERJAVA02));
    }

    public static void setUp() {
        ProjectDao projectDao = DBIProvider.getDao(ProjectDao.class);
        GroupDao groupDao = DBIProvider.getDao(GroupDao.class);
        projectDao.clean();
        DBIProvider.getDBI().useTransaction((conn, status) -> {
            projectDao.insert(MASTERJAVA);
            projectDao.insert(TOPJAVA);
        });
    }
}
