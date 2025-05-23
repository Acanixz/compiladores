package GALS;

import java.util.ArrayList;
import java.util.List;

public class IDE_Warnings {
    public static class LogEntry {
        private final int position;
        private final String message;
        private final String lexeme;

        public LogEntry(int position, String message, String lexeme) {
            this.position = position;
            this.message = message;
            this.lexeme = lexeme;
        }

        public int getPosition() {
            return position;
        }

        public String getMessage() {
            return message;
        }

        public String getLexeme() {
            return lexeme;
        }

        @Override
        public String toString() {
            return "LogEntry{position=" + position + ", message='" + message + ", lexeme='" + lexeme + "'}";
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

    public void addWarning(String message, int position, String lexeme) {
        warnings.add(new LogEntry(position, message, lexeme));
    }

    public void addError(String message, int position, String lexeme) {
        errors.add(new LogEntry(position, message, lexeme));
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
