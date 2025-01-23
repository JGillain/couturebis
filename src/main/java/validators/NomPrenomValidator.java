package validators;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("rawtypes")
@FacesValidator("nomPrenomValidator")

public class NomPrenomValidator implements Validator{

    private static final String NOMPRE = "^[a-zA-Zéèàùûêâôëç]{1}[a-zA-Zéèàùûêâôëç \\'-]*[a-zA-Zéèàùûêâôëç]$";

    private Pattern pattern;
    private Matcher matcher;

    public NomPrenomValidator()
    {
        pattern = Pattern.compile(NOMPRE);
    }

    @Override
    public void validate(FacesContext context, UIComponent component, 	Object value)
            throws ValidatorException {

        matcher = pattern.matcher(value.toString());
        if(!matcher.matches()){

            FacesMessage msg = new FacesMessage("Le nom/prenom n'est pas valide, il doit contenir entre 2 et 100 caractères et uniquelement lettre et caractère accentué.","Le nom/prenom n'est pas valide, il doit contenir entre 2 et 100 caractères et uniquelement lettre et caractère accentué.");
            msg.setSeverity(FacesMessage.SEVERITY_ERROR);
            throw new ValidatorException(msg);
        }

    }
}
