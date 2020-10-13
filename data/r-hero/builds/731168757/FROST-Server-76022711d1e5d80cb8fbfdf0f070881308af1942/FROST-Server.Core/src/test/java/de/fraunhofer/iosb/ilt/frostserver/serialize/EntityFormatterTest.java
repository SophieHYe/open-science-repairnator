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
package de.fraunhofer.iosb.ilt.frostserver.serialize;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.fraunhofer.iosb.ilt.frostserver.json.serialize.JsonWriter;
import de.fraunhofer.iosb.ilt.frostserver.model.Datastream;
import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.FeatureOfInterest;
import de.fraunhofer.iosb.ilt.frostserver.model.HistoricalLocation;
import de.fraunhofer.iosb.ilt.frostserver.model.Location;
import de.fraunhofer.iosb.ilt.frostserver.model.MultiDatastream;
import de.fraunhofer.iosb.ilt.frostserver.model.Observation;
import de.fraunhofer.iosb.ilt.frostserver.model.ObservedProperty;
import de.fraunhofer.iosb.ilt.frostserver.model.Sensor;
import de.fraunhofer.iosb.ilt.frostserver.model.Thing;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.model.core.EntitySet;
import de.fraunhofer.iosb.ilt.frostserver.model.core.EntitySetImpl;
import de.fraunhofer.iosb.ilt.frostserver.model.core.IdLong;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.TimeInstant;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.UnitOfMeasurement;
import de.fraunhofer.iosb.ilt.frostserver.parser.path.PathParser;
import de.fraunhofer.iosb.ilt.frostserver.parser.query.QueryParser;
import de.fraunhofer.iosb.ilt.frostserver.path.ResourcePath;
import de.fraunhofer.iosb.ilt.frostserver.path.Version;
import de.fraunhofer.iosb.ilt.frostserver.query.Query;
import de.fraunhofer.iosb.ilt.frostserver.query.QueryDefaults;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import de.fraunhofer.iosb.ilt.frostserver.util.SimpleJsonMapper;
import de.fraunhofer.iosb.ilt.frostserver.util.TestHelper;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.joda.time.DateTimeZone;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author jab
 * @author scf
 */
public class EntityFormatterTest {

    private static CoreSettings coreSettings;
    private static QueryDefaults queryDefaults;

    @BeforeClass
    public static void initClass() {
        if (queryDefaults == null) {
            coreSettings = new CoreSettings();
            queryDefaults = coreSettings.getQueryDefaults();
            queryDefaults.setUseAbsoluteNavigationLinks(false);
        }
    }

    @Test
    public void writeThingBasicAbs() throws IOException {
        String expResult
                = "{\n"
                + "\"@iot.id\": 1,\n"
                + "\"@iot.selfLink\": \"http://example.org/v1.0/Things(1)\",\n"
                + "\"Locations@iot.navigationLink\": \"http://example.org/v1.0/Things(1)/Locations\",\n"
                + "\"Datastreams@iot.navigationLink\": \"http://example.org/v1.0/Things(1)/Datastreams\",\n"
                + "\"MultiDatastreams@iot.navigationLink\": \"http://example.org/v1.0/Things(1)/MultiDatastreams\",\n"
                + "\"HistoricalLocations@iot.navigationLink\": \"http://example.org/v1.0/Things(1)/HistoricalLocations\",\n"
                + "\"TaskingCapabilities@iot.navigationLink\": \"http://example.org/v1.0/Things(1)/TaskingCapabilities\",\n"
                + "\"name\": \"This thing is an oven.\",\n"
                + "\"description\": \"This thing is an oven.\",\n"
                + "\"properties\": {\n"
                + "\"owner\": \"John Doe\",\n"
                + "\"color\": \"Silver\"\n"
                + "}\n"
                + "}";
        ResourcePath path = PathParser.parsePath("http://example.org", Version.V_1_0, "/Things(1)");
        Query query = new Query(new QueryDefaults(true, false, 100, 1000), path).validate();
        Thing entity = new Thing()
                .setQuery(query)
                .setId(new IdLong(1))
                .setName("This thing is an oven.")
                .setDescription("This thing is an oven.")
                .addProperty("owner", "John Doe")
                .addProperty("color", "Silver");
        Assert.assertTrue(jsonEqual(expResult, JsonWriter.writeEntity(entity)));
    }

    @Test
    public void writeThingBasicRel() throws IOException {
        String expResult
                = "{\n"
                + "\"@iot.id\": 1,\n"
                + "\"@iot.selfLink\": \"http://example.org/v1.0/Things(1)\",\n"
                + "\"Locations@iot.navigationLink\": \"Things(1)/Locations\",\n"
                + "\"Datastreams@iot.navigationLink\": \"Things(1)/Datastreams\",\n"
                + "\"MultiDatastreams@iot.navigationLink\": \"Things(1)/MultiDatastreams\",\n"
                + "\"HistoricalLocations@iot.navigationLink\": \"Things(1)/HistoricalLocations\",\n"
                + "\"TaskingCapabilities@iot.navigationLink\": \"Things(1)/TaskingCapabilities\",\n"
                + "\"name\": \"This thing is an oven.\",\n"
                + "\"description\": \"This thing is an oven.\",\n"
                + "\"properties\": {\n"
                + "\"owner\": \"John Doe\",\n"
                + "\"color\": \"Silver\"\n"
                + "}\n"
                + "}";
        ResourcePath path = PathParser.parsePath("http://example.org", Version.V_1_0, "/Things(1)");
        Query query = new Query(new QueryDefaults(false, false, 100, 1000), path).validate();
        Thing entity = new Thing()
                .setQuery(query)
                .setId(new IdLong(1))
                .setName("This thing is an oven.")
                .setDescription("This thing is an oven.")
                .addProperty("owner", "John Doe")
                .addProperty("color", "Silver");
        Assert.assertTrue(jsonEqual(expResult, JsonWriter.writeEntity(entity)));
    }

    @Test
    public void writeThingSelect() throws IOException {
        String expResult
                = "{\n"
                + "\"@iot.id\": 1,\n"
                + "\"name\": \"This thing is an oven.\"\n"
                + "}";
        ResourcePath path = PathParser.parsePath("http://example.org", Version.V_1_0, "/Things(1)");
        Query query = QueryParser.parseQuery("$select=id,name", coreSettings, path)
                .validate();
        Thing entity = new Thing()
                .setQuery(query)
                .setId(new IdLong(1))
                .setName("This thing is an oven.")
                .setDescription("This thing is an oven.")
                .addProperty("owner", "John Doe")
                .addProperty("color", "Silver");
        Assert.assertTrue(jsonEqual(expResult, JsonWriter.writeEntity(entity)));
    }

    @Test
    public void writeThingsBasic() throws IOException {
        String thing
                = "{\n"
                + "\"@iot.id\": 1,\n"
                + "\"@iot.selfLink\": \"http://example.org/v1.0/Things(1)\",\n"
                + "\"Locations@iot.navigationLink\": \"Things(1)/Locations\",\n"
                + "\"Datastreams@iot.navigationLink\": \"Things(1)/Datastreams\",\n"
                + "\"MultiDatastreams@iot.navigationLink\": \"Things(1)/MultiDatastreams\",\n"
                + "\"HistoricalLocations@iot.navigationLink\": \"Things(1)/HistoricalLocations\",\n"
                + "\"TaskingCapabilities@iot.navigationLink\": \"Things(1)/TaskingCapabilities\",\n"
                + "\"name\": \"This thing is an oven.\",\n"
                + "\"description\": \"This thing is an oven.\",\n"
                + "\"properties\": {\n"
                + "\"owner\": \"John Doe\",\n"
                + "\"color\": \"Silver\"\n"
                + "}\n"
                + "}";
        String expResult
                = "{ \"value\":[\n"
                + thing + ",\n"
                + thing
                + "]}";
        ResourcePath path = PathParser.parsePath("http://example.org", Version.V_1_0, "/Things");
        Query query = new Query(queryDefaults, path).validate();
        Thing entity = new Thing()
                .setQuery(query)
                .setId(new IdLong(1))
                .setName("This thing is an oven.")
                .setDescription("This thing is an oven.")
                .addProperty("owner", "John Doe")
                .addProperty("color", "Silver");
        EntitySet<Thing> things = new EntitySetImpl<>(EntityType.THING);
        things.add(entity);
        things.add(entity);
        Assert.assertTrue(jsonEqual(expResult, JsonWriter.writeEntityCollection(things)));
    }

    @Test
    public void writeThingOnlyId() throws IOException {
        String expResult = "{\"@iot.id\": 1}";
        ResourcePath path = PathParser.parsePath("http://example.org", Version.V_1_0, "/Things(1)");
        Query query = QueryParser.parseQuery("$select=id", coreSettings, path)
                .validate();
        Thing entity = new Thing()
                .setQuery(query)
                .setId(new IdLong(1))
                .setName("This thing is an oven.")
                .setDescription("This thing is an oven.")
                .addProperty("owner", "John Doe")
                .addProperty("color", "Silver")
                .addDatastream(new Datastream(new IdLong(2)));
        Assert.assertTrue(jsonEqual(expResult, JsonWriter.writeEntity(entity)));
    }

    @Test
    public void writeThingsWithExpandedDatastream() throws IOException {
        String thing
                = "{\n"
                + "\"@iot.id\": 1,\n"
                + "\"@iot.selfLink\": \"http://example.org/v1.0/Things(1)\",\n"
                + "\"Datastreams@iot.count\":1,\n"
                + "\"Datastreams\": [\n"
                + "{\n"
                + "	\"@iot.id\":1,\n"
                + "	\"@iot.selfLink\": \"http://example.org/v1.0/Datastreams(1)\",\n"
                + "	\"name\": \"This is a datastream measuring the temperature in an oven.\",\n"
                + "	\"description\": \"This is a datastream measuring the temperature in an oven.\",\n"
                + "	\"unitOfMeasurement\": {\n"
                + "		\"name\": \"degree Celsius\",\n"
                + "		\"symbol\": \"°C\",\n"
                + "		\"definition\": \"http://unitsofmeasure.org/ucum.html#para-30\"\n"
                + "	},\n"
                + "	\"observationType\": \"http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement\",\n"
                + "	\"observedArea\": {\n"
                + "		\"type\": \"Polygon\",\n"
                + "		\"coordinates\": [[[100.0,0.0],[101.0,0.0],[101.0,1.0],[100.0,1.0],[100.0,0.0]]]\n"
                + "	},\n"
                + "	\"phenomenonTime\": \"2014-03-01T13:00:00.000Z/2015-05-11T15:30:00.000Z\",\n"
                + "	\"resultTime\": \"2014-03-01T13:00:00.000Z/2015-05-11T15:30:00.000Z\""
                + "}\n"
                + "],\n"
                + "\"Datastreams@iot.navigationLink\": \"Things(1)/Datastreams\",\n"
                + "\"MultiDatastreams@iot.navigationLink\": \"Things(1)/MultiDatastreams\",\n"
                + "\"Locations@iot.navigationLink\": \"Things(1)/Locations\",\n"
                + "\"HistoricalLocations@iot.navigationLink\": \"Things(1)/HistoricalLocations\",\n"
                + "\"TaskingCapabilities@iot.navigationLink\": \"Things(1)/TaskingCapabilities\",\n"
                + "\"name\": \"This thing is an oven.\",\n"
                + "\"description\": \"This thing is an oven.\",\n"
                + "\"properties\": {\n"
                + "\"owner\": \"John Doe\",\n"
                + "\"color\": \"Silver\"\n"
                + "}\n"
                + "}";
        String expResult
                = "{ "
                + "\"@iot.count\": 1,\n"
                + "\"value\":[\n"
                + thing
                + "]}";
        ResourcePath path = PathParser.parsePath("http://example.org", Version.V_1_0, "/Things");
        Query query = QueryParser.parseQuery("$expand=Datastreams", coreSettings, path)
                .validate();
        Thing entity = new Thing()
                .setQuery(query)
                .setId(new IdLong(1))
                .addDatastream(new Datastream()
                        .setQuery(query.getExpand().get(0).getSubQuery())
                        .setId(new IdLong(1))
                        .setName("This is a datastream measuring the temperature in an oven.")
                        .setDescription("This is a datastream measuring the temperature in an oven.")
                        .setUnitOfMeasurement(new UnitOfMeasurement()
                                .setName("degree Celsius")
                                .setSymbol("°C")
                                .setDefinition("http://unitsofmeasure.org/ucum.html#para-30")
                        )
                        .setObservationType("http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement")
                        .setObservedArea(TestHelper.getPolygon(2, 100, 0, 101, 0, 101, 1, 100, 1, 100, 0))
                        .setPhenomenonTime(TestHelper.createTimeInterval(2014, 03, 1, 13, 0, 0, 2015, 05, 11, 15, 30, 0, DateTimeZone.UTC))
                        .setResultTime(TestHelper.createTimeInterval(2014, 03, 01, 13, 0, 0, 2015, 05, 11, 15, 30, 0, DateTimeZone.UTC))
                )
                .setName("This thing is an oven.")
                .setDescription("This thing is an oven.")
                .addProperty("owner", "John Doe")
                .addProperty("color", "Silver");
        entity.getDatastreams().setCount(1);
        EntitySet<Thing> things = new EntitySetImpl<>(EntityType.THING);
        things.add(entity);
        things.setCount(1);
        Assert.assertTrue(jsonEqual(expResult, JsonWriter.writeEntityCollection(things)));
    }

    @Test
    public void writeThingWithExpandedDatastream1() throws IOException {
        String expResult
                = "{\n"
                + "\"@iot.id\": 1,\n"
                + "\"@iot.selfLink\": \"http://example.org/v1.0/Things(1)\",\n"
                + "\"Datastreams\": [{\"@iot.id\":123}],\n"
                + "\"name\": \"This thing is an oven.\",\n"
                + "\"description\": \"This thing is an oven.\",\n"
                + "\"properties\": {\n"
                + "\"owner\": \"John Doe\",\n"
                + "\"color\": \"Silver\"\n"
                + "},\n"
                + "\"Datastreams@iot.navigationLink\": \"Things(1)/Datastreams\",\n"
                + "\"MultiDatastreams@iot.navigationLink\": \"Things(1)/MultiDatastreams\",\n"
                + "\"Locations@iot.navigationLink\": \"Things(1)/Locations\",\n"
                + "\"HistoricalLocations@iot.navigationLink\": \"Things(1)/HistoricalLocations\",\n"
                + "\"TaskingCapabilities@iot.navigationLink\": \"Things(1)/TaskingCapabilities\"\n"
                + "}";
        ResourcePath path = PathParser.parsePath("http://example.org", Version.V_1_0, "/Things");
        Query query = QueryParser.parseQuery("$expand=Datastreams($select=id)", coreSettings, path)
                .validate();
        Thing entity = new Thing()
                .setQuery(query)
                .setId(new IdLong(1))
                .setLocations(new EntitySetImpl(EntityType.LOCATION))
                .addDatastream(new Datastream()
                        .setQuery(query.getExpand().get(0).getSubQuery())
                        .setId(new IdLong(123))
                )
                .setName("This thing is an oven.")
                .setDescription("This thing is an oven.")
                .addProperty("owner", "John Doe")
                .addProperty("color", "Silver");
        Assert.assertTrue(jsonEqual(expResult, JsonWriter.writeEntity(entity)));
    }

    @Test
    public void writeThingWithExpandedDatastream2() throws IOException {
        String expResult = "{\n"
                + "\"@iot.id\": 1,\n"
                + "\"Locations@iot.navigationLink\": \"Things(1)/Locations\",\n"
                + "\"Datastreams\": [{\"@iot.id\":123}],\n"
                + "\"name\": \"This thing is an oven.\"\n"
                + "}";
        ResourcePath path = PathParser.parsePath("http://example.org", Version.V_1_0, "/Things");
        Query query = QueryParser.parseQuery("$select=id,name,Locations&$expand=Datastreams($select=id)", coreSettings, path)
                .validate();
        Thing entity = new Thing()
                .setQuery(query)
                .setId(new IdLong(1))
                .addDatastream(new Datastream()
                        .setQuery(query.getExpand().get(0).getSubQuery())
                        .setId(new IdLong(123))
                )
                .setName("This thing is an oven.")
                .setDescription("This thing is an oven.")
                .addProperty("owner", "John Doe")
                .addProperty("color", "Silver");
        Assert.assertTrue(jsonEqual(expResult, JsonWriter.writeEntity(entity)));
    }

    @Test
    public void writeThingWithExpandedDatastream3() throws IOException {
        String expResult = "{\n"
                + "\"@iot.selfLink\": \"http://example.org/v1.0/Things(1)\",\n"
                + "\"Locations@iot.navigationLink\": \"Things(1)/Locations\",\n"
                + "\"Datastreams\": [{\"@iot.id\":123, \"@iot.selfLink\": \"http://example.org/v1.0/Datastreams(123)\"}],\n"
                + "\"name\": \"This thing is an oven.\"\n"
                + "}";
        ResourcePath path = PathParser.parsePath("http://example.org", Version.V_1_0, "/Things");
        Query query = QueryParser.parseQuery("$select=@iot.selfLink,name,Locations&$expand=Datastreams($select=@iot.selfLink,id)", coreSettings, path)
                .validate();
        Thing entity = new Thing()
                .setQuery(query)
                .setId(new IdLong(1))
                .setLocations(new EntitySetImpl(EntityType.LOCATION))
                .addDatastream(new Datastream()
                        .setQuery(query.getExpand().get(0).getSubQuery())
                        .setId(new IdLong(123))
                )
                .setHistoricalLocations(new EntitySetImpl(EntityType.HISTORICALLOCATION))
                .setName("This thing is an oven.")
                .setDescription("This thing is an oven.")
                .addProperty("owner", "John Doe")
                .addProperty("color", "Silver");
        Assert.assertTrue(jsonEqual(expResult, JsonWriter.writeEntity(entity)));
    }

    @Test
    public void writeThingWithExpandedDatastream4() throws IOException {
        String expResult = "{\n"
                + "  \"@iot.id\": 1,\n"
                + "  \"Datastreams\": [],\n"
                + "  \"name\": \"This thing is an oven.\"\n"
                + "}";
        ResourcePath path = PathParser.parsePath("http://example.org", Version.V_1_0, "/Things");
        Query query = QueryParser.parseQuery("$select=id,name&$expand=Datastreams", coreSettings, path)
                .validate();
        Thing entity = new Thing()
                .setQuery(query)
                .setId(new IdLong(1))
                .setLocations(new EntitySetImpl(EntityType.LOCATION))
                .setHistoricalLocations(new EntitySetImpl(EntityType.HISTORICALLOCATION))
                .setName("This thing is an oven.")
                .setDescription("This thing is an oven.")
                .addProperty("owner", "John Doe")
                .addProperty("color", "Silver");
        Assert.assertTrue(jsonEqual(expResult, JsonWriter.writeEntity(entity)));
    }

    @Test
    public void writeLocationBasic() throws IOException {
        {
            String expResult
                    = "{\n"
                    + "	\"@iot.id\": 1,\n"
                    + "	\"@iot.selfLink\": \"http://example.org/v1.0/Locations(1)\",\n"
                    + "	\"Things@iot.navigationLink\": \"Locations(1)/Things\",\n"
                    + "	\"HistoricalLocations@iot.navigationLink\": \"Locations(1)/HistoricalLocations\",\n"
                    + "	\"encodingType\": \"application/vnd.geo+json\""
                    + "}";
            ResourcePath path = PathParser.parsePath("http://example.org", Version.V_1_0, "/Locations(1)");
            Query query = QueryParser.parseQuery("$select=id,@iot.selfLink,encodingType,Things,HistoricalLocations", coreSettings, path)
                    .validate();
            Entity entity = new Location()
                    .setQuery(query)
                    .setId(new IdLong(1))
                    .setThings(new EntitySetImpl(EntityType.THING))
                    .setHistoricalLocations(new EntitySetImpl(EntityType.HISTORICALLOCATION))
                    .setEncodingType("application/vnd.geo+json");
            Assert.assertTrue(jsonEqual(expResult, JsonWriter.writeEntity(entity)));
        }
        {
            String expResult
                    = "{\n"
                    + "	\"@iot.id\": 1,\n"
                    + "	\"@iot.selfLink\": \"http://example.org/v1.0/Locations(1)\",\n"
                    + "	\"Things@iot.navigationLink\": \"Locations(1)/Things\",\n"
                    + "	\"HistoricalLocations@iot.navigationLink\": \"Locations(1)/HistoricalLocations\",\n"
                    + "	\"encodingType\": \"application/geo+json\""
                    + "}";
            ResourcePath path = PathParser.parsePath("http://example.org", Version.V_1_0, "/Locations(1)");
            Query query = QueryParser.parseQuery("", coreSettings, path)
                    .validate();
            Entity entity = new Location()
                    .setQuery(query)
                    .setId(new IdLong(1))
                    .setEncodingType("application/geo+json");
            Assert.assertTrue(jsonEqual(expResult, JsonWriter.writeEntity(entity)));
        }
    }

    @Test
    public void writeLocationWithGeoJsonLocation() throws IOException {
        String expResult
                = "{\n"
                + "	\"@iot.id\": 1,\n"
                + "	\"@iot.selfLink\": \"http://example.org/v1.0/Locations(1)\",\n"
                + "	\"Things@iot.navigationLink\": \"Locations(1)/Things\",\n"
                + "	\"HistoricalLocations@iot.navigationLink\": \"Locations(1)/HistoricalLocations\",\n"
                + "	\"encodingType\": \"application/geo+json\""
                + ",\n"
                + "	\"location\": \n"
                + "	{\n"
                + "		\"type\": \"Feature\",\n"
                + "		\"geometry\":\n"
                + "		{\n"
                + "			\"type\": \"Point\",\n"
                + "			\"coordinates\": [-114.06,51.05]\n"
                + "		}\n"
                + "	}\n"
                + "}";
        ResourcePath path = PathParser.parsePath("http://example.org", Version.V_1_0, "/Locations(1)");
        Query query = QueryParser.parseQuery("", coreSettings, path)
                .validate();
        Entity entity = new Location()
                .setQuery(query)
                .setId(new IdLong(1))
                .setEncodingType("application/geo+json")
                .setLocation(TestHelper.getFeatureWithPoint(-114.06, 51.05));
        Assert.assertTrue(jsonEqual(expResult, JsonWriter.writeEntity(entity)));
    }

    @Test
    public void writeHistoricalLocationBasic() throws IOException {
        String expResult
                = "{\n"
                + "	\"@iot.id\": 1,\n"
                + "	\"@iot.selfLink\": \"http://example.org/v1.0/HistoricalLocations(1)\",\n"
                + "	\"Locations@iot.navigationLink\": \"HistoricalLocations(1)/Locations\",\n"
                + "	\"Thing@iot.navigationLink\": \"HistoricalLocations(1)/Thing\",\n"
                + "	\"time\": \"2015-01-25T19:00:00.000Z\"\n"
                + "}";
        ResourcePath path = PathParser.parsePath("http://example.org", Version.V_1_0, "/HistoricalLocations(1)");
        Query query = QueryParser.parseQuery("", coreSettings, path)
                .validate();
        Entity entity = new HistoricalLocation()
                .setQuery(query)
                .setId(new IdLong(1))
                .setTime(TestHelper.createTimeInstant(2015, 01, 25, 12, 0, 0, DateTimeZone.forOffsetHours(-7), DateTimeZone.UTC));
        Assert.assertTrue(jsonEqual(expResult, JsonWriter.writeEntity(entity)));
    }

    @Test
    public void writeDatastreamBasic() throws IOException {
        String expResult
                = "{\n"
                + "	\"@iot.id\": 1,\n"
                + "	\"@iot.selfLink\": \"http://example.org/v1.0/Datastreams(1)\",\n"
                + "	\"Thing@iot.navigationLink\": \"Datastreams(1)/Thing\",\n"
                + "	\"Sensor@iot.navigationLink\": \"Datastreams(1)/Sensor\",\n"
                + "	\"ObservedProperty@iot.navigationLink\": \"Datastreams(1)/ObservedProperty\",\n"
                + "	\"Observations@iot.navigationLink\": \"Datastreams(1)/Observations\",\n"
                + "	\"name\": \"This is a datastream measuring the temperature in an oven.\",\n"
                + "	\"description\": \"This is a datastream measuring the temperature in an oven.\",\n"
                + "	\"unitOfMeasurement\": \n"
                + "	{\n"
                + "		\"name\": \"degree Celsius\",\n"
                + "		\"symbol\": \"°C\",\n"
                + "		\"definition\": \"http://unitsofmeasure.org/ucum.html#para-30\"\n"
                + "	},\n"
                + "	\"observationType\": \"http://www.opengis.net/def/observationType/OGCOM/2.0/OM_Measurement\",\n"
                + "	\"phenomenonTime\": \"2014-03-01T13:00:00.000Z/2015-05-11T15:30:00.000Z\",\n"
                + "	\"resultTime\": \"2014-03-01T13:00:00.000Z/2015-05-11T15:30:00.000Z\"\n"
                + "}";
        ResourcePath path = PathParser.parsePath("http://example.org", Version.V_1_0, "/Datastreams(1)");
        Query query = QueryParser.parseQuery("", coreSettings, path)
                .validate();
        Entity entity = new Datastream()
                .setQuery(query)
                .setId(new IdLong(1))
                .setName("This is a datastream measuring the temperature in an oven.")
                .setDescription("This is a datastream measuring the temperature in an oven.")
                .setUnitOfMeasurement(new UnitOfMeasurement()
                        .setName("degree Celsius")
                        .setSymbol("°C")
                        .setDefinition("http://unitsofmeasure.org/ucum.html#para-30")
                )
                .setObservationType("http://www.opengis.net/def/observationType/OGCOM/2.0/OM_Measurement")
                .setPhenomenonTime(TestHelper.createTimeInterval(2014, 03, 1, 13, 0, 0, 2015, 05, 11, 15, 30, 0, DateTimeZone.UTC))
                .setResultTime(TestHelper.createTimeInterval(2014, 03, 01, 13, 0, 0, 2015, 05, 11, 15, 30, 0, DateTimeZone.UTC));
        Assert.assertTrue(jsonEqual(expResult, JsonWriter.writeEntity(entity)));
    }

    @Test
    public void writeDatastreamWithEmptyUnitOfMeasurement() throws IOException {
        String expResult
                = "{\n"
                + "	\"@iot.id\": 1,\n"
                + "	\"@iot.selfLink\": \"http://example.org/v1.0/Datastreams(1)\",\n"
                + "	\"Thing@iot.navigationLink\": \"Datastreams(1)/Thing\",\n"
                + "	\"Sensor@iot.navigationLink\": \"Datastreams(1)/Sensor\",\n"
                + "	\"ObservedProperty@iot.navigationLink\": \"Datastreams(1)/ObservedProperty\",\n"
                + "	\"Observations@iot.navigationLink\": \"Datastreams(1)/Observations\",\n"
                + "	\"name\": \"This is a datastream measuring the temperature in an oven.\",\n"
                + "	\"description\": \"This is a datastream measuring the temperature in an oven.\",\n"
                + "	\"unitOfMeasurement\": \n"
                + "	{\n"
                + "		\"name\": null,\n"
                + "		\"symbol\": null,\n"
                + "		\"definition\": null\n"
                + "	},\n"
                + "	\"observationType\": \"http://www.opengis.net/def/observationType/OGCOM/2.0/OM_Measurement\",\n"
                + "	\"phenomenonTime\": \"2014-03-01T13:00:00.000Z/2015-05-11T15:30:00.000Z\",\n"
                + "	\"resultTime\": \"2014-03-01T13:00:00.000Z/2015-05-11T15:30:00.000Z\"\n"
                + "}";
        ResourcePath path = PathParser.parsePath("http://example.org", Version.V_1_0, "/Datastreams(1)");
        Query query = QueryParser.parseQuery("", coreSettings, path)
                .validate();
        Entity entity = new Datastream()
                .setQuery(query)
                .setId(new IdLong(1))
                .setUnitOfMeasurement(new UnitOfMeasurement())
                .setName("This is a datastream measuring the temperature in an oven.")
                .setDescription("This is a datastream measuring the temperature in an oven.")
                .setObservationType("http://www.opengis.net/def/observationType/OGCOM/2.0/OM_Measurement")
                .setPhenomenonTime(TestHelper.createTimeInterval(2014, 03, 1, 13, 0, 0, 2015, 05, 11, 15, 30, 0, DateTimeZone.UTC))
                .setResultTime(TestHelper.createTimeInterval(2014, 03, 01, 13, 0, 0, 2015, 05, 11, 15, 30, 0, DateTimeZone.UTC));
        Assert.assertTrue(jsonEqual(expResult, JsonWriter.writeEntity(entity)));
    }

    @Test
    public void writeDatastreamWithObservedAreaGeoJsonPolygon() throws IOException {
        String expResult
                = "{\n"
                + "	\"@iot.id\": 1,\n"
                + "	\"@iot.selfLink\": \"http://example.org/v1.0/Datastreams(1)\",\n"
                + "	\"Thing@iot.navigationLink\": \"Datastreams(1)/Thing\",\n"
                + "	\"Sensor@iot.navigationLink\": \"Datastreams(1)/Sensor\",\n"
                + "	\"ObservedProperty@iot.navigationLink\": \"Datastreams(1)/ObservedProperty\",\n"
                + "	\"Observations@iot.navigationLink\": \"Datastreams(1)/Observations\",\n"
                + "	\"name\": \"This is a datastream measuring the temperature in an oven.\",\n"
                + "	\"description\": \"This is a datastream measuring the temperature in an oven.\",\n"
                + "	\"unitOfMeasurement\": \n"
                + "	{\n"
                + "		\"name\": \"degree Celsius\",\n"
                + "		\"symbol\": \"°C\",\n"
                + "		\"definition\": \"http://unitsofmeasure.org/ucum.html#para-30\"\n"
                + "	},\n"
                + "	\"observationType\": \"http://www.opengis.net/def/observationType/OGCOM/2.0/OM_Measurement\",\n"
                + "	\"observedArea\": \n"
                + "	{\n"
                + "		\"type\": \"Polygon\",\n"
                + "		\"coordinates\": [[[100.0,0.0],[101.0,0.0],[101.0,1.0],[100.0,1.0],[100.0,0.0]]]\n"
                + "	},\n"
                + "	\"phenomenonTime\": \"2014-03-01T13:00:00.000Z/2015-05-11T15:30:00.000Z\",\n"
                + "	\"resultTime\": \"2014-03-01T13:00:00.000Z/2015-05-11T15:30:00.000Z\"\n"
                + "}";
        ResourcePath path = PathParser.parsePath("http://example.org", Version.V_1_0, "/Datastreams(1)");
        Query query = QueryParser.parseQuery("", coreSettings, path)
                .validate();
        Entity entity = new Datastream()
                .setQuery(query)
                .setId(new IdLong(1))
                .setName("This is a datastream measuring the temperature in an oven.")
                .setDescription("This is a datastream measuring the temperature in an oven.")
                .setUnitOfMeasurement(new UnitOfMeasurement()
                        .setName("degree Celsius")
                        .setSymbol("°C")
                        .setDefinition("http://unitsofmeasure.org/ucum.html#para-30")
                )
                .setObservationType("http://www.opengis.net/def/observationType/OGCOM/2.0/OM_Measurement")
                .setObservedArea(TestHelper.getPolygon(2, 100, 0, 101, 0, 101, 1, 100, 1, 100, 0))
                .setPhenomenonTime(TestHelper.createTimeInterval(2014, 03, 1, 13, 0, 0, 2015, 05, 11, 15, 30, 0, DateTimeZone.UTC))
                .setResultTime(TestHelper.createTimeInterval(2014, 03, 01, 13, 0, 0, 2015, 05, 11, 15, 30, 0, DateTimeZone.UTC));
        Assert.assertTrue(jsonEqual(expResult, JsonWriter.writeEntity(entity)));
    }

    @Test
    public void writeMultiDatastreamBasic() throws IOException {
        String expResult
                = "{\n"
                + "	\"@iot.id\": 1,\n"
                + "	\"@iot.selfLink\": \"http://example.org/v1.0/MultiDatastreams(1)\",\n"
                + "	\"Thing@iot.navigationLink\": \"MultiDatastreams(1)/Thing\",\n"
                + "	\"Sensor@iot.navigationLink\": \"MultiDatastreams(1)/Sensor\",\n"
                + "	\"ObservedProperties@iot.navigationLink\": \"MultiDatastreams(1)/ObservedProperties\",\n"
                + "	\"Observations@iot.navigationLink\": \"MultiDatastreams(1)/Observations\",\n"
                + "	\"name\": \"This is a datastream measuring the wind.\",\n"
                + "	\"description\": \"This is a datastream measuring wind direction and speed.\",\n"
                + " \"unitOfMeasurements\": [\n"
                + "  {\n"
                + "   \"name\": \"DegreeAngle\",\n"
                + "   \"symbol\": \"deg\",\n"
                + "   \"definition\": \"http://www.qudt.org/qudt/owl/1.0.0/unit/Instances.html#DegreeAngle\"\n"
                + "  },\n"
                + "  {\n"
                + "   \"name\": \"MeterPerSecond\",\n"
                + "   \"symbol\": \"m/s\",\n"
                + "   \"definition\": \"http://www.qudt.org/qudt/owl/1.0.0/unit/Instances.html#MeterPerSecond\"\n"
                + "  }\n"
                + " ],\n"
                + "	\"observationType\": \"http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_ComplexObservation\",\n"
                + " \"multiObservationDataTypes\": [\n"
                + "  \"http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement\",\n"
                + "  \"http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement\"\n"
                + " ],\n"
                + "	\"phenomenonTime\": \"2014-03-01T13:00:00.000Z/2015-05-11T15:30:00.000Z\",\n"
                + "	\"resultTime\": \"2014-03-01T13:00:00.000Z/2015-05-11T15:30:00.000Z\"\n"
                + "}";
        ResourcePath path = PathParser.parsePath("http://example.org", Version.V_1_0, "/MultiDatastreams(1)");
        Query query = QueryParser.parseQuery("", coreSettings, path)
                .validate();
        Entity entity = new MultiDatastream()
                .setQuery(query)
                .setId(new IdLong(1))
                .setName("This is a datastream measuring the wind.")
                .setDescription("This is a datastream measuring wind direction and speed.")
                .addUnitOfMeasurement(new UnitOfMeasurement()
                        .setName("DegreeAngle")
                        .setSymbol("deg")
                        .setDefinition("http://www.qudt.org/qudt/owl/1.0.0/unit/Instances.html#DegreeAngle")
                )
                .addUnitOfMeasurement(new UnitOfMeasurement()
                        .setName("MeterPerSecond")
                        .setSymbol("m/s")
                        .setDefinition("http://www.qudt.org/qudt/owl/1.0.0/unit/Instances.html#MeterPerSecond")
                )
                .addObservationType("http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement")
                .addObservationType("http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement")
                .setPhenomenonTime(TestHelper.createTimeInterval(2014, 03, 1, 13, 0, 0, 2015, 05, 11, 15, 30, 0, DateTimeZone.UTC))
                .setResultTime(TestHelper.createTimeInterval(2014, 03, 01, 13, 0, 0, 2015, 05, 11, 15, 30, 0, DateTimeZone.UTC));
        Assert.assertTrue(jsonEqual(expResult, JsonWriter.writeEntity(entity)));
    }

    @Test
    public void writeSensorBasic() throws IOException {
        String expResult
                = "{\n"
                + "	\"@iot.id\": 1,\n"
                + "	\"@iot.selfLink\": \"http://example.org/v1.0/Sensors(1)\",\n"
                + "	\"Datastreams@iot.navigationLink\": \"Sensors(1)/Datastreams\",\n"
                + "	\"MultiDatastreams@iot.navigationLink\": \"Sensors(1)/MultiDatastreams\",\n"
                + "	\"name\": \"TMP36 - Analog Temperature sensor\",\n"
                + "	\"description\": \"TMP36 - Analog Temperature sensor\",\n"
                + "	\"encodingType\": \"application/pdf\",\n"
                + "	\"metadata\": \"http://example.org/TMP35_36_37.pdf\"\n"
                + "}";
        ResourcePath path = PathParser.parsePath("http://example.org", Version.V_1_0, "/Sensors(1)");
        Query query = QueryParser.parseQuery("", coreSettings, path)
                .validate();
        Entity entity = new Sensor()
                .setQuery(query)
                .setId(new IdLong(1))
                .setName("TMP36 - Analog Temperature sensor")
                .setDescription("TMP36 - Analog Temperature sensor")
                .setEncodingType("application/pdf")
                .setMetadata("http://example.org/TMP35_36_37.pdf");
        Assert.assertTrue(jsonEqual(expResult, JsonWriter.writeEntity(entity)));
    }

    @Test
    public void writeSensorEmptyDatastreamsCollection() throws IOException {
        String expResult
                = "{\n"
                + "	\"@iot.id\": 1,\n"
                + "	\"name\": \"TMP36 - Analog Temperature sensor\",\n"
                + "	\"description\": \"TMP36 - Analog Temperature sensor\",\n"
                + "	\"encodingType\": \"application/pdf\",\n"
                + "	\"metadata\": \"http://example.org/TMP35_36_37.pdf\"\n,"
                + " \"Datastreams\": []"
                + "}";
        ResourcePath path = PathParser.parsePath("http://example.org", Version.V_1_0, "/Sensors(1)");
        Query query = QueryParser.parseQuery("$select=id,name,description,encodingType,metadata&$expand=Datastreams", coreSettings, path)
                .validate();
        Entity entity = new Sensor()
                .setQuery(query)
                .setId(new IdLong(1))
                .setName("TMP36 - Analog Temperature sensor")
                .setDescription("TMP36 - Analog Temperature sensor")
                .setEncodingType("application/pdf")
                .setMetadata("http://example.org/TMP35_36_37.pdf");
        Assert.assertTrue(jsonEqual(expResult, JsonWriter.writeEntity(entity)));
    }

    @Test
    public void writeObservedPropertyBasic() throws IOException {
        String expResult
                = "{\n"
                + "	\"@iot.id\": 1,\n"
                + "	\"@iot.selfLink\": \"http://example.org/v1.0/ObservedProperties(1)\",\n"
                + "	\"Datastreams@iot.navigationLink\": \"ObservedProperties(1)/Datastreams\",\n"
                + "	\"MultiDatastreams@iot.navigationLink\": \"ObservedProperties(1)/MultiDatastreams\",\n"
                + "	\"description\": \"The dewpoint temperature is the temperature to which the air must be cooled, at constant pressure, for dew to form. As the grass and other objects near the ground cool to the dewpoint, some of the water vapor in the atmosphere condenses into liquid water on the objects.\",\n"
                + "	\"name\": \"DewPoint Temperature\",\n"
                + "	\"definition\": \"http://dbpedia.org/page/Dew_point\"\n"
                + "}";
        ResourcePath path = PathParser.parsePath("http://example.org", Version.V_1_0, "/ObservedProperties(1)");
        Query query = QueryParser.parseQuery("", coreSettings, path)
                .validate();
        Entity entity = new ObservedProperty()
                .setQuery(query)
                .setId(new IdLong(1))
                .setDescription("The dewpoint temperature is the temperature to which the air must be cooled, at constant pressure, for dew to form. As the grass and other objects near the ground cool to the dewpoint, some of the water vapor in the atmosphere condenses into liquid water on the objects.")
                .setName("DewPoint Temperature")
                .setDefinition("http://dbpedia.org/page/Dew_point");
        Assert.assertTrue(jsonEqual(expResult, JsonWriter.writeEntity(entity)));
    }

    @Test
    public void writeObservationBasic() throws IOException {
        String expResult
                = "{\n"
                + "	\"@iot.id\": 1,\n"
                + "	\"@iot.selfLink\": \"http://example.org/v1.0/Observations(1)\",\n"
                + "	\"FeatureOfInterest@iot.navigationLink\": \"Observations(1)/FeatureOfInterest\",\n"
                + "	\"Datastream@iot.navigationLink\":\"Observations(1)/Datastream\",\n"
                + "	\"MultiDatastream@iot.navigationLink\":\"Observations(1)/MultiDatastream\",\n"
                + "	\"phenomenonTime\": \"2014-12-31T11:59:59.000Z\",\n"
                + "	\"resultTime\": \"2014-12-31T19:59:59.000Z\",\n"
                + "	\"result\": 70.40\n"
                + "}";
        ResourcePath path = PathParser.parsePath("http://example.org", Version.V_1_0, "/Observations(1)");
        Query query = QueryParser.parseQuery("", coreSettings, path)
                .validate();
        Entity entity = new Observation()
                .setQuery(query)
                .setId(new IdLong(1))
                .setPhenomenonTime(TestHelper.createTimeInstantUTC(2014, 12, 31, 11, 59, 59))
                .setResultTime(TestHelper.createTimeInstantUTC(2014, 12, 31, 19, 59, 59))
                .setResult(new BigDecimal("70.40"));
        Assert.assertTrue(jsonEqual(expResult, JsonWriter.writeEntity(entity)));
    }

    @Test
    public void writeObservationBasicWithNullResult() throws IOException {
        String expResult
                = "{\n"
                + "	\"@iot.id\": 1,\n"
                + "	\"@iot.selfLink\": \"http://example.org/v1.0/Observations(1)\",\n"
                + "	\"FeatureOfInterest@iot.navigationLink\": \"Observations(1)/FeatureOfInterest\",\n"
                + "	\"Datastream@iot.navigationLink\":\"Observations(1)/Datastream\",\n"
                + "	\"MultiDatastream@iot.navigationLink\":\"Observations(1)/MultiDatastream\",\n"
                + "	\"phenomenonTime\": \"2014-12-31T11:59:59.000Z\",\n"
                + "	\"resultTime\": \"2014-12-31T19:59:59.000Z\",\n"
                + "	\"result\": null\n"
                + "}";
        ResourcePath path = PathParser.parsePath("http://example.org", Version.V_1_0, "/Observations(1)");
        Query query = QueryParser.parseQuery("", coreSettings, path)
                .validate();
        Entity entity = new Observation()
                .setQuery(query)
                .setId(new IdLong(1))
                .setPhenomenonTime(TestHelper.createTimeInstantUTC(2014, 12, 31, 11, 59, 59))
                .setResultTime(TestHelper.createTimeInstantUTC(2014, 12, 31, 19, 59, 59))
                .setResult(null);
        Assert.assertTrue(jsonEqual(expResult, JsonWriter.writeEntity(entity)));
    }

    @Test
    public void writeObservationWithEmptyResultTime() throws IOException {
        String expResult
                = "{\n"
                + "	\"@iot.id\": 1,\n"
                + "	\"@iot.selfLink\": \"http://example.org/v1.0/Observations(1)\",\n"
                + "	\"FeatureOfInterest@iot.navigationLink\": \"Observations(1)/FeatureOfInterest\",\n"
                + "	\"Datastream@iot.navigationLink\":\"Observations(1)/Datastream\",\n"
                + "	\"MultiDatastream@iot.navigationLink\":\"Observations(1)/MultiDatastream\",\n"
                + "	\"phenomenonTime\": \"2014-12-31T11:59:59.000Z\",\n"
                + "	\"resultTime\": null,\n"
                + "	\"result\": \"70.4\"\n"
                + "}";
        ResourcePath path = PathParser.parsePath("http://example.org", Version.V_1_0, "/Observations(1)");
        Query query = QueryParser.parseQuery("", coreSettings, path)
                .validate();
        Entity entity = new Observation()
                .setQuery(query)
                .setId(new IdLong(1))
                .setPhenomenonTime(TestHelper.createTimeInstantUTC(2014, 12, 31, 11, 59, 59))
                .setResultTime(new TimeInstant(null))
                .setResult("70.4");
        Assert.assertTrue(jsonEqual(expResult, JsonWriter.writeEntity(entity)));
    }

    @Test
    public void writeFeatureOfInterstBasic() throws IOException {
        String expResult
                = "{\n"
                + "	\"@iot.id\": 1,\n"
                + "	\"@iot.selfLink\": \"http://example.org/v1.0/FeaturesOfInterest(1)\",\n"
                + "	\"Observations@iot.navigationLink\": \"FeaturesOfInterest(1)/Observations\",\n"
                + "	\"name\": \"This is a weather station.\",\n"
                + "	\"description\": \"This is a weather station.\",\n"
                + "	\"encodingType\": \"application/geo+json\""
                + "}";
        ResourcePath path = PathParser.parsePath("http://example.org", Version.V_1_0, "/FeaturesOfInterest(1)");
        Query query = QueryParser.parseQuery("", coreSettings, path)
                .validate();
        Entity entity = new FeatureOfInterest()
                .setQuery(query)
                .setId(new IdLong(1))
                .setName("This is a weather station.")
                .setDescription("This is a weather station.")
                .setEncodingType("application/geo+json");
        Assert.assertTrue(jsonEqual(expResult, JsonWriter.writeEntity(entity)));
    }

    @Test
    public void writeFeatureOfInterstWithGeoJsonPointFeature() throws IOException {
        String expResult
                = "{\n"
                + "	\"@iot.id\": 1,\n"
                + "	\"@iot.selfLink\": \"http://example.org/v1.0/FeaturesOfInterest(1)\",\n"
                + "	\"Observations@iot.navigationLink\": \"FeaturesOfInterest(1)/Observations\",\n"
                + "	\"name\": \"This is a weather station.\",\n"
                + "	\"description\": \"This is a weather station.\",\n"
                + "	\"encodingType\": \"application/vnd.geo+json\""
                + ",\n"
                + "	\"feature\": \n"
                + "	{\n"
                + "		\"type\": \"Feature\",\n"
                + "		\"geometry\":\n"
                + "		{\n"
                + "			\"type\": \"Point\",\n"
                + "			\"coordinates\": [-114.06,51.05]\n"
                + "		}\n"
                + "	}\n"
                + "}";
        ResourcePath path = PathParser.parsePath("http://example.org", Version.V_1_0, "/FeaturesOfInterest(1)");
        Query query = QueryParser.parseQuery("", coreSettings, path)
                .validate();
        Entity entity = new FeatureOfInterest()
                .setQuery(query)
                .setId(new IdLong(1))
                .setName("This is a weather station.")
                .setDescription("This is a weather station.")
                .setEncodingType("application/vnd.geo+json")
                .setFeature(TestHelper.getFeatureWithPoint(-114.06, 51.05));
        Assert.assertTrue(jsonEqual(expResult, JsonWriter.writeEntity(entity)));
    }

    private boolean jsonEqual(String string1, String string2) {
        ObjectMapper mapper = SimpleJsonMapper.getSimpleObjectMapper();
        try {
            JsonNode json1 = mapper.readTree(string1);
            JsonNode json2 = mapper.readTree(string2);
            return json1.equals(json2);
        } catch (IOException ex) {
            Logger.getLogger(EntityFormatterTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

}
