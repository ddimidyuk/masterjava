package ru.javaops.masterjava.persist;

import com.google.common.collect.ImmutableList;
import ru.javaops.masterjava.persist.dao.CityDao;
import ru.javaops.masterjava.persist.model.City;

import java.util.List;

public class CityTestData {
    public static City S_PETERSBURG;
    public static City MOSCOW;
    public static City KIEV;
    public static City MINSK;
    public static List<City> FIRST3_CITIES;


    public static void init() {
        KIEV = new City("kiv", "Киев");
        MINSK = new City("mnsk", "Минск");
        S_PETERSBURG = new City("spb", "Санкт-Петербург");
        MOSCOW = new City("mow", "Москва");
        FIRST3_CITIES = ImmutableList.of(KIEV, MINSK, S_PETERSBURG);
    }

    public static void setUp() {
        CityDao dao = DBIProvider.getDao(CityDao.class);
        dao.clean();
        DBIProvider.getDBI().useTransaction((conn, status) -> {
            FIRST3_CITIES.forEach(dao::insert);
            dao.insert(MOSCOW);
        });
    }
}
