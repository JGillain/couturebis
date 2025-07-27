package converter;

import entities.Categorie;
import org.apache.log4j.Logger;
import services.SvcCategorie;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

@FacesConverter(value = "categorieConverter")
public class CategorieConverter implements Converter {
    private static final Logger log = Logger.getLogger(CategorieConverter.class);
    private final SvcCategorie service = new SvcCategorie();

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
            return String.valueOf(((Categorie) o).getId());
        }
        else
            return null;
    }
}