package fertilizer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;

public class Model {

    private Model() {
        super();
        loadDefaults();
    }

    private static Model instance;

    public static synchronized Model getInstance() {
        if (instance == null)
            instance = new Model();
        return instance;
    }

    public static void reset() {
        instance = null;
    }

    public Preferences preferences;
    public ArrayList<ArrayList<String>> ingredientRows, priceRows, requirementRows, mergedMatrix;
    public File appDir;
    private List<String> reportHeaders;

    public List<String> getReportHeaders() {
        return reportHeaders;
    }

    public SimpleDoubleProperty batchWt = new SimpleDoubleProperty(8000.0);
    public SimpleStringProperty contact = new SimpleStringProperty("Stamford Farmers Cooperative");
    public SimpleStringProperty notes = new SimpleStringProperty("Notes:");
    public SimpleStringProperty version = new SimpleStringProperty();
    private File currentDefaults;

    private void loadDefaults()  {
        File userDir = new File(System.getProperty("user.home"));
        appDir = new File(userDir, ".fertilizer");
        if (!appDir.exists()) {
            appDir.mkdirs();
        }
        currentDefaults = new File(appDir, "currentDefaults");
        Path defaultPath = Path.of("./defaults");        
        deepCopy(defaultPath, currentDefaults.toPath());
        preferences = Preferences.userNodeForPackage(getClass());
        priceRows = readCsvfile("defaultPrices.csv");
        ingredientRows = readCsvfile("defaultIngredients.csv");
        requirementRows = readCsvfile("defaultRequirements.csv");
        reportHeaders = readTextFile("header.txt");
        version.set(loadProperty("version.properties", "application.version"));
    }

    public String loadProperty(String fileName, String propertyName) {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(fileName)) {
            Properties properties = new Properties();
            properties.load(input);
            String version = properties.getProperty(propertyName);
            return version;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<String> readTextFile(String defaultFileName) {
        try {
            Path defaultPath = Files.walk(appDir.toPath()).filter(path -> path.endsWith(defaultFileName)).findFirst().get();
            List<String> lines = Files.lines(defaultPath).collect(Collectors.toList());
            return lines;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public ArrayList<ArrayList<String>> readCsvfile(String defaultFileName) {
        try {
            Path defaultPath = Files.walk(currentDefaults.toPath())
                    .filter(path -> path.endsWith(defaultFileName))
                    .findFirst().get();
            ArrayList<ArrayList<String>> lines = Files.lines(defaultPath)
                    .filter(line -> line.length() != 0)
                    .map(line -> line.split(","))
                    .map(array -> new ArrayList<String>(Arrays.asList(array)))
                    .collect(Collectors.toCollection((ArrayList::new)));
            return lines;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void deepCopy(Path sourcePath, Path targetPath)  {
        try (Stream<Path> stream = Files.walk(sourcePath)) {
            stream.forEach(source -> copy(sourcePath, targetPath, source));
        } catch (IOException e) {
           throw new RuntimeException(e);
        }
    }

    public static void copy(Path sourcePath, Path targetPath, Path source)  {
        Path target = targetPath.resolve(sourcePath.relativize(source));
        if (!(target.toFile().exists())) {
            try {
                Files.copy(source, target);
            } catch (IOException e) {
               throw new RuntimeException(e);
            }
        }
    }

}
