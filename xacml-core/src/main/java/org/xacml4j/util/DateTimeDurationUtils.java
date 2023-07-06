package org.xacml4j.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xacml4j.v30.SyntaxException;
import org.xacml4j.v30.types.XacmlTypes;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.*;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class DateTimeDurationUtils
{
    private final static Logger LOG = LoggerFactory.getLogger(DateTimeDurationUtils.class);

    private static DatatypeFactory DF_FACTORY = null;

    static{
        try{
            DF_FACTORY = DatatypeFactory.newInstance();
        }catch(DatatypeConfigurationException e){
            LOG.error(e.getMessage(), e);
        }
    }

    public static OffsetDateTime toISO860DateTime(XMLGregorianCalendar c){
        if(c == null){
            throw SyntaxException
                    .invalidAttributeValue(null, XacmlTypes.DATETIME);
        }
        if(c.getXMLSchemaType() != DatatypeConstants.DATETIME){
            throw SyntaxException
                    .invalidAttributeValue(c, XacmlTypes.DATETIME);
        }
        ZoneOffset offset = c.getTimezone() != DatatypeConstants.FIELD_UNDEFINED?
                ZoneOffset.ofTotalSeconds(c.getTimezone() * 60):ZoneOffset.UTC;
        BigDecimal fractionalSecond = c.getFractionalSecond();
        int nanos = fractionalSecond != null
                ? fractionalSecond.multiply(BigDecimal.valueOf(1_000_000_000)).intValueExact()
                : 0;
        return OffsetDateTime.of(c.getYear(), c.getMonth(), c.getDay(),
                c.getHour(), c.getMinute(), c.getSecond(), nanos, offset);
    }

    public static LocalDate toISO860Date(XMLGregorianCalendar v){
        if(v == null){
            throw SyntaxException
                    .invalidAttributeValue(null, XacmlTypes.DATE);
        }
        if(!(v.getXMLSchemaType() == DatatypeConstants.DATE ||
                v.getXMLSchemaType() == DatatypeConstants.DATETIME)){
            throw SyntaxException
                    .invalidAttributeValue(v, XacmlTypes.DATE);
        }
        return LocalDate.of(v.getYear(),
                v.getMonth(),
                v.getDay());
    }

    public static OffsetTime toISO860Time(XMLGregorianCalendar c){
        if(c == null){
            throw SyntaxException
                    .invalidAttributeValue(null, XacmlTypes.TIME);
        }
        if(!(c.getXMLSchemaType().equals(DatatypeConstants.DATETIME) ||
                c.getXMLSchemaType().equals(DatatypeConstants.TIME))){
            throw SyntaxException
                    .invalidAttributeValue(c, XacmlTypes.TIME);
        }
        ZoneOffset offset = c.getTimezone() != DatatypeConstants.FIELD_UNDEFINED?
                ZoneOffset.ofTotalSeconds(c.getTimezone() * 60):ZoneOffset.UTC;
        BigDecimal fractionalSecond = c.getFractionalSecond();
        int nanos = fractionalSecond != null
                ? fractionalSecond.multiply(BigDecimal.valueOf(1_000_000_000)).intValueExact()
                : 0;
        return OffsetTime.of(c.getHour(), c.getMinute(), c.getSecond(), nanos, offset);
    }


    public static Period toYearMonthPeriod(String v){
        assert DF_FACTORY != null;
        Duration duration = DF_FACTORY.newDuration(v);
        if(duration.getXMLSchemaType() != DatatypeConstants.DURATION_YEARMONTH){
            throw SyntaxException
                    .invalidAttributeValue(v, XacmlTypes.YEARMONTHDURATION);
        }
        Period p = Period.of(duration.getYears(),
                duration.getMonths(),
                duration.getDays());
        return duration.getSign() > 0?p:p.negated();
    }


    public static LocalDate toISO860Date(GregorianCalendar v){
        if(v == null){
            throw SyntaxException
                    .invalidAttributeValue(null, XacmlTypes.DATE);
        }
        return LocalDate.of(v.get(Calendar.YEAR),
                v.get(Calendar.MONTH),
                v.get(Calendar.DAY_OF_MONTH));
    }

    public static LocalDate toISO860Date(Instant v){
        if(v == null){
            throw SyntaxException
                    .invalidAttributeValue(null, XacmlTypes.DATE);
        }
        return v.atOffset(ZoneOffset.UTC)
                .toLocalDate();
    }

    public static LocalDate toISO860Date(String v){
        assert DF_FACTORY != null;
        if(v == null){
            throw SyntaxException
                    .invalidAttributeValue(null, XacmlTypes.DATE);
        }
        XMLGregorianCalendar d = DF_FACTORY.newXMLGregorianCalendar(v);
        if(d.getXMLSchemaType() != DatatypeConstants.DATE){
            throw SyntaxException
                    .invalidAttributeValue(v, XacmlTypes.DATE);
        }
        try{
            return toISO860Date(d);
        }catch (Exception e){
            throw new SyntaxException(e);
        }
    }

    public static OffsetDateTime toISO860DateTime(Instant v){
        if(v == null){
            throw SyntaxException
                    .invalidAttributeValue(null, XacmlTypes.DATETIME);
        }
        return v.atOffset(ZoneOffset.UTC);
    }

    public static OffsetTime toISO860Time(Instant v){
        if(v == null){
            throw SyntaxException
                    .invalidAttributeValue(null, XacmlTypes.TIME);
        }
        return v.atOffset(ZoneOffset.UTC).toOffsetTime();
    }

    public static OffsetTime toISO860Time(GregorianCalendar calendar){
        if(calendar == null){
            throw SyntaxException
                    .invalidAttributeValue(null, XacmlTypes.TIME);
        }
        return calendar.toZonedDateTime()
                .toOffsetDateTime()
                .toOffsetTime();
    }

    public static OffsetDateTime toISO860DateTime(GregorianCalendar calendar){
        if(calendar == null){
            throw SyntaxException
                    .invalidAttributeValue(null, XacmlTypes.DATETIME);
        }
        return calendar.toZonedDateTime()
                .toOffsetDateTime();
    }


    public static OffsetTime parseISO860Time(String v) throws SyntaxException{
        if(StringUtils.isNullOrEmpty(v)){
            throw SyntaxException
                    .invalidAttributeValue(v, XacmlTypes.TIME);
        }
        XMLGregorianCalendar c = DF_FACTORY.newXMLGregorianCalendar(v);
        if(c.getXMLSchemaType() != DatatypeConstants.TIME){
            throw SyntaxException
                    .invalidAttributeValue(v, XacmlTypes.DATE);
        }
        return toISO860Time(c);
    }

    public static OffsetDateTime parseISO860DateTime(String v) throws SyntaxException{
        if(StringUtils.isNullOrEmpty(v)){
            throw SyntaxException
                    .invalidAttributeValue(v, XacmlTypes.DATETIME);
        }
       return toISO860DateTime(DF_FACTORY.newXMLGregorianCalendar(v));
    }

    public static java.time.Duration toISO8601DayTimeDuration(Duration v){
        Duration d = DF_FACTORY.newDurationDayTime(v.getSign() > 0,
                v.getDays(), v.getHours(), v.getMinutes(), v.getSeconds());
        java.time.Duration duration = null;
        if(d.getDays() != DatatypeConstants.FIELD_UNDEFINED){
            duration = java.time.Duration.ofDays(d.getDays());
        }
        if(d.getHours() != DatatypeConstants.FIELD_UNDEFINED){
            duration = duration != null?duration.plusHours(d.getHours()):
                    java.time.Duration.ofHours(d.getHours());
        }
        if(d.getMinutes() != DatatypeConstants.FIELD_UNDEFINED){
            duration = duration != null?duration.plusMinutes(d.getMinutes()):
                    java.time.Duration.ofMinutes(d.getMinutes());
        }
        if(d.getSeconds() != DatatypeConstants.FIELD_UNDEFINED){
            duration = duration != null?duration.plusSeconds(d.getSeconds()):
                    java.time.Duration.ofSeconds(d.getSeconds());
        }
        if(duration == null){
            throw SyntaxException
                    .invalidAttributeValue(v, XacmlTypes.DAYTIMEDURATION);
        }
        return d.getSign() > 0?duration:duration.negated();
    }

    public static java.time.Duration parseISO8601DayTimeDuration(String v){
        assert DF_FACTORY != null;
        return toISO8601DayTimeDuration(DF_FACTORY.newDuration(v));

    }

    public static Period toISO8601YearMonthDuration(Duration v){
        assert DF_FACTORY != null;
        Duration d = DF_FACTORY.newDurationYearMonth(v.getSign() > 0, v.getYears(), v.getMonths());
        Period p = null;
        if(d.getYears() != DatatypeConstants.FIELD_UNDEFINED){
            p = Period.ofYears(d.getYears());
        }
        if(d.getMonths() != DatatypeConstants.FIELD_UNDEFINED){
            p = p != null?p.plusMonths(d.getMonths()):Period.ofMonths(d.getMonths());
        }
        if(p == null){
            throw SyntaxException.invalidAttributeValue(v, XacmlTypes.YEARMONTHDURATION);
        }
        return d.getSign() > 0?p:p.negated();
    }

    public static Period parseISO8601YearMonthDuration(String v){
        assert DF_FACTORY != null;
        return toISO8601YearMonthDuration(DF_FACTORY.newDuration(v));
    }

    public static Duration toXmlDayTimeDuration(java.time.Duration d)
    {
        BigInteger seconds = BigDecimal.valueOf(d.toSecondsPart())
                .add(BigDecimal.valueOf(d.getNano(), 9))
                .stripTrailingZeros().toBigInteger();
        return DF_FACTORY.newDurationDayTime(
                !d.isNegative(),
                d.toDays() == 0?null:BigInteger.valueOf(d.toDays()),
                d.toHoursPart() == 0?null:BigInteger.valueOf(d.toHoursPart()),
                d.toMinutesPart() == 0?null:BigInteger.valueOf(d.toMinutesPart()),
                seconds.equals(BigInteger.ZERO)?null:seconds);
    }

    public static Duration toXmlYearMonthDuration(Period v)
    {
        assert DF_FACTORY != null;
        return DF_FACTORY.newDurationYearMonth(!v.isNegative(),
                v.getYears() == 0?null:BigInteger.valueOf(Math.abs(v.getYears())),
                v.getMonths() == 0?null:BigInteger.valueOf(Math.abs(v.getMonths())));
    }


    public XMLGregorianCalendar toXmlCalendar(OffsetTime v){
        assert DF_FACTORY != null;
        return DF_FACTORY.newXMLGregorianCalendarTime(
                v.getHour(),
                v.getMinute(),
                v.getSecond(),
                v.getOffset().getTotalSeconds() / 60);
    }

}
