package com.liang.activitidesign.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.liang.activitidesign.Model.DeploymentVO;
import com.liang.activitidesign.service.ModelerServiceI;
import com.liang.activitidesign.utils.ToWeb;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import lombok.extern.slf4j.Slf4j;
import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.Process;
import org.activiti.editor.constants.ModelDataJsonConstants;
import org.activiti.editor.language.json.converter.BpmnJsonConverter;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.Model;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Component
@Slf4j
public class ModelerServiceImpl implements ModelerServiceI {

  @Autowired
  private RepositoryService repositoryService;

  @Autowired
  private ObjectMapper objectMapper;

  @Override
  @Transactional
  public String newActivitiModel() {
    //初始化一个空模型
    Model model = repositoryService.newModel();

    //设置一些默认信息
    String name = "new-process";
    String description = "";
    int revision = 1;
    String key = "process";

    ObjectNode modelNode = objectMapper.createObjectNode();
    modelNode.put(ModelDataJsonConstants.MODEL_NAME, name);
    modelNode.put(ModelDataJsonConstants.MODEL_DESCRIPTION, description);
    modelNode.put(ModelDataJsonConstants.MODEL_REVISION, revision);

    model.setName(name);
    model.setKey(key);
    model.setMetaInfo(modelNode.toString());
    repositoryService.saveModel(model);
    String id = model.getId();
    //完善ModelEditorSource
    ObjectNode editorNode = objectMapper.createObjectNode();
    editorNode.put("id", "canvas");
    editorNode.put("resourceId", "canvas");
    ObjectNode stencilSetNode = objectMapper.createObjectNode();
    stencilSetNode.put("namespace", "http://b3mn.org/stencilset/bpmn2.0#");
    editorNode.put("stencilset", stencilSetNode);
    repositoryService.addModelEditorSource(id, editorNode.toString().getBytes(StandardCharsets.UTF_8));
    return id;
  }

  @Override
  public String uploadBpmn(MultipartFile file) throws Exception {
    InputStream inputStream = file.getInputStream();
    BpmnXMLConverter xmlConverter = new BpmnXMLConverter();
    XMLStreamReader xmlStreamReader;
    try {
      xmlStreamReader = XMLInputFactory.newInstance().createXMLStreamReader(inputStream);
    } catch (XMLStreamException e) {
      log.error("xml转换失败", e);
      e.printStackTrace();
      throw new Exception("xml转换失败");
    }
    BpmnModel bpmnModel = xmlConverter.convertToBpmnModel(xmlStreamReader);
    BpmnJsonConverter bpmnJsonConverter = new BpmnJsonConverter();
    ObjectNode modelNode = bpmnJsonConverter.convertToJson(bpmnModel);
    Model model = repositoryService.newModel();
    List<Process> processes = bpmnModel.getProcesses();
    String name = "newProcess";
    String key = "process";
    Integer version = 1;
    if (processes.size() > 0) {
      if (StringUtils.isNotBlank(processes.get(0).getName())) {
        name = processes.get(0).getName();
      }
      if (StringUtils.isNotBlank(processes.get(0).getId())) {
        key = processes.get(0).getId();
      }
    }
    model.setName(name);
    model.setKey(key);
    List<Model> modelList = repositoryService.createModelQuery().modelKey(key).orderByModelVersion()
        .desc().list();
    if (modelList.size() > 0) {
      version = modelList.get(0).getVersion() + 1;
    }
    model.setVersion(version);
    ObjectNode objectNode = objectMapper.createObjectNode();
    objectNode.put(ModelDataJsonConstants.MODEL_NAME, name);
    objectNode.put(ModelDataJsonConstants.MODEL_REVISION, String.valueOf(version));

    model.setMetaInfo(objectNode.toString());
    repositoryService.saveModel(model);
    repositoryService
        .addModelEditorSource(model.getId(), modelNode.toString().getBytes(StandardCharsets.UTF_8));

    return name;
  }

  @Override
  public void downloadModel(String id, HttpServletResponse response) throws Exception {
    //根据数据库id获取model
    Model model = repositoryService.getModel(id);
    JsonNode editorNode;
    try {
      //获取核心数据
      editorNode = new ObjectMapper().readTree(repositoryService.getModelEditorSource(id));
    } catch (IOException e) {
      log.error("流程model转换为JsonNode失败", e);
      throw new Exception("流程model转换为JsonNode失败");
    }
    //JsonNode转换为BpmnNode
    BpmnJsonConverter bpmnJsonConverter = new BpmnJsonConverter();
    BpmnModel bpmnModel = bpmnJsonConverter.convertToBpmnModel(editorNode);

    byte[] bpmnBytes = new BpmnXMLConverter().convertToXML(bpmnModel, "utf-8");
    String fileName = model.getName() + ".bpmn20.xml";

    response.reset();
    response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder
        .encode(fileName, "utf-8"));
    response.setCharacterEncoding("utf-8");
    response.setContentLength(bpmnBytes.length);
    response.setContentType("application/force-download;charset=utf-8");
    OutputStream outputStream = new BufferedOutputStream(response.getOutputStream());
    outputStream.write(bpmnBytes);
    outputStream.flush();
    outputStream.close();
  }

  @Override
  public void downBpmnModel(String processDefinitionId, HttpServletResponse response)
      throws IOException {
    //获取bpmnModel
    BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefinitionId);
    //bpmnModle转换为xml
    byte[] bpmnBytes = new BpmnXMLConverter().convertToXML(bpmnModel, "utf-8");
    //获取文件名
    String bpmnName = "newBpmnXMLDownload";
    List<Process> processList = bpmnModel.getProcesses();
    if (processList.size() > 0) {
      if (StringUtils.isNotBlank(processList.get(0).getName())) {
        bpmnName = processList.get(0).getName();
      }
    }
    String fileName = bpmnName + ".bpmn20.xml";

    //下载文件
    response.reset();
    response.setHeader("Content-Disposition",
        "attachment;filename=" + URLEncoder.encode(fileName, "utf-8"));
    response.setCharacterEncoding("utf-8");
    response.setContentLength(bpmnBytes.length);
    response.setContentType("application/force-download;charset=utf-8");
    OutputStream outputStream = new BufferedOutputStream(response.getOutputStream());
    outputStream.write(bpmnBytes);
    outputStream.flush();
    outputStream.close();
  }

  @Override
  public String deployModel(String id) throws Exception {
    //获取模型
    Model modelData = repositoryService.getModel(id);
    byte[] bytes = repositoryService.getModelEditorSource(modelData.getId());

    if (bytes == null) {
      throw new Exception("模型数据为空，请先设计流程并成功保存，再进行发布");
    }

    JsonNode modelNode = new ObjectMapper().readTree(bytes);

    BpmnModel model = new BpmnJsonConverter().convertToBpmnModel(modelNode);
    if (model.getProcesses().size() == 0) {
      throw new Exception("数据模型不符要求，请至少设计一条主线流程");
    }
    byte[] bpmnBytes = new BpmnXMLConverter().convertToXML(model);

    //发布流程
    String processName = modelData.getName() + ".bpmn20.xml";
    Deployment deployment = repositoryService.createDeployment()
        .name(modelData.getName())
        .addString(processName, new String(bpmnBytes, StandardCharsets.UTF_8))
        .deploy();
    modelData.setDeploymentId(deployment.getId());
    repositoryService.saveModel(modelData);

    return processName;
  }

  @Override
  public Object getModelList(Integer rowSize, Integer page) {
    List<Model> list = repositoryService.createModelQuery().listPage(rowSize * (page - 1)
        , rowSize);
    long count = repositoryService.createModelQuery().count();

    return ToWeb.buildResult().setRows(
        ToWeb.Rows.buildRows().setCurrent(page)
            .setTotalPages((int) (count / rowSize + 1))
            .setTotalRows(count)
            .setList(list)
            .setRowSize(rowSize)
    );
  }

  @Override
  public Object getDeploymentList(Integer rowSize, Integer page) {
    List<Deployment> deployments = repositoryService.createDeploymentQuery()
        .listPage(rowSize * (page - 1), rowSize);
    long count = repositoryService.createDeploymentQuery().count();
    List<DeploymentVO> list = new ArrayList<>();
    for (Deployment deployment : deployments) {
      list.add(new DeploymentVO(deployment));
    }

    return ToWeb.buildResult().setRows(
        ToWeb.Rows.buildRows()
            .setRowSize(rowSize)
            .setTotalPages((int) (count / rowSize + 1))
            .setTotalRows(count)
            .setList(list)
            .setCurrent(page)
    );
  }

  @Override
  @Transactional
  public String deleteModel(String id) {
    repositoryService.deleteModel(id);
    return "删除流程模型成功";
  }
}
