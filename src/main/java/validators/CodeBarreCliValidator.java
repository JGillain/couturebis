package validators;

import entities.Utilisateur;
import managedBean.CodeBarreBean;
import services.SvcUtilisateur;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;
import javax.enterprise.inject.spi.CDI;
import javax.inject.Inject;
import java.util.List;

@FacesValidator(value="codeBarreCliValidator",managed = true)
public class CodeBarreCliValidator implements Validator
{
    @Inject
    private CodeBarreBean codeBarreBean;
    
    @Override
    public void validate(FacesContext facesContext, UIComponent uiComponent, Object o) throws ValidatorException {
        String CB = (String) o;
        if (codeBarreBean==null){
            codeBarreBean=CDI.current().select(CodeBarreBean.class).get();
        }
        if(CB.length() != 13) {
            throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR,"La valeur doit être d'exactement 13 caracteres",null));
        } else if ((!CB.chars().allMatch(Character::isDigit))) {
            throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Le code-barres doit contenir uniquement des chiffres", null));
        } else if (codeBarreBean.calculateCheckDigit(CB.substring(0,12))!=Integer.parseInt(CB.substring(12,13))) {
            throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR,"Le code barre encodé n'est pas valide",null));
        }

        SvcUtilisateur serviceU = new SvcUtilisateur();
        try {
            List<Utilisateur> L = serviceU.getByNumMembre(CB);
            if (L.isEmpty()) {
                throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR,"Le numéro de membre n'existe pas",null));
            } else if(!L.get(0).getActif()) {
                throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR,"L'utilisateur n'est pas actif",null));
            }
        }
        finally {
            serviceU.close();
        }
    }

}
