package converter;

import entities.Fabricant;
import org.apache.log4j.Logger;
import services.SvcFabricant;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

@FacesConverter(value = "fabricantConverter")
public class FabricantConverter implements Converter {
    private static final Logger log = Logger.getLogger(FabricantConverter.class);
    private final SvcFabricant service = new SvcFabricant();

    @Override
    public Object getAsObject(FacesContext facesContext, UIComponent uiComponent, String s) {
        if (s != null && s.trim().length() > 0) {
            int id = Integer.parseInt(s);
            return service.getById(id);
        } else
            return null;

    }

    @Override
    public String getAsString(FacesContext facesContext, UIComponent uiComponent, Object o) {
        if (o != null) {
            return String.valueOf(((Fabricant) o).getId());
        }
        else
            return null;
    }
}