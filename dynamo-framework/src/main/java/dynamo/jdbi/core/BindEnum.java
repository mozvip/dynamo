package dynamo.jdbi.core;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.sql.Types;
import java.util.Collection;

import org.apache.commons.lang3.StringUtils;
import org.skife.jdbi.v2.SQLStatement;
import org.skife.jdbi.v2.sqlobject.Binder;
import org.skife.jdbi.v2.sqlobject.BinderFactory;
import org.skife.jdbi.v2.sqlobject.BindingAnnotation;

@BindingAnnotation(BindEnum.PathBinderFactory.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.PARAMETER })
public @interface BindEnum {
	String value();

	public static class PathBinderFactory implements BinderFactory {
		public Binder build(Annotation annotation) {
			return new Binder<BindEnum, Object>() {
				public void bind(SQLStatement q, BindEnum bind, Object parameter) {
					if ( parameter == null) {
						q.bindNull(bind.value(), Types.VARCHAR);
					} else {
						if (parameter instanceof Collection) {
							q.bind(bind.value(), StringUtils.join((Collection)parameter, ";"));
						} else {
							q.bind(bind.value(), ((Enum)parameter).name());
						}
					}
				}
			};
		}
	}
}