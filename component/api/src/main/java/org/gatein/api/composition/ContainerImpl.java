package org.gatein.api.composition;

import java.util.List;

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
    private String[] accessPermissions;
    private String[] moveAppsPermissions;
    private String[] moveContainersPermissions;

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
    public String[] getAccessPermissions() {
        return accessPermissions;
    }

    @Override
    public void setAccessPermissions(String[] accessPermissions) {
        this.accessPermissions = accessPermissions;
    }

    @Override
    public String[] getMoveAppsPermissions() {
        return moveAppsPermissions;
    }

    @Override
    public void setMoveAppsPermissions(String[] moveAppsPermissions) {
        this.moveAppsPermissions = moveAppsPermissions;
    }

    @Override
    public String[] getMoveContainersPermissions() {
        return moveContainersPermissions;
    }

    @Override
    public void setMoveContainersPermissions(String[] moveContainersPermissions) {
        this.moveContainersPermissions = moveContainersPermissions;
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
