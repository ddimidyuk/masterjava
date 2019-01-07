package ru.javaops.masterjava.persist.dao;

import com.bertoncelj.jdbi.entitymapper.EntityMapperFactory;
import org.skife.jdbi.v2.sqlobject.*;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapperFactory;
import ru.javaops.masterjava.persist.DBIProvider;
import ru.javaops.masterjava.persist.model.Project;

@RegisterMapperFactory(EntityMapperFactory.class)
public abstract class ProjectDao implements AbstractDao {

    private static final GroupDao groupDao;

    static {
        groupDao = DBIProvider.getDao(GroupDao.class);
    }

    @SqlUpdate("TRUNCATE projects CASCADE")
    @Override
    public abstract void clean();

    public Project insert(Project project) {
        int id;
        if (project.isNew()) {
            id = insertGeneratedId(project);
            project.setId(id);
        } else {
            insertWitId(project);
            id = project.getId();
        }
        groupDao.insertBatch(project.getGroups(), id, 1000);
        return project;
    }

    @SqlUpdate("INSERT INTO projects (name, description) VALUES (:name, :description) ")
    @GetGeneratedKeys
    abstract int insertGeneratedId(@BindBean Project project);

    @SqlUpdate("INSERT INTO projects (id, name, description) VALUES (:id, :name, :description) ")
    @GetGeneratedKeys
    abstract int insertWitId(@BindBean Project project);

    @SqlQuery("SELECT * FROM projects WHERE id = :it")
    abstract Project getByIdWithoutGroups(@Bind int id);

    public Project getById(int id){
        Project project = getByIdWithoutGroups(id);
        project.setGroups(groupDao.getByProject(id));
        return project;
    }
}
