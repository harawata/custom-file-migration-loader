package test;

import static org.junit.Assert.*;

import java.io.File;
import java.math.BigDecimal;
import java.util.List;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.migration.Change;
import org.junit.Test;

public class CustomFileMigrationLoaderTest {
  @Test
  public void shouldLoadScriptsInSubDirectories() throws Exception {
    File scriptDir = Resources.getResourceAsFile("case1");
    CustomFileMigrationLoader migrationLoader = new CustomFileMigrationLoader(scriptDir, "utf-8", null);
    List<Change> migrations = migrationLoader.getMigrations();
    assertEquals(4, migrations.size());
    assertEquals(BigDecimal.valueOf(100), migrations.get(0).getId());
    assertEquals(BigDecimal.valueOf(101), migrations.get(1).getId());
    assertEquals(BigDecimal.valueOf(102), migrations.get(2).getId());
    assertEquals(BigDecimal.valueOf(103), migrations.get(3).getId());
  }
}
