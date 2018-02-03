package com.hansight.kunlun.collector.reader;

import java.util.regex.Pattern;

public abstract class RegexLineFileLogReader extends LineFileLogReader {
    public abstract boolean getFinishedInEnd();

    public abstract Pattern getPattern();
}