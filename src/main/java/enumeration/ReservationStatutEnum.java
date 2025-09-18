package enumeration;

public enum ReservationStatutEnum
{
    file("file"),
    pret("pret"),
    valide("valide"),
    annule("annule"),
    expire ("expire ");

    private final String label;

    ReservationStatutEnum(String label)
    {
        this.label = label;
    }

    public String getLabel()
    {
        return label;
    }
}
