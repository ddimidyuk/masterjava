package ru.javaops.masterjava.persist.dao;

import com.bertoncelj.jdbi.entitymapper.EntityMapperFactory;
import org.skife.jdbi.v2.sqlobject.*;
import org.skife.jdbi.v2.sqlobject.customizers.BatchChunkSize;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapperFactory;
import ru.javaops.masterjava.persist.model.Group;

import java.util.List;

@RegisterMapperFactory(EntityMapperFactory.class)
public abstract class GroupDao implements AbstractDao {

    @SqlUpdate("TRUNCATE groups CASCADE")
    @Override
    public abstract void clean();

    public Group insert(Group group) {
        if (group.isNew()) {
            int id = insertGeneratedId(group);
            group.setId(id);
        } else {
            insertWitId(group);
        }
        return group;
    }

    @SqlUpdate("INSERT INTO groups (name, type) VALUES (:name, CAST(:type AS GROUP_TYPE)) ")
    @GetGeneratedKeys
    abstract int insertGeneratedId(@BindBean Group group);

    @SqlUpdate("INSERT INTO groups (id, name, type) VALUES (:id, :name, CAST(:type AS GROUP_TYPE)) ")
    @GetGeneratedKeys
    abstract int insertWitId(@BindBean Group group);

    @SqlBatch("INSERT INTO groups (name, type, project_id) VALUES (:name, CAST(:type AS GROUP_TYPE), :projectId)" +
            "ON CONFLICT (name) DO UPDATE SET type = CAST(:type AS GROUP_TYPE), project_id = :projectId")
    public abstract int[] insertBatch(@BindBean List<Group> groups, @Bind("projectId") int projectId, @BatchChunkSize int chunkSize);

    @SqlQuery("SELECT * FROM groups WHERE project_id = :it")
    public abstract List<Group> getByProject(@Bind int projectId);

    @SqlQuery("SELECT g.* FROM user_groups ug INNER JOIN groups g ON ug.group_id = g.id WHERE ug.user_id = :it")
    public abstract List<Group> getByUserId(@Bind int userId);
}
