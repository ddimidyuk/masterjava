package ru.javaops.masterjava.servlet;

import ru.javaops.masterjava.xml.schema.User;
import ru.javaops.masterjava.xml.util.JaxbParser;
import ru.javaops.masterjava.xml.util.StaxStreamProcessor;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

@WebServlet("/upload")
@MultipartConfig(fileSizeThreshold = 1024 * 1024 * 2, // 2MB
        maxFileSize = 1024 * 1024 * 10,      // 10MB
        maxRequestSize = 1024 * 1024 * 50)   // 50MB
public class FileUploadServlet extends HttpServlet {
    /**
     * Name of the directory where uploaded files will be saved, relative to
     * the web application directory.
     */
    private static final String SAVE_DIR = "uploadFiles";
    private static final Comparator<User> USER_COMPARATOR = Comparator.comparing(User::getValue).thenComparing(User::getEmail);

    /**
     * handles file upload
     */
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response) throws ServletException, IOException {
        // constructs path of the directory to save uploaded file
        String savePath = request.getServletContext().getRealPath("") + SAVE_DIR;

        // creates the save directory if it does not exists
        File fileSaveDir = new File(savePath);
        if (!fileSaveDir.exists()) {
            fileSaveDir.mkdir();
        }

        Set<User> users = new HashSet<>();

        for (Part part : request.getParts()) {
            String fileName = extractFileName(part);
            // refines the fileName in case it is an absolute path
            fileName = new File(fileName).getName();
            part.write(savePath + File.separator + fileName);
            users.addAll(getUsersFromPayloadXml(savePath + File.separator + fileName));
        }

        request.setAttribute("users", users);
        getServletContext().getRequestDispatcher("/users.jsp").forward(
                request, response);
    }

    /**
     * Extracts file name from HTTP header content-disposition
     */
    private String extractFileName(Part part) {
        String contentDisp = part.getHeader("content-disposition");
        String[] items = contentDisp.split(";");
        for (String s : items) {
            if (s.trim().startsWith("filename")) {
                return s.substring(s.indexOf("=") + 2, s.length() - 1);
            }
        }
        return "";
    }

    private static Set<User> getUsersFromPayloadXml(String file) {
        Set<User> users = new TreeSet<>(USER_COMPARATOR);

        try (InputStream is = new FileInputStream(new File(file))) {
            StaxStreamProcessor processor = new StaxStreamProcessor(is);
            // Users loop

            JaxbParser parser = new JaxbParser(User.class);
            while (processor.doUntil(XMLEvent.START_ELEMENT, "User")) {
                User user = parser.unmarshal(processor.getReader(), User.class);
                users.add(user);
            }
        } catch (IOException e) {
            System.out.println("Couldn't process \'" + file + "\' file");
        } catch (XMLStreamException | JAXBException e) {
            System.out.println("Error while parsing \'" + file + "\' file");
        }
        return users;
    }
}

