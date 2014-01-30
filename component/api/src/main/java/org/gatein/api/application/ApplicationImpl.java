package org.gatein.api.application;

import java.util.Date;
import java.util.List;

/**
 * Represents an implementation of the public API contract representing an Application, which can be a Gadget, a Portlet
 * or a WSRP.
 *
 * Internally, it combines properties from the different sources of Application data, which means that this is a
 * best-effort into representing the persisted data.
 *
 * @see org.gatein.api.application.Application
 *
 * @author <a href="mailto:jpkroehling+javadoc@redhat.com">Juraci Paixão Kröhling</a>
 */
public class ApplicationImpl implements Application {

    private String id;
    private String applicationName;
    private String categoryName;
    private ApplicationType type;

    private String displayName;
    private String description;
    private String iconURL;

    private Date createdDate;
    private Date modifiedDate;

    private List<String> accessPermissions;

    // TODO: the following fields exist on Application<S> from model, but not from registry's Application... figure out if they are critical
    private String title;
    private String height;
    private boolean modifiable;
    private boolean showApplicationMode;
    private boolean showApplicationState;
    private boolean showInfoBar;
    private String theme;
    private String width;


    public ApplicationImpl() {
    }

    public ApplicationImpl(String applicationName) {
        this.applicationName = applicationName;
    }

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    @Override
    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    @Override
    public ApplicationType getType() {
        return type;
    }

    public void setType(ApplicationType type) {
        this.type = type;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String getIconURL() {
        return iconURL;
    }

    public void setIconURL(String iconURL) {
        this.iconURL = iconURL;
    }

    @Override
    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    @Override
    public Date getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(Date modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    @Override
    public List<String> getAccessPermissions() {
        return accessPermissions;
    }

    public void setAccessPermissions(List<String> accessPermissions) {
        this.accessPermissions = accessPermissions;
    }

    @Override
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String getHeight() {
        return height;
    }

    public void setHeight(String height) {
        this.height = height;
    }

    @Override
    public boolean getModifiable() {
        return modifiable;
    }

    public void setModifiable(boolean modifiable) {
        this.modifiable = modifiable;
    }

    @Override
    public boolean getShowApplicationMode() {
        return showApplicationMode;
    }

    public void setShowApplicationMode(boolean showApplicationMode) {
        this.showApplicationMode = showApplicationMode;
    }

    @Override
    public boolean getShowApplicationState() {
        return showApplicationState;
    }

    public void setShowApplicationState(boolean showApplicationState) {
        this.showApplicationState = showApplicationState;
    }

    @Override
    public boolean getShowInfoBar() {
        return showInfoBar;
    }

    public void setShowInfoBar(boolean showInfoBar) {
        this.showInfoBar = showInfoBar;
    }

    @Override
    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    @Override
    public String getWidth() {
        return width;
    }

    public void setWidth(String width) {
        this.width = width;
    }

    @Override
    public String toString() {
        return "ApplicationImpl{" +
                "id='" + id + '\'' +
                ", applicationName='" + applicationName + '\'' +
                ", categoryName='" + categoryName + '\'' +
                ", type=" + type +
                ", title='" + title + '\'' +
                ", displayName='" + displayName + '\'' +
                ", description='" + description + '\'' +
                ", iconURL='" + iconURL + '\'' +
                ", height='" + height + '\'' +
                ", modifiable=" + modifiable +
                ", showApplicationMode=" + showApplicationMode +
                ", showApplicationState=" + showApplicationState +
                ", showInfoBar=" + showInfoBar +
                ", theme='" + theme + '\'' +
                ", width='" + width + '\'' +
                ", createdDate=" + createdDate +
                ", modifiedDate=" + modifiedDate +
                ", accessPermissions=" + accessPermissions +
                '}';
    }
}
