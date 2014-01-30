package org.gatein.api.page;

import java.util.Collections;
import java.util.List;

/**
 * Basic representation of a Container, as defined by the public API.
 *
 * @see org.gatein.api.page.Container
 *
 * @author <a href="mailto:jpkroehling+javadoc@redhat.com">Juraci Paixão Kröhling</a>
 */
public class ContainerImpl implements Container {

    private List<ContainerItem> children;
    private String template = "system:/groovy/portal/webui/container/UIContainer.gtmpl";

    public ContainerImpl(List<ContainerItem> children) {
        this.children = children;
    }

    @Override
    public List<ContainerItem> getChildren() {
        return Collections.unmodifiableList(children);
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
    public String toString() {
        return "ContainerImpl{" +
                "children=" + children +
                ", hashCode=" + hashCode() +
                '}';
    }
}
