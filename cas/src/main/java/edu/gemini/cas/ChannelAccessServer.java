package edu.gemini.cas;

import com.cosylab.epics.caj.cas.util.DefaultServerImpl;
import com.google.common.collect.ImmutableList;
import gov.aps.jca.*;
import gov.aps.jca.cas.ServerContext;
import org.apache.felix.ipojo.annotations.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class ChannelAccessServer. Implements the bulk of the giapi-cas bundle.
 *
 * @author Nicolas A. Barriga
 *         Date: Sep 30, 2010
 */
@Component
@Instantiate
@Provides
public class ChannelAccessServer implements IChannelAccessServer {
    private static final Logger LOG = Logger.getLogger(ChannelAccessServer.class.getName());
    private DefaultServerImpl server;
    private ServerContext serverContext = null;
    private ExecutorService executor;
    private final JCALibrary jca = JCALibrary.getInstance();
    private Map<String, IChannel<?>> channels;

    /**
     * Constructor.
     */
    public ChannelAccessServer() {
    }

    /**
     * Creates a server, a jca context and spawns a new thread to run the server.
     *
     * @throws IllegalStateException if trying to start an already started server
     * @throws CAException           is thrown if the jca context could not be instantiated.
     */
    @Validate
    public void start() throws CAException {
        executor = Executors.newSingleThreadExecutor();
        channels = new HashMap<String, IChannel<?>>();
        server = new DefaultServerImpl();
        if (serverContext != null) {
            throw new IllegalStateException("Tried to start the ChannelAccessServer more than once");
        }
        serverContext = jca.createServerContext(JCALibrary.CHANNEL_ACCESS_SERVER_JAVA, server);
        executor.execute(new Runnable() {
            public void run() {
                try {
                    serverContext.run(0);
                } catch (IllegalStateException ex) {
                    LOG.log(Level.SEVERE, ex.getMessage(), ex);
                } catch (CAException ex) {
                    LOG.log(Level.SEVERE, ex.getMessage(), ex);
                }

            }
        });

        try {
            //TODO: this is UGLY!! need a way to see that the server is up and running
            Thread.sleep(2000);
        } catch (InterruptedException ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
        }
    }

    @Override
    public <T> IChannel<T> createChannel(String name, T value) throws CAException {
        return createChannel(name, ImmutableList.of(value));
    }
    @Override
    public <T> IChannel<T> createChannel(String name, List<T> values) throws CAException {
        if(values.isEmpty()){
            throw new IllegalArgumentException("At least one value must be passed");
        }
        IChannel ch = null;
        if (values.get(0) instanceof Integer) {
            ch = createIntegerChannel(name, values.size());
        } else if (values.get(0) instanceof Float) {
            ch = createFloatChannel(name, values.size());
        } else if (values.get(0) instanceof Double) {
            ch = createDoubleChannel(name, values.size());
        } else if (values.get(0) instanceof String) {
            ch = createStringChannel(name, values.size());
        } else {
            throw new IllegalArgumentException("Unsupported item type " + values.get(0).getClass());
        }
        ch.setValue(values);
        return ch;
    }

    @Override
    public IntegerChannel createIntegerChannel(String name, int length) {
        if (channels.containsKey(name)) {
            IChannel ch = channels.get(name);
            if (ch instanceof IntegerChannel) {
                return (IntegerChannel) ch;
            } else {
                throw new IllegalArgumentException("Channel " + name + " already exists, but is not of type Integer");
            }
        }
        IntegerChannel ch = new IntegerChannel(name, length);
        ch.register(server);
        channels.put(name, ch);
        return ch;
    }

    @Override
    public FloatChannel createFloatChannel(String name, int length) {
        if (channels.containsKey(name)) {
            IChannel ch = channels.get(name);
            if (ch instanceof FloatChannel) {
                return (FloatChannel) ch;
            } else {
                throw new IllegalArgumentException("Channel " + name + " already exists, but is not of type Float");
            }
        }
        FloatChannel ch = new FloatChannel(name, length);
        ch.register(server);
        channels.put(name, ch);
        return ch;
    }

    @Override
    public DoubleChannel createDoubleChannel(String name, int length) {
        if (channels.containsKey(name)) {
            IChannel ch = channels.get(name);
            if (ch instanceof DoubleChannel) {
                return (DoubleChannel) ch;
            } else {
                throw new IllegalArgumentException("Channel " + name + " already exists, but is not of type Double");
            }
        }
        DoubleChannel ch = new DoubleChannel(name, length);
        ch.register(server);
        channels.put(name, ch);
        return ch;
    }

    @Override
    public StringChannel createStringChannel(String name, int length) {
        if (channels.containsKey(name)) {
            IChannel ch = channels.get(name);
            if (ch instanceof StringChannel) {
                return (StringChannel) ch;
            } else {
                throw new IllegalArgumentException("Channel " + name + " already exists, but is not of type String");
            }
        }
        StringChannel ch = new StringChannel(name, length);
        ch.register(server);
        channels.put(name, ch);
        return ch;
    }
    @Override
    public <T> IAlarmChannel<T> createAlarmChannel(String name, T value) throws CAException {
        return createAlarmChannel(name,ImmutableList.of(value));
    }
    @Override
    public <T> IAlarmChannel<T> createAlarmChannel(String name, List<T> values) throws CAException {
        if(values.isEmpty()){
            throw new IllegalArgumentException("At least one value must be passed");
        }
        IAlarmChannel ch = null;
        if (values.get(0) instanceof Integer) {
            ch = createIntegerAlarmChannel(name, values.size());
        } else if (values.get(0) instanceof Float) {
            ch = createFloatAlarmChannel(name, values.size());
        } else if (values.get(0) instanceof Double) {
            ch = createDoubleAlarmChannel(name, values.size());
        } else if (values.get(0) instanceof String) {
            ch = createStringAlarmChannel(name, values.size());
        } else {
            throw new IllegalArgumentException("Unsupported item type " + values.get(0).getClass());
        }
        ch.setValue(values);
        return ch;
    }

    @Override
    public IntegerAlarmChannel createIntegerAlarmChannel(String name, int length) {
        if (channels.containsKey(name)) {
            IChannel ch = channels.get(name);
            if (ch instanceof IntegerAlarmChannel) {
                return (IntegerAlarmChannel) ch;
            } else {
                throw new IllegalArgumentException("Channel " + name + " already exists, but is not of type IntegerAlarmChannel");
            }
        }
        IntegerAlarmChannel ch = new IntegerAlarmChannel(name, length);
        ch.register(server);
        channels.put(name, ch);
        return ch;
    }

    @Override
    public FloatAlarmChannel createFloatAlarmChannel(String name, int length) {
        if (channels.containsKey(name)) {
            IChannel ch = channels.get(name);
            if (ch instanceof FloatAlarmChannel) {
                return (FloatAlarmChannel) ch;
            } else {
                throw new IllegalArgumentException("Channel " + name + " already exists, but is not of type FloatAlarmChannel");
            }
        }
        FloatAlarmChannel ch = new FloatAlarmChannel(name, length);
        ch.register(server);
        channels.put(name, ch);
        return ch;
    }

    @Override
    public DoubleAlarmChannel createDoubleAlarmChannel(String name, int length) {
        if (channels.containsKey(name)) {
            IChannel ch = channels.get(name);
            if (ch instanceof DoubleAlarmChannel) {
                return (DoubleAlarmChannel) ch;

            } else {
                throw new IllegalArgumentException("Channel " + name + " already exists, but is not of type DoubleAlarmChannel");
            }
        }
        DoubleAlarmChannel ch = new DoubleAlarmChannel(name, length);
        ch.register(server);
        channels.put(name, ch);
        return ch;
    }

    @Override
    public StringAlarmChannel createStringAlarmChannel(String name, int length) {
        if (channels.containsKey(name)) {
            IChannel ch = channels.get(name);
            if (ch instanceof StringAlarmChannel) {
                return (StringAlarmChannel) ch;

            } else {
                throw new IllegalArgumentException("Channel " + name + " already exists, but is not of type StringAlarmChannel");
            }
        }
        StringAlarmChannel ch = new StringAlarmChannel(name, length);
        ch.register(server);
        channels.put(name, ch);
        return ch;
    }

    /**
     * Removes channel from internal Map, unregisters from server and destroys PV
     *
     * @param channel the IChannel to remove
     */
    @Override
    public void destroyChannel(IChannel channel) {
        IChannel ch;
        try {
            ch = channels.get(channel.getName());
        } catch (NullPointerException ex) {//if channel was already destroyed
            return;
        }
        if (ch != null) {
            channels.remove(ch.getName());
            if (ch instanceof AbstractChannel) {
                ((AbstractChannel) ch).destroy(server);
            } else if (ch instanceof AbstractAlarmChannel) {
                ((AbstractAlarmChannel) ch).destroy(server);
            } else {
                LOG.warning("Unknown channel type: " + ch.getClass().getName());
            }
        }
    }


    /**
     * Destroys the jca context and waits for the thread to return.
     *
     * @throws CAException if there are problems with Channel Access
     * @throws java.lang.IllegalStateException
     *                     if the context has already been destroyed.
     */
    @Invalidate
    public void stop() throws CAException {
        for (String name : channels.keySet()) {
            IChannel ch = channels.get(name);
            if (ch instanceof AbstractChannel) {
                ((AbstractChannel) ch).destroy(server);
            } else if (ch instanceof AbstractAlarmChannel) {
                ((AbstractAlarmChannel) ch).destroy(server);
            } else {
                LOG.warning("Unknown channel type: " + ch.getClass().getName());
            }
        }
        channels.clear();
        executor.shutdown();
        serverContext.destroy();
        channels = null;
        server = null;
        serverContext = null;
    }


    /**
     * Main method for starting the server from the command line.
     *
     * @param args number of milliseconds to run the server
     */
    public static void main(String[] args) {
        ChannelAccessServer giapicas = new ChannelAccessServer();
        try {

            giapicas.start();
            //giapicas.addVariable("test", -1);

        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
        }

        try {
            Thread.sleep((args[0] != null) ? Integer.parseInt(args[0]) : 10);
            giapicas.stop();
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
        }
    }
}
