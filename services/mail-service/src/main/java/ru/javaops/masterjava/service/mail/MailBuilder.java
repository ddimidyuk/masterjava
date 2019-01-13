package ru.javaops.masterjava.service.mail;

import com.typesafe.config.Config;
import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;
import ru.javaops.masterjava.config.Configs;

public class MailBuilder {

    private MailBuilder() {
    }

    private static String host;
    private static Integer port;
    private static String userName;
    private static String password;
    private static boolean useSSL;
    private static boolean useTLS;
    private static boolean debug;
    private static String fromName;

    static {
        Config cfg = Configs.getConfig("mail.conf", "mail");
        host = cfg.getString("host");
        port = cfg.getInt("port");
        userName = cfg.getString("username");
        password = cfg.getString("password");
        useSSL = cfg.getBoolean("useSSL");
        useTLS = cfg.getBoolean("useTLS");
        debug = cfg.getBoolean("debug");
        fromName = cfg.getString("fromName");
    }

    public static Email createEmail(String email, String msg, String subj) throws EmailException {
        Email emailObj = new SimpleEmail();
        emailObj.setHostName(host);
        emailObj.setSmtpPort(port);
        emailObj.setAuthenticator(new DefaultAuthenticator(userName, password));
        emailObj.setSSLOnConnect(useSSL);
        emailObj.setFrom(fromName);
        emailObj.setSubject(subj);
        emailObj.setMsg(msg);
        emailObj.addTo(email);
        return emailObj;
    }
}
