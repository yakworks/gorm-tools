/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.commons.io

import java.nio.charset.Charset
import java.nio.file.Path
import java.util.zip.Deflater
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import net.lingala.zip4j.ZipFile
import net.lingala.zip4j.io.inputstream.ZipInputStream

/*
 * Util methods for file processing such as deleting temp files or zipping
 */

@CompileStatic
@Slf4j
class ZipUtils {

    /**
     * Unzip files to specified directory
     */
    static unzip(Path zip, Path destDir) {
        new ZipFile(zip.toFile()).extractAll(destDir.toString())
    }

    /**
     * Returns input stream for a specific file inside zip if exists
     */
    static InputStream getZipEntryInputStream(File zip, String entryName) {
        java.util.zip.ZipFile zipFile = new java.util.zip.ZipFile(zip)
        ZipEntry entry = zipFile.getEntry(entryName)
        if(entry) return zipFile.getInputStream(entry)
        return null
    }

    // static InputStream getZipEntryInputStream(InputStream inputStream, String entryName) {
    //     ZipInputStream zip = new ZipInputStream(inputStream);
    //     var zipis = new java.util.zip.ZipInputStream(inputStream)
    //     zipis.get
    //     FileHeader fileHeader = zip.getFileHeader("entry_name_in_zip.txt");
    //     InputStream inputStream = zipFile.getInputStream(fileHeader);
    //
    //     getZipEntryInputStream(zip.toFile(), entryName)
    // }

    /**
     * Zips multiple files into single zip
     */
    static File zip(String zipName, File destinationDir, File[] files) {
        if(!files) return

        if (!destinationDir) destinationDir = files[0].parentFile
        File zip = new File(destinationDir, zipName)
        FileOutputStream fout = new FileOutputStream(zip)
        ZipOutputStream zout = new ZipOutputStream(fout)
        zout.setLevel(Deflater.BEST_COMPRESSION)

        Closure addZipEntry
        addZipEntry = { ZipOutputStream zoutStream, File fileToZip, String parent ->
            if (fileToZip == null || !fileToZip.exists()) return
            String zipEntryName = fileToZip.getName()
            if (parent!=null && !parent.isEmpty()) {
                zipEntryName = parent + "/" + fileToZip.getName()
            }

            if (fileToZip.isDirectory()) {
                for (File file : fileToZip.listFiles()) {
                    addZipEntry(zoutStream, file, zipEntryName);
                }
            } else {
                ZipEntry entry = new ZipEntry(zipEntryName)
                zoutStream.putNextEntry(entry)
                fileToZip.withInputStream { fin ->
                    zoutStream << fin
                }
                zoutStream.closeEntry()
            }
        }


        files.each { File f ->
            addZipEntry(zout, f, null)
        }
        zout.close()
        return zip
    }

    /**
     * Zips given file
     */
    static File zip(File file, File destDir = null) {
        assert file.exists()
        if (!destDir) destDir = file.parentFile
        String name = PathTools.changeExtension(file.name, 'zip')
        return zip(name, destDir, file)
    }
}
