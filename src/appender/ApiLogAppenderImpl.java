package appender;

import java.io.Serializable;
import java.util.concurrent.locks.*;

import org.apache.commons.collections.buffer.CircularFifoBuffer;
import org.apache.logging.log4j.core.*;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.appender.AppenderLoggingException;
import org.apache.logging.log4j.core.config.plugins.*;
import org.apache.logging.log4j.core.layout.PatternLayout;

@Plugin(name="ApiLogAppender", category="Core", elementType="appender", printObject=true)
public final class ApiLogAppenderImpl extends AbstractAppender {

	private static final long serialVersionUID = -2356365286570230684L;
	
	private final ReadWriteLock rwLock = new ReentrantReadWriteLock();
    private final Lock readLock = rwLock.readLock();

    public static CircularFifoBuffer messageBuffer = new CircularFifoBuffer(500);
    
    protected ApiLogAppenderImpl(String name, Filter filter, Layout<? extends Serializable> layout, final boolean ignoreExceptions) {
        super(name, filter, layout, ignoreExceptions);
    }
    
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