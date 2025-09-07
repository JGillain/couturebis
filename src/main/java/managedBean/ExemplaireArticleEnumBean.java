package managedBean;

import enumeration.ExemplaireArticleEtatEnum;
import enumeration.ExemplaireArticleStatutEnum;
import enumeration.FactureEtatEnum;
import enumeration.FactureTypeEnum;

import javax.enterprise.context.SessionScoped;
import javax.inject.Named;
import java.io.Serializable;

@Named
@SessionScoped
public class ExemplaireArticleEnumBean implements Serializable{

        private static final long serialVersionUID = 1L;

        public ExemplaireArticleStatutEnum[] getStatutEnum()
        {
            return ExemplaireArticleStatutEnum.values();
        }
        public ExemplaireArticleEtatEnum[] getEtatEnum()
        {
            return ExemplaireArticleEtatEnum.values();
        }

}
