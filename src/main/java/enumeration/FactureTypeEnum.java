package enumeration;

public enum FactureTypeEnum
{
    Location("Location"),
    Vente("Vente"),
    Penalite("Penalite");

    private final String label;

    FactureTypeEnum(String label)
    {
        this.label = label;
    }

    public String getLabel()
    {
        return label;
    }
}
