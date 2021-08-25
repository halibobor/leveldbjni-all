package org.fusesource.leveldbjni.load.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class NativeUtils {

  public static final String NATIVE_FOLDER_PATH_PREFIX = "native-utils.";
  /**
   * The minimum length a prefix for a file has to
   * have according to {@link File#createTempFile(String, String)}}.
   */
  private static final int MIN_PREFIX_LENGTH = 3;
  /**
   * Temporary directory which will contain the DLLs.
   */
  private static File temporaryDir;

  /**
   * Private constructor - this class will never be instanced
   */
  private NativeUtils() {
  }

  public static synchronized void loadLibraryFromJar(String path) throws IOException {
    loadLibraryFromJar(path, null);
  }

  /**
   * The file from JAR is copied into system temporary directory and then loaded.
   * The temporary file is deleted after
   * exiting.
   * Method uses String as filename because the pathname is "abstract", not system-dependent.
   *
   * @param path      lib path，must start with '/'
   * @param loadClass load lib {@link ClassLoader}，if null,use NativeUtils.class
   * @throws IOException           io error
   * @throws FileNotFoundException not find error
   */
  public static synchronized void loadLibraryFromJar(String path, Class<?> loadClass)
      throws IOException {

    if (null == path || !path.startsWith("/")) {
      throw new IllegalArgumentException("The path has to be absolute (start with '/').");
    }

    // Obtain filename from path
    String[] parts = path.split("/");
    String filename = (parts.length > 1) ? parts[parts.length - 1] : null;

    // Check if the filename is okay
    if (filename == null || filename.length() < MIN_PREFIX_LENGTH) {
      throw new IllegalArgumentException("The filename has to be at least 3 characters long.");
    }

    if (temporaryDir == null) {
      temporaryDir = createTempDirectory(NATIVE_FOLDER_PATH_PREFIX);
      temporaryDir.deleteOnExit();
    }

    File temp = new File(temporaryDir, filename);
    Class<?> clazz = loadClass == null ? NativeUtils.class : loadClass;

    try (InputStream is = clazz.getResourceAsStream(path)) {
      Files.copy(is, temp.toPath(), StandardCopyOption.REPLACE_EXISTING);
    } catch (IOException e) {
      temp.delete();
      throw e;
    } catch (NullPointerException e) {
      temp.delete();
      throw new FileNotFoundException("File " + path + " was not found inside JAR.");
    }

    try {
      System.load(temp.getAbsolutePath());
      System.out.println("******** load [" + temp.getAbsolutePath() + "] success ********");
    } finally {

      temp.deleteOnExit();
    }
  }

  private static File createTempDirectory(String prefix) throws IOException {
    String tempDir = System.getProperty("java.io.tmpdir");
    File generatedDir = new File(tempDir, prefix + System.nanoTime());

    if (!generatedDir.mkdir()) {
      throw new IOException("Failed to create temp directory " + generatedDir.getName());
    }
    return generatedDir;
  }
}