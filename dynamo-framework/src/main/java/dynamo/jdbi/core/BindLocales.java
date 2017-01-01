package dynamo.jdbi.core;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.sql.Types;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.skife.jdbi.v2.SQLStatement;
import org.skife.jdbi.v2.sqlobject.Binder;
import org.skife.jdbi.v2.sqlobject.BinderFactory;
import org.skife.jdbi.v2.sqlobject.BindingAnnotation;

@BindingAnnotation(BindLocales.PathBinderFactory.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.PARAMETER })
public @interface BindLocales {
	String value();

	public static class PathBinderFactory implements BinderFactory {
		public Binder build(Annotation annotation) {
			return new Binder<BindLocales, Set<Locale>>() {

				@Override
				public void bind(SQLStatement<?> q, BindLocales bind,
						Set<Locale> value) {
					if (value == null) {
						q.bindNull(bind.value(), Types.VARCHAR);
					} else {
						q.bind(bind.value(), StringUtils.join(value, ";"));
					}
					
				}


			};
		}
	}
}