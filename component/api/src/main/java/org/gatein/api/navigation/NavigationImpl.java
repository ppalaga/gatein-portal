/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.gatein.api.navigation;

import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.exoplatform.commons.utils.ExpressionUtil;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.portal.mop.Described;
import org.exoplatform.portal.mop.Described.State;
import org.exoplatform.portal.mop.description.DescriptionService;
import org.exoplatform.portal.mop.navigation.NavigationContext;
import org.exoplatform.portal.mop.navigation.NavigationService;
import org.exoplatform.portal.mop.navigation.NavigationState;
import org.exoplatform.portal.mop.navigation.NodeChangeListener;
import org.exoplatform.portal.mop.navigation.NodeContext;
import org.exoplatform.portal.mop.navigation.Scope;
import org.exoplatform.services.resources.LocaleConfig;
import org.exoplatform.services.resources.LocaleConfigService;
import org.exoplatform.services.resources.ResourceBundleManager;
import org.gatein.api.ApiException;
import org.gatein.api.PortalRequest;
import org.gatein.api.Util;
import org.gatein.api.common.i18n.LocalizedString;
import org.gatein.api.internal.Parameters;
import org.gatein.api.site.Site;
import org.gatein.api.site.SiteId;
import org.gatein.api.site.SiteType;

/**
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public class NavigationImpl implements Navigation {
    private final NavigationService navigationService;
    private final NavigationContext navCtx;
    private final DescriptionService descriptionService;
    private final ResourceBundleManager bundleManager;
    private final LocaleConfigService localeConfigService;

    private final SiteId siteId;
    private final ApiNodeModel model;

    private Navigation18NResolver i18nResolver;

    public NavigationImpl(SiteId siteId, NavigationService navigationService, NavigationContext navCtx, DescriptionService descriptionService,
            ResourceBundleManager bundleManager, LocaleConfigService localeConfigService) {
        this.siteId = siteId;
        this.navigationService = navigationService;
        this.navCtx = navCtx;
        this.descriptionService = descriptionService;
        this.bundleManager = bundleManager;
        this.localeConfigService = localeConfigService;
        this.model = new ApiNodeModel(this);
    }

    // Used for unit testing
    NavigationImpl(SiteId siteId) {
        this.siteId = siteId;
        this.navigationService = null;
        this.navCtx = null;
        this.descriptionService = null;
        this.bundleManager = null;
        this.localeConfigService = null;
        this.model = null;
    }

    @Override
    public boolean removeNode(NodePath path) {
        Parameters.requireNonNull(path, "path");

        Node parent = getNode(path.parent(), Nodes.visitChildren());
        if (parent == null || !parent.removeChild(path.getLastSegment())) {
            return false;
        }

        saveNode(parent);
        return true;
    }

    @Override
    public Node getNode(String... nodePath) {
        return getNode(NodePath.path(nodePath));
    }

    @Override
    public Node getNode(NodePath nodePath) {
        return getNode(nodePath, Nodes.visitNone());
    }

    @Override
    public Node getNode(NodePath nodePath, NodeVisitor visitor) {
        Parameters.requireNonNull(nodePath, "nodePath");
        Parameters.requireNonNull(visitor, "visitor");

        NodeContext<ApiNode> ctx = getNodeContext(nodePath, visitor);
        return (ctx == null) ? null : ctx.getNode();
    }

    @Override
    public int getPriority() {
        return navCtx.getState().getPriority();
    }

    @Override
    public SiteId getSiteId() {
        return siteId;
    }

    @Override
    public Node getRootNode(NodeVisitor visitor) {
        NodeContext<ApiNode> ctx = loadNodeContext(visitor);
        return (ctx == null) ? null : ctx.getNode();
    }

    @Override
    public void refreshNode(Node node) {
        refreshNode(node, Nodes.visitNone());
    }

    @Override
    public void refreshNode(Node node, NodeVisitor visitor) {
        Parameters.requireNonNull(node, "node");
        Parameters.requireNonNull(visitor, "visitor");

        NodeContext<ApiNode> ctx = ((ApiNode) node).getContext();
        rebaseNodeContext(ctx, new NodeVisitorScope(visitor), null);

        Node r = node;
        while (!r.isRoot())
            r = r.getParent();
        clearCached(r);
    }

    private void clearCached(Node node) {
        ((ApiNode) node).clearCached();
        if (node.isChildrenLoaded()) {
            for (Node c : node) {
                clearCached(c);
            }
        }
    }

    @Override
    public void saveNode(Node node) {
        Parameters.requireNonNull(node, "node");

        NodeContext<ApiNode> ctx = ((ApiNode) node).getContext();
        saveNodeContext(ctx, null);
        saveDisplayNames(ctx);
    }

    @Override
    public void setPriority(int priority) {
        navCtx.setState(new NavigationState(priority));
        save(navCtx);
    }

    Map<Locale, Described.State> loadDescriptions(String id) {
        try {
            return descriptionService.getDescriptions(id);
        } catch (Throwable t) {
            throw new ApiException("Failed to retrieve descriptions", t);
        }
    }

    String resolve(NodeContext<ApiNode> ctx) {
        if (i18nResolver == null) {
            PortalRequest request = PortalRequest.getInstance();
            Site site;
            if (request.getSiteId().equals(siteId)) {
                site = request.getSite();
            } else { // look it up
                site = request.getPortal().getSite(siteId);
            }

            if (site == null) {
                throw new ApiException("Could not resolve display name because site " + siteId + " could not be found.");
            }

            i18nResolver = new Navigation18NResolver(descriptionService, bundleManager, site.getLocale(), siteId);
        }

        return i18nResolver.resolveName(ctx.getState().getLabel(), ctx.getId(), ctx.getName());
    }

    /**
     * Resolves the given WebUI style i18n place holder. Under the hood a similar thing happens in
     * {@link NavigationImpl#resolve(NodeContext)}.
     * <p>
     * In particuler, it checks whether {@code expression} is a WebUI style i18n place holder of form
     * <code>#{resource.bundle.key}</code>. In case it is, it iterates over {@link LocaleConfigService#getLocalConfigs()} and
     * resolves the key against each available {@link LocaleConfig#getNavigationResourceBundle(String, String)}. The resolved
     * values are stored in a new {@link LocalizedString} and returned.
     * <p>
     * If {@code expression} is not a WebUI style i18n place holder or if the resoltion does not succed for any locale,
     * {@code new LocalizedString(expression) is returned.}
     *
     * @param expression possibly a WebUI style i18n place holder
     * @return
     */
    LocalizedString resolveExpression(String expression) {
        LocalizedString result = null;
        if (ExpressionUtil.isResourceBindingExpression(expression)) {
            String key = expression.substring(2, expression.length() - 1);
            String ownerId = siteId.getName();
            if (siteId.getType() == SiteType.SPACE) {
                /* Remove the initial '/' in a group name */
                ownerId = ownerId.substring(1);
            }
            String siteOwnerType = Util.from(siteId).getTypeName();
            for (LocaleConfig localeConfig : localeConfigService.getLocalConfigs()) {
                Locale locale = localeConfig.getLocale();
                ResourceBundle rb = localeConfig.getNavigationResourceBundle(siteOwnerType , ownerId);
                if (rb != null) {
                    try {
                        String value = rb.getString(key);
                        if (value != null) {
                            if (result == null) {
                                result = new LocalizedString(locale, value);
                            } else {
                                result.setLocalizedValue(locale, value);
                            }
                        }
                    } catch (MissingResourceException e) {
                    }
                }
            }
        }
        return result == null ? new LocalizedString(expression) : result;
    }

    NodeContext<ApiNode> getNodeContext(NodePath nodePath, NodeVisitor visitor) {
        NodeContext<ApiNode> ctx = loadNodeContext(Nodes.visitNodes(nodePath, visitor));
        for (String name : nodePath) {
            ctx = ctx.get(name);
            if (ctx == null)
                return null;
        }

        return ctx;
    }

    private NodeContext<ApiNode> loadNodeContext(NodeVisitor visitor) {
        return loadNodeContext(new NodeVisitorScope(visitor), null);
    }

    private NodeContext<ApiNode> loadNodeContext(Scope scope, NodeChangeListener<NodeContext<ApiNode>> listener) {
        try {
            return navigationService.loadNode(model, navCtx, scope, listener);
        } catch (Throwable e) {
            throw new ApiException("Failed to load node", e);
        }
    }

    void rebaseNodeContext(NodeContext<ApiNode> ctx, Scope scope, NodeChangeListener<NodeContext<ApiNode>> listener) {
        try {
            navigationService.rebaseNode(ctx, scope, listener);
        } catch (Throwable e) {
            throw new ApiException("Failed to refresh node", e);
        }
    }

    private void saveNodeContext(NodeContext<ApiNode> ctx, NodeChangeListener<NodeContext<ApiNode>> listener) {
        try {
            navigationService.saveNode(ctx, listener);
        } catch (Throwable e) {
            throw new ApiException("Failed to save node", e);
        }
    }

    private void save(NavigationContext ctx) {
        try {
            navigationService.saveNavigation(ctx);
        } catch (Throwable e) {
            throw new ApiException("Failed to save navigation", e);
        }
    }

    private void saveDisplayNames(NodeContext<ApiNode> ctx) {
        ApiNode node = ctx.getNode();
        if (node != null && node.isDisplayNameChanged()) {
            if (!node.getDisplayNames().isLocalized()) {
                Map<Locale, Described.State> descriptions = loadDescriptions(ctx.getId());
                if (descriptions != null) {
                    setDescriptions(ctx.getId(), null);
                }
            } else {
                Map<Locale, State> descriptions = ObjectFactory.createDescriptions(node.getDisplayNames());
                setDescriptions(ctx.getId(), descriptions);
            }
        }

        for (NodeContext<ApiNode> c = ctx.getFirst(); c != null; c = c.getNext()) {
            saveDisplayNames(c);
        }
    }

    private void setDescriptions(String id, Map<Locale, Described.State> descriptions) {
        try {
            descriptionService.setDescriptions(id, descriptions);
        } catch (Throwable t) {
            throw new ApiException("Failed to set descriptions", t);
        }
    }
}
