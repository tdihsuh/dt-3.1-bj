package com.hansight.kunlun.web.base.jcaptcha.filter;

import java.awt.image.BufferedImage;   
import java.io.IOException;   
  
import javax.imageio.ImageIO;   
import javax.servlet.Filter;   
import javax.servlet.FilterChain;   
import javax.servlet.FilterConfig;   
import javax.servlet.ServletException;   
import javax.servlet.ServletOutputStream;   
import javax.servlet.ServletRequest;   
import javax.servlet.ServletResponse;   
import javax.servlet.http.HttpServletRequest;   
import javax.servlet.http.HttpServletResponse;   
  
import org.apache.commons.lang3.StringUtils;   
import org.springframework.context.ApplicationContext;   
import org.springframework.web.context.support.WebApplicationContextUtils;   
  
import com.octo.captcha.service.CaptchaService;   
import com.octo.captcha.service.CaptchaServiceException;  

public class JcaptchaFilter implements Filter{
	
	//web.xml涓殑鍙傛暟鍚嶅畾涔�  
    public static final String PARAM_CAPTCHA_PARAMTER_NAME = "captchaParamterName";   
    public static final String PARAM_CAPTCHA_SERVICE_ID = "captchaServiceId";   
    public static final String PARAM_FILTER_PROCESSES_URL = "filterProcessesUrl";   
    public static final String PARAM_FAILURE_URL = "failureUrl";   
    public static final String PARAM_AUTO_PASS_VALUE = "autoPassValue";   
  
    //榛樿鍊煎畾涔�  
    public static final String DEFAULT_FILTER_PROCESSES_URL = "/j_spring_security_check";   
    public static final String DEFAULT_CAPTCHA_SERVICE_ID = "captchaService";   
    public static final String DEFAULT_CAPTCHA_PARAMTER_NAME = "j_captcha";   
     
       
    private String failureUrl;   
    private String filterProcessesUrl = DEFAULT_FILTER_PROCESSES_URL;   
    private String captchaServiceId = DEFAULT_CAPTCHA_SERVICE_ID;   
    private String captchaParamterName = DEFAULT_CAPTCHA_PARAMTER_NAME;   
    private String autoPassValue;   
  
    private CaptchaService captchaService;   
       
    /**  
     * Filter鍥炶皟鍒濆鍖栧嚱鏁�  
     */  
    public void init(FilterConfig filterConfig) throws ServletException {   
        // TODO Auto-generated method stub   
        initParameters(filterConfig);   
        initCaptchaService(filterConfig);   
  
    }   
  
    public void doFilter(ServletRequest theRequest, ServletResponse theResponse,   
            FilterChain chain) throws IOException, ServletException {   
        HttpServletRequest request = (HttpServletRequest) theRequest;   
        HttpServletResponse response = (HttpServletResponse) theResponse;   
        String servletPath = request.getServletPath();
        //绗﹀悎filterProcessesUrl涓洪獙璇佸鐞嗚姹�鍏朵綑涓虹敓鎴愰獙璇佸浘鐗囪姹�   
        if (StringUtils.startsWith(servletPath, filterProcessesUrl)) {   
            boolean validated = validateCaptchaChallenge(request);   
            if (validated) {   
                chain.doFilter(request, response);   
            } else {   
                redirectFailureUrl(request, response);   
            }   
        } else {   
            genernateCaptchaImage(request, response);   
        }   
    }   
  
    /**  
     * Filter鍥炶皟閫�嚭鍑芥暟.  
     */  
    public void destroy() {   
        // TODO Auto-generated method stub   
  
    }   
       
    /**  
     * 鍒濆鍖杦eb.xml涓畾涔夌殑filter init-param.  
     */  
    protected void initParameters(final FilterConfig fConfig) {   
        if (StringUtils.isBlank(fConfig.getInitParameter(PARAM_FAILURE_URL))) {   
            throw new IllegalArgumentException("CaptchaFilter缂哄皯failureUrl鍙傛暟");   
        }   
  
        failureUrl = fConfig.getInitParameter(PARAM_FAILURE_URL);    
  
        if (StringUtils.isNotBlank(fConfig.getInitParameter(PARAM_FILTER_PROCESSES_URL))) {   
            filterProcessesUrl = fConfig.getInitParameter(PARAM_FILTER_PROCESSES_URL);   
        }   
  
        if (StringUtils.isNotBlank(fConfig.getInitParameter(PARAM_CAPTCHA_SERVICE_ID))) {   
            captchaServiceId = fConfig.getInitParameter(PARAM_CAPTCHA_SERVICE_ID);   
        }   
  
        if (StringUtils.isNotBlank(fConfig.getInitParameter(PARAM_CAPTCHA_PARAMTER_NAME))) {   
            captchaParamterName = fConfig.getInitParameter(PARAM_CAPTCHA_PARAMTER_NAME);   
        }   
  
        if (StringUtils.isNotBlank(fConfig.getInitParameter(PARAM_AUTO_PASS_VALUE))) {   
            autoPassValue = fConfig.getInitParameter(PARAM_AUTO_PASS_VALUE);   
        }   
    }   
       
    /**  
     * 浠嶢pplicatonContext鑾峰彇CaptchaService瀹炰緥.  
     */  
    protected void initCaptchaService(final FilterConfig fConfig) {   
        ApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(fConfig.getServletContext());   
        captchaService = (CaptchaService) context.getBean(captchaServiceId);   
    }   
       
    /**  
     * 鐢熸垚楠岃瘉鐮佸浘鐗�  
     */  
    protected void genernateCaptchaImage(final HttpServletRequest request, final HttpServletResponse response)   
            throws IOException {   
  
        setDisableCacheHeader(response);   
        response.setContentType("image/jpeg");   
  
        ServletOutputStream out = response.getOutputStream();   
        try {   
            String captchaId = request.getSession(true).getId();   
            BufferedImage challenge = (BufferedImage) captchaService.getChallengeForID(captchaId, request.getLocale());   
            ImageIO.write(challenge, "jpg", out);   
            out.flush();   
        } catch (CaptchaServiceException e) {   
        } finally {   
            out.close();   
        }   
    }   
       
    /**  
     * 楠岃瘉楠岃瘉鐮�  
     */  
    protected boolean validateCaptchaChallenge(final HttpServletRequest request) {   
        try {   
            String captchaID = request.getSession().getId();
            String challengeResponse = request.getParameter(captchaParamterName);   
            //鑷姩閫氳繃鍊煎瓨鍦ㄦ椂,妫�獙杈撳叆鍊兼槸鍚︾瓑浜庤嚜鍔ㄩ�杩囧�   
            if (StringUtils.isNotBlank(autoPassValue) && autoPassValue.equals(challengeResponse)) {   
                return true;   
            }   
            return captchaService.validateResponseForID(captchaID, challengeResponse);   
        } catch (CaptchaServiceException e) {   
            return false;   
        }   
    }   
    /**  
     * 璺宠浆鍒板け璐ラ〉闈�  
     *   
     * 鍙湪瀛愮被杩涜鎵╁睍, 姣斿鍦╯ession涓斁鍏pringSecurity鐨凟xception.  
     */  
    protected void redirectFailureUrl(final HttpServletRequest request, final HttpServletResponse response)   
            throws IOException {    
        response.sendRedirect(request.getContextPath() + failureUrl);   
    }   
       
    /**  
     * 璁剧疆绂佹瀹㈡埛绔紦瀛樼殑Header.  
     */  
    public static void setDisableCacheHeader(HttpServletResponse response) {   
        //Http 1.0 header   
        response.setDateHeader("Expires", 1L);   
        response.addHeader("Pragma", "no-cache");   
        //Http 1.1 header   
        response.setHeader("Cache-Control", "no-cache, no-store, max-age=0");   
    }   

}
