package enumeration;

public enum ExemplaireArticleEtatEnum
{
    Bon("Bon"),
    Moyen("Moyen"),
    Mauvais("Mauvais");

    private final String label;

    ExemplaireArticleEtatEnum(String label)
    {
        this.label = label;
    }

    public String getLabel()
    {
        return label;
    }
}
