// Synthetic scanner control; outside all Maven source roots.
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.ClassRule;
import org.junit.Assume;
import org.junit.runner.RunWith;
import org.junit.experimental.categories.Category;
import org.junit.rules.TemporaryFolder;
import org.junit.*;
import org.easymock.EasyMock;
import static org.easymock.EasyMock.verify;
import junit.framework.TestCase;
class LegacyScanControl {
    @Before void before() {}
    @BeforeClass void beforeClass() {}
    @After void after() {}
    @AfterClass void afterClass() {}
    @Ignore void ignored() {}
    @Rule Object rule;
    @ClassRule Object classRule;
    @Test(expected = IllegalArgumentException.class) void expectedException() {}
    void directCalls() {
        EasyMock.verify(null);
        org.junit.Assert.assertEquals(1, 1);
        org.easymock.EasyMock.verify(null);
    }
}
