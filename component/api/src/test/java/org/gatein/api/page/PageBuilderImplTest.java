package org.gatein.api.page;

import org.gatein.api.application.Application;
import org.gatein.api.application.ApplicationImpl;
import org.gatein.api.composition.Container;
import org.gatein.api.composition.PageBuilder;
import org.gatein.api.composition.PageBuilderImpl;
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
     * The simplest scenario ever: shows what are the required information to compose a page, as well as adds
     * a single application (which is actually not required, but an empty page isn't that useful, is it?)
     */
    @Test
    public void testSimplestScenario() {
        PageBuilder pageBuilder = new PageBuilderImpl();
        Application gadgetCalculator = new ApplicationImpl("gadgetCalculator");
            Page page = pageBuilder
                    .child(gadgetCalculator)
                    .siteName("classic") // to make it even simpler, we could assume that "classic" is the default, no?
                    .siteType("portal") // same here: should "portal" be the default? or better explicit than implicit?
                    .name("awesome")
                    .build(); // finishes the page
        assertEquals("should have 1 application", 1, page.getChildren().size());
        Application application = (Application) page.getChildren().get(0);
        assertEquals("application should be gadgetCalculator", "gadgetCalculator", application.getApplicationName());
    }

    /**
     * Same as the simple scenario, but with two rows
     */
    @Test
    public void testSimplestScenarioWithTwoChildren() {
        PageBuilder pageBuilder = new PageBuilderImpl();
        Application gadgetCalculator = new ApplicationImpl("gadgetCalculator");
        Application gadgetRss = new ApplicationImpl("gadgetRss");
            Page page = pageBuilder
                    .child(gadgetCalculator)
                    .child(gadgetRss)
                    .siteName("classic") // to make it even simpler, we could assume that "classic" is the default, no?
                    .siteType("portal") // same here: should "portal" be the default? or better explicit than implicit?
                    .name("awesome")
                    .build(); // finishes the page
        assertEquals("should have 2 child container", 2, page.getChildren().size());
        Application firstApplication = (Application) page.getChildren().get(0);
        Application secondApplication = (Application) page.getChildren().get(1);
        assertEquals("first application should gadgetCalculator", "gadgetCalculator", firstApplication.getApplicationName());
        assertEquals("second application should be gadgetRss", "gadgetRss", secondApplication.getApplicationName());
    }

    /**
     * Demonstrates a scenario where a layout has a single row, with a single application. The end result is equivalent
     * to the testSimplestScenario, but you can use it in debug mode to see how the data structure looks like.
     */
    @Test
    public void testRowApplicationScenario() {
        PageBuilder pageBuilder = new PageBuilderImpl();
        Application gadgetCalculator = new ApplicationImpl("gadgetCalculator");
        Page page = pageBuilder

                        .newRowsBuilder() // new single row on the page
                            .child(gadgetCalculator) // application on the row
                        .buildChildren() // finishes the row

                        .build() // finishes the layout
                        .siteName("classic")
                        .siteType("portal")
                        .name("awesome")
                    .build(); // finishes the page

        assertEquals("should have 1 application", 1, page.getChildren().size());
        Application application = (Application) page.getChildren().get(0);
        assertEquals("application should be named", "gadgetCalculator", application.getApplicationName());
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

        Page page = pageBuilder
                .newColumnsBuilder() // top-level column row, with 3 cells
                    .child(gadgetRss) // an application in the first column, about 33% of the total width of the screen
                    .newColumnsBuilder() // a new column set inside the second column, 33% of the width of the screen
                        .child(gadgetCalculator) // 50% of the second column, ie, ~16% of the screen size
                        .child(wsrpCompanyNews) // same as above
                    .buildChildren() // finishes the second column

                    .newColumnsBuilder() // third column, the remaining 33%
                        .child(portletUsefulLinks) // about 1/4 of the remaining 33%
                        .child(portletUsefulLinks) // same as above
                        .child(gadgetCalculator) // same as above
                        .child(gadgetRss) // same as above
                    .buildChildren() // finishes the third column
                .buildChildren()

                .build() // finishes the layout
                .siteName("classic")
                .siteType("portal")
                .name("awesome")
                .displayName("Awesome page")
                .showMaxWindow(false)
                .accessPermission(accessPermission)
                .editPermission(Permission.everyone())
                .moveAppsPermissions(moveAppsPermissions)
                .moveContainersPermissions(moveContainersPermissions)
            .build(); // finishes the page

        assertEquals("should have 3 children containers", 3, page.getChildren().size());
        Application firstRowApp = (Application) page.getChildren().get(0);
        Container secondRow = (Container) page.getChildren().get(1);
        Container thirdRow = (Container) page.getChildren().get(2);

        assertEquals("first row app should be gadgetRss", "gadgetRss", firstRowApp.getApplicationName());

        assertEquals("second row should have 2 child container", 2, secondRow.getChildren().size());
        Application secondRowFirstApp = (Application) secondRow.getChildren().get(0);
        Application secondRowSecondApp = (Application) secondRow.getChildren().get(1);
        assertEquals("second row first app should be gadgetCalculator", "gadgetCalculator", secondRowFirstApp.getApplicationName());
        assertEquals("second row second app should be wsrpCompanyNews", "wsrpCompanyNews", secondRowSecondApp.getApplicationName());

        assertEquals("third row should have 4 child container", 4, thirdRow.getChildren().size());
        Application thirdRowFirstApp = (Application) thirdRow.getChildren().get(0);
        Application thirdRowSecondApp = (Application) thirdRow.getChildren().get(1);
        Application thirdRowThirdApp = (Application) thirdRow.getChildren().get(2);
        Application thirdRowFourthApp = (Application) thirdRow.getChildren().get(3);
        assertEquals("third row first app should be portletUsefulLinks", "portletUsefulLinks", thirdRowFirstApp.getApplicationName());
        assertEquals("third row second app should be portletUsefulLinks", "portletUsefulLinks", thirdRowSecondApp.getApplicationName());
        assertEquals("third row third app should be gadgetCalculator", "gadgetCalculator", thirdRowThirdApp.getApplicationName());
        assertEquals("third row fourth app should be gadgetRss", "gadgetRss", thirdRowFourthApp.getApplicationName());

    }
}
