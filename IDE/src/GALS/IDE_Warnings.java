package GALS;

import java.util.ArrayList;
import java.util.List;

public class IDE_Warnings {
    public static class LogEntry {
        private final int position;
        private final String message;

        public LogEntry(int position, String message) {
            this.position = position;
            this.message = message;
        }

        public int getPosition() {
            return position;
        }

        public String getMessage() {
            return message;
        }

        @Override
        public String toString() {
            return "LogEntry{position=" + position + ", message='" + message + "'}";
        }
    }

    private static IDE_Warnings instance;
    private final List<LogEntry> warnings;
    private final List<LogEntry> errors;

    private IDE_Warnings() {
        warnings = new ArrayList<>();
        errors = new ArrayList<>();
    }

    public static IDE_Warnings getInstance() {
        if (instance == null) {
            instance = new IDE_Warnings();
        }
        return instance;
    }

    public void addWarning(String message, int position) {
        warnings.add(new LogEntry(position, message));
    }

    public void addError(String message, int position) {
        errors.add(new LogEntry(position, message));
    }

    public LogEntry getError(int index) {
        return errors.get(index);
    }

    public List<LogEntry> getWarnings() {
        return new ArrayList<>(warnings);
    }

    public List<LogEntry> getErrors() {
        return new ArrayList<>(errors);
    }

    public void clearWarnsAndErrors() {
        warnings.clear();
        errors.clear();
    }
}
