package com.pg85.otg.logging;

import com.pg85.otg.util.helpers.StringHelper;

import java.util.List;

public abstract class Logger
{
    protected LogMarker minimumLevel = LogMarker.INFO;

    public void setLevel(LogMarker level)
    {
        minimumLevel = level;
    }

    public void log(LogMarker level, List<String> message)
    {
        log(level, "{}", (Object) StringHelper.join(message, " "));
    }

    public abstract void log(LogMarker level, String message, Object... params);
}
