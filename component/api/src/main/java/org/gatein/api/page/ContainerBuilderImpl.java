package org.gatein.api.page;

import org.gatein.api.application.Application;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides the main implementation for the ContainerBuilder. Main characteristics:
 *
 * At the top-level, a new container builder is created by direct instantiation by LayoutBuilderImpl,
 * passing the class type of the builder that is invoking it, so that it can be returned later once
 * the work is finished.
 *
 * At each new level, the parent is added as a reference to the child, and a reference to the child
 * is added to the parent's context, as well as an Enum, indicating what's the type of container
 * to be created (row, column, ...)
 *
 * When each level is finished, the parent builder is returned. A level is finished by a call to buildColumns (which
 * might return itself, if it's the parent level) or by calling buildLayout.
 *
 * For instance:
 *
 * - <code>row().application()</code> : two ContainerBuilder's are involved:
 * <ol>
 *     <li>
 *         An implicit builder, for the top-level container of the page, whose context.containerBuilder points,
 *         at this point, to the row and whose context.state is ROW.
 *     </li>
 *     <li>
 *         One row, which contains a reference to the parent (implicit builder). Context is null, as there's nothing
 *         down from this row other than an application (and an application doesn't requires a container builder).
 *         This row contains a list of Containers, with one element (the application called after row()).
 *     </li>
 * </ol>
 *
 * @author <a href="mailto:jpkroehling+javadoc@redhat.com">Juraci Paixão Kröhling</a>
 */
public class ContainerBuilderImpl<T> implements ContainerBuilder<T> {
    private static final Logger log = LoggerFactory.getLogger(ContainerBuilderImpl.class);

    /**
     * Holds the context for the current container builder, ie, if this is part of a row, column or generic container.
     */
    private Context context;

    /**
     * A list of all containers added to this container. For instance, this container might hold two columns and a row.
     * Or just an application.
     */
    private List<ContainerItem> containers = new ArrayList<ContainerItem>();

    /**
     * Holds a reference to the container that created this container. Might be null if the container is at
     * the top-level.
     */
    private ContainerBuilderImpl<T> parent;

    /**
     * Whenever this builder finishes building a container, it stores the container here. When null, it means that
     * the container wasn't built yet.
     */
    private Container finishedContainer;

    /**
     * A reference to the top-level builder, usually a PageBuilder.
     */
    private T self;

    /**
     * Basic constructor, receiving a reference to the top-level builder. Serves to return it whenever the caller has
     * finished working with the builder, providing a fluent way to return to the main builder.
     *
     * @param self a reference to the top level builder, usually "this" at the PageBuilder level.
     */
    public ContainerBuilderImpl(T self) {
        this.self = self;
    }

    /**
     * Adds a new container to this. The context state is marked as COLUMN.
     *
     * The behavior is similar to the #container method, except that the builder is one specialized in columns (ie:
     * returning containers whose template is of a column). Other than that, it's the same behavior.
     *
     * @return a new ContainerBuilder
     */
    @Override
    public ContainerBuilder<T> columns() {
        return container(ContextState.COLUMN, new ColumnContainerBuilderImpl<T>(self));
    }

    /**
     * Adds a new container to this, representing a column on this container. Note that the difference between
     * this one and the plural form (columns), is that the rendering of containers created with the singular form is a
     * regular container.
     *
     * @return the newly added container
     */
    @Override
    public ContainerBuilder<T> column() {
        return container(ContextState.COLUMN);
    }

    /**
     * Adds a new generic container to this.
     *
     * @return the newly added container
     */
    @Override
    public ContainerBuilder<T> row() {
        return container(ContextState.ROW);
    }

    /**
     * Adds a new container to this, representing a column or row on this container, based on the given state.
     *
     * @return the newly added container
     */
    private ContainerBuilder<T> container(ContextState state) {
        return container(state, new ContainerBuilderImpl<T>(self));
    }

    /**
     * Adds a new container to this, by assembling a new context based on the provided parameters and setting this as
     * the parent of the new container.
     *
     * @param state               the state on which to create the container
     * @param containerBuilder    the container builder instance to use when creating the context
     * @return a new container builder, which is the same as the provided containerBuilder, but with the parent set
     */
    private ContainerBuilder<T> container(ContextState state, ContainerBuilderImpl<T> containerBuilder) {
        // if this container has been finished, stop processing...
        checkFinishedContainer();

        containerBuilder.parent = this;
        context = new Context(state, containerBuilder);

        if (log.isTraceEnabled()) {
            log.trace("New container builder added: " + context.containerBuilder);
        }

        return containerBuilder;
    }

    /**
     *
     * Adds the application to the list of containers on the parent builder, or to the top-level if the parent is null.
     *
     * @param application the <code>org.gatein.api.application.Application</code> to be added
     * @return the parent container
     * @throws java.lang.IllegalStateException , if the container already has another container inside.
     */
    @Override
    public ContainerBuilder<T> application(Application application) {
        if (null == parent) {
            this.containers.add(application);
            return this;
        } else {
            parent.containers.add(application);
            return parent;
        }
    }

    /**
     * Finishes this container and returns the reference to the parent.
     *
     * @return the parent container
     */
    @Override
    public ContainerBuilder<T> buildColumns() {
        if (log.isTraceEnabled()) {
            log.trace("Building container on " + this);
        }

        if (null == parent) {
            // we are the top level, allow the caller to add stuff to here
            return this;
        }

        // finishes the work on this container
        finishContainer();

        // indicates the parent that we are done
        parent.buildContext();

        // if we are not a container, then we build the parent, until we find who's the parent generic container
        // the reason is: when "buildColumns" is called, we want to build the generic container, not only the row
        // or the column
        if (context.state != ContextState.CONTAINER) {
            return parent.buildColumns();
        }

        // as we are done building, return our parent, so the caller can fluently add more containers to it
        return parent;
    }

    /**
     * Helper that the caller uses to indicate that work is done on the <code>ContainerBuilder</code>, so, we finish
     * this container (and potentially the parent) and returns the stored reference to "self".
     *
     * @return the reference to self, so that the caller can continue building from there.
     * @see org.gatein.api.page.ContainerBuilderImpl#finishContainer()
     */
    @Override
    public T buildLayout() {
        if (log.isTraceEnabled()) {
            log.trace("Finishing layout container ");
        }
        finishContainer();
        return self;
    }

    /**
     * Finishes this container, by setting the internal finishedContainer property to a container that best
     * represents the consumer's intentions expressed on this builder.
     *
     * @throws java.lang.IllegalStateException , if the container has been finished already.
     */
    private void finishContainer() {
        if (log.isTraceEnabled()) {
            log.trace("Finishing container on " + this);
        }

        if (null != this.finishedContainer) {
            throw new IllegalStateException("API Usage error: Tried to finish a container that has been finished already.");
        }

        this.finishedContainer = createContainer(containers);
    }

    /**
     * Creates the best representation of this container. Subclasses might override this method to return a more
     * specialized container type.
     *
     * @param children a list of containers belonging to this container, or null
     * @return a representation of this container.
     */
    protected Container createContainer(List<ContainerItem> children) {
        if (log.isTraceEnabled()) {
            log.trace("Creating a new generic container");
        }
        return new ContainerImpl(children);
    }

    /**
     * Takes the finished container from the context and adds to the list of containers on this. For instance:
     * when a row is being created and the content has been set, the row will call this to indicate to us that it
     * has finished its work. We then add the finished container to our list of containers.
     *
     * The context might be null, if we are at the first level, ie, the context is "page", not "container", nor "row",
     * nor "column".
     */
    private void buildContext() {
        if (log.isTraceEnabled()) {
            log.trace("Building context on " + this);
        }

        if (null == context) {
            // this is the first level container, so, no context yet
            return;
        }

        Container container = context.containerBuilder.finishedContainer;
        this.containers.add(container);
    }

    /**
     * Checks that the container is not finished yet, so that operations that requires an unfinished containers can
     * continue.
     *
     * @throws java.lang.IllegalStateException if the container has been finished already.
     */
    private void checkFinishedContainer() {
        if (null != finishedContainer) {
            throw new IllegalStateException("API Usage error: tried to add a new container to a finished container!");
        }
    }

    protected Container getFinishedContainer() {
        return this.finishedContainer;
    }

    /**
     * Helper class that provides a context for this container. For instance, when a new row is added to this container,
     * the context is then set to row and a reference to the container builder for the row is hold.
     */
    private class Context {
        private ContextState state;
        private ContainerBuilderImpl<T> containerBuilder;

        private Context(ContextState state, ContainerBuilderImpl<T> containerBuilder) {
            this.state = state;
            this.containerBuilder = containerBuilder;
        }
    }

    /**
     * The possible states of the context. Possible values:
     * - ROW , when the caller of the builder indicated that they want to add a new row
     * - COLUMN , similarly, but for a column
     * - CONTAINER , when it's a generic container
     */
    private enum ContextState {
            ROW, COLUMN, CONTAINER, APPLICATION
    }
}
