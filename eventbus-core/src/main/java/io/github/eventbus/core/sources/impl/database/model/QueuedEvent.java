package io.github.eventbus.core.sources.impl.database.model;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Date;

/**
 * @author ALi
 * @version 1.0
 * @date 2022-09-07 17:06
 * @description
 */
public class QueuedEvent {
    /**
     * 状态：未消费
     */
    public static final int STATE_UNCONSUMED = 0;
    /**
     * 状态：已消费
     */
    public static final int STATE_CONSUMED = 1;

    private long id;
    private String serialId;
    private String name;
    private String message;
    private String messageType;
    private String sourceTerminal;
    private int state;
    private Date createTime;
    private Date updateTime;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getSerialId() {
        return serialId;
    }

    public void setSerialId(String serialId) {
        this.serialId = serialId;
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

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
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

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("id", id)
                .append("serialId", serialId)
                .append("name", name)
                .append("message", message)
                .append("messageType", messageType)
                .append("sourceTerminal", sourceTerminal)
                .append("state", state)
                .append("createTime", createTime)
                .append("updateTime", updateTime)
                .toString();
    }
}
