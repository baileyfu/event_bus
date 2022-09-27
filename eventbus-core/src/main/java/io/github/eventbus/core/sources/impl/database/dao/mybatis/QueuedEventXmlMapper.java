package io.github.eventbus.core.sources.impl.database.dao.mybatis;


import io.github.eventbus.core.sources.impl.database.dao.QueuedEventDAO;
import io.github.eventbus.core.sources.impl.database.model.QueuedEvent;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 自定义mapper.xml文件实现SQL功能<br/>
 * 自定义表名和字段需要映射到数据模型QueuedEvent(long[primary key] id,String[length 50] serialId,String name,String message,String messageType,String[length < 64] sourceTerminal,int state,Date createTime,Date updateTime)
 * @author ALi
 * @version 1.0
 * @date 2022-09-07 16:44
 * @description
 */
public interface QueuedEventXmlMapper extends QueuedEventDAO {
    /**
     * mapper.xml需要实现SQL:insert QueuedEvent(id,serialId,name,message,messageType,sourceTerminal,state,createTime)<br/>
     * @param queuedEvent
     * @return
     */
    @Override
    int insert(QueuedEvent queuedEvent);
    @Override
    int updateStateToUnconsumed(@Param("id") long id);
    @Override
    List<QueuedEvent> selectUnconsumedThenUpdateConsumed(String eventNames, int limit, @Param("targetTerminal") String targetTerminal);
    @Override
    int cleanConsumed(@Param("eventNames") String eventNames, @Param("cycleHours") int cycleHours);
}
