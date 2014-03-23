package org.gatein.api.composition;

import java.util.List;

import org.gatein.api.security.Permission;

/**
 * Basic representation of a Container, as defined by the public API.
 *
 * @see org.gatein.api.composition.Container
 *
 * @author <a href="mailto:jpkroehling+javadoc@redhat.com">Juraci Paixão Kröhling</a>
 */
public class ContainerImpl implements Container {

    private List<ContainerItem> children;
    private String template;
    private Permission accessPermissions;
    private Permission moveAppsPermissions;
    private Permission moveContainersPermissions;

    public ContainerImpl() {
    }

    public ContainerImpl(String template, List<ContainerItem> children) {
        this.template = template;
        this.children = children;
    }

    public ContainerImpl(List<ContainerItem> children) {
        this.children = children;
        this.template = "system:/groovy/portal/webui/container/UIContainer.gtmpl";
    }

    @Override
    public List<ContainerItem> getChildren() {
        if (null == children) {
            return null;
        }

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
        return accessPermissions;
    }

    @Override
    public void setAccessPermission(Permission accessPermission) {
        this.accessPermissions = accessPermission;
    }

    @Override
    public Permission getMoveAppsPermission() {
        return moveAppsPermissions;
    }

    @Override
    public void setMoveAppsPermission(Permission moveAppsPermission) {
        this.moveAppsPermissions = moveAppsPermission;
    }

    @Override
    public Permission getMoveContainersPermission() {
        return moveContainersPermissions;
    }

    @Override
    public void setMoveContainersPermission(Permission moveContainersPermission) {
        this.moveContainersPermissions = moveContainersPermission;
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
