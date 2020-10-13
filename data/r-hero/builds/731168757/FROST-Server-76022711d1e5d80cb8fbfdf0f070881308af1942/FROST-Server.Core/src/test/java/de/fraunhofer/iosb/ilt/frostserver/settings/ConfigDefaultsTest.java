package de.fraunhofer.iosb.ilt.frostserver.settings;

import de.fraunhofer.iosb.ilt.frostserver.messagebus.MqttMessageBus;
import static de.fraunhofer.iosb.ilt.frostserver.messagebus.MqttMessageBus.TAG_MAX_IN_FLIGHT;
import static de.fraunhofer.iosb.ilt.frostserver.messagebus.MqttMessageBus.TAG_MQTT_BROKER;
import static de.fraunhofer.iosb.ilt.frostserver.messagebus.MqttMessageBus.TAG_QOS_LEVEL;
import static de.fraunhofer.iosb.ilt.frostserver.messagebus.MqttMessageBus.TAG_RECV_QUEUE_SIZE;
import static de.fraunhofer.iosb.ilt.frostserver.messagebus.MqttMessageBus.TAG_RECV_WORKER_COUNT;
import static de.fraunhofer.iosb.ilt.frostserver.messagebus.MqttMessageBus.TAG_SEND_QUEUE_SIZE;
import static de.fraunhofer.iosb.ilt.frostserver.messagebus.MqttMessageBus.TAG_SEND_WORKER_COUNT;
import static de.fraunhofer.iosb.ilt.frostserver.messagebus.MqttMessageBus.TAG_TOPIC_NAME;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class ConfigDefaultsTest {

    public ConfigDefaultsTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testDefaultValueLookup() {
        MqttMessageBus b = new MqttMessageBus();
        // Test valid integer properties
        assertEquals(2, b.defaultValueInt(TAG_SEND_WORKER_COUNT));
        assertEquals(2, b.defaultValueInt(TAG_RECV_WORKER_COUNT));
        assertEquals(100, b.defaultValueInt(TAG_SEND_QUEUE_SIZE));
        assertEquals(100, b.defaultValueInt(TAG_RECV_QUEUE_SIZE));
        assertEquals(2, b.defaultValueInt(TAG_QOS_LEVEL));
        assertEquals(50, b.defaultValueInt(TAG_MAX_IN_FLIGHT));
        // Test valid string properties
        assertEquals("tcp://127.0.0.1:1884", b.defaultValue(TAG_MQTT_BROKER));
        assertEquals("FROST-Bus", b.defaultValue(TAG_TOPIC_NAME));

        // Test valid boolean properties
        CoreSettings c = new CoreSettings();
        assertEquals(true, c.defaultValueBoolean(CoreSettings.TAG_USE_ABSOLUTE_NAVIGATION_LINKS));
        assertEquals(false, c.defaultValueBoolean(CoreSettings.TAG_AUTH_ALLOW_ANON_READ));

        // Test reading integer properties as strings
        assertEquals("2", b.defaultValue(TAG_SEND_WORKER_COUNT));
        assertEquals("2", b.defaultValue(TAG_RECV_WORKER_COUNT));
        assertEquals("100", b.defaultValue(TAG_SEND_QUEUE_SIZE));
        assertEquals("100", b.defaultValue(TAG_RECV_QUEUE_SIZE));
        assertEquals("2", b.defaultValue(TAG_QOS_LEVEL));
        assertEquals("50", b.defaultValue(TAG_MAX_IN_FLIGHT));

        // Test reading boolean properties as strings
        assertEquals(Boolean.TRUE.toString(), c.defaultValue(CoreSettings.TAG_USE_ABSOLUTE_NAVIGATION_LINKS));
        assertEquals(Boolean.FALSE.toString(), c.defaultValue(CoreSettings.TAG_AUTH_ALLOW_ANON_READ));

        // Test invalid properties
        try {
            b.defaultValueInt("NOT_A_VALID_INT_PROPERTY");
            Assert.fail("Should have thrown an exception for a non-existing default value.");
        } catch (IllegalArgumentException exc) {
            // This should happen.
        }
        try {
            b.defaultValue("NOT_A_VALID_STR_PROPERTY");
            Assert.fail("Should have thrown an exception for a non-existing default value.");
        } catch (IllegalArgumentException exc) {
            // This should happen.
        }
        try {
            b.defaultValueBoolean("NOT_A_VALID_BOOL_PROPERTY");
            Assert.fail("Should have thrown an exception for a non-existing default value.");
        } catch (IllegalArgumentException exc) {
            // This should happen.
        }

        // Test configTags
        Set<String> tags = new HashSet<>();
        tags.add(TAG_SEND_WORKER_COUNT);
        tags.add(TAG_RECV_WORKER_COUNT);
        tags.add(TAG_SEND_QUEUE_SIZE);
        tags.add(TAG_RECV_QUEUE_SIZE);
        tags.add(TAG_QOS_LEVEL);
        tags.add(TAG_MAX_IN_FLIGHT);
        tags.add(TAG_MQTT_BROKER);
        tags.add(TAG_TOPIC_NAME);
        assertEquals(tags, b.configTags());

        // Test configDefaults
        Map<String, String> configDefaults = b.configDefaults();
        assertEquals("tcp://127.0.0.1:1884", configDefaults.get(TAG_MQTT_BROKER));
        assertEquals("FROST-Bus", configDefaults.get(TAG_TOPIC_NAME));
        assertEquals("2", configDefaults.get(TAG_SEND_WORKER_COUNT));
        assertEquals("2", configDefaults.get(TAG_RECV_WORKER_COUNT));
        assertEquals("100", configDefaults.get(TAG_SEND_QUEUE_SIZE));
        assertEquals("100", configDefaults.get(TAG_RECV_QUEUE_SIZE));
        assertEquals("2", configDefaults.get(TAG_QOS_LEVEL));
        assertEquals("50", configDefaults.get(TAG_MAX_IN_FLIGHT));
    }

    @Test
    public void testDefaultValueLookupClass() {
        Class c = MqttMessageBus.class;
        // Test valid integer properties
        assertEquals(2, ConfigUtils.getDefaultValueInt(c, TAG_SEND_WORKER_COUNT));
        assertEquals(2, ConfigUtils.getDefaultValueInt(c, TAG_RECV_WORKER_COUNT));
        assertEquals(100, ConfigUtils.getDefaultValueInt(c, TAG_SEND_QUEUE_SIZE));
        assertEquals(100, ConfigUtils.getDefaultValueInt(c, TAG_RECV_QUEUE_SIZE));
        assertEquals(2, ConfigUtils.getDefaultValueInt(c, TAG_QOS_LEVEL));
        assertEquals(50, ConfigUtils.getDefaultValueInt(c, TAG_MAX_IN_FLIGHT));
        // Test valid string properties
        assertEquals("tcp://127.0.0.1:1884", ConfigUtils.getDefaultValue(c, TAG_MQTT_BROKER));
        assertEquals("FROST-Bus", ConfigUtils.getDefaultValue(c, TAG_TOPIC_NAME));
        // Test valid boolean properties
        assertEquals(true, ConfigUtils.getDefaultValueBoolean(CoreSettings.class, CoreSettings.TAG_USE_ABSOLUTE_NAVIGATION_LINKS));
        assertEquals(false, ConfigUtils.getDefaultValueBoolean(CoreSettings.class, CoreSettings.TAG_AUTH_ALLOW_ANON_READ));

        // Test reading integer properties as strings
        assertEquals("2", ConfigUtils.getDefaultValue(c, TAG_SEND_WORKER_COUNT));
        assertEquals("2", ConfigUtils.getDefaultValue(c, TAG_RECV_WORKER_COUNT));
        assertEquals("100", ConfigUtils.getDefaultValue(c, TAG_SEND_QUEUE_SIZE));
        assertEquals("100", ConfigUtils.getDefaultValue(c, TAG_RECV_QUEUE_SIZE));
        assertEquals("2", ConfigUtils.getDefaultValue(c, TAG_QOS_LEVEL));
        assertEquals("50", ConfigUtils.getDefaultValue(c, TAG_MAX_IN_FLIGHT));

        // Test reading boolean properties as strings
        assertEquals(Boolean.TRUE.toString(), ConfigUtils.getDefaultValue(CoreSettings.class, CoreSettings.TAG_USE_ABSOLUTE_NAVIGATION_LINKS));
        assertEquals(Boolean.FALSE.toString(), ConfigUtils.getDefaultValue(CoreSettings.class, CoreSettings.TAG_AUTH_ALLOW_ANON_READ));

        // Test invalid properties
        try {
            ConfigUtils.getDefaultValueInt(c, "NOT_A_VALID_INT_PROPERTY");
            Assert.fail("Should have thrown an exception for a non-existing default value.");
        } catch (IllegalArgumentException exc) {
            // This should happen.
        }
        try {
            ConfigUtils.getDefaultValue(c, "NOT_A_VALID_STR_PROPERTY");
            Assert.fail("Should have thrown an exception for a non-existing default value.");
        } catch (IllegalArgumentException exc) {
            // This should happen.
        }
        try {
            ConfigUtils.getDefaultValue(c, "NOT_A_VALID_STR_PROPERTY");
            Assert.fail("Should have thrown an exception for a non-existing default value.");
        } catch (IllegalArgumentException exc) {
            // This should happen.
        }
        // Test configTags
        Set<String> tags = new HashSet<>();
        tags.add(TAG_SEND_WORKER_COUNT);
        tags.add(TAG_RECV_WORKER_COUNT);
        tags.add(TAG_SEND_QUEUE_SIZE);
        tags.add(TAG_RECV_QUEUE_SIZE);
        tags.add(TAG_QOS_LEVEL);
        tags.add(TAG_MAX_IN_FLIGHT);
        tags.add(TAG_MQTT_BROKER);
        tags.add(TAG_TOPIC_NAME);
        assertEquals(tags, ConfigUtils.getConfigTags(c));
        // Test configDefaults
        Map<String, String> configDefaults = ConfigUtils.getConfigDefaults(c);
        assertEquals("tcp://127.0.0.1:1884", configDefaults.get(TAG_MQTT_BROKER));
        assertEquals("FROST-Bus", configDefaults.get(TAG_TOPIC_NAME));
        assertEquals("2", configDefaults.get(TAG_SEND_WORKER_COUNT));
        assertEquals("2", configDefaults.get(TAG_RECV_WORKER_COUNT));
        assertEquals("100", configDefaults.get(TAG_SEND_QUEUE_SIZE));
        assertEquals("100", configDefaults.get(TAG_RECV_QUEUE_SIZE));
        assertEquals("2", configDefaults.get(TAG_QOS_LEVEL));
        assertEquals("50", configDefaults.get(TAG_MAX_IN_FLIGHT));
    }
}
