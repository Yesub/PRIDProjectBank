package pridbank.controller;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.TransferEvent;
import org.primefaces.event.UnselectEvent;
import pridbank.model.entity.Utilisateur;

@Named
@RequestScoped
public class BaseBacking extends Base {

    private static final long serialVersionUID = 1L;
    protected Utilisateur utilisateurCourant;

    @Inject
    protected LogInBacking backing;

    @PostConstruct
    public void init() {
        utilisateurCourant = backing.getUtilisateurCourant();
    }
    
    public Utilisateur getUtilisateurCourant() {
        return utilisateurCourant;
    }
    
    public boolean comparaisonContenuListes(List<?> l1, List<?> l2) {
        ArrayList<?> cp = new ArrayList<>(l1);
        for (Object o : l2) {
            if (!cp.remove(o)) {
                return false;
            }
        }
        return cp.isEmpty();
    }
    
    public void onTransfer(TransferEvent event) {
        StringBuilder builder = new StringBuilder();
        for(Object item : event.getItems()) {
            builder.append(item).append("<br />");
        }
         
//        FacesMessage msg = new FacesMessage();
//        msg.setSeverity(FacesMessage.SEVERITY_INFO);
//        msg.setSummary("Items Transferred");
//        msg.setDetail(builder.toString());
//         
//        FacesContext.getCurrentInstance().addMessage(null, msg);
    } 
 
    public void onSelect(SelectEvent event) {
//        FacesContext context = FacesContext.getCurrentInstance();
//        context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Item Selected", event.getObject().toString()));
    }
     
    public void onUnselect(UnselectEvent event) {
//        FacesContext context = FacesContext.getCurrentInstance();
//        context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Item Unselected", event.getObject().toString()));
    }
     
    public void onReorder() {
//        FacesContext context = FacesContext.getCurrentInstance();
//        context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "List Reordered", null));
    } 
// BaseBacking    
    public Utilisateur refreshUtilisateur(Utilisateur utilisateur){
        return model.trouverUtilisateur(utilisateur.getIdClient());
    }
}