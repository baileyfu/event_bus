package io.github.eventbus.core.sources.impl.database.mybatis.dao;

import io.github.eventbus.core.sources.impl.database.mybatis.model.QueuedEvent;

/**
 * 自定义mapper.xml文件实现SQL功能<br/>
 * 自定义表名和字段需要映射到数据模型QueuedEvent(long[primary key] id,String[length 50] serialId,String name,String message,String messageType,String[length < 64] sourceTerminal,int state,Date createTime,Date updateTime)
 * @author ALi
 * @version 1.0
 * @date 2022-09-07 16:44
 * @description
 */
public interface QueuedEventXmlMapper extends QueuedEventMapper{
    /**
     * mapper.xml需要实现SQL:insert QueuedEvent(id,serialId,name,message,messageType,sourceTerminal,state,createTime)<br/>
     * @param queuedEvent
     * @return
     */
    @Override
    int insert(QueuedEvent queuedEvent);
}
