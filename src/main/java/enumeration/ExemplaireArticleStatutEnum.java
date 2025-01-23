package enumeration;

public enum ExemplaireArticleStatutEnum
{
    Location("Location"),
    Vente("Vente");

    private final String label;

    ExemplaireArticleStatutEnum(String label)
    {
        this.label = label;
    }

    public String getLabel()
    {
        return label;
    }
}
