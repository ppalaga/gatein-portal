package org.gatein.api.page;

import java.util.List;

/**
 * Essentially a <code>ContainerImpl</code>, with the additional aspect that consumers might need to know the
 * if the container is intended to be a column.
 *
 * Provides a specific template for columns.
 *
 * @author <a href="mailto:jpkroehling+javadoc@redhat.com">Juraci Paixão Kröhling</a>
 */
public class ColumnContainerImpl extends ContainerImpl {
    private String template = "system:/groovy/portal/webui/container/UITableColumnContainer.gtmpl";

    public ColumnContainerImpl(List<ContainerItem> containers) {
        super(containers);
    }

    @Override
    public String getTemplate() {
        return this.template;
    }

    @Override
    public void setTemplate(String template) {
        this.template = template;
    }
}
