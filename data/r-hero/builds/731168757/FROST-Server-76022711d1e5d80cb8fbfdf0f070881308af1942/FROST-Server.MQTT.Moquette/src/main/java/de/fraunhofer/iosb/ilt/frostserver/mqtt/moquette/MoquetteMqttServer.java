/*
 * Copyright (C) 2016 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
 * Karlsruhe, Germany.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.fraunhofer.iosb.ilt.frostserver.mqtt.moquette;

import de.fraunhofer.iosb.ilt.frostserver.mqtt.MqttServer;
import de.fraunhofer.iosb.ilt.frostserver.mqtt.create.EntityCreateEvent;
import de.fraunhofer.iosb.ilt.frostserver.mqtt.create.EntityCreateListener;
import de.fraunhofer.iosb.ilt.frostserver.mqtt.subscription.SubscriptionEvent;
import de.fraunhofer.iosb.ilt.frostserver.mqtt.subscription.SubscriptionListener;
import de.fraunhofer.iosb.ilt.frostserver.settings.ConfigDefaults;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import de.fraunhofer.iosb.ilt.frostserver.settings.MqttSettings;
import de.fraunhofer.iosb.ilt.frostserver.settings.Settings;
import de.fraunhofer.iosb.ilt.frostserver.settings.annotation.DefaultValue;
import de.fraunhofer.iosb.ilt.frostserver.settings.annotation.DefaultValueInt;
import de.fraunhofer.iosb.ilt.frostserver.util.StringHelper;
import io.moquette.BrokerConstants;
import io.moquette.interception.AbstractInterceptHandler;
import io.moquette.interception.InterceptHandler;
import io.moquette.interception.messages.InterceptConnectMessage;
import io.moquette.interception.messages.InterceptDisconnectMessage;
import io.moquette.interception.messages.InterceptPublishMessage;
import io.moquette.interception.messages.InterceptSubscribeMessage;
import io.moquette.interception.messages.InterceptUnsubscribeMessage;
import io.moquette.server.Server;
import io.moquette.server.config.IConfig;
import io.moquette.server.config.MemoryConfig;
import io.moquette.spi.impl.subscriptions.Subscription;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import javax.swing.event.EventListenerList;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jab
 */
public class MoquetteMqttServer implements MqttServer, ConfigDefaults {

    /**
     * Custom Settings | Tags
     */
    @DefaultValueInt(9876)
    public static final String TAG_WEBSOCKET_PORT = "WebsocketPort";
    @DefaultValueInt(50)
    public static final String TAG_MAX_IN_FLIGHT = "maxInFlight";
    @DefaultValue("io.moquette.persistence.mapdb.MapDBPersistentStore")
    public static final String STORAGE_CLASS_NAME = BrokerConstants.STORAGE_CLASS_NAME;
    @DefaultValue("")
    public static final String TAG_KEYSTORE_PATH = "javaKeystorePath";
    @DefaultValue("")
    public static final String TAG_KEYSTORE_PASS = "keyStorePassword";
    @DefaultValue("")
    public static final String TAG_KEYMANAGER_PASS = "keyManagerPassword";
    @DefaultValueInt(8883)
    public static final String TAG_SSL_PORT = "sslPort";
    @DefaultValueInt(443)
    public static final String TAG_SSL_WEBSOCKET_PORT = "secureWebsocketPort";

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(MoquetteMqttServer.class);

    private Server mqttBroker;
    private MqttClient client;
    protected EventListenerList subscriptionListeners = new EventListenerList();
    protected EventListenerList entityCreateListeners = new EventListenerList();
    private CoreSettings settings;
    private final Map<String, List<String>> clientSubscriptions = new HashMap<>();
    /**
     * The MQTT Id used by the FROST server to connect to the MQTT broker.
     */
    private final String frostClientId;

    public MoquetteMqttServer() {
        frostClientId = "SensorThings API Server (" + UUID.randomUUID() + ")";
    }

    @Override
    public void addEntityCreateListener(EntityCreateListener listener) {
        entityCreateListeners.add(EntityCreateListener.class, listener);
    }

    @Override
    public void publish(String topic, byte[] payload, int qos) {
        if (mqttBroker != null && client != null) {
            if (!client.isConnected()) {
                LOGGER.warn("MQTT client is not connected while trying to publish.");
                try {
                    client.reconnect();
                } catch (MqttException ex) {
                    LOGGER.warn("MQTT client failed to reconnect.");
                    return;
                }
            }
            try {
                LOGGER.trace("    FROST -> Moquette on {}", topic);
                client.publish(topic, payload, qos, false);
            } catch (MqttException ex) {
                LOGGER.error("publish on topic '{}' failed.", topic, ex);
            }
        }
    }

    @Override
    public void addSubscriptionListener(SubscriptionListener listener) {
        subscriptionListeners.add(SubscriptionListener.class, listener);
    }

    @Override
    public void removeEntityCreateListener(EntityCreateListener listener) {
        entityCreateListeners.remove(EntityCreateListener.class, listener);
    }

    @Override
    public void removeSubscriptionListener(SubscriptionListener listener) {
        subscriptionListeners.remove(SubscriptionListener.class, listener);
    }

    protected void fireEntityCreate(EntityCreateEvent e) {
        Object[] listeners = entityCreateListeners.getListenerList();
        for (int i = 0; i < listeners.length; i = i + 2) {
            if (listeners[i] == EntityCreateListener.class) {
                ((EntityCreateListener) listeners[i + 1]).onEntityCreate(e);
            }
        }
    }

    protected void fireSubscribe(SubscriptionEvent e) {
        Object[] listeners = subscriptionListeners.getListenerList();
        for (int i = 0; i < listeners.length; i = i + 2) {
            if (listeners[i] == SubscriptionListener.class) {
                ((SubscriptionListener) listeners[i + 1]).onSubscribe(e);
            }
        }
    }

    protected void fireUnsubscribe(SubscriptionEvent e) {
        Object[] listeners = subscriptionListeners.getListenerList();
        for (int i = 0; i < listeners.length; i = i + 2) {
            if (listeners[i] == SubscriptionListener.class) {
                ((SubscriptionListener) listeners[i + 1]).onUnsubscribe(e);
            }
        }
    }

    @Override
    public void start() {
        mqttBroker = new Server();
        final List<? extends InterceptHandler> userHandlers = Arrays.asList(new AbstractInterceptHandlerImpl());

        IConfig config = new MemoryConfig(new Properties());
        MqttSettings mqttSettings = settings.getMqttSettings();
        Settings customSettings = mqttSettings.getCustomSettings();

        config.setProperty(BrokerConstants.PORT_PROPERTY_NAME, Integer.toString(mqttSettings.getPort()));
        config.setProperty(BrokerConstants.HOST_PROPERTY_NAME, mqttSettings.getHost());
        config.setProperty(BrokerConstants.ALLOW_ANONYMOUS_PROPERTY_NAME, Boolean.TRUE.toString());

        String defaultPersistentStore = Paths.get(settings.getTempPath(), BrokerConstants.DEFAULT_MOQUETTE_STORE_MAP_DB_FILENAME).toString();
        String persistentStore = customSettings.get(BrokerConstants.PERSISTENT_STORE_PROPERTY_NAME, defaultPersistentStore);
        config.setProperty(BrokerConstants.PERSISTENT_STORE_PROPERTY_NAME, persistentStore);

        String storageClass = customSettings.get(STORAGE_CLASS_NAME, getClass());
        config.setProperty(BrokerConstants.STORAGE_CLASS_NAME, storageClass);
        config.setProperty(BrokerConstants.WEB_SOCKET_PORT_PROPERTY_NAME, customSettings.get(TAG_WEBSOCKET_PORT, getClass()));

        String keystorePath = customSettings.get(TAG_KEYSTORE_PATH, getClass());
        if (!keystorePath.isEmpty()) {
            LOGGER.info("Configuring keystore for ssl");
            config.setProperty(BrokerConstants.JKS_PATH_PROPERTY_NAME, keystorePath);
            config.setProperty(BrokerConstants.KEY_STORE_PASSWORD_PROPERTY_NAME, customSettings.get(TAG_KEYSTORE_PASS, getClass()));
            config.setProperty(BrokerConstants.KEY_MANAGER_PASSWORD_PROPERTY_NAME, customSettings.get(TAG_KEYMANAGER_PASS, getClass()));
            config.setProperty(BrokerConstants.SSL_PORT_PROPERTY_NAME, customSettings.get(TAG_SSL_PORT, getClass()));
            config.setProperty(BrokerConstants.WSS_PORT_PROPERTY_NAME, customSettings.get(TAG_SSL_WEBSOCKET_PORT, getClass()));
        }

        AuthWrapper authWrapper = createAuthWrapper();

        int maxInFlight = customSettings.getInt(TAG_MAX_IN_FLIGHT, getClass());
        try {
            mqttBroker.startServer(config, userHandlers, null, authWrapper, authWrapper);
            String broker = "tcp://" + mqttSettings.getInternalHost() + ":" + mqttSettings.getPort();

            client = new MqttClient(broker, frostClientId, new MemoryPersistence());
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            connOpts.setAutomaticReconnect(true);
            connOpts.setKeepAliveInterval(30);
            connOpts.setConnectionTimeout(30);
            connOpts.setMaxInflight(maxInFlight);
            LOGGER.info("paho-client connecting to broker: {}", broker);

            client.connect(connOpts);
            LOGGER.info("paho-client connected to broker");
        } catch (MqttException ex) {
            LOGGER.error("Could not create MQTT Client.", ex);
        } catch (IOException ex) {
            LOGGER.error("Could not create MQTT Server.", ex);
        }
        fetchOldSubscriptions();
    }

    private AuthWrapper createAuthWrapper() {
        Settings authSettings = settings.getAuthSettings();
        String authProviderClassName = authSettings.get(CoreSettings.TAG_AUTH_PROVIDER, "");
        if (!StringHelper.isNullOrEmpty(authProviderClassName)) {
            return new AuthWrapper(settings, authProviderClassName, frostClientId);
        }
        return null;
    }

    private void fetchOldSubscriptions() {
        LOGGER.info("Checking for pre-existing subscriptions.");
        int count = 0;
        for (Subscription sub : mqttBroker.getSubscriptions()) {
            String subClientId = sub.getClientId();
            if (subClientId.equalsIgnoreCase(frostClientId)) {
                continue;
            }
            String topic = sub.getTopicFilter().toString();
            LOGGER.debug("Re-subscribing existing subscription for {} on {}.", subClientId, topic);
            List<String> clientSubList = clientSubscriptions.computeIfAbsent(
                    subClientId,
                    k -> new ArrayList<>()
            );
            try {
                fireSubscribe(new SubscriptionEvent(topic));
                clientSubList.add(topic);
            } catch (IllegalArgumentException e) {
                LOGGER.warn("Exception initialising old subscription for client {} to topic {}", subClientId, topic, e);
            }
            count++;
        }
        LOGGER.info("Found {} pre-existing subscriptions.", count);
    }

    @Override
    public void stop() {
        if (client != null && client.isConnected()) {
            LOGGER.info("Disconnecting internal MQTT client...");
            try {
                client.disconnectForcibly();
            } catch (MqttException ex) {
                LOGGER.debug("exception when forcefully disconnecting MQTT client", ex);
            }
        }
        if (client != null) {
            LOGGER.info("Closing internal MQTT client...");
            try {
                client.close();
            } catch (MqttException ex) {
                LOGGER.debug("exception when closing the MQTT client", ex);
            }
            LOGGER.info("Closing internal MQTT client done.");
        }
        if (mqttBroker != null) {
            mqttBroker.stopServer();
        }
    }

    @Override
    public void init(CoreSettings settings) {
        if (settings == null) {
            throw new IllegalArgumentException("MqttSettings must be non-null");
        }
        this.settings = settings;
    }

    private class AbstractInterceptHandlerImpl extends AbstractInterceptHandler {

        @Override
        public void onPublish(InterceptPublishMessage msg) {
            if (msg.getClientID().equalsIgnoreCase(frostClientId)) {
                return;
            }
            LOGGER.trace("      Moquette -> FROST on {}", msg.getTopicName());
            String payload = msg.getPayload().toString(StringHelper.UTF8);
            fireEntityCreate(new EntityCreateEvent(this, msg.getTopicName(), payload));
        }

        @Override
        public void onConnect(InterceptConnectMessage msg) {
            final String clientId = msg.getClientID();
            if (clientId.equalsIgnoreCase(frostClientId)) {
                return;
            }
            LOGGER.trace("      Client connected: {}", clientId);
            clientSubscriptions.put(clientId, new ArrayList<>());
        }

        @Override
        public void onDisconnect(InterceptDisconnectMessage msg) {
            final String clientId = msg.getClientID();
            if (clientId.equalsIgnoreCase(frostClientId)) {
                return;
            }
            LOGGER.trace("      Client disconnected: {}", clientId);
            clientSubscriptions.getOrDefault(clientId, new ArrayList<>())
                    .stream().forEach(
                            subscribedTopic -> fireUnsubscribe(new SubscriptionEvent(subscribedTopic))
                    );
            clientSubscriptions.remove(clientId);
        }

        @Override
        public void onSubscribe(InterceptSubscribeMessage msg) {
            final String clientId = msg.getClientID();
            if (clientId.equalsIgnoreCase(frostClientId)) {
                return;
            }
            final String topicFilter = msg.getTopicFilter();
            LOGGER.trace("      Client {} subscribed to {}", clientId, topicFilter);
            clientSubscriptions.getOrDefault(
                    clientId, new ArrayList<>()
            ).add(topicFilter);
            fireSubscribe(new SubscriptionEvent(topicFilter));
        }

        @Override
        public void onUnsubscribe(InterceptUnsubscribeMessage msg) {
            final String clientId = msg.getClientID();
            if (clientId.equalsIgnoreCase(frostClientId)) {
                return;
            }
            final String topicFilter = msg.getTopicFilter();
            LOGGER.trace("      Client {} unsubscribed from {}", clientId, topicFilter);
            clientSubscriptions.getOrDefault(clientId, new ArrayList<>())
                    .remove(topicFilter);
            fireUnsubscribe(new SubscriptionEvent(topicFilter));
        }

        @Override
        public String getID() {
            return frostClientId;
        }
    }

}
