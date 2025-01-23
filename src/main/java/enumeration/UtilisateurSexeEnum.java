package enumeration;

public enum UtilisateurSexeEnum
{
    F("F"),
    M("M"),
    A("A");

    private final String label;

    UtilisateurSexeEnum(String label)
    {
        this.label = label;
    }

    public String getLabel()
    {
        return label;
    }
}
