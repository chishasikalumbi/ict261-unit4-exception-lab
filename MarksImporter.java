import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/** Imports student marks while reporting bad records without stopping the whole file. */
public class MarksImporter {
    private static final Logger LOGGER = Logger.getLogger(MarksImporter.class.getName());

    /** Custom unchecked exception used when a student ID appears more than once. */
    static class DuplicateStudentException extends RuntimeException {
        public DuplicateStudentException(String message) {
            super(message);
        }
    }

    /**
     * Imports a text file. Invalid individual records are skipped; processing continues.
     * IOException is checked, so this method declares it with throws for the caller to handle.
     */
    static void importMarks(String filename) throws IOException {
        Set<String> seen = new HashSet<>();

        // try-with-resources automatically closes the reader, even if reading or processing fails.
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            int lineNumber = 0;
            while ((line = br.readLine()) != null) {
                lineNumber++;
                try {
                    String[] parts = line.split(",", -1);

                    // TODO 1: exactly two fields are required.
                    if (parts.length != 2) {
                        throw new IllegalArgumentException("Expected exactly 2 fields (student ID, mark)");
                    }

                    String studentId = parts[0].trim();
                    String markText = parts[1].trim();
                    if (studentId.isEmpty()) {
                        throw new IllegalArgumentException("Student ID cannot be empty");
                    }

                    // TODO 2: parse the mark and validate the inclusive range 0..100.
                    int mark = Integer.parseInt(markText); // NumberFormatException is handled below.
                    if (mark < 0 || mark > 100) {
                        throw new IllegalArgumentException("Mark must be between 0 and 100; received " + mark);
                    }

                    // TODO 3: reject a repeated student ID.
                    if (!seen.add(studentId)) {
                        throw new DuplicateStudentException("Duplicate student ID: " + studentId);
                    }

                    // TODO 4: display an accepted record.
                    System.out.println("Accepted: " + studentId + " = " + mark);
                } catch (NumberFormatException ex) {
                    System.out.println("Line " + lineNumber + ": Invalid numeric mark; record skipped.");
                    LOGGER.log(Level.WARNING, "Line " + lineNumber + " rejected (" + ex.getClass().getSimpleName() + "): " + ex.getMessage(), ex);
                } catch (DuplicateStudentException ex) {
                    System.out.println("Line " + lineNumber + ": " + ex.getMessage() + "; record skipped.");
                    LOGGER.log(Level.WARNING, "Line " + lineNumber + " rejected (" + ex.getClass().getSimpleName() + "): " + ex.getMessage(), ex);
                } catch (IllegalArgumentException ex) {
                    System.out.println("Line " + lineNumber + ": Invalid record - " + ex.getMessage() + "; record skipped.");
                    LOGGER.log(Level.WARNING, "Line " + lineNumber + " rejected (" + ex.getClass().getSimpleName() + "): " + ex.getMessage(), ex);
                }
            }
        }
    }

    public static void main(String[] args) {
        String filename = args.length > 0 ? args[0] : "marks.txt";

        // Technical details go to a log file rather than being exposed as stack traces to users.
        try {
            FileHandler fileHandler = new FileHandler("import-errors.log", true);
            fileHandler.setFormatter(new SimpleFormatter());
            LOGGER.addHandler(fileHandler);
            LOGGER.setUseParentHandlers(false);
        } catch (IOException logSetupError) {
            System.out.println("Warning: technical log file could not be opened.");
        }

        try {
            importMarks(filename);
        } catch (FileNotFoundException ex) {
            System.out.println("Marks file not found; check the file name.");
            LOGGER.log(Level.SEVERE, "FileNotFoundException while opening '" + filename + "': " + ex.getMessage(), ex);
        } catch (IOException ex) {
            System.out.println("The marks file could not be read; please check the file and try again.");
            LOGGER.log(Level.SEVERE, "IOException while reading '" + filename + "': " + ex.getMessage(), ex);
        } finally {
            System.out.println("Import attempt finished");
        }
    }
}
