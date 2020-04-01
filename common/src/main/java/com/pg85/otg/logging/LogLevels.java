package com.pg85.otg.logging;

public enum LogLevels
{
    Off(LogMarker.ERROR),
    Quiet(LogMarker.WARN),
    Standard(LogMarker.INFO),
    Debug(LogMarker.DEBUG),
    Trace(LogMarker.TRACE);
    private final LogMarker marker;

    LogLevels(LogMarker marker)
    {
        this.marker = marker;
    }

    public LogMarker getLevel()
    {
        return marker;
    }
}
