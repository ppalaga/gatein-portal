package org.gatein.api.page;

import java.util.List;

/**
 * A basic container, with the possibility of overriding some parameters, like the template to be used.
 *
 * @author <a href="mailto:jpkroehling+javadoc@redhat.com">Juraci Paixão Kröhling</a>
 */
public class RowContainerImpl extends ContainerImpl {
    // currently, we do not override the template, as rows are rendered as regular containers by the UI.
    // but note that if a Row is added to a container that is of "columns" type, it will be rendered in a column
    public RowContainerImpl(List<ContainerItem> containers) {
        super(containers);
    }
}
