package dynamo.jdbi.core;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.skife.jdbi.v2.SQLStatement;
import org.skife.jdbi.v2.sqlobject.Binder;
import org.skife.jdbi.v2.sqlobject.BinderFactory;
import org.skife.jdbi.v2.sqlobject.BindingAnnotation;

@BindingAnnotation(BindUpper.PathBinderFactory.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.PARAMETER })
public @interface BindUpper {
	String value();

	public static class PathBinderFactory implements BinderFactory {
		@Override
		public Binder build(Annotation annotation) {
			return new Binder<BindUpper, String>() {
				@Override
				public void bind(SQLStatement q, BindUpper bind, String value) {
					q.bind(bind.value(), value != null ? value.trim().toUpperCase() : null);
				}
			};
		}
	}
}