-- phpMyAdmin SQL Dump
-- version 5.0.2
-- https://www.phpmyadmin.net/
--
-- 主机： 127.0.0.1
-- 生成日期： 2020-05-18 16:36:08
-- 服务器版本： 10.4.11-MariaDB
-- PHP 版本： 7.4.4

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- 数据库： `chat`
--
CREATE DATABASE IF NOT EXISTS `chat` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `chat`;

-- --------------------------------------------------------

--
-- 表的结构 `chatmessage`
--

DROP TABLE IF EXISTS `chatmessage`;
CREATE TABLE `chatmessage` (
  `mid` int(11) NOT NULL,
  `cid` int(11) NOT NULL,
  `from_uid` int(11) DEFAULT NULL,
  `from_name` varchar(50) NOT NULL,
  `message` text NOT NULL,
  `time` datetime NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- --------------------------------------------------------

--
-- 表的结构 `chatroom`
--

DROP TABLE IF EXISTS `chatroom`;
CREATE TABLE `chatroom` (
  `cid` int(11) NOT NULL,
  `type` tinyint(4) NOT NULL,
  `group_number` int(4) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- --------------------------------------------------------

--
-- 表的结构 `chatroom_user`
--

DROP TABLE IF EXISTS `chatroom_user`;
CREATE TABLE `chatroom_user` (
  `cid` int(11) NOT NULL,
  `uid` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- --------------------------------------------------------

--
-- 表的结构 `user`
--

DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `uid` int(11) NOT NULL,
  `username` varchar(50) NOT NULL,
  `password` varchar(100) NOT NULL,
  `last_online` datetime DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- 转储表的索引
--

--
-- 表的索引 `chatmessage`
--
ALTER TABLE `chatmessage`
  ADD PRIMARY KEY (`mid`),
  ADD KEY `cid` (`cid`),
  ADD KEY `from_uid` (`from_uid`);

--
-- 表的索引 `chatroom`
--
ALTER TABLE `chatroom`
  ADD PRIMARY KEY (`cid`);

--
-- 表的索引 `chatroom_user`
--
ALTER TABLE `chatroom_user`
  ADD PRIMARY KEY (`cid`,`uid`),
  ADD KEY `uid` (`uid`);

--
-- 表的索引 `user`
--
ALTER TABLE `user`
  ADD PRIMARY KEY (`uid`);

--
-- 在导出的表使用AUTO_INCREMENT
--

--
-- 使用表AUTO_INCREMENT `chatmessage`
--
ALTER TABLE `chatmessage`
  MODIFY `mid` int(11) NOT NULL AUTO_INCREMENT;

--
-- 使用表AUTO_INCREMENT `chatroom`
--
ALTER TABLE `chatroom`
  MODIFY `cid` int(11) NOT NULL AUTO_INCREMENT;

--
-- 使用表AUTO_INCREMENT `user`
--
ALTER TABLE `user`
  MODIFY `uid` int(11) NOT NULL AUTO_INCREMENT;

--
-- 限制导出的表
--

--
-- 限制表 `chatmessage`
--
ALTER TABLE `chatmessage`
  ADD CONSTRAINT `chatmessage_ibfk_1` FOREIGN KEY (`cid`) REFERENCES `chatroom` (`cid`) ON DELETE CASCADE,
  ADD CONSTRAINT `chatmessage_ibfk_2` FOREIGN KEY (`from_uid`) REFERENCES `user` (`uid`) ON DELETE SET NULL;

--
-- 限制表 `chatroom_user`
--
ALTER TABLE `chatroom_user`
  ADD CONSTRAINT `chatroom_user_ibfk_1` FOREIGN KEY (`cid`) REFERENCES `chatroom` (`cid`) ON DELETE CASCADE,
  ADD CONSTRAINT `chatroom_user_ibfk_2` FOREIGN KEY (`uid`) REFERENCES `user` (`uid`) ON DELETE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
