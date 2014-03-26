package org.gatein.api.composition;

import java.util.List;

import org.gatein.api.page.PageImpl;
import org.gatein.api.security.Permission;

/**
 * Basic representation of a Container, as defined by the public API. Children are rendered in rows.
 *
 * @see org.gatein.api.composition.Container
 *
 * @author <a href="mailto:jpkroehling+javadoc@redhat.com">Juraci Paixão Kröhling</a>
 */
public class ContainerImpl implements Container {

    /**
     * Internal to this implementation. May change without notice.
     */
    private static final String ROWS_TEMPLATE_URL = "system:/groovy/portal/webui/container/UIContainer.gtmpl";

    private List<ContainerItem> children;
    private String template;

    /**
     * Beware that {@link PageImpl} which is a subclass of this uses another mechanism to store
     * access permissions. Therefore it is generally safer to use
     * {@code myContainer.getAccessPermission()} rather than {@code myContainer.accessPermissions}.
     */
    private Permission accessPermission = Container.DEFAULT_ACCESS_PERMISSION;

    /**
     * Beware that {@link PageImpl} which is a subclass of this uses another mechanism to store
     * move applications permissions. Therefore it is generally safer to use
     * {@code myContainer.getMoveAppsPermission()} rather than {@code myContainer.moveAppsPermissions}.
     */
    private Permission moveAppsPermission = Container.DEFAULT_MOVE_APPS_PERMISSION;

    /**
     * Beware that {@link PageImpl} which is a subclass of this uses another mechanism to store
     * move containers permissions. Therefore it is generally safer to use
     * {@code myContainer.getMoveContainersPermission()} rather than {@code myContainer.moveContainersPermissions}.
     */
    private Permission moveContainersPermission = Container.DEFAULT_MOVE_CONTAINERS_PERMISSION;

    public ContainerImpl() {
    }

    public ContainerImpl(String template, List<ContainerItem> children) {
        this.template = template;
        this.children = children;
    }

    public ContainerImpl(List<ContainerItem> children) {
        this.children = children;
        this.template = ROWS_TEMPLATE_URL;
    }

    @Override
    public List<ContainerItem> getChildren() {
        return children;
    }

    @Override
    public void setChildren(List<ContainerItem> children) {
        this.children = children;
    }

    @Override
    public String getTemplate() {
        return template;
    }

    @Override
    public void setTemplate(String template) {
        this.template = template;
    }

    @Override
    public Permission getAccessPermission() {
        return accessPermission;
    }

    @Override
    public void setAccessPermission(Permission accessPermission) {
        this.accessPermission = accessPermission;
    }

    @Override
    public Permission getMoveAppsPermission() {
        return moveAppsPermission;
    }

    @Override
    public void setMoveAppsPermission(Permission moveAppsPermission) {
        this.moveAppsPermission = moveAppsPermission;
    }

    @Override
    public Permission getMoveContainersPermission() {
        return moveContainersPermission;
    }

    @Override
    public void setMoveContainersPermission(Permission moveContainersPermission) {
        this.moveContainersPermission = moveContainersPermission;
    }

    public boolean isChildrenSet() {
        return !(null == children);
    }

    @Override
    public String toString() {
        return "ContainerImpl{" +
                "children=" + children +
                ", hashCode=" + hashCode() +
                '}';
    }
}
