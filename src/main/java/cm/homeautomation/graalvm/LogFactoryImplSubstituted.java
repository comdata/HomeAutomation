package cm.homeautomation.graalvm;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.impl.LogFactoryImpl;
import org.apache.commons.logging.impl.SimpleLog;

import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;

@TargetClass(LogFactoryImpl.class)
final class LogFactoryImplSubstituted {
	@Substitute
	private Log discoverLogImplementation(String logCategory) {
		return new SimpleLog(logCategory);
	}
}