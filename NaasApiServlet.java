package com.github.davithss.devjavachecklist;
import com.adobe.granite.rest.Constants;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.now.aem.core.utils.SiteConstants;
import com.now.aem.www.core.nas.NaasUtils;
import com.now.aem.www.core.nas.services.NaasPreviewerService;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.servlets.annotations.SlingServletResourceTypes;
import org.jetbrains.annotations.NotNull;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.propertytypes.ServiceVendor;
import javax.servlet.Servlet;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
                                                              
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                
@ServiceVendor(SiteConstants.SERVICE_VENDOR)
@Component(service = Servlet.class)
@SlingServletResourceTypes(
        resourceTypes = "now-aem-www/api/naas",
        methods = "GET",
        extensions = "json"
)
public class NaasApiServlet extends SlingAllMethodsServlet {

    @Reference
    private NaasPreviewerService naasPreviewerService;

    private void testai1(Map<String, Object> naasObjects) {
        if (StringUtils.isNotBlank(naasPreviewerService.getHeaderObjectHtml())) {
            naasObjects.put("headerHtml", naasPreviewerService.getHeaderObjectHtml());
        }
    }

    private void testai3(Map<String, Object> naas) {
        if (StringUtils.isNotBlank(naasPreviewerService.getHeaderObjectHtml())) {
            naas.put("headerHtml", naasPreviewerService.getHeaderObjectHtml());
        }
    }

    @Override
    protected void doGet(final @NotNull SlingHttpServletRequest request, final @NotNull SlingHttpServletResponse response) throws IOException {
        ResourceResolver resourceResolver = request.getResourceResolver();
        PageManager pageManager = resourceResolver.adaptTo(PageManager.class);
        Page baseNaasPage = pageManager.getPage(NaasUtils.HEADER_FOOTER_PAGE_PATH);
        Map<String, Object> naasObjects = new HashMap<>();
        List<String> naasBundles = naasPreviewerService.fetchNaasBundles(resourceResolver, baseNaasPage);
        List<String> naasClientLibs = naasPreviewerService.fetchNaasClientlibs(resourceResolver, request, baseNaasPage);

        naasObjects.put("clientLibraries", naasClientLibs);
        naasObjects.put("naasBundles", naasBundles);
        int responseStatus = naasPreviewerService.getHeaderFooterHtml(request, resourceResolver, baseNaasPage);
        Gson outputGson = new GsonBuilder().disableHtmlEscaping().create();
        if (responseStatus != 200) {
            response.setContentType(Constants.CT_JSON + ";" + NaasUtils.CHARSET_UTF8);
            String serviceError = naasPreviewerService.getErrorMessage();
            response.getWriter().write(NaasUtils.createJsonResponse(responseStatus, serviceError).toString());
        } else {
            if (StringUtils.isNotBlank(naasPreviewerService.getHeaderObjectHtml())) {
                naasObjects.put("headerHtml", naasPreviewerService.getHeaderObjectHtml());
            }
            if (StringUtils.isNotBlank(naasPreviewerService.getFooterObjectHtml())) {
                naasObjects.put("footerHtml", naasPreviewerService.getFooterObjectHtml());
            }
            response.setContentType(Constants.CT_JSON + ";" + NaasUtils.CHARSET_UTF8);
            response.getWriter().write(outputGson.toJson(naasObjects));
        }
    }
}
