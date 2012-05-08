package edu.gemini.aspen.giapi.statusservice;

import edu.gemini.aspen.giapi.status.StatusItem;
import edu.gemini.aspen.giapi.statusservice.generated.StatusType;
import edu.gemini.aspen.giapi.util.jms.status.StatusSetter;
import edu.gemini.aspen.gmp.top.Top;
import edu.gemini.jms.api.JmsArtifact;
import edu.gemini.jms.api.JmsProvider;
import edu.gemini.shared.util.immutable.Option;
import org.apache.felix.ipojo.annotations.*;

import javax.jms.JMSException;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class JmsStatusItemTranslatorImpl will publish status items translations over JMS
 *
 * @author Nicolas A. Barriga
 *         Date: 4/5/12
 */
@Component
@Provides
public class JmsStatusItemTranslatorImpl extends AbstractStatusItemTranslator implements JmsArtifact, StatusItemTranslator {
    private static final Logger LOG = Logger.getLogger(JmsStatusItemTranslatorImpl.class.getName());
    private final Map<String, StatusSetter> setters = new HashMap<String, StatusSetter>();

    public JmsStatusItemTranslatorImpl(@Requires Top top,
                                       @Property(name = "xmlFileName", value = "INVALID", mandatory = true) String xmlFileName) {
        super(top, xmlFileName);
    }

    @Validate
    public void start() throws IOException, JAXBException {
        super.start();
        //create status setters
        for (StatusType status : config.getStatuses()) {
            setters.put(
                    top.buildStatusItemName(status.getOriginalName()),
                    new StatusSetter(
                            this.getName() + status.getOriginalName(),
                            top.buildStatusItemName(status.getOriginalName())));
        }
    }


    @Invalidate
    public void stop() {
        super.stop();
    }

    @Override
    public void startJms(JmsProvider provider) throws JMSException {
        for (StatusSetter ss : setters.values()) {
            ss.startJms(provider);
        }
    }

    @Override
    public void stopJms() {
        for (StatusSetter ss : setters.values()) {
            ss.stopJms();
        }
    }

    @Override
    public <T> void update(StatusItem<T> item) {
        Option<StatusItem<?>> itemOpt = translate(item);

        //publish translation
        if (!itemOpt.isEmpty()) {
            try {
                setters.get(item.getName()).setStatusItem(itemOpt.getValue());
            } catch (JMSException e) {
                LOG.log(Level.SEVERE, e.getMessage(), e);
            }
        }
    }
}