package test;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;

import org.apache.ibatis.migration.Change;
import org.apache.ibatis.migration.FileMigrationLoader;
import org.apache.ibatis.migration.MigrationException;

public class CustomFileMigrationLoader extends FileMigrationLoader {

  public CustomFileMigrationLoader(File scriptsDir, String charset, Properties variables) {
    super(scriptsDir, charset, variables);
  }

  @Override
  public List<Change> getMigrations() {
    List<Change> migrations = new ArrayList<Change>();
    if (scriptsDir.isDirectory()) {
      List<String> relativePaths = listRelativePaths(scriptsDir, "", new ArrayList<String>());
      if (relativePaths == null) {
        throw new MigrationException(scriptsDir + " does not exist.");
      }
      Collections.sort(relativePaths, getRelativePathByFilenameComparator());
      for (String relativePath : relativePaths) {
        if (relativePath.endsWith(".sql") && !isSpecialFile(relativePath)) {
          Change change = parseChangeFromFilename(relativePath);
          migrations.add(change);
        }
      }
    }
    return migrations;
  }

  private Comparator<String> getRelativePathByFilenameComparator() {
    return new Comparator<String>() {
      @Override
      public int compare(String relativePath1, String relativePath2) {
        return extractFilename(relativePath1).compareTo(extractFilename(relativePath2));
      }
    };
  }

  private int getLastIndexOfSlash(String relativePath) {
    return relativePath.lastIndexOf("/") + 1;
  }

  private String extractFilename(String relativePath) {
    return relativePath.substring(getLastIndexOfSlash(relativePath));
  }

  private String extractPath(String relativePath) {
    return relativePath.substring(0, getLastIndexOfSlash(relativePath));
  }

  private List<String> listRelativePaths(File scriptsDir, String path, List<String> relativePathsAccumulator) {
    File[] files = scriptsDir.listFiles();
    for (File file : files) {
      if (file.isFile()) {
        relativePathsAccumulator.add(path + file.getName());
      } else {
        listRelativePaths(file, path + file.getName() + "/", relativePathsAccumulator);
      }
    }
    return relativePathsAccumulator;
  }

  protected Change parseChangeFromFilename(String filePath) {
    try {
      CustomChange change = new CustomChange();
      String filename = extractFilename(filePath);
      int lastIndexOfDot = filename.lastIndexOf(".");
      String[] parts = filename.substring(0, lastIndexOfDot).split("_");
      change.setId(new BigDecimal(parts[0]));
      StringBuilder builder = new StringBuilder();
      for (int i = 1; i < parts.length; i++) {
        if (i > 1) {
          builder.append(" ");
        }
        builder.append(parts[i]);
      }
      change.setDescription(builder.toString());
      change.setFilename(filename);
      change.setPath(extractPath(filePath));
      return change;
    } catch (Exception e) {
      throw new MigrationException("Error parsing change from file.  Cause: " + e, e);
    }
  }

  protected class CustomChange extends Change {
    private String path;

    @Override
    public String getFilename() {
      return getPath() + super.getFilename();
    }

    public String getPath() {
      return path;
    }

    public void setPath(String path) {
      this.path = path;
    }
  }
}