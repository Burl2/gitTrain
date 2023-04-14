package com.atguigu.process.service.impl;


import com.atguigu.model.process.ProcessTemplate;
import com.atguigu.model.process.ProcessType;
import com.atguigu.process.mapper.OaProcessTypeMapper;
import com.atguigu.process.service.OaProcessTemplateService;
import com.atguigu.process.service.OaProcessTypeService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 审批类型 服务实现类
 * </p>
 *
 * @author atguigu
 * @since 2023-03-13
 */
@Service
public class OaProcessTypeServiceImpl extends ServiceImpl<OaProcessTypeMapper, ProcessType> implements OaProcessTypeService {



    @Autowired
    private OaProcessTemplateService processTemplateService;

    @Override
    public List<ProcessType> findProcessType() {

        List<ProcessType> processTypeList = this.list();

        for (ProcessType processType: processTypeList) {
            List<ProcessTemplate> processTemplateList = processTemplateService
                    .list(new LambdaQueryWrapper<ProcessTemplate>()
                            .eq(ProcessTemplate::getProcessTypeId, processType.getId()));

            processType.setProcessTemplateList(processTemplateList);
        }
        return processTypeList;
    }
}
