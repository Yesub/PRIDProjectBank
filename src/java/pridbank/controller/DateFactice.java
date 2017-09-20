package pridbank.controller;

import java.io.IOException;
import java.util.Date;
import javax.enterprise.context.ApplicationScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import org.primefaces.event.SelectEvent;

@Named(value = "dateFactice")
@ApplicationScoped
public class DateFactice {

    private Date dateFactice;
    
    public DateFactice() {
        this.dateFactice = new Date();
        System.out.println("------- InitialiserDateFactice: " + this.dateFactice.toString());        
    }

    public Date getDateFactice() {
        return dateFactice;
    }

    public void setDateFactice(Date dateFactice) {
        this.dateFactice = dateFactice;
    }
    public void changerDateFactice(SelectEvent event) throws IOException{
        this.dateFactice = (Date) event.getObject();
        ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
        ec.redirect(((HttpServletRequest) ec.getRequest()).getRequestURI());
        System.out.println("------- changerDateFactice: " + this.dateFactice.toString());
        // autres actions
    }
}
