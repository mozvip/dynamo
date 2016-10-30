package dynamo.jdbi.core;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.sql.Date;
import java.time.LocalDate;

import org.skife.jdbi.v2.SQLStatement;
import org.skife.jdbi.v2.sqlobject.Binder;
import org.skife.jdbi.v2.sqlobject.BinderFactory;
import org.skife.jdbi.v2.sqlobject.BindingAnnotation;

@BindingAnnotation(BindLocalDate.PathBinderFactory.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.PARAMETER })
public @interface BindLocalDate {
	String value();

	public static class PathBinderFactory implements BinderFactory {
		@Override
		public Binder build(Annotation annotation) {
			return new Binder<BindLocalDate, LocalDate>() {
				@Override
				public void bind(SQLStatement q, BindLocalDate bind, LocalDate value) {
					q.bind(bind.value(), value != null ? Date.valueOf(value) : null);
				}
			};
		}
	}
}