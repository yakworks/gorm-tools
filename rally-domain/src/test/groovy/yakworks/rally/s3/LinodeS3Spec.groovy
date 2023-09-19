package yakworks.rally.s3

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.Bucket
import software.amazon.awssdk.services.s3.model.ListBucketsResponse
import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Specification
import yakworks.i18n.icu.ICUMessageSource
import yakworks.testing.gorm.unit.DataRepoTest

import static org.carlspring.cloud.storage.s3fs.S3Factory.ACCESS_KEY;
import static org.carlspring.cloud.storage.s3fs.S3Factory.SECRET_KEY;

/**
 * sanity check that messages are picked up in plugins
 */
@Ignore
class LinodeS3Spec extends Specification  {

    @Shared S3Client client

    static String accessKey = "PUT IN DOT ENV"
    static String secretKey = "PUT IN DOT ENV"

    void setupSpec() {
        System.setProperty("aws.accessKeyId", accessKey)
        System.setProperty("aws.secretKey", secretKey)
        //AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);

        client = S3Client.builder()
            .endpointOverride(new URI("https://us-southeast-1.linodeobjects.com"))
            //.credentialsProvider(StaticCredentialsProvider.create(credentials))
            .serviceConfiguration(srvcConf -> {
                srvcConf.pathStyleAccessEnabled();
            })
            .region(Region.US_EAST_1) // this is not used, but the AWS SDK requires it
            .build();


    }

    FileSystem s3FileSystem() {
        Map<String, String> env = new HashMap<>();
        env.put(ACCESS_KEY, accessKey);
        env.put(SECRET_KEY, secretKey);

        return FileSystems.newFileSystem(URI.create("s3:///"),
            env,
            Thread.currentThread().getContextClassLoader());
    }

    void "smoke test"(){
        when:
        client
        ListBucketsResponse lbResponse = client.listBuckets();
        for (Bucket bucket : lbResponse.buckets()) {
            System.out.println(bucket.name() + "\t" + bucket.creationDate());
        }
        then:
        client
    }

    void "nio test"(){
        when:
        //FileSystem fs = FileSystems.newFileSystem("s3://us-southeast-1.linodeobjects.com/wtf", [:]);
        FileSystem fs = FileSystems.newFileSystem(URI.create("s3://us-southeast-1.linodeobjects.com"), Collections.EMPTY_MAP, Thread.currentThread().getContextClassLoader());
        Path p = fs.getPath("/wtf/krtest2")
        assert p.exists()
        Files.walk(p).forEach(System.out::println)
        // for (Path rootDir : fs.getRootDirectories()) {
        //     Files.walk(rootDir).forEach(System.out::println);
        // }
        then:
        fs
        p

        cleanup:
        fs.close();
    }

    void "nio write test"(){
        when:
        //FileSystem fs = FileSystems.newFileSystem("s3://us-southeast-1.linodeobjects.com/wtf", [:]);
        FileSystem fs = FileSystems.newFileSystem(URI.create("s3://us-southeast-1.linodeobjects.com"), Collections.EMPTY_MAP, Thread.currentThread().getContextClassLoader());
        Path p = Path.of(URI.create("s3://us-southeast-1.linodeobjects.com/wtf/adir/foo.txt"))
        assert !p.exists()
        p.text = "it worked"
        assert p.exists()

        then:
        p
    }
}
