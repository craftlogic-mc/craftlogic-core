package ru.craftlogic.util.text;

import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentBase;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TextComponentDate extends TextComponentBase {
    private long date;
    private String format;

    public TextComponentDate(Date date, SimpleDateFormat format) {
        this(date.toInstant().toEpochMilli(), format.toPattern());
    }

    public TextComponentDate(long date, String format) {
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
    public String getUnformattedComponentText() {
        SimpleDateFormat format = new SimpleDateFormat(this.format);
        return format.format(new Date(this.date));
    }

    @Override
    public ITextComponent createCopy() {
        TextComponentDate component = new TextComponentDate(this.date, this.format);
        component.setStyle(this.getStyle().createShallowCopy());

        for (ITextComponent s : this.getSiblings()) {
            component.appendSibling(s.createCopy());
        }

        return component;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (!(obj instanceof TextComponentDate)) {
            return false;
        } else {
            TextComponentDate target = (TextComponentDate)obj;
            return this.date == target.date && this.format.equals(target.format) && super.equals(obj);
        }
    }

    @Override
    public String toString() {
        return "DateComponent{date='" + this.date + "', format='" + this.format + "', siblings=" + this.siblings + ", style=" + this.getStyle() + '}';
    }
}
