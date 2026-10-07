import org.xacml4j.v30.policy.function.impl.DateTimeArithmeticFunctions;
import org.xacml4j.v30.types.ISO8601Date;
import org.xacml4j.v30.types.ISO8601YearMonthDuration;

/** Production calls and independent candidate calculations; no source writes. */
class DiagnoseDatePeriod {
    public static void main(String[] args) {
        ISO8601Date start = ISO8601Date.of("2002-03-22");
        String[] durations = {"P1Y2M", "-P1Y2M", "P0M"};
        String[] sums = {"2003-05-22", "2001-01-22", "2002-03-22"};
        String[] differences = {"2001-01-22", "2003-05-22", "2002-03-22"};
        int failures = 0;
        for (int i = 0; i < durations.length; i++) {
            ISO8601YearMonthDuration duration = ISO8601YearMonthDuration.of(durations[i]);
            System.out.println("Operand " + durations[i] + ": toDuration=" + duration.toDuration()
                    + ", toPeriod=" + duration.toPeriod());
            String sum = DateTimeArithmeticFunctions.add(start, duration).toXacmlString();
            String difference = DateTimeArithmeticFunctions.subtract(start, duration).toXacmlString();
            String candidateSum = start.get().plus(duration.toPeriod()).toString();
            String candidateDifference = start.get().minus(duration.toPeriod()).toString();
            System.out.println("date-add-yearMonthDuration: expected=" + sums[i] + ", actual=" + sum
                    + ", proposal calculation=" + candidateSum);
            System.out.println("date-subtract-yearMonthDuration: expected=" + differences[i] + ", actual=" + difference
                    + ", proposal calculation=" + candidateDifference);
            if (!sums[i].equals(sum)) failures++;
            if (!differences[i].equals(difference)) failures++;
            if (!sums[i].equals(candidateSum) || !differences[i].equals(candidateDifference)) {
                throw new AssertionError("Candidate does not match independently computed expected dates");
            }
        }
        System.out.println("Production checks: 6; failed: " + failures + "; candidate calculations matched: 6");
        if (failures != 4) throw new AssertionError("Unexpected baseline diagnosis");
        System.out.println("IIC106 classification: pre-existing core defect; both ISO8601Date operations use day-time Duration instead of calendar Period.");
    }
}
