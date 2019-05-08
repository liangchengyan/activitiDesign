package com.liang.activitidesign.Model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.Date;
import lombok.Data;
import org.activiti.engine.repository.Deployment;
import org.activiti.rest.common.util.DateToStringSerializer;

/**
 * @Auther: chengyan.liang
 * @Despriction:
 * @Date:Created in 9:13 PM 2019/4/25
 * @Modify by:
 */
@Data
public class DeploymentVO {

  private String id;
  private String name;
  @JsonSerialize(using = DateToStringSerializer.class, as= Date.class)
  private Date deploymentTime;
  private String category;
  private String tenantId;

  public DeploymentVO(Deployment deployment) {
    this.id = deployment.getId();
    this.name = deployment.getName();
    this.deploymentTime = deployment.getDeploymentTime();
    this.category = deployment.getCategory();
    this.tenantId = deployment.getTenantId();
  }

}
