package validators;


import org.apache.log4j.Logger;
import services.SvcUtilisateur;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

@FacesValidator("utilisateurExistValidator")
public class UtilisateurExistValidator implements Validator
{
    private static final Logger log = Logger.getLogger(UtilisateurExistValidator.class);

    public UtilisateurExistValidator()
    {

    }

    @Override
    public void validate(FacesContext facesContext, UIComponent uiComponent, Object o) throws ValidatorException {
        String login = (String) o;


        SvcUtilisateur serviceU = new SvcUtilisateur();
        try {
            if(login.length() < 4)
            {
                throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR,"Le login doit avoir au moins 4 caracteres",null));
            }
            else if (serviceU.findByLogin(login).size() != 0)

                throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR,"Le pseudo est deja utilise, veuillez en choisir un autre",null));

        } finally {
            serviceU.close();
        }
    }


}