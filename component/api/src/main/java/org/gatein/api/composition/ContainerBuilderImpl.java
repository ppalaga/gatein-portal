package org.gatein.api.composition;

import java.util.ArrayList;
import java.util.List;

import org.gatein.api.security.Permission;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;

/**
 * Provides the main implementation for the ContainerBuilder. Allows the caller to build a container, which
 * might in turn hold other containers. Provides access to other specialized containers, like ColumnContainerBuilderImpl,
 * as well as a CustomContainerBuilderImpl.
 *
 * An instance of this class holds a list of ContainerItem, a reference to the topBuilder (usually a PageBuilder), and
 * the parent (another ContainerBuilder).
 *
 * @author <a href="mailto:jpkroehling+javadoc@redhat.com">Juraci Paixão Kröhling</a>
 */
public class ContainerBuilderImpl<T extends LayoutBuilder<T>> implements ContainerBuilder<T> {
    private static final Logger log = LoggerFactory.getLogger(ContainerBuilderImpl.class);
    private Permission accessPermissions;
    private Permission moveAppsPermissions;
    private Permission moveContainersPermissions;
    private boolean childrenBuild = false;

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
     * A reference to the top-level builder, usually a PageBuilder.
     */
    private T topBuilder;

    /**
     * Basic constructor, receiving a reference to the top-level builder. Serves to return it whenever the caller has
     * finished working with the builder, providing a fluent way to return to the main builder.
     *
     * @param topBuilder a reference to the top level builder, usually "this" at the PageBuilder level.
     */
    public ContainerBuilderImpl(T topBuilder) {
        this.topBuilder = topBuilder;
    }

    /**
     * Basic constructor, receiving a reference to the top-level builder and a reference to the ContainerBuilder that
     * is creating this ContainerBuilder.
     *
     * @param topBuilder a reference to the top level builder, usually "this" at the PageBuilder level.
     * @param parent a reference to the container that is creating this container
     */
    public ContainerBuilderImpl(T topBuilder, ContainerBuilderImpl<T> parent) {
        this.topBuilder = topBuilder;
        this.parent = parent;
    }

    /**
     * Adds a new child to this container.
     * @return itself , so that the caller can continue interacting with this builder
     */
    @Override
    public ContainerBuilder<T> child(ContainerItem containerItem) {
        if (null == this.containers) {
            this.containers = new ArrayList<ContainerItem>();
        }
        this.containers.add(containerItem);
        return this;
    }

    /**
     * Adds the provided list of children to the existing list of children for this builder. If a null value is provided,
     * the current list of children is cleared.
     *
     * @param children    the list of {@link ContainerItem} to add to this container
     * @return this builder
     */
    @Override
    public ContainerBuilder<T> children(List<ContainerItem> children) {
        if (null == children) {
            this.containers.clear();
            return this;
        }

        this.containers.addAll(children);
        return this;
    }

    /**
     * Marks the end of the inclusion of children for this container and adds itself as a child of the container
     * that has created this builder, returning the parent.
     * If there's no parent (ie, is at the top level), then add itself as a child of the root container
     * (usually at the Page level) and returns itself.
     *
     * @return the parent container or itself if this container is placed at the top level
     */
    @Override
    public ContainerBuilder<T> buildChildren() {
        if (log.isTraceEnabled()) {
            log.trace("Building container on " + this);
        }

        if (null == parent && !childrenBuild) {
            // we are the top level
            topBuilder.children(this.containers);
            childrenBuild = true;
            return this;
        }

        // finishes the work on this container
        Container result = createContainer(containers);
        parent.containers.add(result);

        // as we are done building, return our parent, so the caller can fluently add more containers to it
        return parent;
    }

    /**
     * Marks the end of the work on building containers.
     *
     * Adds itself to the list of children of the topBuilder and returns the topBuilder (usually a PageBuilder).
     * @return the PageBuilder that started this ContainerBuilder.
     */
    @Override
    public T build() {
        if (!childrenBuild) {
            topBuilder.children(this.containers);
            childrenBuild = true;
        }
        return topBuilder;
    }

    /**
     * Starts a new builder, using the column template. Children added to this new builder will be rendered as
     * columns on the screen.
     *
     * @return a newly created ColumnContainerBuilderImpl
     */
    @Override
    public ContainerBuilder<T> newColumnsBuilder() {
        if (log.isTraceEnabled()) {
            log.trace("Creating a new columns container");
        }

        return new ColumnContainerBuilderImpl<T>(topBuilder, this);
    }

    /**
     * Starts a new builder, using the default template, which renders the children as rows in the screen (ie: block
     * elements).
     * @return a newly created ContainerBuilderImpl
     */
    @Override
    public ContainerBuilder<T> newRowsBuilder() {
        if (log.isTraceEnabled()) {
            log.trace("Creating a new row builder");
        }

        return new ContainerBuilderImpl<T>(topBuilder, this);
    }

    /**
     * Starts a new builder, that builds on top of the provided Container. Useful when a custom container type is
     * required.
     *
     * @param container    the container to serve as base for the builder.
     * @return a newly created CustomContainerBuilderImpl
     */
    @Override
    public ContainerBuilder<T> newCustomContainerBuilder(Container container) {
        if (log.isTraceEnabled()) {
            log.trace("Creating a new custom container builder");
        }

        return new CustomContainerBuilderImpl<T>(container, topBuilder, this);
    }

    /**
     * Starts a new builder, that builds on top of the provided Container, using a generic container but with a
     * specific template. Useful to prevent creating a specific container type just to change the template.
     *
     * @param template    the template to use when rendering container created via this builder
     * @return a newly created CustomContainerBuilderImpl
     */
    @Override
    public ContainerBuilder<T> newCustomContainerBuilder(String template) {
        if (log.isTraceEnabled()) {
            log.trace("Creating a new custom container builder");
        }

        Container container = new ContainerImpl(template, null);
        return new CustomContainerBuilderImpl<T>(container, topBuilder, this);
    }

    @Override
    public ContainerBuilder<T> accessPermission(Permission accessPermission) {
        this.accessPermissions = accessPermission;
        return this;
    }

    @Override
    public ContainerBuilder<T> moveAppsPermission(Permission moveAppsPermission) {
        this.moveAppsPermissions = moveAppsPermission;
        return this;
    }

    @Override
    public ContainerBuilder<T> moveContainersPermission(Permission moveContainersPermission) {
        this.moveContainersPermissions = moveContainersPermission;
        return this;
    }

    /**
     * Creates a Container representation based on the information provided to this builder. Specific implementations
     * might override this method and might have different requirements and rules.
     *
     * @param children a list of containers belonging to this container, or null
     * @return a representation of this container.
     */
    protected Container createContainer(List<ContainerItem> children) {
        if (log.isTraceEnabled()) {
            log.trace("Creating a new generic container");
        }
        return completeContainer(new ContainerImpl(children));
    }

    /**
     * Maps common properties to all containers from this builder, like the permissions, into the provided container.
     *
     * @param container    the base container, to add the remaining properties to
     * @return the complete container
     */
    protected Container completeContainer(Container container) {
        container.setAccessPermission(accessPermissions);
        //TODO make sure that move*Permissions are really required
        container.setMoveContainersPermission(moveContainersPermissions);
        container.setMoveAppsPermission(moveAppsPermissions);
        return container;
    }

}
