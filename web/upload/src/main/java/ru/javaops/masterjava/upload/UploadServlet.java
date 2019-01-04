package ru.javaops.masterjava.upload;

import org.thymeleaf.context.WebContext;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;

import static ru.javaops.masterjava.common.web.ThymeleafListener.engine;

@WebServlet(urlPatterns = "/", loadOnStartup = 1)
@MultipartConfig(fileSizeThreshold = 1024 * 1024 * 10) //10 MB in memory limit
public class UploadServlet extends HttpServlet {

    private final UserProcessor userProcessor = new UserProcessor();


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        final WebContext webContext = new WebContext(req, resp, req.getServletContext(), req.getLocale());
        engine.process("upload", webContext, resp.getWriter());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        final WebContext webContext = new WebContext(req, resp, req.getServletContext(), req.getLocale());
        try {
//            http://docs.oracle.com/javaee/6/tutorial/doc/glraq.html
            Part filePart = req.getPart("fileToUpload");
            if (filePart.getSize() == 0) {
                throw new IllegalStateException("Upload file have not been selected");
            }
            try (InputStream is = filePart.getInputStream()) {
                UserProcessor.ProcessResult processResult = userProcessor.processAndSave(is, req.getParameter("chunkSize") != null ? Integer.parseInt(req.getParameter("chunkSize")) : 10);
//                List<User> mailDuplicates = dao.insertBatch(users, req.getParameter("chunkSize") != null ? Integer.parseInt(req.getParameter("chunkSize")) : 10);
                if (processResult.getMailDuplications() != null && !processResult.getMailDuplications().isEmpty()) {
                    webContext.setVariable("users", processResult.getMailDuplications());
                    engine.process("result", webContext, resp.getWriter());
                } else {
                    engine.process("nodups", webContext, resp.getWriter());
                }
            }
        } catch (Exception e) {
            webContext.setVariable("exception", e);
            engine.process("exception", webContext, resp.getWriter());
        }
    }
}
