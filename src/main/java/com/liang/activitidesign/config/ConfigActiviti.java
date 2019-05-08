package com.liang.activitidesign.config;

import javax.sql.DataSource;
import org.activiti.engine.DynamicBpmnService;
import org.activiti.engine.HistoryService;
import org.activiti.engine.ManagementService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.spring.ProcessEngineFactoryBean;
import org.activiti.spring.SpringProcessEngineConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class ConfigActiviti {

  /**
   * 流程配置，与spring整合采用SpringProcessEngineConfiguration这个实现
   */
  @Bean
  public ProcessEngineConfiguration processEngineConfiguration(DataSource dataSource, PlatformTransactionManager transactionManager){
    SpringProcessEngineConfiguration processEngineConfiguration = new SpringProcessEngineConfiguration();
    processEngineConfiguration.setDataSource(dataSource);
    processEngineConfiguration.setDatabaseSchemaUpdate("true");
    processEngineConfiguration.setDatabaseType("h2");

    processEngineConfiguration.setTransactionManager(transactionManager);


    return processEngineConfiguration;
  }

  /**
   * 流程引擎，与spring整合使用factoryBean
   */
  @Bean
  public ProcessEngineFactoryBean processEngine(ProcessEngineConfiguration processEngineConfiguration){
    ProcessEngineFactoryBean processEngineFactoryBean = new ProcessEngineFactoryBean();
    processEngineFactoryBean.setProcessEngineConfiguration((ProcessEngineConfigurationImpl) processEngineConfiguration);
    return processEngineFactoryBean;
  }

  @Bean
  public RepositoryService repositoryService(ProcessEngine processEngine){
    return processEngine.getRepositoryService();
  }

  @Bean
  public RuntimeService runtimeService(ProcessEngine processEngine){
    return processEngine.getRuntimeService();
  }

  @Bean
  public TaskService taskService(ProcessEngine processEngine){
    return processEngine.getTaskService();
  }

  @Bean
  public HistoryService historyService(ProcessEngine processEngine){
    return processEngine.getHistoryService();
  }


  @Bean
  public ManagementService managementService(ProcessEngine processEngine){
    return processEngine.getManagementService();
  }

  @Bean
  public DynamicBpmnService dynamicBpmnService(ProcessEngine processEngine){
    return processEngine.getDynamicBpmnService();
  }
}
