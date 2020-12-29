package cm.homeautomation.graalvm;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.impl.LogFactoryImpl;

import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;

//see https://github.com/oracle/graal/issues/715
@SuppressWarnings("unused")
@TargetClass(LogFactory.class)
final class LogFactorySubstituted {
	@Substitute
	protected static LogFactory newFactory(final String factoryClass, final ClassLoader classLoader,
			final ClassLoader contextClassLoader) {
		return new LogFactoryImpl();
	}
}
