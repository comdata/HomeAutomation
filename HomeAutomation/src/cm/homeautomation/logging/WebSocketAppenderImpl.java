package cm.homeautomation.logging;

import java.io.Serializable;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;


import org.apache.log4j.Logger;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.appender.AppenderLoggingException;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.PatternLayout;

import cm.homeautomation.eventbus.EventBusService;

@Plugin(name = "WebSocketAppender", category = "Core", elementType = "appender", printObject = true)
public class WebSocketAppenderImpl extends AbstractAppender {
	
	 private final ReadWriteLock rwLock = new ReentrantReadWriteLock();
	    private final Lock readLock = rwLock.readLock();

	    protected WebSocketAppenderImpl(String name, Filter filter,
	            Layout<? extends Serializable> layout, final boolean ignoreExceptions) {
	        super(name, filter, layout, ignoreExceptions);
	    }

	    // The append method is where the appender does the work.
	    // Given a log event, you are free to do with it what you want.
	    // This example demonstrates:
	    // 1. Concurrency: this method may be called by multiple threads concurrently
	    // 2. How to use layouts
	    // 3. Error handling
	    @Override
	    public void append(LogEvent event) {
	        readLock.lock();
	        try {
	            final byte[] bytes = getLayout().toByteArray(event);
	            String message=new String(bytes);
	            
	            WebSocketEvent webSocketEvent = new WebSocketEvent(message);
	            
	            EventBusService.getEventBus().post(webSocketEvent);
	        } catch (Exception ex) {
	            if (!ignoreExceptions()) {
	                throw new AppenderLoggingException(ex);
	            }
	        } finally {
	            readLock.unlock();
	        }
	    }

	    // Your custom appender needs to declare a factory method
	    // annotated with `@PluginFactory`. Log4j will parse the configuration
	    // and call this factory method to construct an appender instance with
	    // the configured attributes.
	    @PluginFactory
	    public static WebSocketAppenderImpl createAppender(
	            @PluginAttribute("name") String name,
	            @PluginElement("Layout") Layout<? extends Serializable> layout,
	            @PluginElement("Filter") final Filter filter,
	            @PluginAttribute("otherAttribute") String otherAttribute) {
	        if (name == null) {
	            LOGGER.error("No name provided for MyCustomAppenderImpl");
	            return null;
	        }
	        if (layout == null) {
	            layout = PatternLayout.createDefaultLayout();
	        }
	        return new WebSocketAppenderImpl(name, filter, layout, true);
	    }
	
}
