import javax.xml.datatype.DatatypeFactory;
import org.xacml4j.util.DateTimeDurationUtils;
import org.xacml4j.v30.types.ISO8601DayTimeDuration;

/** Record-only checks requested by the reviewer; no production changes for these gaps. */
class DiagnoseFractionalDurationGaps {
    public static void main(String[] args) throws Exception {
        ISO8601DayTimeDuration half = ISO8601DayTimeDuration.of("PT0.5S");
        if (!half.toDuration().equals(java.time.Duration.ofMillis(500))) {
            throw new AssertionError("The repaired lexical conversion must retain 500ms");
        }
        System.out.println("CHECK lexical PT0.5S -> java.time.Duration=" + half.toDuration());
        System.out.println("GAP ISO8601Duration.getSeconds(PT0.5S)=" + half.getSeconds());
        System.out.println("GAP ISO8601Duration.isZero(PT0.5S)=" + half.isZero());
        javax.xml.datatype.Duration xml = DateTimeDurationUtils.toXmlDayTimeDuration(
                java.time.Duration.ofMillis(500));
        System.out.println("GAP DateTimeDurationUtils.toXmlDayTimeDuration(500ms)=" + xml);
        ISO8601DayTimeDuration constructed = ISO8601DayTimeDuration.of(java.time.Duration.ofMillis(500));
        System.out.println("GAP ISO8601DayTimeDuration.of(java.time.Duration.ofMillis(500))="
                + constructed.toXacmlString());
        try {
            System.out.println("GAP JDK-style PT-0.5S lexical fallback="
                    + ISO8601DayTimeDuration.of("PT-0.5S").toXacmlString());
        } catch (RuntimeException error) {
            System.out.println("GAP JDK-style PT-0.5S lexical fallback throws " + error);
        }
        javax.xml.datatype.Duration tiny = DatatypeFactory.newInstance().newDuration("PT0.0000000001S");
        try {
            System.out.println("OBSERVATION sub-nanosecond conversion="
                    + DateTimeDurationUtils.toISO8601DayTimeDuration(tiny));
        } catch (ArithmeticException error) {
            System.out.println("OBSERVATION sub-nanosecond conversion is rejected exactly: " + error);
        }
    }
}
