package validators;

import org.apache.log4j.Logger;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

@FacesValidator("checkMdpValidator")
    public class CheckMdpValidator implements Validator<Object> {
        private static final Logger log = Logger.getLogger(CheckMdpValidator.class);

        @Override
        public void validate(FacesContext ctx,
                             UIComponent comp,
                             Object value) throws ValidatorException {
            // valeur du champ "confirmation"
            String confirm = value != null ? value.toString() : null;

            // récupérer le champ "mdp" (même formulaire)
            UIInput pwdComp =
                    (UIInput) comp.findComponent("mdp");

            // si introuvable ou déjà invalide (required/length), on ne compare pas
            if (pwdComp == null || !pwdComp.isValid()) return;

            // lire la valeur du mot de passe (convertie en priorité)
            String pwd = pwdComp.getValue() != null
                    ? pwdComp.getValue().toString()
                    : (pwdComp.getSubmittedValue() != null ? pwdComp.getSubmittedValue().toString() : null);

            // laisser les validateurs gérer les vides
            if (pwd == null || confirm == null) return;

            // pas égal → échec de validation
            if (!pwd.equals(confirm)) {
                throw new ValidatorException(
                        new FacesMessage(
                                FacesMessage.SEVERITY_ERROR,
                                "Les mots de passe ne correspondent pas.", null));
            }
        }

        //------------------------------------------------------------------------------------------------
        public static Logger getLog() {
            return log;
        }
    }