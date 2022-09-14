# event_bus
易用的事件收发框架

### 一、事件源
#### 1、SpringEventSource
//TODO
#### 2、DatabaseQueueEventSource
队列形式基于数据库的事件源，每事件仅被一个客户端消费一次。

依赖数据库操作接口QueuedEventDAO，两种实现：
##### 1）、mybatis
##### 配置方式（以mybatis-spring为例）：

A、MapperScannerConfigurer中basePackage属性增加io.github.eventbus.core.sources.impl.database.dao.mybatis包名；

B、SqlSessionFactoryBean中typeAliasesPackage属性增加io.github.eventbus.core.sources.impl.database.model包名；

C、若选择QueuedEventXmlMapper则将对应的mapper.xml文件加入SqlSessionFactoryBean的mapperLocations属性；

##### 两种Mapper接口：

###### QueuedEventAnnotationMapper：
基于注解形式实现，相关表和操作的SQL已经定义好，可直接使用。

建表DDL：
```
CREATE TABLE `eventbus_queued_event` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `serial_id` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `name` varchar(45) COLLATE utf8mb4_unicode_ci NOT NULL,
  `message` varchar(1000) COLLATE utf8mb4_unicode_ci DEFAULT '',
  `message_type` varchar(45) COLLATE utf8mb4_unicode_ci DEFAULT '',
  `source_terminal` varchar(300) COLLATE utf8mb4_unicode_ci DEFAULT '',
  `state` tinyint(1) DEFAULT '0',
  `create_time` datetime NOT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `INDEX_state` (`state`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
```
存储过程DDL：
```
CREATE DEFINER=`root`@`localhost` PROCEDURE `selectUnconsumedThenUpdateConsumed`(IN v_limit INT)
BEGIN
 DECLARE v_id BIGINT DEFAULT 0;
 DECLARE v_serial_id VARCHAR(50) DEFAULT '';
 DECLARE v_name VARCHAR(45) DEFAULT '';
 DECLARE v_message VARCHAR(1000) DEFAULT '';
 DECLARE v_message_type VARCHAR(45) DEFAULT '';
 DECLARE v_source_terminal VARCHAR(300) DEFAULT '';
 DECLARE v_state TINYINT(1) DEFAULT 0;
 DECLARE v_create_time DATETIME DEFAULT NULL;
 DECLARE v_update_time DATETIME DEFAULT NULL;
 DECLARE done INT DEFAULT FALSE;

 DECLARE selectUnconsumed CURSOR FOR 
  select id,serial_id,name,message,message_type,source_terminal,state,create_time,update_time from eventbus_queued_event where state=0 limit v_limit for update;
 DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;
 
 CREATE TEMPORARY TABLE if not exists _tmp_eventbus_queued_event_(`id` BIGINT(20),`serial_id` varchar(50),`name` varchar(45),`message` varchar(1000),`message_type` varchar(45),`source_terminal` varchar(300),`state` tinyint(1),`create_time` datetime,`update_time` datetime);   
 
 SET autocommit = 0;
  OPEN selectUnconsumed;
  TRUNCATE TABLE _tmp_eventbus_queued_event_;
   FETCH selectUnconsumed INTO v_id,v_serial_id,v_name,v_message,v_message_type,v_source_terminal,v_state,v_create_time,v_update_time;
   WHILE done IS FALSE DO
    update eventbus_queued_event set state=1,update_time=now() where id=v_id;
    insert into _tmp_eventbus_queued_event_ values(v_id,v_serial_id,v_name,v_message,v_message_type,v_source_terminal,1,v_create_time,now());
    FETCH selectUnconsumed INTO v_id,v_serial_id,v_name,v_message,v_message_type,v_source_terminal,v_state,v_create_time,v_update_time;
   END WHILE;
  CLOSE selectUnconsumed;
  select * from _tmp_eventbus_queued_event_;
 COMMIT;
END
```

###### QueuedEventXmlMapper：
适用于需自定义事件存储结构及操作的情况，需按接口注释实现对应功能的方法。
##### 2）、JPA
//TODO
#### 3、DatabaseTopicEventSource
主题形式（发布-订阅）基于数据库的事件源，每事件可被若干客户端各消费一次。
//TODO

### 注意事项
SpringBoot环境下需在启动时显示调用ConfigurableApplicationContext.start()方法；