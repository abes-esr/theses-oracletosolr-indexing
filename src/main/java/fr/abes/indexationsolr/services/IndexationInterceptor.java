package fr.abes.indexationsolr.services;

import lombok.Getter;
import lombok.Setter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Array;

@Component
@Getter
@Setter
@ComponentScan(basePackages = "fr.abes.indexationsolr.services")
public class IndexationInterceptor extends HandlerInterceptorAdapter {

    private Logger logger = LogManager.getLogger(IndexationInterceptor.class);

    private String contexte;
    private String iddoc;
    private IndexationSolrSujet indexationSolrSujet;
   /* @Autowired
    private IndexationSolrSujet indexationSolrSujet;
    @Autowired
    private IndexationSolrStar indexationSolrStar;
    @Autowired
    private IndexationSolrPortail indexationSolrPortail;*/
   @Autowired
   ApplicationContext ctx;


    public IndexationInterceptor() {
    }

    public IndexationInterceptor(String contexte, String iddoc, IndexationSolrSujet indexationSolrSujet) {
        this.contexte = contexte;
        this.iddoc = iddoc;
        this.indexationSolrSujet = indexationSolrSujet;

    }

    @Override
    public boolean preHandle(HttpServletRequest requestServlet, HttpServletResponse responseServlet, Object handler) throws Exception
    {

        logger.info("MINIMAL: INTERCEPTOR PREHANDLE CALLED");
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception
    {
        logger.info("MINIMAL: INTERCEPTOR POSTHANDLE CALLED");
        logger.info(request.getMethod());
        logger.info(request.getParameter("iddoc"));
        this.iddoc = request.getParameter("iddoc");
        this.contexte = request.getParameter("contexte");
        this.indexationSolrSujet = new IndexationSolrSujet();
        this.indexationSolrSujet.setIddoc(Integer.parseInt(this.iddoc));
        this.indexationSolrSujet.indexation();

        //this.indexationSolrSujet = (IndexationSolrSujet) modelAndView.getModel().get("indexationSolrSujet");
        //modelAndView.addObject(indexationSolrSujet);

        logger.info(request.getParameter("contexte"));
        logger.info(request.toString());
        logger.info(request.getHeaderNames());
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception exception) throws Exception
    {
        System.out.println("MINIMAL: INTERCEPTOR AFTERCOMPLETION CALLED");
        logger.info("afterCompletion contexte : " + this.contexte);


        if(this.contexte.contains("sujets")) {
            logger.info("ici");
            //IndexationSolrSujet indexationSolrSujet = (IndexationSolrSujet) ctx.getBean("IndexationSolrSujet");
            logger.info("là");
            this.indexationSolrSujet.setIddoc(Integer.parseInt(this.iddoc));
            this.indexationSolrSujet.indexation();
        }
        else if(this.contexte.contains("star")) {
            IndexationSolrStar indexationSolrStar = (IndexationSolrStar) ctx.getBean("IndexationSolrStar");
            indexationSolrStar.setIddoc(Integer.parseInt(this.iddoc));
            indexationSolrStar.indexation();
        }
        else {
            IndexationSolrPortail indexationSolrPortail = (IndexationSolrPortail) ctx.getBean("IndexationSolrPortail");
            indexationSolrPortail.setIddoc(Integer.parseInt(this.iddoc));
            indexationSolrPortail.indexation();
        }
    }
}
