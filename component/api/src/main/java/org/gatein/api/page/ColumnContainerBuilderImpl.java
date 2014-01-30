package org.gatein.api.page;

import java.util.List;

/**
 * Builds a <code>ColumnContainerImpl</code>, with a specialized container type.
 *
 * @see org.gatein.api.page.ColumnContainerImpl
 *
 * @author <a href="mailto:jpkroehling+javadoc@redhat.com">Juraci Paixão Kröhling</a>
 */
public class ColumnContainerBuilderImpl<T> extends ContainerBuilderImpl<T> {
    public ColumnContainerBuilderImpl(T self) {
        super(self);
    }

    /**
     * Specializes the Container type, by wrapping the given list of containers into a ColumnContainerImpl
     *
     * @see org.gatein.api.page.ColumnContainerImpl
     * @param containers the list of container to be included into a column
     * @return the column, with the list of containers as children
     */
    @Override
    protected Container createContainer(List<ContainerItem> containers) {
        return new ColumnContainerImpl(containers);
    }
}
