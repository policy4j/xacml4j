# Arithmetic and identifier references

The official OASIS XACML 3.0 Standard (22 January 2013), Appendix A.3.7,
defines exactly the six date/dateTime arithmetic functions exercised in
DateTimeArithmeticBoundaryTest. It does not define time duration arithmetic
or date day-time duration arithmetic functions. The opened production
DateTimeArithmeticFunctions class provides those same six operations.

https://docs.oasis-open.org/xacml/3.0/xacml-3.0-core-spec-os-en.html

XACML delegates calendar arithmetic to XML Schema Part 2, Appendix E.
Its E.1 algorithm applies the combined year/month adjustment before pinning
the day to the destination month's range. Applying years and clipping first,
then months, loses February 29 unnecessarily for a P1Y1M period. The expected
March 29 and January 29 boundary values in the new tests follow that rule.

https://www.w3.org/TR/xmlschema-2/#adding-durations-to-dateTimes

XACML's normative function table lists the 3.0 identifiers for all four bag
operations on each of dayTimeDuration and yearMonthDuration. The unchanged
IIC150 fixture uses the required 3.0 dayTimeDuration-one-and-only identifier.
Fresh provider lookup and import diagnostics in run 37 confirm the core only
registers their older 1.0 names. This is a bag registration defect, outside
the currently approved arithmetic and content mapping scope.

The URLs were opened with web.run in this turn. Assertions about the running
implementation are supported by Maven/run-37 evidence, not inferred from docs.
