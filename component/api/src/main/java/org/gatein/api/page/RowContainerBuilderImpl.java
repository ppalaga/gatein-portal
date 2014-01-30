package org.gatein.api.page;

import java.util.List;

/**
 * Specialized container for rows. Consistent behavior with ContainerBuilderImpl, but returning a RowContainerImpl
 * when creating a container.
 *
 * @author <a href="mailto:jpkroehling+javadoc@redhat.com">Juraci Paixão Kröhling</a>
 */
public class RowContainerBuilderImpl<T> extends ContainerBuilderImpl<T> {
    public RowContainerBuilderImpl(T self) {
        super(self);
    }

    @Override
    protected Container createContainer(List<ContainerItem> containers) {
        return new RowContainerImpl(containers);
    }

}
