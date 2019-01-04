package ru.javaops.masterjava.users;

import org.thymeleaf.context.WebContext;
import ru.javaops.masterjava.persist.DBIProvider;
import ru.javaops.masterjava.persist.dao.UserDao;
import ru.javaops.masterjava.persist.model.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.DriverManager;
import java.util.List;

import static ru.javaops.masterjava.common.web.ThymeleafListener.engine;

@WebServlet(urlPatterns = "/", loadOnStartup = 1)
public class UserServlet extends HttpServlet {

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

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        final WebContext webContext = new WebContext(req, resp, req.getServletContext(), req.getLocale());
        List<User> users = dao.getWithLimit(20);
        if (!users.isEmpty()) {
            webContext.setVariable("users", users);
            engine.process("result", webContext, resp.getWriter());
        } else {
            engine.process("nousers", webContext, resp.getWriter());
        }
    }
}
