package cm.homeautomation.helper;

import de.a9d3.testing.checks.CheckInterface;
import de.a9d3.testing.method.GetterSetterMatcher;
import de.a9d3.testing.method.IsSetterMatcher;
import de.a9d3.testing.method.MethodMatcherInterface;
import de.a9d3.testing.testdata.TestDataProvider;
import de.a9d3.testing.tuple.MethodTuple;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GetterIsSetterCheck implements CheckInterface {
    private static final Logger LOGGER = Logger.getLogger(GetterIsSetterCheck.class.getName());

    private TestDataProvider provider;
    private String regexExcluded;
    private String seed;

    /**
     * Getter/Is and Setter methods are often overlooked and annoying to test. This
     * checkClass will execute setter and getter/is methods and compare the results.
     * Initialize empty, with custom TestDataProvider and/or regexExclude String
     * (excludes certain getter/setter pairs)
     */
    public GetterIsSetterCheck() {
        this("");
    }

    public GetterIsSetterCheck(String regexExcluded) {
        this(new TestDataProvider(), regexExcluded);
    }

    public GetterIsSetterCheck(TestDataProvider provider) {
        this(provider, "");
    }

    public GetterIsSetterCheck(TestDataProvider provider, String regexExcluded) {
        this.provider = provider;
        this.regexExcluded = regexExcluded;
        this.seed = "48107951-0256-4a84-9f8e-132ee651ae9e";
    }

    @Override
    public boolean check(Class c) {
        MethodMatcherInterface getterSetterMatcher = new GetterSetterMatcher();
        MethodMatcherInterface isSetterMatcher = new IsSetterMatcher();

        Pattern pattern = Pattern.compile(regexExcluded);

        List<MethodTuple> tuples = Stream
                .concat(getterSetterMatcher.match(c).stream(), isSetterMatcher.match(c).stream())
                .filter(tuple -> !(pattern.matcher(tuple.getA().getName()).matches()
                        && pattern.matcher(tuple.getB().getName()).matches()))
                .collect(Collectors.toList());

        return check(c, tuples);
    }

    public boolean check(Class c, List<MethodTuple> tuples) {
        Object instance = provider.fill(c, "test", true);

        CountDownLatch countDownLatch = new CountDownLatch(tuples.size());

        List<Boolean> testTuplesLog = new ArrayList<>();

        for (int i = 0; i < tuples.size(); i++) {
            MethodTuple tuple = tuples.get(i);
            final int a=i;

            Runnable checkThread = () -> {
                System.out.println("running test: "+tuple.getA().getName()+"/"+tuple.getB().getName());
                if (checkIfGetterReturnSetValue(tuple, instance, a)) {
                    testTuplesLog.add(Boolean.FALSE);
                }

                countDownLatch.countDown();
                ;
            };
            new Thread(checkThread).start();
        }

        for (Boolean testTuplesStatus : testTuplesLog) {
            if (!testTuplesStatus.booleanValue()) {
                return false;
            }
        }

        return true;
    }

    private boolean checkIfGetterReturnSetValue(MethodTuple tuple, Object instance, int i) {
        Object data = provider.fill(tuple.getB().getParameterTypes()[0], seed + i, true);
        if (data == null) {
            System.out.println("data is null while calling " + instance.getClass().getCanonicalName() + "."
                    + tuple.getB().getName() + ". Please provide test data for this method.");
            return true;
        } else {
            try {
                tuple.getB().invoke(instance, data);

                if (!data.equals(tuple.getA().invoke(instance))) {
                    CheckHelperFunctions.logFailedCheckStep(LOGGER, tuple,
                            "Getter return value did not match previously set value.");

                    return true;
                }

            } catch (IllegalAccessException | InvocationTargetException e) {

                CheckHelperFunctions.logFailedCheckStep(LOGGER, tuple, "Failed to invoke. See exception.");

                return true;
            } catch (NullPointerException e) {
                CheckHelperFunctions.logFailedCheckStep(LOGGER, tuple,
                        "Failed to invoke. Got null pointer. See exception.");
                return true;
            }
        }
        return false;
    }
}
