package ru.javaops.masterjava.upload;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import ru.javaops.masterjava.persist.DBIProvider;
import ru.javaops.masterjava.persist.dao.CityDao;
import ru.javaops.masterjava.persist.model.City;
import ru.javaops.masterjava.xml.util.StaxStreamProcessor;

import javax.xml.stream.XMLStreamException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class CityProcessor {

    private static CityDao dao = DBIProvider.getDao(CityDao.class);

    public Map<String, City> process(final InputStream is) throws XMLStreamException {
        log.info("Start city processing");
        val processor = new StaxStreamProcessor(is);

        Map<String, City> citiesToProcess = new HashMap<>();
        while (processor.startElement("City", "Cities")) {
            String cityCode = processor.getAttribute("id");
            String cityName = processor.getText();
            final City city = new City(cityCode, cityName);
            citiesToProcess.put(cityCode, city);
        }
        dao.insertBatch(new ArrayList<>(citiesToProcess.values()), 1000);

        return citiesToProcess;
    }
}
