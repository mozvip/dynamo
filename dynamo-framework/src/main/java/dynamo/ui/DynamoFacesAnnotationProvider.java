package dynamo.ui;

import java.lang.annotation.Annotation;
import java.net.URI;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.faces.config.AnnotationScanner;

import dynamo.core.manager.ConfigurationManager;
import dynamo.core.manager.DynamoObjectFactory;

public class DynamoFacesAnnotationProvider extends AnnotationScanner {

    private static final Logger LOGGER = LoggerFactory.getLogger( DynamoFacesAnnotationProvider.class );

    // ------------------------------------------------------------ Constructors


    /**
     * Creates a new <code>AnnotationScanner</code> instance.
     *
     * @param sc the <code>ServletContext</code> for the application to be
     *  scanned
     */
    public DynamoFacesAnnotationProvider(ServletContext sc) {
        super(sc);
    }


    // ---------------------------------------------------------- Public Methods


    /**
     * @return a <code>Map</code> of classes mapped to a specific annotation type.
     *  If no annotations are present, or the application is considered
     * <code>metadata-complete</code> <code>null</code> will be returned.
     */
    @Override
    public Map<Class<? extends Annotation>,Set<Class<?>>> getAnnotatedClasses(Set<URI> uris) {

        Set<String> classList = new HashSet<String>();

        processClasses(sc, classList);
        processScripts(classList);

        return processClassList(classList);
    }

    /**
     * Scan <code>WEB-INF/classes</code> for classes that may be annotated
     * with any of the Faces configuration annotations.
     *
     * @param sc the <code>ServletContext</code> for the application being
     *  scanned
     * @param classList the <code>Set</code> to which annotated classes
     *  will be added
     */
    private void processClasses(ServletContext sc, Set<String> classList) {
    	
    	DynamoObjectFactory<Class<?>> factory = new DynamoObjectFactory<Class<?>>( ConfigurationManager.DYNAMO_PACKAGE_PREFIX );
    	Set<Class<? extends Class<?>>> matchingClasses = factory.getMatchingClasses( false, false );
    	for (Class<?> klass : matchingClasses) {
            String cname = klass.getName();
            if (!processClass(cname)) {
                continue;
            }
            
            Annotation[] annotations = klass.getAnnotations();
            for (Annotation annotation : annotations) {
				if ( AnnotationScanner.FACES_ANNOTATION_TYPE.contains(annotation.annotationType()) ) {
	                classList.add(cname);
	                if (LOGGER.isDebugEnabled()) {
	                    LOGGER.debug( String.format("Found annotated Class: %s", cname));
	                }
	                break;
				}
			}
		}

    }

}
