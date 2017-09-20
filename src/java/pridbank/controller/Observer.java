package pridbank.controller;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.event.Observes;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Named;

@Named
@RequestScoped
public class Observer extends Base {
    
    protected void myEventObserver(@Observes FacesMessage message) {
        FacesContext.getCurrentInstance().addMessage(null, message);
    }

}
