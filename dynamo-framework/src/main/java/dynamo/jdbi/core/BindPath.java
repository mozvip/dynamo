package dynamo.jdbi.core;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.nio.file.Path;
import java.sql.Types;

import org.skife.jdbi.v2.SQLStatement;
import org.skife.jdbi.v2.sqlobject.Binder;
import org.skife.jdbi.v2.sqlobject.BinderFactory;
import org.skife.jdbi.v2.sqlobject.BindingAnnotation;

@BindingAnnotation(BindPath.PathBinderFactory.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.PARAMETER })
public @interface BindPath {
	String value();

	public static class PathBinderFactory implements BinderFactory {
		@Override
		public Binder build(Annotation annotation) {
			return new Binder<BindPath, Path>() {
				@Override
				public void bind(SQLStatement q, BindPath bind, Path path) {
					if (path == null) {
						q.bindNull(bind.value(), Types.VARCHAR);
					} else {
						q.bind(bind.value(), path.toAbsolutePath().normalize().toString());
					}
				}
			};
		}
	}
}