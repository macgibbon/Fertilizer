package fertilizer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;

public class Model {

    private Model() {
        super();
        try {
            loadDefaults();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
    private Path pricesPath, ingredientsPath, requirementsPath;
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
    
    private void loadDefaults() throws IOException {
        File userDir = new File(System.getProperty("user.home"));
        appDir = new File(userDir, ".fertilizer");
        if (!appDir.exists()) {
            appDir.mkdirs();
        }
        currentDefaults = new File(appDir, "currentDefaults");    
        currentDefaults.createNewFile(); 
        Path defaultPath = Files.walk(Path.of("."))
                .filter(path -> path.endsWith("defaults"))
                .findFirst().get();
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
               Path defaultPath = Files.walk(appDir.toPath())
                       .filter(path -> path.endsWith(defaultFileName))
                       .findFirst().get();
               List<String> lines = Files.lines(defaultPath)
                       .collect(Collectors.toList());
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
                    .filter(line -> line.length() != 0).map(line -> line.split(","))
                    .map(array -> new ArrayList<String>(Arrays.asList(array)))
                    .collect(Collectors.toCollection((ArrayList::new)));
            return lines;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
	
	public static void deepCopy(Path sourcePath, Path targetPath) throws IOException {
       
        Files.walkFileTree(sourcePath, new java.nio.file.SimpleFileVisitor<Path>() {
            @Override
            public java.nio.file.FileVisitResult preVisitDirectory(Path dir, java.nio.file.attribute.BasicFileAttributes attrs) throws IOException {
                Path targetDirRelative = targetPath.resolve(sourcePath.relativize(dir));
                if (!Files.exists(targetDirRelative)) {
                    Files.createDirectories(targetDirRelative);
                }
                return java.nio.file.FileVisitResult.CONTINUE;
            }

            @Override
            public java.nio.file.FileVisitResult visitFile(Path file, java.nio.file.attribute.BasicFileAttributes attrs) throws IOException {
                Files.copy(file, targetPath.resolve(sourcePath.relativize(file)), StandardCopyOption.REPLACE_EXISTING);
                return java.nio.file.FileVisitResult.CONTINUE;
            }

            @Override
            public java.nio.file.FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                 // Log the error or handle it as needed
                System.err.println("Failed to copy " + file + ". Reason: " + exc.getMessage());
                return java.nio.file.FileVisitResult.CONTINUE;
            }
        });
    }

}
