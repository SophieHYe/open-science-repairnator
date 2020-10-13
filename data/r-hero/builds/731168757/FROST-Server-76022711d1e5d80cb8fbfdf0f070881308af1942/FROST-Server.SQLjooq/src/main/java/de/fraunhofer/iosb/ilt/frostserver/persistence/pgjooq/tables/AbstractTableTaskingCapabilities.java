package de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables;

import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.bindings.JsonBinding;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.bindings.JsonValue;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.relations.RelationOneToMany;
import org.jooq.Field;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultDataType;
import org.jooq.impl.SQLDataType;

public abstract class AbstractTableTaskingCapabilities<J extends Comparable> extends StaTableAbstract<J> {

    private static final long serialVersionUID = -1460005950;

    /**
     * The column <code>public.TASKINGCAPABILITIES.DESCRIPTION</code>.
     */
    public final TableField<Record, String> colDescription = createField(DSL.name("DESCRIPTION"), SQLDataType.CLOB, this, "");

    /**
     * The column <code>public.TASKINGCAPABILITIES.NAME</code>.
     */
    public final TableField<Record, String> colName = createField(DSL.name("NAME"), SQLDataType.CLOB.defaultValue(DSL.field("'no name'::text", SQLDataType.CLOB)), this, "");

    /**
     * The column <code>public.TASKINGCAPABILITIES.PROPERTIES</code>.
     */
    public final TableField<Record, JsonValue> colProperties = createField(DSL.name("PROPERTIES"), DefaultDataType.getDefaultDataType(TYPE_JSONB), this, "", new JsonBinding());

    /**
     * The column <code>public.TASKINGCAPABILITIES.TASKING_PARAMETERS</code>.
     */
    public final TableField<Record, JsonValue> colTaskingParameters = createField(DSL.name("TASKING_PARAMETERS"), DefaultDataType.getDefaultDataType(TYPE_JSONB), this, "", new JsonBinding());

    /**
     * Create a <code>public.TASKINGCAPABILITIES</code> table reference
     */
    protected AbstractTableTaskingCapabilities() {
        this(DSL.name("TASKINGCAPABILITIES"), null);
    }

    protected AbstractTableTaskingCapabilities(Name alias, AbstractTableTaskingCapabilities<J> aliased) {
        this(alias, aliased, null);
    }

    protected AbstractTableTaskingCapabilities(Name alias, AbstractTableTaskingCapabilities<J> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""));
    }

    @Override
    public void initRelations() {
        final TableCollection<J> tables = getTables();
        registerRelation(
                new RelationOneToMany<>(this, tables.getTableThings(), EntityType.THING)
                        .setSourceFieldAccessor(AbstractTableTaskingCapabilities::getThingId)
                        .setTargetFieldAccessor(AbstractTableThings::getId)
        );

        registerRelation(
                new RelationOneToMany<>(this, tables.getTableActuators(), EntityType.ACTUATOR)
                        .setSourceFieldAccessor(AbstractTableTaskingCapabilities::getActuatorId)
                        .setTargetFieldAccessor(AbstractTableActuators::getId)
        );

        registerRelation(
                new RelationOneToMany<>(this, tables.getTableTasks(), EntityType.TASK, true)
                        .setSourceFieldAccessor(AbstractTableTaskingCapabilities::getId)
                        .setTargetFieldAccessor(AbstractTableTasks::getTaskingCapabilityId)
        );
    }

    @Override
    public abstract TableField<Record, J> getId();

    public abstract TableField<Record, J> getActuatorId();

    public abstract TableField<Record, J> getThingId();

    @Override
    public abstract AbstractTableTaskingCapabilities<J> as(String alias);

    @Override
    public abstract AbstractTableTaskingCapabilities<J> as(Name as);

}
