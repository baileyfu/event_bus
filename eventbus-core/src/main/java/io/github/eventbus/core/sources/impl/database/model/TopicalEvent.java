package io.github.eventbus.core.sources.impl.database.model;

/**
 * @author ALi
 * @version 1.0
 * @date 2022-09-07 17:07
 * @description
 */
public class TopicalEvent extends QueuedEvent{
    private String terminalId;

    public String getTerminalId() {
        return terminalId;
    }

    public void setTerminalId(String terminalId) {
        this.terminalId = terminalId;
    }
}
