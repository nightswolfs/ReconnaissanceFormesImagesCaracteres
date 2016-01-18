package com.company;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import com.company.ImageRelation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface RelationAnnotation {
    SimpleAttributeType simpleType() default SimpleAttributeType.NONE;
    Class<? extends Enum> complexType() default Enum.class;
}
