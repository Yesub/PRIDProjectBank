package pridbank.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
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

@Named
@ViewScoped
public class GestionUtilisateurBacking extends UtilisateurBacking {
    
    private boolean lierCompte = true;
    private DualListModel<Compte> listeDouble;
    private String modifierMotDePasse, confirmationMotDePasse;
    private List<Utilisateur> listeUtilisateurs;
    private Utilisateur utilisateurOriginal;
    private boolean continuerApresCreation = false;
    
    @PostConstruct
    public void onload() {
        String param = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("idClient");
        if(param != null) {
            setUtilisateurSelectionne(model.trouverUtilisateur(Integer.valueOf(param)));
        }
    }
    
    public void appelCreationNouvelUtilisateur() {
        setUtilisateurSelectionne(null);
        setNomUtilisateur(null);
        setMotDePasse(null);
        confirmationMotDePasse = null;
        setDateDeNaissance(null);
        setEstUnGestionnaire(null);
        listeDouble = null;
    }
    
    public void creerUtilisateur() {
        setUtilisateurSelectionne(model.creerUtilisateur(getNomUtilisateur(), getMotDePasse(), confirmationMotDePasse, getDateDeNaissance(), getEstUnGestionnaire(), listeDouble.getTarget()));
        if(getUtilisateurSelectionne() != null) {
            setNomUtilisateur(null);
            setMotDePasse(null);
            confirmationMotDePasse = null;
            setDateDeNaissance(null);
            setEstUnGestionnaire(null);
            listeUtilisateurs.add(getUtilisateurSelectionne());
            RequestContext context = RequestContext.getCurrentInstance();
            if(continuerApresCreation) {
                setUtilisateurSelectionne(null);
                listeDouble = null;
                context.execute("PF('wiz').loadStep(PF('wiz').cfg.steps[0], true);");
            } else {
                setUtilisateurSelectionne(null);
                listeDouble = null;
                context.execute("PF('modaleCreerUtilisateur').hide();");
                context.execute("PF('modaleGererUtilisateur').hide();");
            }
        }
    }
    
    public void redirigerCompte(int numeroCompte) throws IOException {
        FacesContext.getCurrentInstance().getExternalContext().redirect("/pridbank_groupe12/faces/index.xhtml/faces/protected/gestionnaire/listerComptes.xhtml?idCompte="+numeroCompte);
    }
    
    public void editerUtilisateur() {
        listeDouble = null;
        setUtilisateurSelectionne(model.trouverUtilisateur(getUtilisateurSelectionne().getIdClient()));
        utilisateurOriginal = new Utilisateur(getUtilisateurSelectionne());
    }
    
    public void modifierUtilisateur() {
        Utilisateur resultat = model.modifierUtilisateur(getUtilisateurSelectionne(), modifierMotDePasse, confirmationMotDePasse);
        if(resultat != null) {
            setUtilisateurSelectionne(resultat);
            utilisateurOriginal = new Utilisateur(getUtilisateurSelectionne());
        }
    }
    
    public void supprimerUtilisateur(int idUtilisateur) {
        if(model.supprimerUtilisateur(idUtilisateur,utilisateurCourant.getIdClient())) {
            setUtilisateurSelectionne(null);
            for(Utilisateur utilisateur : listeUtilisateurs) {
                if(utilisateur.getIdClient() == idUtilisateur) {
                    listeUtilisateurs.remove(utilisateur);
                    break;
                }
            }
        }
    }

    public boolean longeurMotDePasseOK(String mdp){
        return model.longeurMotDePasseOK(mdp);
    }
    
    public boolean confirmMotDePasseOK(String confirm, String mdp){
        return model.confirmMotDePasseOK(confirm, mdp) ;
    }

    public boolean verifierAge(Date date){
        return model.ageMinimalRequis(date);
    }
    
    public void rendreProprietaire() {
        if(!comparaisonContenuListes(utilisateurOriginal.getListeDesComptes(), listeDouble.getTarget())) {
            Utilisateur resultat = model.lierComptesClient(utilisateurOriginal, listeDouble.getTarget());
            for (Compte compteAssocie : listeDouble.getTarget()){
                if (listeDouble.getSource().contains(compteAssocie))
                    listeDouble.getSource().remove(compteAssocie);
            }
            setUtilisateurSelectionne(resultat);
            utilisateurOriginal = (resultat == null ? null : new Utilisateur(getUtilisateurSelectionne()));
            //utilisateurOriginal.setListeDesComptes(listeDouble.getTarget());
            //setUtilisateurSelectionne(model.rafraichirUtilisateur(utilisateurOriginal));
            //addMessage("creerUtilisateur", "Sauvegarde réussie.");
        }
    }

    public DualListModel<Compte> getListeDouble() {
        if(listeDouble == null) {
            List<Compte> comptesSource;
            List<Compte> comptesCible;
            if(getUtilisateurSelectionne() == null) {
                comptesSource = model.getListeComptes();
                comptesCible = new ArrayList<>();
            } else {
                comptesSource = model.getNonComptesDuClient(getUtilisateurSelectionne());
                comptesCible = model.getComptesDuClient(getUtilisateurSelectionne());
            }
            listeDouble = new DualListModel<>(comptesSource, comptesCible);
        }
        return listeDouble;
    }

    public void setListeDouble(DualListModel<Compte> listeDouble) {
        this.listeDouble = listeDouble;
    }
    
    public String getModifierMotDePasse() {
        return modifierMotDePasse;
    }

    public void setModifierMotDePasse(String modifierMotDePasse) {
        this.modifierMotDePasse = modifierMotDePasse;
    }
    
    public String getConfirmationMotDePasse() {
        return confirmationMotDePasse;
    }

    public void setConfirmationMotDePasse(String confirmationMotDePasse) {
        this.confirmationMotDePasse = confirmationMotDePasse;
    }
    
    public Utilisateur getUtilisateurOriginal() {
        return utilisateurOriginal;
    }

    public void setUtilisateurOriginal(Utilisateur utilisateurOriginal) {
        this.utilisateurOriginal = utilisateurOriginal;
    }

    public void initialiserUtilisateur(int idClient) {
        Utilisateur utilisateur = model.trouverUtilisateur(idClient);
        setUtilisateurSelectionne(utilisateur);
    }
    
    public boolean verifierNomUtilisateur() {
        return model.verifierNomUtilisateur(getNomUtilisateur());
    }
    
    public void verifierChangementNomUtilisateur(){
        if(!getUtilisateurSelectionne().getNomUtilisateur().equals(utilisateurOriginal.getNomUtilisateur())) {
            model.verifierNomUtilisateur(getUtilisateurSelectionne().getNomUtilisateur());
        }
    }
    
    // Nice to have
    private String genererMotDePasse(){
        // method to generate a random string to use as password
        return "mdp";
    } 
    
    public List<Utilisateur> getListeUtilisateurs() {
        if (listeUtilisateurs == null){
            setListeUtilisateurs();
        }
        return listeUtilisateurs;
    }

    public void setListeUtilisateurs() {
        this.listeUtilisateurs = model.getListeUtilisateurs();
    }
    
    public void rafraichirListeUtilisateurs() {
        setListeUtilisateurs();
    }
    // Wizard
    public boolean isLierCompte() {
        return lierCompte;
    }
 
    public void setLierCompte(boolean lierCompte) {
        this.lierCompte = lierCompte;
    }
     
    public String onFlowProcess(FlowEvent event) {
        if (this.verifierAge(this.getDateDeNaissance()) && this.verifierNomUtilisateur()){
            if(!lierCompte || (getEstUnGestionnaire() != null && getEstUnGestionnaire())) {
                if(event.getOldStep().equals("informations")) {
                    return "confirmation";
                } else {
                    return "informations";
                }
            } else {
                return event.getNewStep();
            }            
        }
        return "informations" ;
    }
    
    public boolean getContinuerApresCreation() {
        return continuerApresCreation;
    }

    public void setContinuerApresCreation(boolean continuerApresCreation) {
        this.continuerApresCreation = continuerApresCreation;
    }

    public void onRowSelectUtilisateur(SelectEvent event) {
        this.setUtilisateurSelectionne((Utilisateur) event.getObject());
    }
    
    public void onRowUnselectUtilisateur(UnselectEvent event) {
        this.setUtilisateurSelectionne(null);
    }
    
    public void onRowDblclickCompteUtilisateur(SelectEvent event) throws IOException {
        redirigerCompte(((Compte) event.getObject()).getNumero());
    }

}
