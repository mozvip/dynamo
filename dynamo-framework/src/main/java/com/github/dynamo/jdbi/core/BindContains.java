package com.github.dynamo.jdbi.core;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.skife.jdbi.v2.SQLStatement;
import org.skife.jdbi.v2.sqlobject.Binder;
import org.skife.jdbi.v2.sqlobject.BinderFactory;
import org.skife.jdbi.v2.sqlobject.BindingAnnotation;

@BindingAnnotation(BindContains.PathBinderFactory.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.PARAMETER })
public @interface BindContains {
	String value();

	public static class PathBinderFactory implements BinderFactory {
		@Override
		public Binder build(Annotation annotation) {
			return new Binder<BindContains, String>() {
				public void bind(SQLStatement q, BindContains bind, String value) {
					if (value != null) {
						q.bind(bind.value(), '%' + value.trim().toUpperCase() + '%');
					} else {
						q.bind(bind.value(), "%");
					}
				}
			};
		}
	}
}