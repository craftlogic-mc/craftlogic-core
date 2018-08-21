package ru.craftlogic.api.text;

import ru.craftlogic.util.text.TextComponentDate;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TextDate extends Text<TextComponentDate, TextDate> {
    private final long date;
    private final String format;

    TextDate(Date date, SimpleDateFormat format) {
        this(date.toInstant().toEpochMilli(), format.toPattern());
    }

    TextDate(long date, String format) {
        this.date = date;
        this.format = format;
    }

    public long getDate() {
        return date;
    }

    public String getFormat() {
        return format;
    }

    @Override
    protected TextComponentDate buildSub() {
        return new TextComponentDate(this.date, this.format);
    }
}
