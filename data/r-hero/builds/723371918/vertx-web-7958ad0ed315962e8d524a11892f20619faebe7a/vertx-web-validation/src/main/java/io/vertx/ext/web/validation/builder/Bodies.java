package io.vertx.ext.web.validation.builder;

import io.vertx.ext.json.schema.Schema;
import io.vertx.ext.json.schema.common.dsl.ObjectSchemaBuilder;
import io.vertx.ext.json.schema.common.dsl.SchemaBuilder;
import io.vertx.ext.json.schema.common.dsl.StringSchemaBuilder;
import io.vertx.ext.web.validation.impl.ValueParserInferenceUtils;
import io.vertx.ext.web.validation.impl.body.FormBodyProcessorImpl;
import io.vertx.ext.web.validation.impl.body.JsonBodyProcessorImpl;
import io.vertx.ext.web.validation.impl.body.TextPlainBodyProcessorImpl;
import io.vertx.ext.web.validation.impl.validator.SchemaValidator;

/**
 * In this interface you can find all available {@link BodyProcessorFactory} to use in {@link ValidationHandlerBuilder}. <br/>
 *
 * To create new schemas using {@link SchemaBuilder}, look at the <a href="https://vertx.io/docs/vertx-json-schema/java/">docs of vertx-json-schema</a>
 */
public interface Bodies {

  /**
   * Create a json body processor
   *
   * @param schemaBuilder
   * @return
   */
  static BodyProcessorFactory json(SchemaBuilder schemaBuilder) {
    return parser -> new JsonBodyProcessorImpl(new SchemaValidator(schemaBuilder.build(parser)));
  }

  /**
   * Create a {@code text/plain} body processor
   *
   * @param schemaBuilder
   * @return
   */
  static BodyProcessorFactory textPlain(StringSchemaBuilder schemaBuilder) {
    return parser -> new TextPlainBodyProcessorImpl(new SchemaValidator(schemaBuilder.build(parser)));
  }

  /**
   * Create a form {@code application/x-www-form-urlencoded} processor
   *
   * @param schemaBuilder
   * @return
   */
  static BodyProcessorFactory formUrlEncoded(ObjectSchemaBuilder schemaBuilder) {
    return parser -> {
      Schema s = schemaBuilder.build(parser);
      Object jsonSchema = s.getJson();
      return new FormBodyProcessorImpl(
        ValueParserInferenceUtils.infeerPropertiesFormValueParserForObjectSchema(jsonSchema),
        ValueParserInferenceUtils.infeerPatternPropertiesFormValueParserForObjectSchema(jsonSchema),
        ValueParserInferenceUtils.infeerAdditionalPropertiesFormValueParserForObjectSchema(jsonSchema),
        "application/x-www-form-urlencoded",
        new SchemaValidator(s)
      );
    };
  }

  /**
   * Create a form {@code multipart/form-data} processor
   *
   * @param schemaBuilder
   * @return
   */
  static BodyProcessorFactory multipartFormData(ObjectSchemaBuilder schemaBuilder) {
    return parser -> {
      Schema s = schemaBuilder.build(parser);
      Object jsonSchema = s.getJson();
      return new FormBodyProcessorImpl(
        ValueParserInferenceUtils.infeerPropertiesFormValueParserForObjectSchema(jsonSchema),
        ValueParserInferenceUtils.infeerPatternPropertiesFormValueParserForObjectSchema(jsonSchema),
        ValueParserInferenceUtils.infeerAdditionalPropertiesFormValueParserForObjectSchema(jsonSchema),
        "multipart/form-data",
        new SchemaValidator(s)
      );
    };
  }
}
