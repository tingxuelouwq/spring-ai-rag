/*
 Navicat Premium Data Transfer

 Source Server         : localhost
 Source Server Type    : MySQL
 Source Server Version : 50714
 Source Host           : 127.0.0.1:3306
 Source Schema         : rag

 Target Server Type    : MySQL
 Target Server Version : 50714
 File Encoding         : 65001

 Date: 07/10/2026 23:27:25
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for ali_oss_file
-- ----------------------------
DROP TABLE IF EXISTS `ali_oss_file`;
CREATE TABLE `ali_oss_file`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `file_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件名',
  `url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '链接地址',
  `vector_id` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '该文件分割出的多段向量文本ID',
  `create_time` timestamp(0) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` timestamp(0) NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '阿里云OSS文件表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of ali_oss_file
-- ----------------------------
INSERT INTO `ali_oss_file` VALUES (1, 'rag项目实战.md', 'https://kb-oss-2026.oss-cn-beijing.aliyuncs.com/53128d98-19c9-4019-98fe-0cd35166ecea.md', '[\"4679cf4f-8288-4f32-8f8b-0dfa45e17da2\",\"62493bc2-13b4-4158-b060-8d4ad6676fed\"]', '2026-10-06 15:30:52', '2026-10-06 15:30:52');
INSERT INTO `ali_oss_file` VALUES (2, '单反教程.docx', 'https://kb-oss-2026.oss-cn-beijing.aliyuncs.com/05157d0c-62a9-4ef0-9d2c-a6b13aba9ebb.docx', '[\"d673ee0b-aa2e-4fee-9133-b1415a46a622\"]', '2026-10-06 16:41:23', '2026-10-06 16:41:23');
INSERT INTO `ali_oss_file` VALUES (3, 'rag项目实战.md', 'https://kb-oss-2026.oss-cn-beijing.aliyuncs.com/65fe1f4b-b3b0-472f-a024-24b4151a41e7.md', '[\"5c818d76-4152-444e-baea-96bbfec16757\",\"a3cc06d3-92c4-45fa-b82e-470a85abdc33\",\"339eced7-9234-486c-a4ef-a5346cbe90d3\",\"4f085e63-ed53-41e8-b8dd-74227a41c0dc\",\"deaf88c2-af5c-4956-b048-2cedd1b4d179\",\"7b1b3677-cf25-4dad-aab8-5f542f3dc9d4\",\"3ef7ced5-3bed-468a-aee2-422be9cebdef\",\"4b6884c8-b45d-4e2a-b6f0-9212030caa1d\"]', '2026-10-07 14:03:59', '2026-10-07 14:03:59');
INSERT INTO `ali_oss_file` VALUES (4, 'rag项目实战.md', 'https://kb-oss-2026.oss-cn-beijing.aliyuncs.com/f7a73694-a143-4fea-bdd0-c97d6f26e578.md', '[\"7893a080-b73a-43ec-be46-568b9c25dc04\",\"5e091550-fcce-4015-9ecc-4fb44f409d55\",\"00a9806a-eae8-49f8-8cad-282482ec4ea7\"]', '2026-10-07 14:26:53', '2026-10-07 14:26:53');

-- ----------------------------
-- Table structure for log_info
-- ----------------------------
DROP TABLE IF EXISTS `log_info`;
CREATE TABLE `log_info`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `method_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '方法名',
  `class_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '类目',
  `request_time` datetime(3) NULL DEFAULT NULL COMMENT '请求时间戳',
  `request_params` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '请求参数',
  `response` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '响应结果',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 46 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '日志信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of log_info
-- ----------------------------
INSERT INTO `log_info` VALUES (1, 'generatePost', 'com.kevin.springai.rag.controller.AiRagController', '2026-10-07 04:21:20.967', '[\"你好\\r\\n\"]', NULL);
INSERT INTO `log_info` VALUES (2, 'generatePost', 'com.kevin.springai.rag.controller.AiRagController', '2026-10-07 04:21:28.234', '[\"你好\\r\\n\"]', NULL);
INSERT INTO `log_info` VALUES (3, 'generatePost', 'com.kevin.springai.rag.controller.AiRagController', '2026-10-07 04:21:36.899', '[\"你是谁？\\r\\n\"]', NULL);
INSERT INTO `log_info` VALUES (4, 'generatePost', 'com.kevin.springai.rag.controller.AiRagController', '2026-10-07 04:21:45.056', '[\"你是谁？\\r\\n\"]', NULL);
INSERT INTO `log_info` VALUES (5, 'generatePost', 'com.kevin.springai.rag.controller.AiRagController', '2026-10-07 04:27:09.531', '[\"你好，你是谁？\\r\\n\"]', NULL);
INSERT INTO `log_info` VALUES (6, 'generatePost', 'com.kevin.springai.rag.controller.AiRagController', '2026-10-07 04:32:44.915', '[\"你好，你是谁？\\r\\n\"]', NULL);
INSERT INTO `log_info` VALUES (7, 'generatePost', 'com.kevin.springai.rag.controller.AiRagController', '2026-10-07 05:32:43.115', '[\"你好，你是谁？\\r\\n\"]', NULL);
INSERT INTO `log_info` VALUES (8, 'generatePost', 'com.kevin.springai.rag.controller.AiRagController', '2026-10-07 05:34:02.301', '[\"你好，你是谁？\\r\\n\"]', NULL);
INSERT INTO `log_info` VALUES (9, 'generatePost', 'com.kevin.springai.rag.controller.AiRagController', '2026-10-07 05:34:18.001', '[\"你好，你是谁？\\r\\n\"]', NULL);
INSERT INTO `log_info` VALUES (10, 'generatePost', 'com.kevin.springai.rag.controller.AiRagController', '2026-10-07 05:34:29.946', '[\"你好，你是谁？\\r\\n\"]', NULL);
INSERT INTO `log_info` VALUES (11, 'generatePost', 'com.kevin.springai.rag.controller.AiRagController', '2026-10-07 05:34:53.316', '[\"你好，你是谁？\\r\\n\"]', NULL);
INSERT INTO `log_info` VALUES (12, 'generatePost', 'com.kevin.springai.rag.controller.AiRagController', '2026-10-07 05:35:08.299', '[\"你好，你是谁？\\r\\n\"]', NULL);
INSERT INTO `log_info` VALUES (13, 'generatePost', 'com.kevin.springai.rag.controller.AiRagController', '2026-10-07 05:36:14.920', '[\"你好，你是谁？\\r\\n\"]', NULL);
INSERT INTO `log_info` VALUES (14, 'generatePost', 'com.kevin.springai.rag.controller.AiRagController', '2026-10-07 05:36:47.692', '[\"你好，你是谁？\\r\\n\"]', NULL);
INSERT INTO `log_info` VALUES (15, 'generatePost', 'com.kevin.springai.rag.controller.AiRagController', '2026-10-07 05:38:45.946', '[\"你好，你是谁？\\r\\n\"]', NULL);
INSERT INTO `log_info` VALUES (16, 'generatePost', 'com.kevin.springai.rag.controller.AiRagController', '2026-10-07 05:39:26.383', '[\"你能做什么？\\r\\n\\r\\n\"]', NULL);
INSERT INTO `log_info` VALUES (17, 'generatePost', 'com.kevin.springai.rag.controller.AiRagController', '2026-10-07 05:39:44.668', '[\"你能做什么？\\r\\n\\r\\n\"]', NULL);
INSERT INTO `log_info` VALUES (18, 'generatePost', 'com.kevin.springai.rag.controller.AiRagController', '2026-10-07 05:39:51.336', '[\"你能做什么？\\r\\n\\r\\n\"]', NULL);
INSERT INTO `log_info` VALUES (19, 'generatePost', 'com.kevin.springai.rag.controller.AiRagController', '2026-10-07 05:40:33.081', '[\"你能做什么？\\r\\n\\r\\n\"]', NULL);
INSERT INTO `log_info` VALUES (20, 'generatePost', 'com.kevin.springai.rag.controller.AiRagController', '2026-10-07 05:40:36.686', '[\"你能做什么？\\r\\n\\r\\n\"]', NULL);
INSERT INTO `log_info` VALUES (21, 'generatePost', 'com.kevin.springai.rag.controller.AiRagController', '2026-10-07 05:51:09.881', '[\"阿里云Milvus有哪些注意事项\\r\\n\"]', NULL);
INSERT INTO `log_info` VALUES (22, 'generatePost', 'com.kevin.springai.rag.controller.AiRagController', '2026-10-07 05:51:33.682', '[\"阿里云Milvus有哪些注意事项\\r\\n\"]', NULL);
INSERT INTO `log_info` VALUES (23, 'generatePost', 'com.kevin.springai.rag.controller.AiRagController', '2026-10-07 05:51:38.033', '[\"阿里云Milvus有哪些注意事项\\r\\n\"]', NULL);
INSERT INTO `log_info` VALUES (24, 'generatePost', 'com.kevin.springai.rag.controller.AiRagController', '2026-10-07 05:51:44.517', '[\"阿里云Milvus有哪些注意事项\\r\\n\"]', NULL);
INSERT INTO `log_info` VALUES (25, 'generatePost', 'com.kevin.springai.rag.controller.AiRagController', '2026-10-07 05:51:51.731', '[\"阿里云Milvus有哪些注意事项\\r\\n\"]', NULL);
INSERT INTO `log_info` VALUES (26, 'generatePost', 'com.kevin.springai.rag.controller.AiRagController', '2026-10-07 06:00:20.569', '[\"阿里云Milvus有什么注意事项？\\r\\n\"]', NULL);
INSERT INTO `log_info` VALUES (27, 'generatePost', 'com.kevin.springai.rag.controller.AiRagController', '2026-10-07 06:02:11.874', '[\"阿里云Milvus有什么注意事项？\\r\\n\"]', NULL);
INSERT INTO `log_info` VALUES (28, 'generatePost', 'com.kevin.springai.rag.controller.AiRagController', '2026-10-07 06:02:27.878', '[\"阿里云Milvus有什么注意事项？\\r\\n\\r\\n\\r\\n\"]', NULL);
INSERT INTO `log_info` VALUES (29, 'generatePost', 'com.kevin.springai.rag.controller.AiRagController', '2026-10-07 06:07:17.559', '[\"阿里云Milvus注意事项\\r\\n\"]', NULL);
INSERT INTO `log_info` VALUES (30, 'generatePost', 'com.kevin.springai.rag.controller.AiRagController', '2026-10-07 06:15:51.968', '[\"阿里云Milvus注意事项\\r\\n\\r\\n\\r\\n\"]', NULL);
INSERT INTO `log_info` VALUES (31, 'generatePost', 'com.kevin.springai.rag.controller.AiRagController', '2026-10-07 06:16:40.962', '[\"阿里云Milvus注意事项\\r\\n\\r\\n\\r\\n\"]', NULL);
INSERT INTO `log_info` VALUES (32, 'generatePost', 'com.kevin.springai.rag.controller.AiRagController', '2026-10-07 06:20:09.746', '[\"阿里云对象存储OSS注意事项有哪些？\\r\\n\"]', NULL);
INSERT INTO `log_info` VALUES (33, 'generatePost', 'com.kevin.springai.rag.controller.AiRagController', '2026-10-07 10:29:42.169', '[\"阿里云对象存储OSS注意事项？\\r\\n\"]', NULL);
INSERT INTO `log_info` VALUES (34, 'generatePost', 'com.kevin.springai.rag.controller.AiRagController', '2026-10-07 10:33:41.099', '[\"阿里云Milvus注意事项？\\r\\n\"]', NULL);
INSERT INTO `log_info` VALUES (35, 'generatePost', 'com.kevin.springai.rag.controller.AiRagController', '2026-10-07 10:39:59.956', '[\"阿里云Milvus注意事项？\\r\\n\"]', NULL);
INSERT INTO `log_info` VALUES (36, 'generatePost', 'com.kevin.springai.rag.controller.AiRagController', '2026-10-07 10:57:01.098', '[\"阿里云对象存储OSS注意事项\\r\\n\"]', NULL);
INSERT INTO `log_info` VALUES (37, 'generatePost', 'com.kevin.springai.rag.controller.AiRagController', '2026-10-07 10:59:45.570', '[\"阿里云Milvus注意事项有哪些？\\r\\n\"]', NULL);
INSERT INTO `log_info` VALUES (38, 'generatePost', 'com.kevin.springai.rag.controller.AiRagController', '2026-10-07 14:46:31.709', '[\"阿里云Milvus注意事项\",\"qwen3.8-max-0902\"]', NULL);
INSERT INTO `log_info` VALUES (39, 'generatePost', 'com.kevin.springai.rag.controller.AiRagController', '2026-10-07 14:47:05.034', '[\"阿里云对象存储OSS注意事项\\r\\n\",\"deepseek-v4.1-flash\"]', NULL);
INSERT INTO `log_info` VALUES (40, 'generatePost', 'com.kevin.springai.rag.controller.AiRagController', '2026-10-07 14:47:39.913', '[\"阿里云Milvus注意事项\\r\\n\\r\\n\\r\\n\",\"deepseek-v4.1-flash\"]', NULL);
INSERT INTO `log_info` VALUES (41, 'generatePost', 'com.kevin.springai.rag.controller.AiRagController', '2026-10-07 14:47:56.954', '[\"阿里云对象存储OSS注意事项\\r\\n\\r\\n\\r\\n\",\"kimi-k3\"]', NULL);
INSERT INTO `log_info` VALUES (42, 'generatePost', 'com.kevin.springai.rag.controller.AiRagController', '2026-10-07 14:57:56.387', '[\"阿里云对象存储OSS注意事项\\r\\n\\r\\n\",\"glm-5.3\"]', NULL);
INSERT INTO `log_info` VALUES (43, 'generatePost', 'com.kevin.springai.rag.controller.AiRagController', '2026-10-07 15:00:27.983', '[\"阿里云对象存储OSS注意事项\\r\\n\",\"qwen3.8-max-0902\"]', NULL);
INSERT INTO `log_info` VALUES (44, 'generatePost', 'com.kevin.springai.rag.controller.AiRagController', '2026-10-07 15:17:44.692', '[\"阿里云Milvus注意事项？\\r\\n\",\"glmChatModel\"]', NULL);
INSERT INTO `log_info` VALUES (45, 'generatePost', 'com.kevin.springai.rag.controller.AiRagController', '2026-10-07 15:24:38.783', '[\"阿里云对象存储OSS注意事项？\\r\\n\",\"glmChatModel\"]', NULL);

-- ----------------------------
-- Table structure for sensitive_category
-- ----------------------------
DROP TABLE IF EXISTS `sensitive_category`;
CREATE TABLE `sensitive_category`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `category_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '分类名',
  `created_time` datetime(3) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(3) NULL DEFAULT NULL COMMENT '更新时间',
  `status` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '状态',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '敏感词分类表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sensitive_category
-- ----------------------------
INSERT INTO `sensitive_category` VALUES (3, '违禁词', '2026-10-07 11:34:11.846', '2026-10-07 11:34:11.846', '1');
INSERT INTO `sensitive_category` VALUES (4, '广告引流', '2026-10-07 11:55:34.868', '2026-10-07 11:55:34.868', '1');

-- ----------------------------
-- Table structure for sensitive_word
-- ----------------------------
DROP TABLE IF EXISTS `sensitive_word`;
CREATE TABLE `sensitive_word`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `word` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '敏感词内容',
  `category` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '敏感词类别',
  `status` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '敏感词状态',
  `created_at` datetime(3) NULL DEFAULT NULL COMMENT '创建时间戳',
  `updated_at` datetime(3) NULL DEFAULT NULL COMMENT '更新时间戳',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 19 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '敏感词表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sensitive_word
-- ----------------------------
INSERT INTO `sensitive_word` VALUES (7, '弹药', '3', '1', '2026-10-07 11:54:52.564', '2026-10-07 11:54:52.564');
INSERT INTO `sensitive_word` VALUES (8, '爆炸物', '3', '1', '2026-10-07 11:55:01.010', '2026-10-07 11:55:01.010');
INSERT INTO `sensitive_word` VALUES (9, '枪支', '3', '1', '2026-10-07 11:55:10.444', '2026-10-07 11:55:10.444');
INSERT INTO `sensitive_word` VALUES (10, '毒品', '3', '1', '2026-10-07 11:55:16.271', '2026-10-07 11:55:16.271');
INSERT INTO `sensitive_word` VALUES (11, '违禁品', '3', '1', '2026-10-07 11:55:21.441', '2026-10-07 11:55:21.441');
INSERT INTO `sensitive_word` VALUES (12, '加微信', '4', '1', '2026-10-07 11:55:43.472', '2026-10-07 11:55:43.472');
INSERT INTO `sensitive_word` VALUES (13, '加QQ', '4', '1', '2026-10-07 11:55:50.347', '2026-10-07 11:55:50.347');
INSERT INTO `sensitive_word` VALUES (14, '扫码进群', '4', '1', '2026-10-07 11:55:56.979', '2026-10-07 11:55:56.979');
INSERT INTO `sensitive_word` VALUES (15, '免费领取', '4', '1', '2026-10-07 11:56:04.575', '2026-10-07 11:56:04.575');
INSERT INTO `sensitive_word` VALUES (16, '点击链接', '4', '1', '2026-10-07 11:56:10.451', '2026-10-07 11:56:10.451');
INSERT INTO `sensitive_word` VALUES (17, '私聊', '4', '1', '2026-10-07 11:56:18.497', '2026-10-07 11:56:18.497');
INSERT INTO `sensitive_word` VALUES (18, '代购', '4', '1', '2026-10-07 11:56:28.847', '2026-10-07 11:56:28.847');

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '姓名',
  `user_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户名',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码',
  `phone` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '手机号',
  `sex` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '性别',
  `id_number` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '身份证号',
  `status` int(11) NOT NULL DEFAULT 1 COMMENT '状态 0：禁用 1：启用',
  `create_time` datetime(3) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(3) NULL DEFAULT NULL COMMENT '更新时间',
  `create_user` bigint(20) NULL DEFAULT NULL COMMENT '创建人',
  `update_user` bigint(20) NULL DEFAULT NULL COMMENT '修改人',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 666509 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user` VALUES (666497, '管理员', 'admin', '21232f297a57a5a743894a0e4a801fc3', '13800138000', '男', '11010519491231002X', 1, '2025-03-03 00:00:00.000', '2026-10-06 13:31:32.579', NULL, 666497);
INSERT INTO `user` VALUES (666498, '古歌', 'kevin', 'a4a229f33f9c9bc1f44d4f7393405114', '18810662082', '男', '42213019920217001X', 1, '2026-10-06 10:18:37.427', '2026-10-06 13:32:37.104', NULL, 666497);
INSERT INTO `user` VALUES (666499, '听雪楼', 'tingxuelouwq', 'a4a229f33f9c9bc1f44d4f7393405114', '18810662092', '男', '42213019921202661X', 1, '2026-10-06 13:19:15.622', '2026-10-06 13:19:15.622', NULL, NULL);
INSERT INTO `user` VALUES (666500, '测试1', 'test1', 'a4a229f33f9c9bc1f44d4f7393405114', '18810662092', '男', '42213019921202661X', 1, '2026-10-06 13:19:15.623', '2026-10-06 13:19:15.622', NULL, NULL);
INSERT INTO `user` VALUES (666501, '测试2', 'test2', 'a4a229f33f9c9bc1f44d4f7393405114', '18810662092', '男', '42213019921202661X', 1, '2026-10-06 13:19:15.624', '2026-10-06 13:19:15.622', NULL, NULL);
INSERT INTO `user` VALUES (666502, '测试3', 'test3', 'a4a229f33f9c9bc1f44d4f7393405114', '18810662092', '男', '42213019921202661X', 1, '2026-10-06 13:19:15.625', '2026-10-06 13:19:15.622', NULL, NULL);
INSERT INTO `user` VALUES (666503, '测试4', 'test4', 'a4a229f33f9c9bc1f44d4f7393405114', '18810662092', '男', '42213019921202661X', 1, '2026-10-06 13:19:15.626', '2026-10-06 13:19:15.622', NULL, NULL);
INSERT INTO `user` VALUES (666504, '测试5', 'test5', 'a4a229f33f9c9bc1f44d4f7393405114', '18810662092', '男', '42213019921202661X', 1, '2026-10-06 13:19:15.627', '2026-10-06 13:19:15.622', NULL, NULL);
INSERT INTO `user` VALUES (666505, '测试6', 'test6', 'a4a229f33f9c9bc1f44d4f7393405114', '18810662092', '男', '42213019921202661X', 1, '2026-10-06 13:19:15.628', '2026-10-06 13:19:15.622', NULL, NULL);
INSERT INTO `user` VALUES (666506, '测试7', 'test7', 'a4a229f33f9c9bc1f44d4f7393405114', '18810662092', '男', '42213019921202661X', 1, '2026-10-06 13:19:15.629', '2026-10-06 13:19:15.622', NULL, NULL);
INSERT INTO `user` VALUES (666507, '测试8', 'test8', 'a4a229f33f9c9bc1f44d4f7393405114', '18810662092', '男', '42213019921202661X', 1, '2026-10-06 13:19:15.630', '2026-10-06 13:19:15.622', NULL, NULL);
INSERT INTO `user` VALUES (666508, '测试9', 'test9', 'a4a229f33f9c9bc1f44d4f7393405114', '18810662092', '男', '42213019921202661X', 1, '2026-10-06 13:19:15.631', '2026-10-06 13:19:15.622', NULL, NULL);

-- ----------------------------
-- Table structure for vector_store
-- ----------------------------
DROP TABLE IF EXISTS `vector_store`;
CREATE TABLE `vector_store`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
  `metadata` json NULL,
  `embedding` json NULL COMMENT '向量数据，存储为JSON数组，原PostgreSQL为vector(1536)',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of vector_store
-- ----------------------------

-- ----------------------------
-- Table structure for word_frequency
-- ----------------------------
DROP TABLE IF EXISTS `word_frequency`;
CREATE TABLE `word_frequency`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `word` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '分词',
  `count_num` int(11) NULL DEFAULT NULL COMMENT '出现频次',
  `business_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '业务类型',
  `create_time` date NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` date NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '词频统计表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of word_frequency
-- ----------------------------

SET FOREIGN_KEY_CHECKS = 1;
