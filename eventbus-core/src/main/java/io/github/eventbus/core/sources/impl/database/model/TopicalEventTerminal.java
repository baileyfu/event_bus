package io.github.eventbus.core.sources.impl.database.model;

import java.util.Date;

/**
 * @author ALi
 * @version 1.0
 * @date 2022-09-14 17:01
 * @description
 */
public class TopicalEventTerminal {
    public static final int TERMINAL_STATE_NORMAL = 0;
    public static final int TERMINAL_STATE_DOWNTIME = 1;
    private int id;
    /**
     * 唯一索引
     */
    private String terminalId;
    private int state;
    private Date createTime;
    private Date lastActiveTime;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTerminalId() {
        return terminalId;
    }

    public void setTerminalId(String terminalId) {
        this.terminalId = terminalId;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getLastActiveTime() {
        return lastActiveTime;
    }

    public void setLastActiveTime(Date lastActiveTime) {
        this.lastActiveTime = lastActiveTime;
    }
}
