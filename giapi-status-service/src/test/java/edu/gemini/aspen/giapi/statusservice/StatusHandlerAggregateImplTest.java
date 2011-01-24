package edu.gemini.aspen.giapi.statusservice;

import edu.gemini.aspen.giapi.status.StatusHandler;
import edu.gemini.aspen.giapi.status.StatusItem;
import edu.gemini.aspen.giapi.status.impl.BasicStatus;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

/**
 * Simple tests for the class StatusHandlerAggregate
 *
 * @author cquiroz
 *
 */
public class StatusHandlerAggregateImplTest {

    private StatusHandlerAggregateImpl manager;
    private StatusHandler handler;
    private StatusItem item;

    @Before
    public void setupTest() {
        manager = new StatusHandlerAggregateImpl();
        handler = mock(StatusHandler.class);
        item = new BasicStatus<Integer>("status", Integer.valueOf(0));
    }

    @Test
    public void testConstruction() {
        assertNotNull(manager);
    }

    @Test
    public void testGetName() {
        assertNotNull(manager.getName());
    }

    @Test
    public void testNoAggregation() {
        // Check there are no interactions before a handler is passed
        verifyNoInteraction(handler, item);
    }

    @Test
    public void testHandlerBindUnbind() {
        manager.bindStatusHandler(handler);
        verifyStatusPassedAlong(handler, item);

        manager.unbindStatusHandler(handler);
        verifyNoInteraction(handler, item);
    }

    private void verifyStatusPassedAlong(StatusHandler handler, StatusItem item) {
        // Now pass the handler and verify interaction
        manager.update(item);
        verify(handler).update(item);
    }

    private void verifyNoInteraction(StatusHandler handler, StatusItem item) {
        // Verify that a call doesn't apply to handler yet
        manager.update(item);
        verifyZeroInteractions(handler);
    }
}
