package pridbank.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import org.primefaces.context.RequestContext;
import org.primefaces.event.FlowEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.UnselectEvent;
import org.primefaces.model.DualListModel;
import pridbank.model.entity.Compte;
import pridbank.model.entity.Utilisateur;
import pridbank.model.entity.Virement;

@Named
@ViewScoped
public class GestionCompteBacking extends CompteBacking {
    
    private DualListModel<Utilisateur> listeDouble;
    private Compte compteOriginal;
    private HashMap<Virement, List<String>> virementsRefuses;
    private List<Compte> listeComptes;
    private boolean continuerApresCreation = false;
    
    @PostConstruct
    public void onload() {
        String param = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("idCompte");
        if(param != null) {
            setCompteSelectionne(model.trouverCompte(Integer.valueOf(param)));
        }
    }
    
    public Compte getCompteOriginal() {
        return compteOriginal;
    }

    public void setCompteOriginal(Compte compteOriginal) {
        this.compteOriginal = compteOriginal;
    }
    
    public void appelCreationNouveauCompte(){
        setCompteSelectionne(null);
        setType(null);
        setSolde(null);
        setEstBloque(null);
        listeDouble = null;
    }
    
    public DualListModel<Utilisateur> getListeDouble() {
        if(listeDouble == null) {
            List<Utilisateur> clientsSource;
            List<Utilisateur> clientsCible;
            if(getCompteSelectionne() == null) {
                clientsSource = model.getClients();
                clientsCible = new ArrayList<>();
            } else {
                clientsSource = model.getNonClientsCompte(getCompteSelectionne());
                clientsCible = model.getClientsCompte(getCompteSelectionne());
            }
            listeDouble = new DualListModel<>(clientsSource, clientsCible);
        }
        return listeDouble;
    }

    public void setListeDouble(DualListModel<Utilisateur> listeDouble) {
        this.listeDouble = listeDouble;
    }
    
    public void verifierSoldeCreation() {
        model.verifierSoldeCreation(getSolde());
    }
    
    public void creerCompte() {
        setCompteSelectionne(model.creerCompte(getType(), getSolde(), getEstBloque(), listeDouble.getTarget()));
        if(getCompteSelectionne() != null) {
            setType(null);
            setSolde(null);
            setEstBloque(null);
            listeComptes.add(getCompteSelectionne());
            RequestContext context = RequestContext.getCurrentInstance();
            if(continuerApresCreation) {
                setCompteSelectionne(null);
                listeDouble = null;
                context.execute("PF('wiz').loadStep(PF('wiz').cfg.steps[0], true);");
            } else {
                setCompteSelectionne(null);
                listeDouble = null;
                context.execute("PF('modaleCreerCompte').hide();");
                context.execute("PF('modaleGererCompte').hide();");
            }
        }
    }
    
    public void redirigerClient(int idClient) throws IOException {
        FacesContext.getCurrentInstance().getExternalContext().redirect("/pridbank_groupe12/faces/index.xhtml/faces/protected/gestionnaire/listerUtilisateurs.xhtml?idClient="+idClient);
    }
    
    public void editerCompte() {
        listeDouble = null;
        setCompteSelectionne(model.trouverCompte(getCompteSelectionne().getNumero()));
        compteOriginal = new Compte(getCompteSelectionne());
    }
    
    public void bloquerCompte(Compte compte) {
        model.bloquerCompte(compte);
        this.rafraichirListeComptes();
    }
    
    public void supprimerCompte(int numero) {
        model.supprimerCompte(numero);
        setCompteSelectionne(null);
        this.rafraichirListeComptes();
    }
    
    public void modifierCompte() {
        if(!getCompteSelectionne().getType().equals(compteOriginal.getType()) || getCompteSelectionne().getEstBloque() != compteOriginal.getEstBloque()) {
            Compte resultat = model.modifierCompte(compteOriginal, getCompteSelectionne().getType(), getCompteSelectionne().getEstBloque());
            if(resultat != null) {
                setCompteSelectionne(resultat);
                compteOriginal = new Compte(getCompteSelectionne());
            }
        }
    }
    
    public void rendreProprietaire() {
        if(!comparaisonContenuListes(compteOriginal.getListeProprietaires(), listeDouble.getTarget())) {
            Compte resultat = model.lierClientsCompte(compteOriginal, listeDouble.getTarget());
            if(resultat != null) {
                setCompteSelectionne(resultat);
                compteOriginal = new Compte(getCompteSelectionne());
            }
        }
    }
    
    public HashMap<Virement, List<String>> getVirementsRefuses() {
        return virementsRefuses;
    }

    public void setVirementsRefuses(HashMap<Virement, List<String>> virementsRefuses) {
        this.virementsRefuses = virementsRefuses;
    }
    
    public void executerTousVirements() throws IOException {
        virementsRefuses = model.executerTousVirements();
        if(!virementsRefuses.isEmpty()) {
            RequestContext context = RequestContext.getCurrentInstance();
            context.execute("PF('modaleVirementsRefuses').show();");
        }
    }
    
    public List<Virement> getTrouverTousVirementsAExecuter(){
        return model.trouverTousVirementsAExecuter();
    }
    public boolean pasDeVirementsAExecuter(){
        return model.trouverTousVirementsAExecuter().isEmpty();
    }
    
    public String onFlowProcess(FlowEvent event) {
        return event.getNewStep();
    }
    
    @Override
    public List<Compte> getListeComptes() {
        if (listeComptes == null){
            setListeComptes();
        }
        return listeComptes;
    }

    public void setListeComptes() {
        this.listeComptes = model.getListeComptes();
    }
    
    public void rafraichirListeComptes() {
        setListeComptes();
    }
    
    public boolean getContinuerApresCreation() {
        return continuerApresCreation;
    }

    public void setContinuerApresCreation(boolean continuerApresCreation) {
        this.continuerApresCreation = continuerApresCreation;
    }
    
    public void onRowSelectCompte(SelectEvent event) {
        this.setCompteSelectionne((Compte) event.getObject());
    }
    
    public void onRowUnselectUtilisateur(UnselectEvent event) {
        this.setCompteSelectionne(null);
    }
    
    public void onRowDblclickProprietaireCompte(SelectEvent event) throws IOException {
        redirigerClient(((Utilisateur) event.getObject()).getIdClient());
    }
}
