package gorm.tools.async;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;

/**
 * see https://stackoverflow.com/a/59444016/6500859
 * Java 11 fix for the class loader and ClassNotFoundExceptions problems.
 * Before jdk9 ForkJoinPool.common() returns an Executor with a ClassLoader of your main Thread, in Java 9 this behave
 * changes, and return an executor with the system jdk system classloader. So it's easy to find ClassNotFoundExceptions
 * inside CompletableFutures code while upgrading from Java 8 to Java 9 / 10 / 11, due to this change.
 */
public class ClassLoaderThreadFactory implements ForkJoinPool.ForkJoinWorkerThreadFactory {

    @Override
    public final ForkJoinWorkerThread newThread(ForkJoinPool pool) {
        return new CommonForkJoinWorkerThread(pool);
    }

    private static class CommonForkJoinWorkerThread extends ForkJoinWorkerThread {

        private CommonForkJoinWorkerThread(final ForkJoinPool pool) {
            super(pool);
            // set the correct classloader here
            setContextClassLoader(Thread.currentThread().getContextClassLoader());
        }
    }
}
