/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.commons.io

import java.nio.channels.FileLock
import java.util.zip.Deflater
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

import groovy.text.SimpleTemplateEngine
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import yakworks.commons.lang.DateUtil

/*
 * Util methods for file processing such as deleting temp files or zipping
 */
@CompileStatic
@Slf4j
class FileUtil {

    /**
     * Deletes a file from specified filePath
     */
    static void deleteTempFile(Object filePath) {
        File file = filePath instanceof File ? (File)filePath : new File((String)filePath)
        delete(file)
    }

    /**
     * Deletes all files present in directory specified by directotyPath and which are older than 1 hour
     */
    static void deleteAllTempFiles(Object directoryPath) {
        File directory = directoryPath instanceof File ? (File)directoryPath : new File((String)directoryPath)
        if (directory.exists()) {
            File[] filesList = directory.listFiles()
            filesList.each {File file ->
                def lastModifiedFileDate = new Date(file.lastModified())
                def diffHours = DateUtil.getDateDifference_inHours(lastModifiedFileDate)
                if (diffHours >= 1) {
                    // If time difference between current time and last modified file time is 1 hr
                    // or greater than 1 hour then delete that file
                    delete(file)
                }
            }
        }
    }

    /**
     * Deletes a file
     */
    static void delete(File file) {
        if (file.exists()) {
            FileOutputStream fos = new FileOutputStream(file.getPath())
            FileLock fl = fos.getChannel().tryLock()
            if (fl) {
                fl.release()
            }
            fos.close()
            file.delete()
        }
    }

    /**
     * Gets extension from name
     */
    static String extractExtension(String name) {
        return name.substring(name.lastIndexOf('.') + 1)
    }

    /**
     * Gets extension from file name
     */
    // static String extractExtension(MultipartFile file) {
    //     String filename = file.getOriginalFilename()
    //     return extractExtension(filename)
    // }
    //
    // /**
    //  * Gets mime type from file
    //  */
    // static String extractMimeType(MultipartFile file) {
    //     return file.getContentType()
    // }

    /**
     * Gets mime type from file
     */
    static String extractMimeType(File file) {
        return URLConnection.guessContentTypeFromName(file.name)
    }

    /**
     * Gets mime type from file name
     */
    static String extractMimeType(String fileName) {
        return URLConnection.guessContentTypeFromName(fileName)
    }

    /**
     * Gets name without extension
     */
    static String extractNameWithoutExtension(String name) {
        return name.substring(0, name.lastIndexOf('.'))
    }

    /**
     * Accepts a string which is formatted like a GString, and a binding map for values.  Parses the values and
     * returns a string based on those values.  An example of where this would be used is Config.groovy, where the
     * values of the GString have not yet been created.
     */
    static String parseStringAsGString(String theString, Map binding) {
        def engine = new SimpleTemplateEngine()
        def template = engine.createTemplate(theString)
        def result = template.make(binding).toString()
        return result
    }

    /**
     * Generates txt file, swallows any Exception and returns null if error
     */
    @SuppressWarnings(['CatchException'])
    static File generateTxtFile(String content, String fileName, String fileLocation) {
        generateTxtFile(content, fileName, new File(fileLocation))
    }

    /**
     * Generates txt file, swallows any Exception and returns null if error
     */
    @SuppressWarnings(['CatchException'])
    static File generateTxtFile(String content, String fileName, File fileDirectory) {
        if (!fileDirectory.exists()) {
            fileDirectory.mkdir()
        }
        def thefile = new File(fileDirectory, "${fileName}.txt")
        thefile.withWriter {
            it.write(content) //or writeLine
        }
        return thefile
    }

    /**
     * Unzip files to specified directory
     *
     */
    static List<File> unzip(File zip, File destDir) {
        List files = []
        if (!destDir.exists()) {
            destDir.mkdir()
        }

        ZipInputStream zis = new ZipInputStream(new FileInputStream(zip))
        ZipEntry zepEntry

        while ((zepEntry = zis.nextEntry) != null) {
            String fileName = zepEntry.name
            File newFile = new File(destDir, fileName)
            newFile.getParentFile().mkdirs() //ensure all dirs are created
            if(zepEntry.isDirectory()) newFile.mkdir()
            else {
                FileOutputStream fos = new FileOutputStream(newFile)

                fos << zis
                fos.flush()
                fos.close()
                files << newFile
            }
        }
        zis.closeEntry()
        zis.close()
        return files
    }

    /**
     * Zips file
     */
    static File zip(File file, File destDir = null) {
        assert file.exists()
        if (!destDir) destDir = file.parentFile
        File zip = new File(destDir, changeExtension(file.name, 'ZIP'))
        FileOutputStream fout = new FileOutputStream(zip)
        ZipOutputStream zout = new ZipOutputStream(fout)
        zout.setLevel(Deflater.BEST_COMPRESSION)
        ZipEntry entry = new ZipEntry(file.name)
        zout.putNextEntry(entry)

        file.withInputStream { fin ->
            zout << fin
        }

        zout.closeEntry()
        zout.close()
        return zip
    }

    /**
     * Changes extension for file name
     */
    static String changeExtension(String name, String newExtension) {
        String changed = name.substring(0, name.lastIndexOf('.')) + '.' + newExtension
        return changed
    }

    static void writeMapToSortedProperties(Map map, File toFile, boolean deleteIfExists = true) {
        if (toFile.exists()) {
            if (deleteIfExists) {
                toFile.delete()
            }
        }
        // TODO Finish this
    }
}
