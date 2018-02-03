CREATE DATABASE IF NOT EXISTS `hansight` DEFAULT CHARACTER SET utf8 ;



USE `hansight`;



DROP TABLE IF EXISTS `CONF_AGENT`;



CREATE TABLE `CONF_AGENT` (

  `ID` varchar(40) NOT NULL COMMENT 'ID',

  `NAME` varchar(255) NOT NULL COMMENT '名称',

  `IP` varchar(20) NOT NULL COMMENT 'ip',

  `DESCRIPTION` varchar(255) DEFAULT NULL COMMENT '描述',

  `state` varchar(50) NOT NULL COMMENT '状态',

  `CREATE_DATE` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '时间',

  PRIMARY KEY (`ID`),

  UNIQUE KEY `NAME` (`NAME`)

) ENGINE=InnoDB DEFAULT CHARSET=utf8;




DROP TABLE IF EXISTS `CONF_CATEGORY`;



CREATE TABLE `CONF_CATEGORY` (

  `ID` varchar(40) NOT NULL COMMENT 'ID',

  `NAME` varchar(100) NOT NULL COMMENT '名称',

  `TYPE` varchar(100) NOT NULL COMMENT '日志类型',

  `PATTERN` varchar(1024) DEFAULT NULL COMMENT '正则表达式',

  `AGENT_PARSER` varchar(255) NOT NULL COMMENT 'AGENT解析器',

  `EXAMPLE` tinyblob DEFAULT NULL COMMENT '日志样本',

  `CREATEDATE` date DEFAULT NULL COMMENT '创建日期',

  `FORWARDER_PARSER` varchar(255) NOT NULL COMMENT 'FORWARDER解析器',

  `PROTOCOL` varchar(10) NOT NULL COMMENT '协议',

  PRIMARY KEY (`ID`)

) ENGINE=InnoDB DEFAULT CHARSET=utf8;



insert  into `CONF_CATEGORY`(`ID`,`NAME`,`TYPE`,`PATTERN`,`AGENT_PARSER`,`EXAMPLE`,`CREATEDATE`,`FORWARDER_PARSER`,`PROTOCOL`) values ('3b2f9f3b-0342-48c6-8488-de04bfea5f1b','windowsevt','event','','winevt','d:\\evtx\\mailboxdatabasefailureitems.evtx	2	2012/6/17 11:08	2012/6/17 11:08	1	4	Information event	3	The name for category 3 in Source \"Microsoft-Exchange-MailboxDatabaseFailureItems\" cannot be found. The local computer may not have the necessary registry i','2014-09-06','xml','file'),('550df1c4-43fe-43e8-aa01-3a62c47517e6','csv','log','{separate:\'\\t\',fields:[ \'#\', \'@timestamp\', \'generated\', \'product_entity_or_endpoint\', \'product\', \'product_or_endpoint_ip\', \'product_or_endpoint_mac\', \'management_server_entity\', \'virus_malware\',\'endpoint\', \'s_host\', \'user\', \'measures\', \'results\', \'detections\', \'entry_type\', \'details\']}','default','X97M_OLEMAL.A,2014/6/27 14:41,武汉赫山测算2014-6-19.xlsm,成功删除不可清除的文件?10.130.70.162,病毒扫描,http,http://sz.mail.ftn.qq.com/ftn_handler/1da1427d96e032aacd94e706bd8086fb693b04025dd3cb05165607344a469f92?compressed=0&amp;dtype=','2014-09-06','delimit','file'),('56e0c402-2117-4b09-b1f1-fdf31140380c','syslog_tcp','log','(?:%{SYSLOGTIMESTAMP:timestamp}|%{TIMESTAMP_ISO8601:timestamp8601}) (?:%{SYSLOGFACILITY} )?%{SYSLOGHOST:logsource} %{SYSLOGPROG}:','default','2014/07/20 12:03:46 PM,URL 阻止,损坏的 Zip 文件,10.152.3.115,正常,0,http://sz.fcloud.store.qq.com/store_file_download?buid=10247&amp;uin=632453359&amp;dir_path=/decorate/&amp;name=bab81d3f39f63a684f2aee68b68ce44d.zip&amp;offset=0&amp;length=30000','2014-09-06','regex','tcp'),('5fa40104-2458-4a7d-be92-8afee69e2964','apache','log','%{IPORHOST:c_ip} %{USER:ident} %{USER:auth} [%{HTTPDATE:@timestamp}] \"(?:%{WORD:method} %{NOTSPACE:request}(?: HTTP/%{NUMBER:httpversion})?|%{DATA:rawrequest})\" %{NUMBER:response} (?:%{NUMBER:bytes}|-)( %{QS:referrer} %{QS:agent} %{NUMBER:read} %{NUMBER:write})?','default','218.30.103.142 - - [28/May/2013:17:06:11 +0800] \"GET /index.php/product/?p=1&a=view&r=469 HTTP/1.1\" 200 10535\r\n218.30.103.142 - - [28/May/2013:17:06:22 +0800] \"GET /index.php/product/?p=1&a=view&r=756 HTTP/1.1\" 200 10518\r\n124.127.254.146 - - [28/May/2013:','2014-09-06','regex','file'),('7c8208cf-5586-40be-b4b8-a5b6a7b380b2','iis','log','%{TIMESTAMP_ISO8601:@timestamp} %{WORD:s_site_name} %{HOST:s_computer_name} %{IP:s_ip} %{WORD:cs_method} %{URIPATH:cs_uri_stem} %{NOTSPACE:cs_uri_query} %{NUMBER:s_port} %{NOTSPACE:cs_username} %{IP:c_ip} %{NOTSPACE:cs_version} %{NOTSPACE:cs_useragent} %{COOKIE:cs_cookie} %{NOTSPACE:cs_referer} %{NOTSPACE:cs_host} %{NUMBER:sc_status} %{NUMBER:sc_substatus} %{NUMBER:sc_winstatus} %{NUMBER:sc_bytes} %{NUMBER:cs_bytes} %{NUMBER:time_taken}','default','#Fields: date time s-sitename s-computername s-ip cs-method cs-uri-stem cs-uri-query s-port cs-username c-ip cs-version cs(User-Agent) cs(Cookie) cs(Referer) cs-host sc-status sc-substatus sc-win32-status sc-bytes cs-bytes time-taken \r\n2013-12-09 15:59:59','2014-09-06','regex','file'),('927c9ea6-1de9-49b1-860d-46204db92194','syslog_udp','log','(?:%{SYSLOGTIMESTAMP:timestamp}|%{TIMESTAMP_ISO8601:timestamp8601}) (?:%{SYSLOGFACILITY} )?%{SYSLOGHOST:logsource} %{SYSLOGPROG}:','default','2014/07/20 12:03:46 PM,URL 阻止,损坏的 Zip 文件,10.152.3.115,正常,0,http://sz.fcloud.store.qq.com/store_file_download?buid=10247&amp;uin=632453359&amp;dir_path=/decorate/&amp;name=bab81d3f39f63a684f2aee68b68ce44d.zip&amp;offset=0&amp;length=30000','2014-09-06','regex','udp'),('9849cbd7-9b7c-493b-bd47-475f79cbc6eb','snmp_tcp','event',NULL,'snmp_tcp','','2014-09-06','json','tcp'),('e6ea7a2e-7947-4692-bb99-da575ddad707','snmp_udp','event',NULL,'snmp_udp','','2014-09-06','json','udp'),('f3dcf7d7-6d9c-41aa-bdce-4afc92df1b6a','other','log',NULL,'default','','2014-09-06','delimit','file');





DROP TABLE IF EXISTS `CONF_DATASOURCE`;



CREATE TABLE `CONF_DATASOURCE` (

  `ID` varchar(40) NOT NULL COMMENT 'ID',

  `AGENT_ID` varchar(40) NOT NULL COMMENT 'CONF_AGENT表中的ID',

  `CATEGORY_ID` varchar(40) NOT NULL COMMENT 'CONF_CATEGORY表中的ID',

  `TYPE` varchar(100) NOT NULL COMMENT '日志类型',

  `PATTERN` varchar(2048) DEFAULT NULL COMMENT '正则表达式',

  `AGENT_PARSER` varchar(100) NOT NULL COMMENT '采集器解析',

  `HOST` varchar(20) DEFAULT NULL COMMENT '部署Ip',

  `PORT` int(5) DEFAULT NULL COMMENT '端口号',

  `PROTOCOL` varchar(80) NOT NULL COMMENT '协议',

  `URL` varchar(255) DEFAULT NULL COMMENT '路径',

  `ENCODING` varchar(10) NOT NULL COMMENT '编码格式',

  `FORWARDER_PARSER` varchar(100) NOT NULL COMMENT '转发器解析',

  `CATEGORY` varchar(40) NOT NULL COMMENT '日志名称',

  `STATE` varchar(50) NOT NULL COMMENT '状态',

  `FORWARDER_NAME` varchar(255) DEFAULT NULL COMMENT '转发器名称',

  `CREATE_DATE` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '时间',

  `DATASOURCE_NAME` varchar(255) NOT NULL COMMENT '数据源名称',

  `CONFIG` varchar(50) NOT NULL COMMENT '配置状态',

  `CONFIGINFO` varchar(1024) DEFAULT NULL COMMENT '配置信息',

  PRIMARY KEY (`ID`),

  KEY `AGENT_ID` (`AGENT_ID`),

  KEY `CATEGORY_ID` (`CATEGORY_ID`),

  CONSTRAINT `CONF_DATASOURCE_ibfk_1` FOREIGN KEY (`AGENT_ID`) REFERENCES `CONF_AGENT` (`ID`) ON DELETE CASCADE ON UPDATE CASCADE,

  CONSTRAINT `CONF_DATASOURCE_ibfk_2` FOREIGN KEY (`CATEGORY_ID`) REFERENCES `CONF_CATEGORY` (`ID`) ON DELETE CASCADE ON UPDATE CASCADE

) ENGINE=InnoDB DEFAULT CHARSET=utf8;



DROP TABLE IF EXISTS `CONF_FORWARDER`;



CREATE TABLE `CONF_FORWARDER` (

  `ID` varchar(40) NOT NULL COMMENT 'ID',

  `NAME` varchar(255) NOT NULL COMMENT '名称',

  `IP` varchar(20) NOT NULL COMMENT 'ip',

  `DESCRIPTION` varchar(255) DEFAULT NULL COMMENT '描述',

  `state` varchar(50) NOT NULL COMMENT '状态',

  `CREATE_DATE` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '时间',

  PRIMARY KEY (`ID`),

  UNIQUE KEY `NAME` (`NAME`)

) ENGINE=InnoDB DEFAULT CHARSET=utf8;




DROP TABLE IF EXISTS `CONF_DS_FORWARDER`;



CREATE TABLE `CONF_DS_FORWARDER` (

  `ID` varchar(40) NOT NULL COMMENT 'ID',

  `DATASOURCE_ID` varchar(40) NOT NULL COMMENT 'CONF_DATASOURCE表中的ID',

  `FORWARDER_ID` varchar(40) NOT NULL COMMENT 'CONF_FORWARDER表中的ID',

  PRIMARY KEY (`ID`),

  KEY `FORWARDER_ID` (`FORWARDER_ID`),

  KEY `DATASOURCE_ID` (`DATASOURCE_ID`),

  CONSTRAINT `CONF_DS_FORWARDER_ibfk_1` FOREIGN KEY (`FORWARDER_ID`) REFERENCES `CONF_FORWARDER` (`ID`) ON DELETE CASCADE ON UPDATE CASCADE,

  CONSTRAINT `CONF_DS_FORWARDER_ibfk_2` FOREIGN KEY (`DATASOURCE_ID`) REFERENCES `CONF_DATASOURCE` (`ID`) ON DELETE CASCADE ON UPDATE CASCADE

) ENGINE=InnoDB DEFAULT CHARSET=utf8;


DROP TABLE IF EXISTS `DB_PANEL`;



CREATE TABLE `DB_PANEL` (

  `ID` varchar(32) NOT NULL,

  `NAME` varchar(100) NOT NULL COMMENT '名称',

  `TYPE` varchar(20) NOT NULL COMMENT '类型',

  `CONTENT` varchar(4000) NOT NULL COMMENT '内容',

  `POSITION` int(3) DEFAULT '0' COMMENT '位置',

  `USER_ID` varchar(80) DEFAULT NULL COMMENT '用户',

  `USE_FLAG` int(1) DEFAULT '1' COMMENT '是否可用',

  `DATE_CREATED` datetime DEFAULT NULL COMMENT '创建日期',

  PRIMARY KEY (`ID`)

) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `T_AUTHORITY`;



CREATE TABLE `T_AUTHORITY` (

  `ID` varchar(32) NOT NULL,

  `NAME` varchar(255) NOT NULL COMMENT '权限名称',

  `DESCRIPTION` varchar(255) NOT NULL COMMENT '权限描述',

  `USE_FLAG` int(1) DEFAULT '1' COMMENT '是否可用',

  PRIMARY KEY (`ID`)

) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='权限表';







DROP TABLE IF EXISTS `T_REQUESTMAP`;



CREATE TABLE `T_REQUESTMAP` (

  `ID` varchar(32) NOT NULL,

  `DESCRIPTION` varchar(255) DEFAULT NULL COMMENT '请求描述',

  `URL` varchar(1000) NOT NULL COMMENT '请求地址',

  `USE_FLAG` int(1) DEFAULT '1' COMMENT '是否可用',

  PRIMARY KEY (`ID`)

) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='请求资源表';





DROP TABLE IF EXISTS `T_AUTHORITY_REQUESTMAP`;



CREATE TABLE `T_AUTHORITY_REQUESTMAP` (

  `ID` varchar(32) NOT NULL,

  `AUTHORITY_ID` varchar(32) NOT NULL,

  `REQUESTMAP_ID` varchar(32) NOT NULL,

  PRIMARY KEY (`ID`),

  KEY `AUTHORITY_ID` (`AUTHORITY_ID`),

  KEY `REQUESTMAP_ID` (`REQUESTMAP_ID`),

  CONSTRAINT `T_AUTHORITY_REQUESTMAP_ibfk_1` FOREIGN KEY (`AUTHORITY_ID`) REFERENCES `T_AUTHORITY` (`ID`),

  CONSTRAINT `T_AUTHORITY_REQUESTMAP_ibfk_2` FOREIGN KEY (`REQUESTMAP_ID`) REFERENCES `T_REQUESTMAP` (`ID`)

) ENGINE=InnoDB DEFAULT CHARSET=utf8;











DROP TABLE IF EXISTS `T_ROLE`;



CREATE TABLE `T_ROLE` (

  `ID` varchar(32) NOT NULL,

  `NAME` varchar(255) NOT NULL COMMENT '角色名称',

  `DESCRIPTION` varchar(255) NOT NULL COMMENT '角色描述',

  `USE_FLAG` int(1) DEFAULT '1' COMMENT '是否可用',

  `SORTID` int(3) DEFAULT '0' COMMENT '排序号',

  PRIMARY KEY (`ID`)

) ENGINE=InnoDB DEFAULT CHARSET=utf8;





insert  into `T_ROLE`(`ID`,`NAME`,`DESCRIPTION`,`USE_FLAG`,`SORTID`) values ('2c905b9b487c07ec01487c767fc80001','ROLE_USER','普通用户',1,0),('2c905b9b487c07ec01487c76c0370002','ROLE_ADMIN','管理员',1,0);





DROP TABLE IF EXISTS `T_ROLE_authority`;



CREATE TABLE `T_ROLE_authority` (

  `ID` varchar(32) NOT NULL,

  `ROLE_ID` varchar(32) NOT NULL,

  `AUTHORITY_ID` varchar(32) NOT NULL,

  PRIMARY KEY (`ID`)

) ENGINE=InnoDB DEFAULT CHARSET=utf8;







DROP TABLE IF EXISTS `T_USER`;



CREATE TABLE `T_USER` (

  `ID` varchar(32) NOT NULL,

  `USER_ID` varchar(80) NOT NULL COMMENT '帐号',

  `PASSWORD` varchar(255) NOT NULL COMMENT '密码',

  `EMAIL` varchar(255) NOT NULL COMMENT '电子邮箱',

  `NICK_NAME` varchar(80) DEFAULT NULL COMMENT '昵称',

  `USE_FLAG` int(1) NOT NULL DEFAULT '1' COMMENT '是否可用',

  `CREATE_DATE` date DEFAULT NULL COMMENT '注册日期',

  `ACCOUNT_NON_EXPIRED` int(1) DEFAULT '0' COMMENT '是否过期',

  `ACCOUNT_NON_LOCKED` int(1) DEFAULT '0' COMMENT '帐户是否锁定',

  `CREDENTIALS_NON_EXPIRED` int(1) DEFAULT '0' COMMENT '证书是否过期',

  `LAST_LOGIN_DATE` date DEFAULT NULL COMMENT '最后登录日期',

  PRIMARY KEY (`ID`)

) ENGINE=InnoDB DEFAULT CHARSET=utf8;





insert  into `T_USER`(`ID`,`USER_ID`,`PASSWORD`,`EMAIL`,`NICK_NAME`,`USE_FLAG`,`CREATE_DATE`,`ACCOUNT_NON_EXPIRED`,`ACCOUNT_NON_LOCKED`,`CREDENTIALS_NON_EXPIRED`,`LAST_LOGIN_DATE`) values ('2c905b9b487c07ec01487c77294f0003','admin','397c5b9d23a8ea5a1eb91572ec3070122a591ece','admin@kunlun.com','管理员',1,'2014-09-16',NULL,NULL,NULL,NULL);







DROP TABLE IF EXISTS `T_USER_ROLE`;



CREATE TABLE `T_USER_ROLE` (

  `USER_ID` varchar(32) NOT NULL,

  `ROLE_ID` varchar(32) NOT NULL,

  PRIMARY KEY (`USER_ID`,`ROLE_ID`),

  KEY `FK3E62963F4D4314F7` (`ROLE_ID`),

  KEY `FK3E62963FF26DD8D7` (`USER_ID`),

  CONSTRAINT `FK3E62963F4D4314F7` FOREIGN KEY (`ROLE_ID`) REFERENCES `T_ROLE` (`ID`),

  CONSTRAINT `FK3E62963FF26DD8D7` FOREIGN KEY (`USER_ID`) REFERENCES `T_USER` (`ID`)

) ENGINE=InnoDB DEFAULT CHARSET=utf8;







insert  into `T_USER_ROLE`(`USER_ID`,`ROLE_ID`) values ('2c905b9b487c07ec01487c77294f0003','2c905b9b487c07ec01487c76c0370002');

DROP TABLE IF EXISTS `SYSTEM_LOG`;

CREATE TABLE `SYSTEM_LOG` (
  `ID` varchar(40) NOT NULL COMMENT 'ID',
  `NAME` varchar(255) NOT NULL COMMENT '用户名',
  `IP` varchar(20) NOT NULL COMMENT '用户ip',
  `DESCRIPTION` text NOT NULL COMMENT '日志描述',
  `LOG_RESULT` varchar(50) NOT NULL COMMENT '日志结果',
  `CREATETIME` timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY  (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `CONF_WARNING`;

CREATE TABLE `CONF_WARNING`(
  `ID`   VARCHAR(40) NOT NULL COMMENT 'ID',
  `CATEGORY`   VARCHAR(30) NOT NULL COMMENT '类别',
  `COMPOTENT`   VARCHAR(50) NOT NULL COMMENT 'COMPOTENT',
  `NAME`   VARCHAR(20) NOT NULL COMMENT '名称',
  `UPDATE_TIME`   VARCHAR(30)  DEFAULT NULL COMMENT '更新日期',
  `EMAIL`   VARCHAR(80) DEFAULT 'admin@kunlun.com'  COMMENT '邮箱地址',
  `DEFAULT_VALUE`   VARCHAR(50) DEFAULT '0.8' COMMENT '默认值',
  `DESCRIPTION`   VARCHAR(100) NOT NULL COMMENT '描述',
  `VALUE`  VARCHAR(30) DEFAULT '0.00' COMMENT '管理员配置空间',
  PRIMARY KEY (`ID`),
  UNIQUE KEY (`NAME`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

insert into CONF_WARNING(`ID`,`CATEGORY`,`COMPOTENT`,`NAME`,`DEFAULT_VALUE`,`DESCRIPTION`) VALUES('2c905b9b487c07ec01487c767fc800022','ALERT','alert.es','alert.es.rate','0.8','es使用空间达到指定阀值，会有邮件形式给指定邮箱发送告警');

CREATE TABLE `SYSTEM_TIME_RANGE` (
  `ID` varchar(40) NOT NULL COMMENT 'ID',
  `CATEGORY` varchar(30) NOT NULL COMMENT '类别',
  `TIME_VALUE` int(10) NOT NULL COMMENT '时间',
  `TIME_UNIT` varchar(20) NOT NULL COMMENT '单位',
  `TIME_REFRESH` int(10) NOT NULL COMMENT '刷新',
  `TIME_REFRESH_UNIT` varchar(20) NOT NULL COMMENT '刷新单位',
  `USER_ID` varchar(80) DEFAULT NULL COMMENT '用户',
  `DATE_UPDATE` datetime DEFAULT NULL COMMENT '更新日期',
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

insert into SYSTEM_TIME_RANGE(ID,CATEGORY,TIME_VALUE,TIME_UNIT,TIME_REFRESH,TIME_REFRESH_UNIT,USER_ID,DATE_UPDATE) values('2c905bdb490cacd201490cae51a00008', 'analyse', '5', 'minute', '30', 'second', NULL, '2014-11-06 11:16:46');
insert into SYSTEM_TIME_RANGE(ID,CATEGORY,TIME_VALUE,TIME_UNIT,TIME_REFRESH,TIME_REFRESH_UNIT,USER_ID,DATE_UPDATE) values('2c905bdb490cacd201490cae51a00009', 'dashboard', '5', 'minute', '30', 'second', NULL, '2014-11-06 11:16:46');

