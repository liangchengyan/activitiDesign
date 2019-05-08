package com.liang.activitidesign.controller;

import com.liang.activitidesign.Model.DeploymentVO;
import com.liang.activitidesign.controller.common.RestServiceController;
import com.liang.activitidesign.service.ModelerServiceI;
import com.liang.activitidesign.utils.ToWeb;
import java.io.IOException;
import javax.servlet.http.HttpServletResponse;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Deployment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("deployments")
public class DeploymentController implements RestServiceController<Deployment, String> {

  @Autowired
  private ModelerServiceI modelerServiceI;

  @Autowired
  RepositoryService repositoryService;

  @Override
  public Object getOne(@PathVariable("id") String id) {

    Deployment deployment = repositoryService.createDeploymentQuery().deploymentId(id)
        .singleResult();

    return ToWeb.buildResult().setObjData(new DeploymentVO(deployment));
  }

  @Override
  public Object getList(
      @RequestParam(value = "rowSize", defaultValue = "1000", required = false) Integer rowSize,
      @RequestParam(value = "page", defaultValue = "1", required = false) Integer page) {

    return modelerServiceI.getDeploymentList(rowSize, page);

  }

  /**
   * 下载已部署的bpmn流程模型
   *
   * @param processDefinitionId 流程定义id
   */
  @GetMapping("/{processDefinitionId}/downloadBpmnModel")
  public void downloadBpmnModel(
      @PathVariable(name = "processDefinitionId") String processDefinitionId,
      HttpServletResponse response) throws IOException {

    modelerServiceI.downBpmnModel(processDefinitionId, response);

  }

  @Override
  public Object deleteOne(@PathVariable("id") String id) {

    repositoryService.deleteDeployment(id);

    return ToWeb.buildResult().refresh();
  }

  @Override
  public Object postOne(@RequestBody Deployment entity) {
    return null;
  }

  @Override
  public Object putOne(@PathVariable("id") String s, @RequestBody Deployment entity) {
    return null;
  }

  @Override
  public Object patchOne(@PathVariable("id") String s, @RequestBody Deployment entity) {
    return null;
  }

}
