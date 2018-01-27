/**
 *    Copyright 2010-2018 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
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

  protected TreeMap<String, File> scripts;

  public CustomFileMigrationLoader(File scriptsDir, String charset, Properties variables) {
    super(scriptsDir, charset, variables);
  }

  @Override
  public List<Change> getMigrations() {
    scanScriptsDir();
    List<Change> migrations = new ArrayList<Change>();
    for (String name : scripts.keySet()) {
      migrations.add(parseChangeFromFilename(name));
    }
    return migrations;
  }

  @Override
  public Reader getScriptReader(Change change, boolean undo) {
    scanScriptsDir();
    File file = scripts.get(change.getFilename());
    try {
      return new MigrationReader(file, charset, undo, variables);
    } catch (IOException e) {
      throw new MigrationException("Error reading " + file, e);
    }
  }

  protected void scanScriptsDir() {
    if (scripts == null) {
      if (!scriptsDir.exists() || !scriptsDir.isDirectory()) {
        throw new MigrationException("Scripts directory does not exist.");
      }
      scripts = new TreeMap<>();
      collectScripts(scripts, scriptsDir);
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