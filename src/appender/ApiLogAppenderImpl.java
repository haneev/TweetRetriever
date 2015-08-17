package appender;

import java.io.Serializable;
import java.util.concurrent.locks.*;

import org.apache.commons.collections.buffer.CircularFifoBuffer;
import org.apache.logging.log4j.core.*;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.appender.AppenderLoggingException;
import org.apache.logging.log4j.core.config.plugins.*;
import org.apache.logging.log4j.core.layout.PatternLayout;

// note: class name need not match the @Plugin name.
@Plugin(name="ApiLogAppender", category="Core", elementType="appender", printObject=true)
public final class ApiLogAppenderImpl extends AbstractAppender {

	private static final long serialVersionUID = -2356365286570230684L;
	
	private final ReadWriteLock rwLock = new ReentrantReadWriteLock();
    private final Lock readLock = rwLock.readLock();

    public static CircularFifoBuffer messageBuffer = new CircularFifoBuffer(500);
    
    protected ApiLogAppenderImpl(String name, Filter filter,
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
        	
            String message = new String(getLayout().toByteArray(event));
            messageBuffer.add(message.trim());
            
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
    public static ApiLogAppenderImpl createAppender(
            @PluginAttribute("name") String name,
            @PluginElement("Layout") Layout<? extends Serializable> layout,
            @PluginElement("Filter") final Filter filter,
            @PluginAttribute("format") String otherAttribute) {
        if (name == null) {
            LOGGER.error("No name provided for ApiLogAppenderImpl");
            return null;
        }
        if (layout == null) {
            layout = PatternLayout.createDefaultLayout();
        }
        return new ApiLogAppenderImpl(name, filter, layout, true);
    }
}