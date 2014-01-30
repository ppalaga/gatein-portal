package org.gatein.api.page;

import org.gatein.api.application.Application;
import org.gatein.api.application.ApplicationImpl;
import org.gatein.api.security.Permission;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Set of tests that shows what's the expected data structure after some usage scenarios.
 *
 * @author <a href="mailto:jpkroehling+javadoc@redhat.com">Juraci Paixão Kröhling</a>
 */
public class PageBuilderImplTest {

    /**
     * The most simple scenario ever: shows what are the required information to compose a page, as well as adds
     * a single application (which is actually not required, but an empty page isn't that useful, is it?)
     */
    @Test
    public void testSimplestScenario() {
        PageBuilder pageBuilder = new PageBuilderImpl();
        Application gadgetCalculator = new ApplicationImpl("gadgetCalculator");
            Page page = pageBuilder
                    .application(gadgetCalculator)
                    .siteName("classic") // to make it even simpler, we could assume that "classic" is the default, no?
                    .siteType("portal") // same here: should "portal" be the default? or better explicit than implicit?
                    .name("awesome")
                    .buildPage();
        assertEquals("should have 1 child container", 1, page.getContainer().getChildren().size());
        Application application = (Application) page.getContainer().getChildren().get(0);
        assertEquals("should have 1 application", "gadgetCalculator", application.getApplicationName());
    }

    /**
     * Demonstrates a scenario where a layout has a single row, with a single application. The end result is equivalent
     * to the testSimplestScenario, but you can use it in debug mode to see how the data structure looks like.
     */
    @Test
    public void testRowApplicationScenario() {
        PageBuilder pageBuilder = new PageBuilderImpl();
        Application gadgetCalculator = new ApplicationImpl("gadgetCalculator");
        Page page = pageBuilder.
                        beginLayoutContainer()
                            .row()
                                .application(gadgetCalculator)
                        .buildLayout()
                        .siteName("classic")
                        .siteType("portal")
                        .name("awesome")
                    .buildPage();

        assertEquals("should have 1 child container", 1, page.getContainer().getChildren().size());
        Application application = (Application) page.getContainer().getChildren().get(0);
        assertEquals("should have 1 application", "gadgetCalculator", application.getApplicationName());
    }

    /**
     * A more complex scenario, which probably won't be the common use case, but demonstrates that the API itself
     * is ready to handle such cases.
     */
    @Test
    public void testComplexReferenceScenario() {
        PageBuilder pageBuilder = new PageBuilderImpl();
        Application gadgetCalculator = new ApplicationImpl("gadgetCalculator");
        Application gadgetRss = new ApplicationImpl("gadgetRss");
        Application wsrpCompanyNews = new ApplicationImpl("wsrpCompanyNews");
        Application portletUsefulLinks = new ApplicationImpl("portletUsefulLinks");

        Permission accessPermission = Permission.everyone();

        List<String> moveAppsPermissions = new ArrayList<String>();
        moveAppsPermissions.add("Everyone");

        List<String> moveContainersPermissions = new ArrayList<String>();
        moveContainersPermissions.add("Everyone");

        Page page = pageBuilder.beginLayoutContainer()
                .row()
                    .application(gadgetRss)

                .row()
                    .columns()
                        .column()
                            .application(gadgetCalculator)
                        .column()
                            .application(wsrpCompanyNews)
                    .buildColumns()

                .row()
                    .columns()
                        .column()
                            .application(portletUsefulLinks)
                        .column()
                            .application(portletUsefulLinks)
                        .column()
                            .application(gadgetCalculator)
                        .row()  // note that this is ambiguous, and based on context information, we infer that the
                                // consumer wants a row in a column, which is effectively the just having a column...
                            .application(gadgetRss)
                    .buildColumns()

                .buildLayout()
                .siteName("classic")
                .siteType("portal")
                .name("awesome")
                .displayName("Awesome page")
                .showMaxWindow(false)
                .accessPermission(accessPermission)
                .editPermission(Permission.everyone())
                .moveAppsPermissions(moveAppsPermissions)
                .moveContainersPermissions(moveContainersPermissions)
                .buildPage();

        assertEquals("should have 1 child container", 3, page.getContainer().getChildren().size());
        Application firstRowApp = (Application) page.getContainer().getChildren().get(0);
        Container secondRow = (Container) page.getContainer().getChildren().get(1);
        Container thirdRow = (Container) page.getContainer().getChildren().get(2);

        assertEquals("first row app should be gadgetRss", "gadgetRss", firstRowApp.getApplicationName());

        assertEquals("second row should have 1 child container", 1, secondRow.getChildren().size());
        Container secondRowColumnContainer = (Container) secondRow.getChildren().get(0);

        assertEquals("second row column container should have 2 applications", 2, secondRowColumnContainer.getChildren().size());
        Application secondRowFirstApp = (Application) secondRowColumnContainer.getChildren().get(0);
        Application secondRowSecondApp = (Application) secondRowColumnContainer.getChildren().get(1);
        assertEquals("second row first app should be gadgetCalculator", "gadgetCalculator", secondRowFirstApp.getApplicationName());
        assertEquals("second row second app should be wsrpCompanyNews", "wsrpCompanyNews", secondRowSecondApp.getApplicationName());

        assertEquals("second row should have 1 child container", 1, secondRow.getChildren().size());
        Container thirdRowColumnContainer = (Container) thirdRow.getChildren().get(0);

        assertEquals("third row column container should have 4 applications", 4, thirdRowColumnContainer.getChildren().size());
        Application thirdRowFirstApp = (Application) thirdRowColumnContainer.getChildren().get(0);
        Application thirdRowSecondApp = (Application) thirdRowColumnContainer.getChildren().get(1);
        Application thirdRowThirdApp = (Application) thirdRowColumnContainer.getChildren().get(2);
        Application thirdRowFourthApp = (Application) thirdRowColumnContainer.getChildren().get(3);
        assertEquals("third row first app should be portletUsefulLinks", "portletUsefulLinks", thirdRowFirstApp.getApplicationName());
        assertEquals("third row second app should be portletUsefulLinks", "portletUsefulLinks", thirdRowSecondApp.getApplicationName());
        assertEquals("third row third app should be gadgetCalculator", "gadgetCalculator", thirdRowThirdApp.getApplicationName());
        assertEquals("third row fourth app should be gadgetRss", "gadgetRss", thirdRowFourthApp.getApplicationName());

    }
}
