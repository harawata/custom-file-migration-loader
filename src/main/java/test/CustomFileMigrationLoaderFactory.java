package test;

import org.apache.ibatis.migration.Environment;
import org.apache.ibatis.migration.FileMigrationLoaderFactory;
import org.apache.ibatis.migration.MigrationLoader;
import org.apache.ibatis.migration.options.SelectedPaths;

public class CustomFileMigrationLoaderFactory implements FileMigrationLoaderFactory {
  @Override
  public MigrationLoader create(SelectedPaths paths, Environment environment) {
    return new CustomFileMigrationLoader(paths.getScriptPath(), environment.getScriptCharset(),
        environment.getVariables());
  }
}
