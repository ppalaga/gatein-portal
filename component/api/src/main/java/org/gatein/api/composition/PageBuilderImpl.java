package org.gatein.api.composition;

import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.page.PageContext;
import org.exoplatform.portal.mop.page.PageKey;
import org.exoplatform.portal.mop.page.PageState;
import org.gatein.api.Util;
import org.gatein.api.page.Page;
import org.gatein.api.page.PageImpl;
import org.gatein.api.security.Permission;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;

import java.util.Arrays;

/**
 * Main point of contact between the consumer of the API and the builders. Provides methods to set all the possible
 * parameters that a persisted page might have, as well as ways to get access to the building blocks of a page, ie,
 * containers.
 *
 * @author <a href="mailto:jpkroehling+javadoc@redhat.com">Juraci Paixão Kröhling</a>
 */
public class PageBuilderImpl extends LayoutBuilderImpl<PageBuilder> implements PageBuilder {
    private static final Logger log = LoggerFactory.getLogger(PageBuilderImpl.class);

    // Page-related properties
    private String name;
    private String description;

    // PageContext-related properties
    private PageContext pageContext;
    private PageKey pageKey;
    private PageState pageState;

    // SiteKey-related properties
    private SiteKey siteKey;
    private String siteType;
    private String siteName;

    // PageState-related properties
    private String displayName;
    private boolean showMaxWindow;
    private Permission accessPermission;
    private Permission editPermission;
    private Permission moveAppsPermission;
    private Permission moveContainersPermission;

    public PageBuilderImpl() {
        if (log.isTraceEnabled()) {
            log.trace("Created a new page builder: " + this);
        }
    }

    /**
     * @see LayoutBuilderImpl#newColumnsBuilder()
     */
    @Override
    public ContainerBuilder<PageBuilder> newColumnsBuilder() {
        return super.newColumnsBuilder();
    }

    @Override
    public PageBuilder name(String name) {
        this.name = name;
        return this;
    }

    @Override
    public PageBuilder description(String description) {
        this.description = description;
        return this;
    }

    @Override
    public PageBuilder siteName(String siteName) {
        this.siteName = siteName;
        return this;
    }

    @Override
    public PageBuilder displayName(String displayName) {
        this.displayName = displayName;
        return this;
    }

    @Override
    public PageBuilder showMaxWindow(boolean showMaxWindow) {
        this.showMaxWindow = showMaxWindow;
        return this;
    }

    @Override
    public PageBuilder accessPermission(Permission accessPermission) {
        this.accessPermission = accessPermission;
        return this;
    }

    @Override
    public PageBuilder editPermission(Permission editPermission) {
        this.editPermission = editPermission;
        return this;
    }

    @Override
    public PageBuilder moveAppsPermission(Permission moveAppsPermission) {
        this.moveAppsPermission = moveAppsPermission;
        return this;
    }

    @Override
    public PageBuilder moveContainersPermission(Permission moveContainersPermission) {
        this.moveContainersPermission = moveContainersPermission;
        return this;
    }

    @Override
    public PageBuilder siteType(String siteType) {

        if (!"portal".equalsIgnoreCase(siteType) && !"site".equalsIgnoreCase(siteType) && !"user".equalsIgnoreCase(siteType)) {
            throw new IllegalArgumentException("siteType must be one of the following: portal, site or user");
        }

        this.siteType = siteType;
        return this;
    }

    /**
     * Builds a new page based on the information provided via this builder.
     * @return the Page that best represents the information stored on this builder
     * @throws java.lang.IllegalStateException if mandatory information is not provided
     */
    @Override
    public Page build() {

        Page page = new PageImpl(getPageContext());
        page.setChildren(this.children);

        if (log.isTraceEnabled()) {
            log.trace("Page finished: " + this);
        }

        return page;
    }

    /**
     * Returns a complete MOP PageContext based on the information from this builder.
     * @return the PageContext for this page
     * @throws java.lang.IllegalStateException if either the PageKey or PageState are null or invalid.
     */
    private PageContext getPageContext() {
        if (null == pageContext) {
            if (null == getPageKey() || null == getPageState()) {
                throw new IllegalStateException("API usage error: either the PageContext should be set or both PageKey and PageState should be set.");
            }

            pageContext = new PageContext(getPageKey(), getPageState());
        }

        pageContext.setState(getPageState());
        return pageContext;
    }

    /**
     * Returns a complete MOP PageState, based on the information from this builder. If, at this stage, the
     * editPermission wasn't set or isn't available, it's assumed that it'd be Permission#everyone.
     * @return the PageState for this page
     */
    private PageState getPageState() {
        if (null == pageState) {
            if (null == editPermission) {
                // here we need to make a crucial decision: either assume that "everyone" would be able to edit the
                // page, or that only an admin, or throw an exception. Probably, the safest is to throw an exception,
                // but we might consider that the usual scenario would be that everyone logged in would be able to change
                // the page. If that's not the case, we need to change this permission.
                editPermission = Permission.everyone();
            }

            if (null == accessPermission) {
                accessPermission = Permission.everyone();
            }

            pageState = new PageState(displayName,
                    description,
                    showMaxWindow,
                    null,
                    Arrays.asList(Util.from(accessPermission)),
                    Util.from(editPermission)[0], // this is the same as the createPage, but is it right?
                    Arrays.asList(Util.from(moveAppsPermission)),
                    Arrays.asList(Util.from(moveContainersPermission)));
        }
        return pageState;
    }

    /**
     * Returns the MOP PageKey, composed of the page name and the SiteKey.
     * @return the PageKey for this page.
     * @throws java.lang.IllegalStateException if the site key is invalid or the name is null
     */
    private PageKey getPageKey() {
        if (null == pageKey) {
            if (null == name || null == getSiteKey()) {
                throw new IllegalStateException("API usage error: either the PageKey should be set or both SiteKey and page name should be set.");
            }

            pageKey = new PageKey(getSiteKey(), name);
        }

        return pageKey;
    }

    /**
     * Returns the MOP SiteKey, composed of the site name and site type.
     * @return the SiteKey for this page.
     * @throws java.lang.IllegalStateException if the siteName or the siteType is null
     */
    private SiteKey getSiteKey() {
        if (null == siteKey) {
            if (null == siteName || null == siteType) {
                throw new IllegalStateException("API usage error: either the SiteKey should be set or both site name and site type should be set.");
            }

            siteKey = new SiteKey(siteType, siteName);
        }

        return siteKey;
    }

    @Override
    public String toString() {
        return "PageBuilderImpl{" +
                super.toString() +
                ", hashCode=" + hashCode() +
                '}';
    }

}
