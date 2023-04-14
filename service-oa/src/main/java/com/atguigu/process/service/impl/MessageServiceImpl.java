package com.atguigu.process.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.atguigu.auth.service.SysUserService;
import com.atguigu.model.process.Process;
import com.atguigu.model.process.ProcessTemplate;
import com.atguigu.model.system.SysUser;
import com.atguigu.process.service.MessageService;
import com.atguigu.process.service.OaProcessService;
import com.atguigu.process.service.OaProcessTemplateService;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.template.WxMpTemplateData;
import me.chanjar.weixin.mp.bean.template.WxMpTemplateMessage;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Map;

@Service
public class MessageServiceImpl implements MessageService {

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private OaProcessService processService;

    @Autowired
    private OaProcessTemplateService processTemplateService;

    @Autowired
    private WxMpService wxMpService;

    @Override
    public void pushPendingMessage(Long processId, Long userId, String taskId) {

        SysUser sysUser = sysUserService.getById(userId);
        Process process = processService.getById(processId);
        ProcessTemplate processTemplate = processTemplateService.getById(process.getProcessTemplateId());
        SysUser submitSysUser = sysUserService.getById(process.getUserId());

        String openId = sysUser.getOpenId();
        if (StringUtils.isEmpty(openId)) {
            openId = "oCTMw5mq9FBEn9pDpCkJ3-ykCBJw";
        }

        WxMpTemplateMessage wxMpTemplateMessage = WxMpTemplateMessage.builder()
                .toUser(openId)
                .templateId("3_zwzhn14jhtNggS2YgBLTVouFEOxNd6XMPC8Nk6ig8")
                .url("http://oaatguigub.viphk.91tunnel.com/#/show/" + processId + "/" + taskId)
                .build();

        JSONObject jsonObject = JSON.parseObject(process.getFormValues());
        JSONObject formShowData = jsonObject.getJSONObject("formShowData");
        StringBuffer content = new StringBuffer();
        for (Map.Entry entry : formShowData.entrySet()) {
            content.append(entry.getKey()).append("：").append(entry.getValue()).append("\n ");
        }

        wxMpTemplateMessage.addData(new WxMpTemplateData("first",
                submitSysUser.getName()+"提交了"+processTemplate.getName()+"审批申请，请注意查看。","#272727"));
        wxMpTemplateMessage.addData(new WxMpTemplateData("keyword1", process.getProcessCode(), "#272727"));
        wxMpTemplateMessage.addData(new WxMpTemplateData("keyword2", new DateTime(process.getCreateTime()).toString("yyyy-MM-dd HH:mm:ss"), "#272727"));
        wxMpTemplateMessage.addData(new WxMpTemplateData("content", content.toString(), "#272727"));

        try {
            String msg = wxMpService.getTemplateMsgService().sendTemplateMsg(wxMpTemplateMessage);
            System.out.println(msg);
        } catch (WxErrorException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void pushProcessedMessage(Long processId, Long userId, Integer status) {

    }
}
