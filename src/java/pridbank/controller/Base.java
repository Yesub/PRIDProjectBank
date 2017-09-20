package pridbank.controller;

import java.io.Serializable;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import pridbank.facade.ModelFacade;

public class Base implements Serializable {

    private static final long serialVersionUID = 1L;
    
    @EJB
    protected ModelFacade model;
    
    protected void addMessage(String clientId, String message) {
        FacesContext.getCurrentInstance().addMessage(clientId, new FacesMessage(message));
    }
    
    public ModelFacade getModel() {
        return model;
    }
    
}