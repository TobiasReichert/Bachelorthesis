package eu.t5r.Logger;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

/**
 * This class is part of the logging for the MDBS and formats the Logs.
 * Example: C1 INFO 11:26:03.858 MDBSServer mbda$new$0() $ [MSG]
 * @author Tobias Reichert
 */
public class LogFormatter extends Formatter {

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("hh:mm:ss.SSS");

    String softwareIdentifier;

    public LogFormatter(String softwareIdentifier) {
        this.softwareIdentifier = softwareIdentifier;
    }

    @Override
    public String format(LogRecord record) {
        char separator = ' ';
        StringBuilder result = new StringBuilder(1000);

        result.append(softwareIdentifier);
        result.append(separator);
        String temp = record.getLevel().toString();
        result.append(temp.substring(0, Math.min(temp.length(), 4)));
        result.append(separator);
        result.append(DATE_FORMAT.format(new Date(record.getMillis())));
        result.append(separator);
        result.append(strToLength(record.getSourceClassName(), 10));
        result.append(separator);
        result.append(strToLength(record.getSourceMethodName(), 10));
        result.append("()");
        result.append(" $ ");

        result.append(formatMessage(record));
        result.append("\n");

        return result.toString();
    }

    private String strToLength(String s, int length) {
        String[] sa = s.split("\\.");
        s = sa[sa.length - 1];

        if (s.length() == length) {
            return s;
        } else if (s.length() < length) {
            return String.format("%1$" + length + "s", s);
        } else {
            return s.substring(s.length() - length);
        }

    }

    @Override
    public String getHead(Handler h) {
        return super.getHead(h);
    }

    @Override
    public String getTail(Handler h) {
        return super.getTail(h);
    }
}
