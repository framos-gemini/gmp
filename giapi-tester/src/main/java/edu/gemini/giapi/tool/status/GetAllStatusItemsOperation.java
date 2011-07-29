package edu.gemini.giapi.tool.status;

import edu.gemini.aspen.giapi.status.StatusItem;
import edu.gemini.aspen.giapitestsupport.TesterException;
import edu.gemini.giapi.tool.arguments.GetAllStatusItemsArgument;
import edu.gemini.giapi.tool.arguments.GetStatusNamesArgument;
import edu.gemini.giapi.tool.arguments.HostArgument;
import edu.gemini.giapi.tool.parser.Argument;
import edu.gemini.giapi.tool.parser.Operation;
import edu.gemini.jms.activemq.provider.ActiveMQJmsProvider;
import edu.gemini.jms.api.JmsProvider;

import javax.jms.JMSException;
import java.util.Collection;
import java.util.Set;
import java.util.logging.Logger;

/**
 * The operation to get the last value of a status item from the GMP.
 */
public class GetAllStatusItemsOperation implements Operation {

    private static final Logger LOG = Logger.getLogger(GetAllStatusItemsOperation.class.getName());

    private String _host = "localhost";

    private boolean ready = false;

    public void setArgument(Argument arg) {
        if (arg instanceof GetAllStatusItemsArgument) {
            ready = true;
        }
        if (arg instanceof HostArgument) {
            _host = ((HostArgument) arg).getHost();
        }
    }

    public boolean isReady() {
        return ready;
    }

    public int execute() throws Exception {
        JmsProvider provider = new ActiveMQJmsProvider("tcp://" + _host + ":61616");

        StatusGetter getter = new StatusGetter();

        try {
            getter.startJms(provider);

            Collection<StatusItem> items = getter.getAllStatusItems();

            if (items != null) {
                System.out.println("All StatusItems received:");
                for (StatusItem item : items) {
                    System.out.println(item);
                }
            } else {
                System.out.println("Couldn't retrieve status items");
            }


        } catch (JMSException ex) {
            LOG.warning("Problem on GIAPI tester: " + ex.getMessage());
        } catch (TesterException ex) {
            LOG.warning("Problem on GIAPI tester: " + ex.getMessage());
        } finally {
            getter.stopJms();
        }
        return 0;
    }
}