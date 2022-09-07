package io.github.eventbus.core.sources.impl.database.mybatis.model;

import java.util.Date;

/**
 * @author ALi
 * @version 1.0
 * @date 2022-09-07 17:06
 * @description
 */
public class QueuedEvent {
    private long id;
    private String name;
    private String message;
    private String sourceTerminal;
    /**
     * 0:unconsumed
     * 1:consumed
     */
    private int state;
    private Date createTime;
    private Date updateTime;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSourceTerminal() {
        return sourceTerminal;
    }

    public void setSourceTerminal(String sourceTerminal) {
        this.sourceTerminal = sourceTerminal;
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

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}
