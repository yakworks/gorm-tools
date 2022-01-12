/*
* Copyright 2022 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.commons.io

import groovy.transform.CompileStatic

import org.codehaus.groovy.runtime.DefaultGroovyMethodsSupport

@CompileStatic
class IOUtils {
    public static final int BUFFER_SIZE = 4096

    /**
     * flush the writer, ignoring IOException. then use groovy's closeWithWarning
     * @param writer the writer to flush close
     */
    @SuppressWarnings("EmptyCatchBlock")
    static void flushAndClose(Writer writer){
        try {
            writer.flush();
        } catch (IOException e) {
            // try to continue even in case of error
        }
        DefaultGroovyMethodsSupport.closeWithWarning(writer);
    }

    /**
     * Copy the contents of the given InputStream to the given OutputStream.
     * Closes both streams when done.
     * @param in the stream to copy from
     * @param out the stream to copy to
     * @return the number of bytes copied
     * @throws IOException in case of I/O errors
     */
    @SuppressWarnings("EmptyCatchBlock")
    static int copy(InputStream ins, OutputStream outs) throws IOException {
        assert ins != null : "No input stream specified"
        assert outs != null : "No output stream specified"
        try {
            int byteCount = 0
            byte[] buffer = new byte[BUFFER_SIZE]
            int bytesRead = -1
            while ((bytesRead = ins.read(buffer)) != -1) {
                outs.write(buffer, 0, bytesRead)
                byteCount += bytesRead
            }
            outs.flush();
            return byteCount
        }
        finally {
            try {
                ins.close()
            }
            catch (IOException ex) {
            }
            try {
                outs.close()
            }
            catch (IOException ex) {
            }
        }
    }
}
