package cm.homeautomation.helper;

import de.a9d3.testing.checks.CheckInterface;
import de.a9d3.testing.executer.Executor;
import de.a9d3.testing.executer.exception.CheckFailedException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;

public class MultiThreadExecutor implements Executor {

    private static final Logger LOGGER = Logger.getLogger(MultiThreadExecutor.class.getName());

    private static String executionLogToString(Class testClass, Map<String, String> executionLog) {
        StringBuilder builder = new StringBuilder();
        builder.append("Tested ");
        builder.append(testClass.getName());
        builder.append("\n");

        executionLog.forEach((check, log) -> {
            builder.append(check);
            builder.append(": ");
            builder.append(log);
            builder.append("\n");
        });

        return builder.toString();
    }

    public Boolean execute(Class c, List<CheckInterface> checks) {
        Map<String, String> executionLog = new HashMap<>();
        Map<String, Boolean> testStatusLog = new HashMap<>();
        boolean failed = false;

        CountDownLatch countDownLatch = new CountDownLatch(checks.size());

        for (CheckInterface check : checks) {

            Runnable checkThread = () -> {

                boolean result = check.check(c);
                testStatusLog.put(check.getClass().getName(), result);
                executionLog.put(check.getClass().getName(), result ? "Passed ✔️" : "Failed ❌");

                countDownLatch.countDown();
            };

            new Thread(checkThread).start();

        }

        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
        }

        for (Boolean testStatus : testStatusLog.values()) {
            if (!testStatus.booleanValue()) {
                failed=true;
            }
        }

        if (failed) {
            throw new CheckFailedException(executionLogToString(c, executionLog));
        }

        LOGGER.info(() -> executionLogToString(c, executionLog));
        return true;
    }

}