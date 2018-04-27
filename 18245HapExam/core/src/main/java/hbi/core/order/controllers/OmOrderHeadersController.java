package hbi.core.order.controllers;

import hbi.core.order.dto.OmOrderLines;
import hbi.core.order.mapper.OmOrderLinesMapper;
import org.springframework.stereotype.Controller;
import com.hand.hap.system.controllers.BaseController;
import com.hand.hap.core.IRequest;
import com.hand.hap.system.dto.ResponseData;
import hbi.core.order.dto.OmOrderHeaders;
import hbi.core.order.service.IOmOrderHeadersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import javax.servlet.http.HttpServletRequest;
import org.springframework.validation.BindingResult;
import java.util.List;

    @Controller
    public class OmOrderHeadersController extends BaseController{

    @Autowired
    private IOmOrderHeadersService service;
    @Autowired
    private OmOrderLinesMapper omOrderLinesMapper;


    @RequestMapping(value = "/hap/om/order/headers/query")
    @ResponseBody
    public ResponseData query(OmOrderHeaders dto, @RequestParam(defaultValue = DEFAULT_PAGE) int page,
        @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) int pageSize, HttpServletRequest request) {
        IRequest requestContext = createRequestContext(request);
        return new ResponseData(service.select(requestContext,dto,page,pageSize));
    }

    @RequestMapping(value = "/hap/om/order/headers/submit")
    @ResponseBody
    public ResponseData update(@RequestBody List<OmOrderHeaders> dto, BindingResult result, HttpServletRequest request){
        getValidator().validate(dto, result);
        if (result.hasErrors()) {
        ResponseData responseData = new ResponseData(false);
        responseData.setMessage(getErrorMessage(result, request));
        return responseData;
        }
        IRequest requestCtx = createRequestContext(request);
        return new ResponseData(service.batchUpdate(requestCtx, dto));
    }

        @RequestMapping(value = "/hap/om/order/headers/submitall")
        @ResponseBody
        public ResponseData updateAll(@RequestBody List<OmOrderHeaders> dto, BindingResult result, HttpServletRequest request){
            IRequest requestCtx = createRequestContext(request);
            for (OmOrderHeaders omOrderHeaders:dto){
                     if(omOrderHeaders.getHeaderId()==null){
                         service.insertSelective(requestCtx,omOrderHeaders);
                         if(omOrderHeaders.getOmOrderLiness()!=null){
                             processOmOrderLines(omOrderHeaders);
                         }
                     }
                     else{
                         service.updateByPrimaryKeySelective(requestCtx,omOrderHeaders);
                         if(omOrderHeaders.getOmOrderLiness()!=null){
                             processOmOrderLines(omOrderHeaders);
                         }
                     }
            }
            return new ResponseData(dto);
        }

    @RequestMapping(value = "/hap/om/order/headers/remove")
    @ResponseBody
    public ResponseData delete(HttpServletRequest request,@RequestBody List<OmOrderHeaders> dto){
        service.batchDelete(dto);
        return new ResponseData();
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    private void processOmOrderLines(OmOrderHeaders omOrderHeaders){
        for(OmOrderLines omOrderLines:omOrderHeaders.getOmOrderLiness()){
            if(omOrderLines.getLineId()==null){
                omOrderLines.setHeaderId(omOrderHeaders.getHeaderId());
                omOrderLinesMapper.insertSelective(omOrderLines);
            }
            else{
                omOrderLinesMapper.updateByPrimaryKeySelective(omOrderLines);
            }
        }
    }
    }