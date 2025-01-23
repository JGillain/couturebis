package enumeration;

public enum FactureEtatEnum
{
    terminer("terminer"),
    annule("annule"),
    en_cours("en_cours");

    private final String label;

    FactureEtatEnum(String label)
    {
        this.label = label;
    }

    public String getLabel()
    {
        return label;
    }
}
