 CREATE TABLE `board` (
      `NUM` int(11) NOT NULL AUTO_INCREMENT COMMENT '번호',
      `SUBJECT` varchar(250) NOT NULL COMMENT '제목',
      `WRITER` varchar(50) NOT NULL COMMENT '작성자',
      `CONTENTS` text COMMENT '내용',
      `HIT` int(11) DEFAULT NULL COMMENT '조회수',
      `IP` varchar(30) NOT NULL COMMENT '아이피',
      `REG_DATE` datetime NOT NULL COMMENT '등록 일시',
      `MOD_DATE` datetime DEFAULT NULL COMMENT '수정 일시',
      PRIMARY KEY (`NUM`),
      UNIQUE KEY `NUM` (`NUM`)
   ) ENGINE=MyISAM DEFAULT CHARSET=utf8 COMMENT='게시판';
