package org.gatein.api.page;

import org.gatein.api.application.Application;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;

/**
 * Basic layout builder, providing a wrapper for the ContainerBuilder. Provides also helper methods for simple
 * common use cases.
 *
 * @author <a href="mailto:jpkroehling+javadoc@redhat.com">Juraci Paixão Kröhling</a>
 */
public class LayoutBuilderImpl<T extends LayoutBuilder> implements LayoutBuilder<T> {
    private static final Logger log = LoggerFactory.getLogger(LayoutBuilderImpl.class);
    private ContainerBuilderImpl<T> containerBuilder;

    /**
     * Marks the begin of work on a layout container, returning a ContainerBuilder that will serve as the implicit
     * context, aka parent container.
     *
     * @return the base layout container
     */
    @Override
    public ContainerBuilder<T> beginLayoutContainer() {
        if (log.isTraceEnabled()) {
            log.trace("Creating a new layout container");
        }

        //noinspection unchecked
        containerBuilder = new ContainerBuilderImpl<T>((T) this);
        return containerBuilder;
    }

    /**
     * Helper method to make it easy to create a simple page with a single application.
     *
     * @param application the application that should be present on the page.
     * @return the layout builder with a complete container, based on the application
     */
    @Override
    public T application(Application application) {
        //noinspection unchecked
        return beginLayoutContainer().row().application(application).buildLayout();
    }

    /**
     * Helper method that provides access to the finished container located at the top-level of the page.
     * @return the top-level finished container
     */
    protected Container getFinishedContainer() {
        return this.containerBuilder.getFinishedContainer();
    }
}
