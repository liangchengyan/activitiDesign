package com.liang.activitidesign.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.liang.activitidesign.controller.common.RestServiceController;
import com.liang.activitidesign.service.ModelerServiceI;
import com.liang.activitidesign.utils.ToWeb;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Model;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/models")
@Slf4j
public class ModelerController implements RestServiceController<Model, String> {

  @Autowired
  RepositoryService repositoryService;

  @Autowired
  ObjectMapper objectMapper;

  @Autowired
  private ModelerServiceI modelerServiceI;


  /**
   * 新建一个空模型
   */
  @PostMapping("newModel")
  public Object newModel() {

    String id = String.valueOf(modelerServiceI.newActivitiModel());

    return ToWeb.buildResult().redirectUrl("/editor?modelId=" + id);
  }


  /**
   * 发布模型为流程定义
   */
  @PostMapping("{id}/deployment")
  public Object deploy(@PathVariable("id") String id) throws Exception {

    modelerServiceI.deployModel(id);

    return ToWeb.buildResult().refresh();
  }

  /**
   * 下载流程模型
   */
  @GetMapping("/{id}/downloadModel")
  public void downloadModel(@PathVariable(name = "id") String id, HttpServletResponse response)
      throws Exception {
    modelerServiceI.downloadModel(id, response);
  }

  @Override
  public Object getOne(@PathVariable("id") String id) {
    Model model = repositoryService.createModelQuery().modelId(id).singleResult();
    return ToWeb.buildResult().setObjData(model);
  }

  @Override
  public Object getList(
      @RequestParam(value = "rowSize", defaultValue = "1000", required = false) Integer rowSize,
      @RequestParam(value = "page", defaultValue = "1", required = false) Integer page) {
    return modelerServiceI.getModelList(rowSize, page);
  }

  @Override
  public Object deleteOne(@PathVariable("id") String id) {
    repositoryService.deleteModel(id);
    return ToWeb.buildResult().refresh();
  }

  @Override
  public Object postOne(@RequestBody Model entity) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Object putOne(@PathVariable("id") String s, @RequestBody Model entity) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Object patchOne(@PathVariable("id") String s, @RequestBody Model entity) {
    throw new UnsupportedOperationException();
  }

}
