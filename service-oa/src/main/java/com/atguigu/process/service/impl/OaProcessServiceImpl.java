package com.atguigu.process.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.atguigu.auth.service.SysUserService;
import com.atguigu.model.process.Process;
import com.atguigu.model.process.ProcessRecord;
import com.atguigu.model.process.ProcessTemplate;
import com.atguigu.model.system.SysUser;
import com.atguigu.process.mapper.OaProcessMapper;
import com.atguigu.process.service.MessageService;
import com.atguigu.process.service.OaProcessRecordService;
import com.atguigu.process.service.OaProcessService;
import com.atguigu.process.service.OaProcessTemplateService;
import com.atguigu.security.custom.LoginUserInfoHelper;
import com.atguigu.vo.process.ApprovalVo;
import com.atguigu.vo.process.ProcessFormVo;
import com.atguigu.vo.process.ProcessQueryVo;
import com.atguigu.vo.process.ProcessVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.activiti.bpmn.model.*;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.history.HistoricTaskInstanceQuery;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipInputStream;

/**
 * <p>
 * 审批类型 服务实现类
 * </p>
 *
 * @author atguigu
 * @since 2023-03-13
 */
@Service
public class OaProcessServiceImpl extends ServiceImpl<OaProcessMapper, Process> implements OaProcessService {

    @Resource
    private OaProcessMapper processMapper;

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private OaProcessTemplateService processTemplateService;

    @Autowired
    private OaProcessRecordService processRecordService;

    @Autowired
    private MessageService messageService;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private HistoryService historyService;


    @Override
    public IPage<ProcessVo> selectPage(Page<ProcessVo> processPage, ProcessQueryVo processQueryVo) {
        IPage<ProcessVo> pageModel= processMapper.selectPage(processPage,processQueryVo);
        return pageModel;
    }

    @Override
    public void deployByZip(String deployPath) {
        Class<? extends OaProcessServiceImpl> aClass = this.getClass();
        ClassLoader classLoader = this.getClass().getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream(deployPath);
        ZipInputStream zipInputStream = new ZipInputStream(inputStream);

        Deployment deployment = repositoryService.createDeployment().addZipInputStream(zipInputStream).deploy();
        System.out.println(deployment.getId());
        System.out.println(deployment.getName());
    }

    @Override
    public void startUp(ProcessFormVo processFormVo) {

        SysUser sysUser = sysUserService.getById(LoginUserInfoHelper.getUserId());

        ProcessTemplate processTemplate = processTemplateService.getById(processFormVo.getProcessTemplateId());

        Process process = new Process();
        BeanUtils.copyProperties(processFormVo,process);
        String workNo = System.currentTimeMillis() + "";
        process.setProcessCode(workNo);
        process.setUserId(LoginUserInfoHelper.getUserId());
        process.setFormValues(processFormVo.getFormValues());
        process.setTitle(sysUser.getName() + "发起" + processTemplate.getName() + "申请");
        process.setStatus(1);
        processMapper.insert(process);


        String processDefinitionKey = processTemplate.getProcessDefinitionKey();
        String businessKey = String.valueOf(process.getId());
        String formValues = processFormVo.getFormValues();
        JSONObject jsonObject = JSON.parseObject(formValues);
        JSONObject formData = jsonObject.getJSONObject("formData");
        HashMap<String, Object> map = new HashMap<>();
        for (Map.Entry<String,Object> entry : formData.entrySet()) {
            map.put(entry.getKey(),entry.getValue());
        }
        HashMap<String, Object> variables = new HashMap<>();
        variables.put("data",map);

        ProcessInstance processInstance = runtimeService
                .startProcessInstanceByKey(processDefinitionKey, businessKey, variables);

        String processInstanceId = processInstance.getId();
        process.setProcessInstanceId(processInstanceId);

        List<Task> taskList = this.getCurrentTaskList(processInstanceId);
        if (!CollectionUtils.isEmpty(taskList)) {
            ArrayList<String> assigneeList = new ArrayList<>();
            for (Task task : taskList) {
                SysUser pendingSysUser = sysUserService.getByUsername(task.getAssignee());
                assigneeList.add(pendingSysUser.getName());

                //推送消息给下一个审批人，后续完善

                messageService.pushPendingMessage(process.getId(),pendingSysUser.getId(),task.getId());
            }
            process.setDescription("等待" + StringUtils.join(assigneeList.toArray(), ",") + "审批");
        }

        processMapper.updateById(process);
        processRecordService.record(process.getId(),1,"发起申请");
    }

    @Override
    public IPage<ProcessVo> findPending(Page<Process> processPage) {

        TaskQuery query = taskService.createTaskQuery()
                .taskAssignee(LoginUserInfoHelper.getUsername()).orderByTaskCreateTime().desc();
        long totalCount = query.count();
        List<Task> taskList = query
                .listPage((int) ((processPage.getCurrent() - 1) * processPage.getSize()), (int) processPage.getSize());
        ArrayList<ProcessVo> processVoList = new ArrayList<>();
        for (Task task : taskList) {
            String processInstanceId = task.getProcessInstanceId();
            ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
                    .processInstanceId(processInstanceId).singleResult();
            if (processInstance == null) {
                continue;
            }
            String businessKey = processInstance.getBusinessKey();
            if (businessKey == null) {
                continue;
            }
            Process process = this.getById(Long.parseLong(businessKey));
            ProcessVo processVo = new ProcessVo();
            BeanUtils.copyProperties(process, processVo);
            processVo.setTaskId(task.getId());
            processVoList.add(processVo);
        }
        IPage<ProcessVo> processVoPage = new Page<>(processPage.getCurrent(), processPage.getSize(), totalCount);
        processVoPage.setRecords(processVoList);
        return processVoPage;
    }

    @Override
    public Map<String, Object> show(Long id) {

        Process process = this.getById(id);
        List<ProcessRecord> processRecordList = processRecordService
                .list(new LambdaQueryWrapper<ProcessRecord>().eq(ProcessRecord::getProcessId, id));
        ProcessTemplate processTemplate = processTemplateService.getById(process.getProcessTemplateId());
        HashMap<String, Object> map = new HashMap<>();
        map.put("process", process);
        map.put("processRecordList", processRecordList);
        map.put("processTemplate", processTemplate);

        boolean isApprove = false;
        List<Task> currentTaskList = this.getCurrentTaskList(process.getProcessInstanceId());
        if (!CollectionUtils.isEmpty(currentTaskList)) {
            for (Task task : currentTaskList) {
                if (task.getAssignee().equals(LoginUserInfoHelper.getUsername())) {
                    isApprove = true;
                }
            }
        }
        map.put("isApprove", isApprove);
        for (Map.Entry entry : map.entrySet()) {
            System.out.println("key+   "+entry.getKey()+ ", Value" + entry.getValue());
        }
        return map;
    }

    @Override
    public void approve(ApprovalVo approvalVo) {
        Map<String, Object> variables = taskService.getVariables(approvalVo.getTaskId());
        for (Map.Entry<String,Object> entry : variables.entrySet()) {
            System.out.println("key = " + entry.getKey() + ", Value = " + entry.getValue());
        }

        if (approvalVo.getStatus() == 1) {
            HashMap<String, Object> variable = new HashMap<>();
            taskService.complete(approvalVo.getTaskId(),variable);
        } else {
            this.endTask(approvalVo.getTaskId());
        }
        String description = approvalVo.getStatus().intValue() == 1 ? "已通过" : "已驳回";
        processRecordService.record(approvalVo.getProcessId(),approvalVo.getStatus(),description);

        Process process = this.getById(approvalVo.getProcessId());
        List<Task> taskList = this.getCurrentTaskList(process.getProcessInstanceId());
        if (!CollectionUtils.isEmpty(taskList)) {
            ArrayList<String> assigneeList = new ArrayList<>();
            for (Task task : taskList) {
                assigneeList.add(sysUserService.getByUsername(task.getAssignee()).getName());

                //推送消息给下一个审批人
            }
            process.setDescription("等待" + StringUtils.join(assigneeList.toArray(), ",") + "审批");
            process.setStatus(1);
        } else {
            if(approvalVo.getStatus().intValue() == 1) {
                process.setDescription("审批完成（同意）");
                process.setStatus(2);
            } else {
                process.setDescription("审批完成（拒绝）");
                process.setStatus(-1);
            }
        }
        this.updateById(process);
    }

    @Override
    public IPage<ProcessVo> findProcessed(Page<Process> pageParam) {
        HistoricTaskInstanceQuery query = historyService
                .createHistoricTaskInstanceQuery().taskAssignee(LoginUserInfoHelper.getUsername())
                .finished().orderByTaskCreateTime().desc();
        List<HistoricTaskInstance> historicTaskInstances = query
                .listPage((int) ((pageParam.getCurrent() - 1) * pageParam.getSize()), (int) pageParam.getSize());
        long totalCount = query.count();

        ArrayList<ProcessVo> processVoList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(historicTaskInstances)) {
            for (HistoricTaskInstance taskInstance : historicTaskInstances) {
                String processInstanceId = taskInstance.getProcessInstanceId();
                Process process = this.
                        getOne(new LambdaQueryWrapper<Process>().eq(Process::getProcessInstanceId, processInstanceId));
                ProcessVo processVo = new ProcessVo();
                BeanUtils.copyProperties(process,processVo);
                processVo.setTaskId("0");
                processVoList.add(processVo);
            }
        }
        IPage<ProcessVo> processVoPage = new Page<>(pageParam.getCurrent(), pageParam.getSize(), totalCount);
        processVoPage.setRecords(processVoList);
        return processVoPage;
    }

    @Override
    public IPage<ProcessVo> findStarted(Page<ProcessVo> pageParam) {

        ProcessQueryVo processQueryVo = new ProcessQueryVo();
        processQueryVo.setUserId(LoginUserInfoHelper.getUserId());
        IPage<ProcessVo> pageModel = baseMapper.selectPage(pageParam, processQueryVo);
        return pageModel;
    }

    private void endTask(String taskId) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        BpmnModel bpmnModel = repositoryService.getBpmnModel(task.getProcessDefinitionId());
        List<EndEvent> endEventList = bpmnModel.getMainProcess().findFlowElementsOfType(EndEvent.class);
        if (CollectionUtils.isEmpty(endEventList)) return;
        FlowNode endFlowNode = (FlowNode)endEventList.get(0);
        FlowNode currentFlowElement = (FlowNode)bpmnModel.getMainProcess().getFlowElement(task.getProcessDefinitionId());
        //  临时保存当前活动的原始方向
        List originalSequenceFlowList = new ArrayList<>();
        originalSequenceFlowList.addAll(currentFlowElement.getOutgoingFlows());

        currentFlowElement.getOutgoingFlows().clear();

        SequenceFlow sequenceFlow = new SequenceFlow();
        sequenceFlow.setId("newFlow");
        sequenceFlow.setSourceFlowElement(currentFlowElement);
        sequenceFlow.setTargetFlowElement(endFlowNode);

        ArrayList newSequenceFlowList = new ArrayList<>();
        newSequenceFlowList.add(sequenceFlow);
        currentFlowElement.setOutgoingFlows(newSequenceFlowList);

        taskService.complete(taskId);
    }

    private List<Task> getCurrentTaskList(String processInstanceId) {
        return taskService.createTaskQuery().processInstanceId(processInstanceId).list();
    }
}
