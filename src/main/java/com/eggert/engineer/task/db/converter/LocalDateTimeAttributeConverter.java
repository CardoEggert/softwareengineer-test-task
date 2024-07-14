package com.eggert.engineer.task.db.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;

// Got help from ChatGPT regarding this, seemed that there were some issues in converting SQLite
// datetime to java LocalDateTime
@Converter(autoApply = true)
public class LocalDateTimeAttributeConverter implements AttributeConverter<LocalDateTime, String> {

  private static final DateTimeFormatter FORMATTER =
      new DateTimeFormatterBuilder()
          .appendPattern("yyyy-MM-dd")
          .optionalStart()
          .appendLiteral('T')
          .optionalEnd()
          .appendPattern("HH:mm:ss")
          .optionalStart()
          .appendFraction(ChronoField.NANO_OF_SECOND, 0, 9, true)
          .optionalEnd()
          .toFormatter();

  @Override
  public String convertToDatabaseColumn(LocalDateTime locDateTime) {
    return locDateTime == null ? null : locDateTime.format(FORMATTER);
  }

  @Override
  public LocalDateTime convertToEntityAttribute(String sqlDate) {
    return sqlDate == null ? null : LocalDateTime.parse(sqlDate, FORMATTER);
  }
}
