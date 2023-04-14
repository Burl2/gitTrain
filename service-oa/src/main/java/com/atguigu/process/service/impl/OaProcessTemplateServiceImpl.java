package com.atguigu.process.service.impl;


import com.atguigu.model.process.ProcessTemplate;
import com.atguigu.model.process.ProcessType;
import com.atguigu.process.mapper.OaProcessTemplateMapper;
import com.atguigu.process.service.OaProcessService;
import com.atguigu.process.service.OaProcessTemplateService;
import com.atguigu.process.service.OaProcessTypeService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 审批模板 服务实现类
 * </p>
 *
 * @author atguigu
 * @since 2023-03-13
 */
@Service
public class OaProcessTemplateServiceImpl extends ServiceImpl<OaProcessTemplateMapper, ProcessTemplate> implements OaProcessTemplateService {

    @Resource
    private OaProcessTemplateMapper processTemplateMapper;

    @Autowired
    private OaProcessTypeService processTypeService;

    @Autowired
    private OaProcessService processService;

    @Override
    public IPage<ProcessTemplate> selectPage(Page<ProcessTemplate> pageParam) {

        Page<ProcessTemplate> page = processTemplateMapper
                .selectPage(pageParam, new LambdaQueryWrapper<ProcessTemplate>().orderByDesc(ProcessTemplate::getId));

        List<ProcessTemplate> processTemplateList = page.getRecords();

//        List<Long> processTypeIdList = processTemplateList
//                .stream().map(ProcessTemplate::getProcessTypeId).collect(Collectors.toList());

        for (ProcessTemplate processTemplate : processTemplateList) {
            ProcessType processType = processTypeService
                    .getOne(new LambdaQueryWrapper<ProcessType>().eq(ProcessType::getId, processTemplate.getProcessTypeId()));

            if (processType == null) continue;

            processTemplate.setProcessTypeName(processType.getName());
        }
        return page;
    }

    @Override
    public void publish(Long id) {
        ProcessTemplate processTemplate = this.getById(id);
        processTemplate.setStatus(1);
        processTemplateMapper.updateById(processTemplate);

        //TODO 部署流程定义，后续完善
        if (!StringUtils.isEmpty(processTemplate.getProcessDefinitionPath())) {
            processService.deployByZip(processTemplate.getProcessDefinitionPath());
        }
    }
}
