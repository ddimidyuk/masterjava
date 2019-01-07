package ru.javaops.masterjava.persist.dao;

import com.bertoncelj.jdbi.entitymapper.EntityMapperFactory;
import org.skife.jdbi.v2.sqlobject.*;
import org.skife.jdbi.v2.sqlobject.customizers.BatchChunkSize;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapperFactory;
import ru.javaops.masterjava.persist.model.City;

import java.util.List;

@RegisterMapperFactory(EntityMapperFactory.class)
public abstract class CityDao implements AbstractDao {

    @SqlUpdate("TRUNCATE cities CASCADE")
    @Override
    public abstract void clean();

    public City insert(City city) {
        if (city.isNew()) {
            int id = insertGeneratedId(city);
            city.setId(id);
        } else {
            insertWitId(city);
        }
        return city;
    }

    @SqlUpdate("INSERT INTO cities (code, name) VALUES (:code, :name) ")
    @GetGeneratedKeys
    abstract int insertGeneratedId(@BindBean City city);

    @SqlUpdate("INSERT INTO cities (id, code, name) VALUES (:id, :code, :name) ")
    abstract void insertWitId(@BindBean City city);

    @SqlBatch("INSERT INTO cities (code, name) VALUES (:code, :name)" +
            "ON CONFLICT DO NOTHING")
    public abstract int[] insertBatch(@BindBean List<City> cities, @BatchChunkSize int chunkSize);

    @SqlQuery("SELECT * FROM cities ORDER BY name LIMIT :it")
    public abstract List<City> getWithLimit(@Bind int limit);

    @SqlQuery("SELECT * FROM cities WHERE code = :it")
    public abstract City getByCode(@Bind String code);
}
