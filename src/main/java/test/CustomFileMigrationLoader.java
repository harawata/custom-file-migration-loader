package test;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.TreeMap;

import org.apache.ibatis.migration.Change;
import org.apache.ibatis.migration.FileMigrationLoader;
import org.apache.ibatis.migration.MigrationException;
import org.apache.ibatis.migration.MigrationReader;

public class CustomFileMigrationLoader extends FileMigrationLoader {

  public CustomFileMigrationLoader(File scriptsDir, String charset, Properties variables) {
    super(scriptsDir, charset, variables);
  }

  @Override
  public List<Change> getMigrations() {
    if (!scriptsDir.exists() || !scriptsDir.isDirectory()) {
      throw new MigrationException("Scripts directory does not exist.");
    }
    TreeMap<String, File> scripts = new TreeMap<>();
    collectScripts(scripts, scriptsDir);
    List<Change> migrations = new ArrayList<Change>();
    for (String name : scripts.keySet()) {
      migrations.add(parseChangeFromFilename(name));
    }
    return migrations;
  }

  @Override
  public Reader getScriptReader(Change change, boolean undo) {
    if (!scriptsDir.exists() || !scriptsDir.isDirectory()) {
      throw new MigrationException(scriptsDir + " does not exist or is not a directory.");
    }
    TreeMap<String, File> scripts = new TreeMap<>();
    collectScripts(scripts, scriptsDir);
    File file = scripts.get(change.getFilename());
    try {
      return new MigrationReader(file, charset, undo, variables);
    } catch (IOException e) {
      throw new MigrationException("Error reading " + file, e);
    }
  }

  protected void collectScripts(TreeMap<String, File> scripts, File dir) {
    File[] files = dir.listFiles();
    if (files == null) {
      throw new MigrationException("Error occured while listing files in directory: " + dir.getAbsolutePath());
    }
    for (File f : files) {
      if (f.isDirectory()) {
        collectScripts(scripts, f);
      } else if (f.isFile()) {
        String name = f.getName();
        if (name.endsWith(".sql") && !isSpecialFile(name)) {
          scripts.put(f.getName(), f);
        }
      }
    }
  }
}