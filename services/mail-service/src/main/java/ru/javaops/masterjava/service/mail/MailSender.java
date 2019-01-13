package ru.javaops.masterjava.service.mail;

import lombok.extern.slf4j.Slf4j;
import one.util.streamex.StreamEx;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class MailSender {

    private static MailServiceExecutor executor = new MailServiceExecutor();

    static void sendMail(List<Addressee> to, List<Addressee> cc, String subject, String body) {
        log.info("Send mail to \'" + to + "\' cc \'" + cc + "\' subject \'" + subject + (log.isDebugEnabled() ? "\nbody=" + body : ""));
        executor.sendToList(body, subject, to.stream().map(Addressee::getEmail).collect(Collectors.toSet()));
    }
}
