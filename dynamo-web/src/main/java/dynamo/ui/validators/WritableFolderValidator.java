package dynamo.ui.validators;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

@FacesValidator("writableFolderValidator")
public class WritableFolderValidator implements Validator {

	@Override
	public void validate(FacesContext context, UIComponent component,
			Object value) throws ValidatorException {
		
		Path path = null;
		
		if (value instanceof String) {
			path = Paths.get( (String) value );
		} else {
			path = (Path) value;
		}
		
		String message = null;
		if (!Files.exists( path )) {
			message = "%s does not exist";
		} else if (!Files.isDirectory( path )) {
			message = "%s is not a directory";
		} else if (!Files.isWritable(path)) {
			message = "%s is not writable";
		}
		if ( message != null) {
			throw new ValidatorException( new FacesMessage( FacesMessage.SEVERITY_ERROR, String.format(message, path.toAbsolutePath().toString()), null ));
		}
	}

}
