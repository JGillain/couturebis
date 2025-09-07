package managedBean;

import enumeration.FactureEtatEnum;
import enumeration.FactureTypeEnum;

import javax.enterprise.context.SessionScoped;
import javax.inject.Named;
import java.io.Serializable;

@Named
@SessionScoped
public class FactureEnumBean implements Serializable{

        private static final long serialVersionUID = 1L;

        public FactureEtatEnum[] getEtatEnum()
        {
            return FactureEtatEnum.values();
        }

        public FactureTypeEnum[] getTypeEnum()
        {
            return FactureTypeEnum.values();
        }

}
