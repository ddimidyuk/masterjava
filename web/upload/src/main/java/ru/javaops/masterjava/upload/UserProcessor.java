package ru.javaops.masterjava.upload;

import org.slf4j.Logger;
import ru.javaops.masterjava.persist.DBIProvider;
import ru.javaops.masterjava.persist.dao.UserDao;
import ru.javaops.masterjava.persist.model.User;
import ru.javaops.masterjava.persist.model.UserFlag;
import ru.javaops.masterjava.xml.schema.ObjectFactory;
import ru.javaops.masterjava.xml.util.JaxbParser;
import ru.javaops.masterjava.xml.util.JaxbUnmarshaller;
import ru.javaops.masterjava.xml.util.StaxStreamProcessor;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.io.InputStream;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

import static org.slf4j.LoggerFactory.getLogger;

public class UserProcessor {
    private static final Logger log = getLogger(UserProcessor.class);
    private static final JaxbParser jaxbParser = new JaxbParser(ObjectFactory.class);
    private final ExecutorService saveExecutor = Executors.newFixedThreadPool(8);
    private static UserDao dao;

    static {
        DBIProvider.init(() -> {
            try {
                Class.forName("org.postgresql.Driver");
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException("PostgreSQL driver not found", e);
            }
            return DriverManager.getConnection("jdbc:postgresql://localhost:5432/masterjava", "service_user", "password");
        });
        dao = DBIProvider.getDao(UserDao.class);
    }

    public ProcessResult processAndSave(final InputStream is, int saveBatchSize) throws Exception {
        final StaxStreamProcessor processor = new StaxStreamProcessor(is);
        List<User> users = new ArrayList<>();

        JaxbUnmarshaller unmarshaller = jaxbParser.createUnmarshaller();
        final CompletionService<List<User>> completionService = new ExecutorCompletionService<>(saveExecutor);
        // k - future duplicated users, v - users to save
        Map<Future<List<User>>, List<User>> futureDuplicates = new HashMap<>();

        while (processor.doUntil(XMLEvent.START_ELEMENT, "User")) {
            ru.javaops.masterjava.xml.schema.User xmlUser = unmarshaller.unmarshal(processor.getReader(), ru.javaops.masterjava.xml.schema.User.class);
            final User user = new User(xmlUser.getValue(), xmlUser.getEmail(), UserFlag.valueOf(xmlUser.getFlag().value()));
            users.add(user);

            if (users.size() == saveBatchSize) {
                List<User> toSave = new ArrayList<>();
                toSave.addAll(users);
                futureDuplicates.put(completionService.submit(() -> dao.insertBatch(new ArrayList<>(toSave), saveBatchSize)), new ArrayList<>(users));
                users.clear();
            }
        }
        futureDuplicates.put(completionService.submit(() -> dao.insertBatch(new ArrayList<>(users), saveBatchSize)), new ArrayList<>(users));

        return ((Callable<ProcessResult>) () -> {
            ProcessResult result = new ProcessResult();
            while (!futureDuplicates.isEmpty()) {
                Future<List<User>> future = null;
                try {
                    future = completionService.poll(10, TimeUnit.SECONDS);
                    if (future == null) {
                        throw new TimeoutException();
                    }
                    futureDuplicates.remove(future);
                    if (future.get() != null) {
                        result.mailDuplications.addAll(future.get());
                    }
                } catch (ExecutionException | InterruptedException e) {
                    result.failed.put(futureDuplicates.get(future), e);
                }
            }
            return result;
        }).call();
    }

    class ProcessResult {
        private List<User> mailDuplications;
        private Map<List<User>, Throwable> failed;

        public ProcessResult() {
            this.mailDuplications = new ArrayList<>();
            this.failed = new HashMap<>();
        }

        public List<User> getMailDuplications() {
            return mailDuplications;
        }

        public Map<List<User>, Throwable> getFailed() {
            return failed;
        }
    }
}
