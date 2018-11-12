package eu.t5r.Logger;

import java.io.IOException;
import java.util.logging.ErrorManager;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

/**
 * 
 * 
 * <p> This file is part of the MDBS
 * 
 * @author Tobias Reichert [mail@teamtobias.de]
 * @version 0.1
 */
public class LogHandler extends Handler {

    @Override
    public void publish(LogRecord record) {
        if (getFormatter() == null) {
            setFormatter(new SimpleFormatter());
        }

        try {
            String message = getFormatter().format(record);
            if (record.getLevel().intValue() >= Level.WARNING.intValue()) {
                System.err.write(message.getBytes());
            } else {
                System.out.write(message.getBytes());
            }
        } catch (IOException exception) {
            reportError(null, exception, ErrorManager.FORMAT_FAILURE);
        }
    }

    @Override
    public void flush() {

    }

    @Override
    public void close() throws SecurityException {

    }

}
