package yakworks.commons.io

import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.StandardCopyOption
import java.nio.file.attribute.BasicFileAttributes

import yakworks.commons.lang.Validate

import static java.nio.file.FileVisitOption.FOLLOW_LINKS

/**
 * Utility methods for copying or deleting from the file system.
 */
abstract class FileSystemUtils {

	/**
	 * Recursively delete any nested directories or files as well.
	 */
	static boolean deleteRecursively(Path root) {
		if (!root) return false

		if (!Files.exists(root)) return false

		Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
			@Override
            FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
				Files.delete(file)
				return FileVisitResult.CONTINUE
			}
			@Override
            FileVisitResult postVisitDirectory(Path dir, IOException exc) {
				Files.delete(dir)
				return FileVisitResult.CONTINUE
			}
		})
		return true
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

}
