package gorm.tools.utils

import javax.servlet.http.HttpServletRequest
import java.text.DecimalFormat

import groovy.transform.CompileStatic

@CompileStatic
@SuppressWarnings(['Println']) //for now
class BenchmarkHelper {

    private static final ThreadLocal<Long> START_TIME_HOLDER = new ThreadLocal<Long>()

    static void printMemory(){
        int mb = 1024*1024;

        //Getting the runtime reference from system
        Runtime runtime = Runtime.getRuntime();

        println("---- Heap utilization statistics [MB] -----")

        //Print used memory
        println("Used Memory:" + (runtime.totalMemory() - runtime.freeMemory()) / mb)

        //Print free memory
        println("Free Memory:" + runtime.freeMemory() / mb)

        //Print total available memory
        println("Total Memory:" + runtime.totalMemory() / mb)

        //Print Maximum available memory
        println("Max Memory:" + runtime.maxMemory() / mb)
    }

    static void printUsedMem(){
        int mb = 1024*1024;

        //Getting the runtime reference from system
        Runtime runtime = Runtime.getRuntime();
        //Print used memory
        println("Used Memory:" + (runtime.totalMemory() - runtime.freeMemory()) / mb)
    }

    /**
     * returns the currentTimeMillis and sets it in a ThreadLocal so ti can be used in endTime if not passed
     */
    static Long startTime(){
        Long startTime = System.currentTimeMillis()
        START_TIME_HOLDER.set(startTime)
        return startTime
    }

    /**
     * clears the START_TIME_HOLDER thread local
     *
     * @param msg the message to append the calced time to
     * @param startTime optional if startTime()
     * @return the mesage
     */
    static String endTimeMsg(String msg, Long startTime = null){
        if(startTime == null) startTime = START_TIME_HOLDER.get()
        DecimalFormat decFmt = new DecimalFormat("0.0")
        BigDecimal endTime = (System.currentTimeMillis() - startTime) / 1000
        String timing = "${decFmt.format(endTime)}s"
        START_TIME_HOLDER.remove()
        return "$msg -- $timing"
    }

    /**
     * does a println with endTimeMsg. Quick way to get message but not recomended in produciton code
     * recomended to wrap endTimeMsg in log.debug or other logger
     * example:
     *   BenchmarkHelper.startTime()
     *   log.debug( BenchmarkHelper.endTimeMsg("foo completed in ) )
     */
    static String printEndTimeMsg(String msg, Long startTime = null){
        def emsg = endTimeMsg(msg, startTime)
        println emsg
        return emsg
    }
}
