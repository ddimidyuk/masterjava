package ru.javaops.masterjava.upload;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import ru.javaops.masterjava.persist.DBIProvider;
import ru.javaops.masterjava.persist.dao.GroupDao;
import ru.javaops.masterjava.persist.dao.ProjectDao;
import ru.javaops.masterjava.persist.model.Group;
import ru.javaops.masterjava.persist.model.Project;
import ru.javaops.masterjava.persist.model.type.GroupType;
import ru.javaops.masterjava.xml.schema.ObjectFactory;
import ru.javaops.masterjava.xml.util.JaxbParser;
import ru.javaops.masterjava.xml.util.StaxStreamProcessor;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import java.util.List;
import java.util.Map;

@Slf4j
public class ProjectGroupProcessor {
    private static final JaxbParser jaxbParser = new JaxbParser(ObjectFactory.class);
    private final ProjectDao projectDao = DBIProvider.getDao(ProjectDao.class);
    private final GroupDao groupDao = DBIProvider.getDao(GroupDao.class);

    /**
     *
     * @param processor processor
     * @return map, where k- name, v- group
     * @throws XMLStreamException parsing error
     * @throws JAXBException parsing error
     */
    public Map<String, Group> process(StaxStreamProcessor processor) throws XMLStreamException, JAXBException {
        val projectMap = projectDao.getAsMap();
        val groupMap = groupDao.getAsMap();

        val unmarshaller = jaxbParser.createUnmarshaller();

        while (processor.startElement("Project", "Projects")) {
            ru.javaops.masterjava.xml.schema.Project xmlProject = unmarshaller.unmarshal(processor.getReader(), ru.javaops.masterjava.xml.schema.Project.class);
            val name = xmlProject.getName();
            val description = xmlProject.getDescription();
            List<ru.javaops.masterjava.xml.schema.Project.Group> xmlGroups = xmlProject.getGroup();
            Project project = projectMap.get(name);
            if (project == null) {
                project = new Project(name, description);
                log.info("Insert new project " + project);
                project.setId(projectDao.insertGeneratedId(project));
                projectMap.put(project.getName(), project);
            }
            for (ru.javaops.masterjava.xml.schema.Project.Group group : xmlGroups) {
                if (!groupMap.containsKey(group.getName())) {
                    Group newGroup = new Group(group.getName(), GroupType.valueOf(group.getType().value()), project.getId());
                    log.info("Insert new group " + newGroup);
                    newGroup.setId(groupDao.insertGeneratedId(newGroup));
                    groupMap.put(newGroup.getName(), newGroup);
                }
            }
        }
        return groupMap;
    }
}
