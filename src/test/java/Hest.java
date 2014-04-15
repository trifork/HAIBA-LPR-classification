import org.joda.time.DateTime;

public class Hest {
    public static void main(String[] args) {
        DateTime dt = new DateTime();
        System.out.println("dt.getYear():" + dt.getYear());
        System.out.println("dt.getYearOfCentury():" + dt.getYearOfCentury());
        System.out.println("dt.getYearOfEra():" + dt.getYearOfEra());
    }
}
