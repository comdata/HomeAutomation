package cm.homeautomation.services.base.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import cm.homeautomation.services.base.GenericStatus;

class GenericStatusTest {

    private static final String ERROR_OCCURED = "Error occured";

    @Test
    void testSuccess() {
        GenericStatus genericStatus = new GenericStatus(true);

        assertTrue(genericStatus.isSuccess());
    }

@Test
    void testSimpleError() {
        GenericStatus genericStatus = new GenericStatus(false);

        assertFalse(genericStatus.isSuccess());
    }

    @Test
    void testComplexError() {
        GenericStatus genericStatus = new GenericStatus(true, ERROR_OCCURED);

        assertFalse(genericStatus.isSuccess());
        assertEquals(ERROR_OCCURED, genericStatus.getErrorMessage());
    }
}