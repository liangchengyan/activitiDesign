package com.liang.activitidesign.service;

import java.io.IOException;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

public interface ModelerServiceI {

  /**
   * 新建一个模型model
   */
  Object newActivitiModel();

  /**
   * 上传bpmn20.xml文件并存入model数据库
   */
  Object uploadBpmn(MultipartFile file) throws Exception;

  /**
   * 下载流程模型
   */
  void downloadModel(String id, HttpServletResponse response) throws Exception;

  /**
   * 下载已部署的bpmnModel
   */
  void downBpmnModel(String processDefinitionId, HttpServletResponse response)
      throws IOException;

  /**
   * 部署model模型
   */
  Object deployModel(String id) throws Exception;

  /**
   * 获取model列表
   */
  Object getModelList(Integer rowSize, Integer page);

  /**
   * 获取流程部署列表
   */
  Object getDeploymentList(Integer rowSize, Integer page);

  /**
   * 删除model
   */
  Object deleteModel(String id);
}
