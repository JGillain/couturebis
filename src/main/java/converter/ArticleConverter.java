package converter;

import entities.Adresse;
import org.apache.log4j.Logger;
import services.SvcArticle;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

@FacesConverter(value = "articleConverter")
public class ArticleConverter implements Converter {
    private static final Logger log = Logger.getLogger(ArticleConverter.class);
    private final SvcArticle service = new SvcArticle();

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
            return String.valueOf(((Adresse) o).getId());
        }
        else
            return null;
    }
}