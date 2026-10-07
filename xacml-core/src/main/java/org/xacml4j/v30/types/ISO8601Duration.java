package org.xacml4j.v30.types;

import org.xacml4j.util.DateTimeDurationUtils;

import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.Duration;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.Period;
import java.time.ZonedDateTime;
import java.util.GregorianCalendar;
import java.util.Objects;

public abstract class ISO8601Duration <T extends ISO8601Duration<T>> extends Value.SelfValue<T> implements Comparable<T>
{
    private Duration duration;
    private transient String formatted;

    protected ISO8601Duration(ValueType attrType) {
        super(attrType);
    }

    protected ISO8601Duration(ValueType attrType,
                              Duration duration) {
        super(attrType);
        this.duration = Objects.requireNonNull(duration, "duration");
    }

    Duration duration(){
        return duration;
    }

    public final Period toPeriod(){
        return DateTimeDurationUtils.toISO8601YearsMonthsPeriod(duration);
    }

    public final java.time.Duration toDuration(){
        return DateTimeDurationUtils.toISO8601DayTimeDuration(duration);
    }
    public int getYears() {
        return duration.getYears() == DatatypeConstants.FIELD_UNDEFINED?0:duration.getYears();
    }

    public int getMonths() {
        return duration.getYears() == DatatypeConstants.FIELD_UNDEFINED?0:duration.getMonths();
    }

    public final int getDays() {
        return duration.getYears() == DatatypeConstants.FIELD_UNDEFINED?0:duration.getDays();
    }

    public final int getHours() {
        return duration.getHours() == DatatypeConstants.FIELD_UNDEFINED?0:duration.getHours();
    }

    public final int getMinutes() {
        return duration.getMinutes() == DatatypeConstants.FIELD_UNDEFINED?0:duration.getMinutes();
    }

    public final BigDecimal getSeconds() {
        BigDecimal seconds = (BigDecimal)duration.getField(DatatypeConstants.SECONDS);
        return seconds == null ? BigDecimal.ZERO : seconds;
    }

    public boolean isZero(){
        return DateTimeDurationUtils.isZero(duration);
    }
    public boolean isNegative(){
        return duration.getSign() < 0;
    }

    public final T plus(T d){
        return build(duration.add(d.duration()));
    }
    public final T minus(T d){
        return build(duration.subtract(d.duration()));
    }
    public final T negated(){
        return build(duration.negate());
    }

    public final T multiply(int factor){
        return build(duration.multiply(factor));
    }

    public final T normalize(ZonedDateTime dateTime){
        return build(duration.normalizeWith(GregorianCalendar.from(dateTime)));
    }

    public final T normalize(OffsetDateTime dateTime){
        return build(duration.normalizeWith(GregorianCalendar.from(dateTime.toZonedDateTime())));
    }

    @Override
    public int compareTo(T o) {
        return duration.compare(o.duration());
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ISO8601Duration<?> that = (ISO8601Duration<?>) o;
        return Objects.equals(duration, that.duration) &&
                getEvaluatesTo().equals(that.getEvaluatesTo());
    }

    public StringVal toStringVal(){
        return StringVal.of(toXacmlString());
    }


    public String toXacmlString(){
        if(formatted == null){
            formatted = duration().toString();
        }
        return formatted;
    }

    @Override
    protected String formatVal(T v) {
        return toXacmlString();
    }

    @Override
    public int hashCode() {
        return Objects.hash(duration, getEvaluatesTo());
    }

    protected abstract T build(Duration d);
}
