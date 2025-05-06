package yakworks.rally.s3

import java.nio.file.FileSystem
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.Bucket
import software.amazon.awssdk.services.s3.model.ListBucketsResponse
import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Specification

import static software.amazon.nio.spi.s3.config.S3NioSpiConfiguration.AWS_ACCESS_KEY_PROPERTY
import static software.amazon.nio.spi.s3.config.S3NioSpiConfiguration.AWS_SECRET_ACCESS_KEY_PROPERTY

/**
 * sanity check that messages are picked up in plugins
 * WIP playground, nothing if fully worked out yet.
 * see https://github.com/awslabs/aws-java-nio-spi-for-s3/tree/main
 */
@Ignore
class LinodeS3Spec extends Specification  {

    @Shared S3Client client
    @Shared Map<String, String> env
    //DO NOT CHECK THESE IN
    static String accessKey = "8CL...."
    static String secretKey = "EKC...."

    void setupSpec() {
        // System.setProperty("aws.accessKeyId", accessKey)
        // System.setProperty("aws.secretKey", secretKey)
        // System.setProperty(AWS_REGION_PROPERTY, "us-east-1");
        // System.setProperty(AWS_ACCESS_KEY_PROPERTY, accessKey);
        // System.setProperty(AWS_SECRET_ACCESS_KEY_PROPERTY, secretKey);
        env = [
            (AWS_ACCESS_KEY_PROPERTY): accessKey,
            (AWS_SECRET_ACCESS_KEY_PROPERTY): secretKey
        ]

        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);

        client = S3Client.builder()
            .endpointOverride(new URI("https://us-southeast-1.linodeobjects.com"))
            .credentialsProvider(() -> credentials)
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

    void "smoke test S3Client"(){
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
        FileSystem fs = FileSystems.newFileSystem(URI.create("s3x://us-southeast-1.linodeobjects.com/wtf"), env);

        Path p = fs.getPath("krtest2/")
        assert Files.isDirectory(p)
        //assert p.exists()
        Files.walk(p).forEach(System.out::println)
        for (Path rootDir : fs.getRootDirectories()) {
            Files.walk(rootDir).forEach(System.out::println);
        }

        then:
        fs
        p

        cleanup:
        fs.close();
    }

    //NOT WORKING YET. Not clear yet why. Next steps would be to do the example with the S3WritableByteChannel deal.
    void "nio test write WIP"(){
        when:

        FileSystem fs = FileSystems.newFileSystem(URI.create("s3x://us-southeast-1.linodeobjects.com/wtf"), env);
        Path p1 = fs.getPath("nio-testing/")
        assert Files.isDirectory(p1)
        Path p2 = p1.resolve("foo.txt")
        assert !p2.exists()
        //this doesnt work
        //p2.text = "testing 123"
        String s = "testing 123"
        try (BufferedWriter writer = Files.newBufferedWriter(p2, StandardOpenOption.CREATE)) {
            writer.write(s, 0, s.length());
            writer.flush();
        }
        //assert p2.exists()

        Path p = fs.getPath("krtest2/")
        assert Files.isDirectory(p)
        //assert p.exists()
        Files.walk(p).forEach(System.out::println)
        for (Path rootDir : fs.getRootDirectories()) {
            Files.walk(rootDir).forEach(System.out::println);
        }

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
