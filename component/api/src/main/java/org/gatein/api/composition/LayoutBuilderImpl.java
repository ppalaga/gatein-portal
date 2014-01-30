package org.gatein.api.composition;

import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Basic layout builder, providing a wrapper for the ContainerBuilder. Provides also helper methods for simple
 * common use cases.
 *
 * @author <a href="mailto:jpkroehling+javadoc@redhat.com">Juraci Paixão Kröhling</a>
 */
public class LayoutBuilderImpl<T extends LayoutBuilder<T>> implements LayoutBuilder<T> {
    private static final Logger log = LoggerFactory.getLogger(LayoutBuilderImpl.class);
    protected List<ContainerBuilderImpl<T>> containerBuilderList = new ArrayList<ContainerBuilderImpl<T>>();
    protected List<ContainerItem> children = new ArrayList<ContainerItem>();

    /**
     * Marks the begin of work on a layout container, returning a ContainerBuilder that will serve as the implicit
     * context, aka parent container.
     *
     * @return the base layout container
     */
    @Override
    public ContainerBuilder<T> newColumnsBuilder() {
        if (log.isTraceEnabled()) {
            log.trace("Creating a new layout container");
        }

        /**
         * this is the root container, located at the first level
         */
        //noinspection unchecked
        ColumnContainerBuilderImpl<T> containerBuilder = new ColumnContainerBuilderImpl<T>((T) this);

        this.containerBuilderList.add(containerBuilder);
        return containerBuilder;
    }

    /**
     * Marks the begin of work on a layout container, returning a ContainerBuilder that will serve as the implicit
     * context, aka parent container.
     *
     * @return the base layout container
     */
    @Override
    public ContainerBuilder<T> newRowsBuilder() {
        if (log.isTraceEnabled()) {
            log.trace("Creating a new layout container");
        }

        /**
         * this is the root container, located at the first level
         */
        //noinspection unchecked
        ContainerBuilderImpl<T> containerBuilder = new ContainerBuilderImpl<T>((T) this);

        this.containerBuilderList.add(containerBuilder);
        return containerBuilder;
    }

    /**
     * Marks the begin of work on a layout container, returning a ContainerBuilder that will serve as the implicit
     * context, aka parent container.
     *
     * @return the base layout container
     */
    @Override
    public ContainerBuilder<T> newCustomContainerBuilder(Container container) {
        if (log.isTraceEnabled()) {
            log.trace("Creating a new layout container");
        }

        /**
         * this is the root container, located at the first level
         */
        //noinspection unchecked
        CustomContainerBuilderImpl<T> containerBuilder = new CustomContainerBuilderImpl<T>(container, (T) this);

        this.containerBuilderList.add(containerBuilder);
        return containerBuilder;
    }

    /**
     * Helper method to make it easy to create a simple page with a single application.
     *
     * @param containerItem the application that should be present on the page.
     * @return the layout builder with a complete container, based on the application
     */
    @Override
    public T child(ContainerItem containerItem) {
        this.children.add(containerItem);
        //noinspection unchecked
        return (T) this;
    }

    /**
     * Helper method to make it easy to create a simple page with a single application.
     *
     * @param children the application that should be present on the page.
     * @return the layout builder with a complete container, based on the application
     */
    @Override
    public T children(List<ContainerItem> children) {
        if (null == children) {
            this.children.clear();
            //noinspection unchecked
            return (T) this;
        }

        this.children.addAll(children);
        //noinspection unchecked
        return (T) this;
    }
}
