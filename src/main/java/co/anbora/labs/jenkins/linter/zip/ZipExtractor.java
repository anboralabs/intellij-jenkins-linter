package co.anbora.labs.jenkins.linter.zip;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public abstract class ZipExtractor {
    public static void extractZip(String zipName, Path outputDirectory) throws IOException {
        InputStream zipInputStream = ZipExtractor.class.getClassLoader().getResourceAsStream("tool/"+zipName);
        if (zipInputStream == null) {
            throw new FileNotFoundException("No se encontró el archivo: " + zipName);
        }

        // Crear el directorio de salida si no existe
        Files.createDirectories(outputDirectory);

        // Leer y extraer los archivos del ZIP
        try (ZipInputStream zis = new ZipInputStream(zipInputStream)) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                Path entryPath = outputDirectory.resolve(entry.getName());

                if (entry.isDirectory()) {
                    // Crear directorios si es necesario
                    Files.createDirectories(entryPath);
                } else {
                    // Crear archivo y escribir contenido
                    Files.createDirectories(entryPath.getParent()); // Asegurar que los directorios existen
                    try (OutputStream os = Files.newOutputStream(entryPath)) {
                        byte[] buffer = new byte[1024];
                        int bytesRead;
                        while ((bytesRead = zis.read(buffer)) != -1) {
                            os.write(buffer, 0, bytesRead);
                        }
                    }
                }
                zis.closeEntry();
            }
        }
    }
}
