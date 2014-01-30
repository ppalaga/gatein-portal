package org.gatein.api.composition;

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
    public ColumnContainerImpl(List<ContainerItem> containers) {
        super("system:/groovy/portal/webui/container/UITableColumnContainer.gtmpl", containers);
    }
}
