package com.github.dynamo.jdbi.core;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.sql.Types;

import org.skife.jdbi.v2.SQLStatement;
import org.skife.jdbi.v2.sqlobject.Binder;
import org.skife.jdbi.v2.sqlobject.BinderFactory;
import org.skife.jdbi.v2.sqlobject.BindingAnnotation;

@BindingAnnotation(BindClassName.PathBinderFactory.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.PARAMETER })
public @interface BindClassName {
	String value();

	public static class PathBinderFactory implements BinderFactory {
		@Override
		public Binder build(Annotation annotation) {
			return new Binder<BindClassName, Class<?>>() {
				@Override
				public void bind(SQLStatement q, BindClassName bind, Class klass) {
					if (klass != null) {
						q.bind(bind.value(), klass.getName());
					} else {
						q.bindNull(bind.value(), Types.VARCHAR);
					}
				}
			};
		}
	}
}