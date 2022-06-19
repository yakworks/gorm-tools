package yakworks.commons.io

import org.springframework.util.FileCopyUtils
import yakworks.commons.lang.DateUtil
import spock.lang.Specification
import yakworks.commons.lang.Validate
import yakworks.commons.util.BuildSupport

class FileUtilTests extends Specification {

    File tempDir

    void setup() {
        File t1 = new File(System.getProperty('java.io.tmpdir'), System.getProperty('user.name'))
        tempDir = new File(t1, 'FileUtilTestTemp')
    }

    void testDeleteTempFile_file(){
        when:
        //Creating tempFile in tempUpload directory
        def tempFileName = addTempFileAndGetFileName()
        FileUtil.deleteTempFile(new File(tempDir,tempFileName))
        //Checking whether the temp file got deleted
        File deletedTempFile = new File(tempDir,tempFileName)

        then:
        !deletedTempFile.exists()
    }

    void testDeleteTempFile_string(){
        setup:
        //Creating tempFile in tempUpload directory
        def tempFileName = addTempFileAndGetFileName()
        def fileString = tempDir.path + File.separator + tempFileName

        when:
        FileUtil.deleteTempFile(fileString)
        //Checking whether the temp file got deleted
        File deletedTempFile = new File(tempDir,tempFileName)

        then:
        !deletedTempFile.exists()
    }

    void testDeleteAllTempFiles(){
        setup:
        //Creating tempFile in tempUpload directory
        addTempFileAndGetFileName()

        when:
        FileUtil.deleteAllTempFiles(tempDir)
        //Checking whether all temp files from tempDir got deleted or not
        File directory = tempDir

        then:
        if(directory.exists()){
            directory.listFiles().each { file ->
                def diffHours = DateUtil.getDateDifference_inHours(new Date(file.lastModified()))
                assert diffHours < 1 //Checking that if any file exists in tempDir then it should not be older than 1 hour
            }
        }
    }

    void testGenerateTxtFile() {
        when:
        File someFile = File.createTempFile('nothing','.txt')
        someFile.deleteOnExit()
        File dir = someFile.parentFile
        FileUtil.generateTxtFile('Hello', 'world', dir.absolutePath)
        File theFile = new File(dir, 'world.txt')

        then:
        theFile.exists()

        cleanup:
        theFile.delete()
        dir.delete()
    }

    def addTempFileAndGetFileName(){
        File dir = tempDir
        if (!dir.exists()) {
            dir.mkdirs();
        }
        def filename = UUID.randomUUID()?.toString()
        File file = FileUtil.generateTxtFile('foo', filename, tempDir)
        return file.name
    }

    void "test extractMimeType"() {
        expect:
        mimeType == FileUtil.extractMimeType(fileName)

        where:
        fileName     |  mimeType
        'foo.pdf'    |  'application/pdf'
        'foo.png'    |  'image/png'
        'foo.txt'    |  'text/plain'
        'foo.doc'    |  'application/msword'
        'foo.docx'   |  'application/vnd.openxmlformats-officedocument.wordprocessingml.document'
        'foo.xls'    |  'application/vnd.ms-excel'
        'foo.xlsx'    |  'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
        'foo.flub'    |  'application/octet-stream'
    }
}
