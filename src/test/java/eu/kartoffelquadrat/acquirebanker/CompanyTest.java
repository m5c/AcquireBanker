package eu.kartoffelquadrat.acquirebanker;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Maximilian Schiedermeier
 */
public class CompanyTest {

    /**
     * Verify the company colour is correctly determined.
     */
    @Test
    public void testCompanyColourAccess() {

        Company sacksonColourTest = new Company(0,"Sackson", CompanyType.NORMAL);
        Assert.assertEquals(208, sacksonColourTest.getColour());
    }

    /**
     * Verify the colour of a fake company can not be resolved.
     */
    @Test(expected = RuntimeException.class)
    public void testCompanyFailedColourAccess() {

        Company fooCompany = new Company(0,"Fooo", CompanyType.NORMAL);
        Assert.assertEquals(208, fooCompany.getColour());
    }

}
