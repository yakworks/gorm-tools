/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.commons.io

import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.SimpleFileVisitor
import java.nio.file.StandardCopyOption
import java.nio.file.attribute.BasicFileAttributes

import groovy.transform.CompileStatic

import org.apache.commons.io.file.PathUtils

import yakworks.commons.lang.Validate

import static java.nio.file.FileVisitOption.FOLLOW_LINKS

/**
 * Utility methods for copying or deleting from the file system.
 */
@CompileStatic
abstract class PathTools {

    /**
     * Deletes a directory recursively. does not throw error, returns false on IOException
     */
    static boolean deleteDirectory(Path root) {
        if (root == null) {
            return false
        }
        try {
            return PathUtils.deleteDirectory(root)
        }
        catch (IOException ex) {
            return false
        }
    }

    /**
     * Deletes a directory recursively. does not throw error, returns false on IOException
     */
    static boolean delete(Path fileOrDir) {
        if (fileOrDir == null) return false

        try {
            return PathUtils.delete(fileOrDir)
        }
        catch (IOException ex) {
            return false
        }
    }

    /**
     * Recursively copy the contents of the {@code src} file/directory
     * to the {@code dest} file/directory.
     */
    static void copyRecursively(Path src, Path dest) {
        Validate.notNull(src, '[src]')
        Validate.notNull(src, '[dest]')

        BasicFileAttributes srcAttr = Files.readAttributes(src, BasicFileAttributes)

        if (srcAttr.isDirectory()) {
            Files.walkFileTree(src, EnumSet.of(FOLLOW_LINKS), Integer.MAX_VALUE, new SimpleFileVisitor<Path>() {
                @Override
                FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                    Files.createDirectories(dest.resolve(src.relativize(dir)))
                    return FileVisitResult.CONTINUE
                }
                @Override
                FileVisitResult visitFile(Path file, BasicFileAttributes attrs)  {
                    Files.copy(file, dest.resolve(src.relativize(file)), StandardCopyOption.REPLACE_EXISTING)
                    return FileVisitResult.CONTINUE
                }
            })
        }
        else if (srcAttr.isRegularFile()) {
            Files.copy(src, dest)
        }
        else {
            throw new IllegalArgumentException("Source File must denote a directory or file")
        }
    }

    /**
     * Files.createDirectories to ensure its created, checks if exists first.
     */
    static Path createDirectories(Path dir){
        if(!Files.exists(dir)) return Files.createDirectories(dir)
        return dir
    }

    static String getBaseName(String filename) {
        return getBaseName(Paths.get(filename))
    }

    /**
     * get base filename without extension for path
     * 'foo'->'foo', 'foo.txt'->'foo', 'foo.tar.gz'->'foo.tar'
     * @return the base name. will be same if no ext exists.
     * @see #getBaseName
     */
    static String getBaseName(Path path) {
        return removeFileExtension(path.fileName.toString())
    }

    /**
     * Strips the
     * @param filename the filename to remove ext
     * @param removeAllExtensions if true then foo.tar.gz will return foo
     * @return the name without extenstion
     */
    static String removeFileExtension(String filename, boolean removeAllExtensions = false) {
        if (filename == null || filename.isEmpty()) return filename
        return filename.replaceAll(extensionPattern(removeAllExtensions), "")
    }

    /**
     * regex pattern to get extension from filename string ,
     * if wholeExtension is true then will match the .tar.gz in foo.tar.gz instead of just the .gz
     */
    static String extensionPattern(boolean wholeExtension = false) {
        return '(?<!^)[.]' + (wholeExtension ? '.*' : '[^.]*$')
    }

    /**
     * Gets extension from name
     * @return null if nothing found of the extension
     */
    static String getExtension(String fileName) {
        if (!fileName) return ''
        getExtension(Paths.get(fileName))
    }

    static String getExtension(Path path, boolean wholeExtension = false) {
        if (!path) return ''
        String fileName = path.fileName
        def match = (fileName =~ extensionPattern(wholeExtension))

        //fist char will be dot so just remove it
        return match.size() ? (match[0] as String).substring(1) : null
    }

    static String extractMimeType(String filename) {
        extractMimeType(Paths.get(filename))
    }
    /**
     * Gets mime type from file name
     */
    static String extractMimeType(Path path) {
        String fileName = path.fileName
        String mimeType = URLConnection.guessContentTypeFromName(fileName)
        if(!mimeType) {
            // see if its word or excel as they are the most common that are not mapped
            Map mimeMap = [
                doc: 'application/msword', docx: 'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
                xls: 'application/vnd.ms-excel', xlsx: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
            ]
            String exten = getExtension(fileName)
            if(mimeMap.containsKey(exten)) return mimeMap[exten]
        }
        return mimeType ?: 'application/octet-stream'
    }

    /**
     * Changes extension for file name
     */
    static String changeExtension(String name, String newExtension) {
        String changed = name.substring(0, name.lastIndexOf('.')) + '.' + newExtension
        return changed
    }
}
