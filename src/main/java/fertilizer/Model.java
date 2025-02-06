package fertilizer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

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
    private Path pricesPath, ingredientsPath, requirementsPath;
    public SolutionModel solutionModel;
    public File appDir;

    private void loadDefaults() {
        File userDir = new File(System.getProperty("user.home"));
        appDir = new File(userDir, ".fertilizer");
        if (!appDir.exists()) {
            appDir.mkdirs();
        }

        preferences = Preferences.userNodeForPackage(getClass());
        pricesPath = Path.of("defaultPrices.csv");
        priceRows = readCsvfile(pricesPath);
        ingredientsPath = Path.of("defaultIngredients.csv");
        ingredientRows = readCsvfile(ingredientsPath);
        requirementsPath = Path.of("defaultRequirements.csv");
        requirementRows = readCsvfile(requirementsPath);
    }

    public ArrayList<ArrayList<String>> readCsvfile(Path ingredientsPath) {
        ArrayList<ArrayList<String>> lines;
        try {
            lines = Files.lines(ingredientsPath).filter(line -> line.length() != 0).map(line -> line.split(","))
                    .map(array -> new ArrayList<String>(Arrays.asList(array)))
                    .collect(Collectors.toCollection((ArrayList::new)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return lines;
    }

}
