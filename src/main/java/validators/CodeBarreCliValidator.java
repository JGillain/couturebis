package validators;

import entities.Utilisateur;
import services.SvcUtilisateur;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;
import java.util.List;

@FacesValidator("codeBarreCliValidator")
public class CodeBarreCliValidator implements Validator
{
    @Override
    public void validate(FacesContext facesContext, UIComponent uiComponent, Object o) throws ValidatorException {
        String CB = (String) o;
        SvcUtilisateur serviceU = new SvcUtilisateur();
        List<Utilisateur> L = serviceU.getByNumMembre(CB);

        if (L.size() == 0)
        {
            throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR,"Le numéro de membre n'existe pas",null));
        }
        else if(CB.length() != 9)
        {
            throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR,"La valeur doit être d'exactement 9 caracteres",null));
        }
        else if(!L.get(0).getActif()){throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR,"L'utilisateur n'est pas actif",null));}

        serviceU.close();
    }

}
